package dev.linkenvisuals.util;

import java.util.Arrays;

/**
 * Minimal open-addressing {@code int -> V} hash map with linear probing.
 * No boxing, no dependencies — a dependency-free stand-in for the fastutil
 * lookups the donor's MSDF font uses. Keys must be non-negative (codepoints).
 */
public final class IntObjectMap<V> {
    private static final int EMPTY = -1;
    private static final float LOAD_FACTOR = 0.75f;

    private int[] keys;
    private Object[] values;
    private int mask;
    private int size;
    private int threshold;

    public IntObjectMap() {
        this(16);
    }

    public IntObjectMap(int expectedSize) {
        int capacity = 16;
        while (capacity < expectedSize / LOAD_FACTOR) {
            capacity <<= 1;
        }
        keys = new int[capacity];
        Arrays.fill(keys, EMPTY);
        values = new Object[capacity];
        mask = capacity - 1;
        threshold = (int) (capacity * LOAD_FACTOR);
    }

    public void put(int key, V value) {
        if (key < 0) {
            throw new IllegalArgumentException("negative key: " + key);
        }
        if (size >= threshold) {
            resize();
        }
        int slot = findSlot(key);
        if (keys[slot] == EMPTY) {
            size++;
        }
        keys[slot] = key;
        values[slot] = value;
    }

    @SuppressWarnings("unchecked")
    public V get(int key) {
        if (key < 0) {
            return null;
        }
        int slot = findSlot(key);
        return keys[slot] == EMPTY ? null : (V) values[slot];
    }

    public int size() {
        return size;
    }

    private int findSlot(int key) {
        int slot = mix(key) & mask;
        while (keys[slot] != EMPTY && keys[slot] != key) {
            slot = (slot + 1) & mask;
        }
        return slot;
    }

    private void resize() {
        int[] oldKeys = keys;
        Object[] oldValues = values;
        int capacity = oldKeys.length << 1;
        keys = new int[capacity];
        Arrays.fill(keys, EMPTY);
        values = new Object[capacity];
        mask = capacity - 1;
        threshold = (int) (capacity * LOAD_FACTOR);
        size = 0;
        for (int i = 0; i < oldKeys.length; i++) {
            if (oldKeys[i] != EMPTY) {
                @SuppressWarnings("unchecked")
                V value = (V) oldValues[i];
                put(oldKeys[i], value);
            }
        }
    }

    /** Spread the low bits: codepoints of one script cluster in the low range. */
    private static int mix(int key) {
        int h = key * 0x9E3779B9;
        return h ^ (h >>> 16);
    }
}
