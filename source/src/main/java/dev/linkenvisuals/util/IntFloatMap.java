package dev.linkenvisuals.util;

import java.util.Arrays;

/**
 * Minimal open-addressing {@code int -> float} hash map with linear probing.
 * Companion to {@link IntObjectMap} for the two-level kerning table
 * (outer: left codepoint, inner: right codepoint -> advance). Missing keys
 * read as {@code 0.0f}, like the donor's {@code Int2FloatOpenHashMap} with
 * {@code defaultReturnValue(0)}.
 */
public final class IntFloatMap {
    private static final int EMPTY = -1;
    private static final float LOAD_FACTOR = 0.75f;

    private int[] keys;
    private float[] values;
    private int mask;
    private int size;
    private int threshold;

    public IntFloatMap() {
        this(16);
    }

    public IntFloatMap(int expectedSize) {
        int capacity = 16;
        while (capacity < expectedSize / LOAD_FACTOR) {
            capacity <<= 1;
        }
        keys = new int[capacity];
        Arrays.fill(keys, EMPTY);
        values = new float[capacity];
        mask = capacity - 1;
        threshold = (int) (capacity * LOAD_FACTOR);
    }

    public void put(int key, float value) {
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

    public float get(int key) {
        if (key < 0) {
            return 0.0f;
        }
        int slot = findSlot(key);
        return keys[slot] == EMPTY ? 0.0f : values[slot];
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
        float[] oldValues = values;
        int capacity = oldKeys.length << 1;
        keys = new int[capacity];
        Arrays.fill(keys, EMPTY);
        values = new float[capacity];
        mask = capacity - 1;
        threshold = (int) (capacity * LOAD_FACTOR);
        size = 0;
        for (int i = 0; i < oldKeys.length; i++) {
            if (oldKeys[i] != EMPTY) {
                put(oldKeys[i], oldValues[i]);
            }
        }
    }

    private static int mix(int key) {
        int h = key * 0x9E3779B9;
        return h ^ (h >>> 16);
    }
}
