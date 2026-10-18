package dev.linkenvisuals.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IntObjectMapTest {
    @Test
    void putAndGetRoundTrip() {
        IntObjectMap<String> map = new IntObjectMap<>();
        map.put(0x41, "A");
        map.put(0x042F, "Ya"); // Cyrillic > 127: no Integer boxing on lookup
        assertEquals("A", map.get(0x41));
        assertEquals("Ya", map.get(0x042F));
        assertEquals(2, map.size());
    }

    @Test
    void missingKeyReturnsNull() {
        IntObjectMap<String> map = new IntObjectMap<>();
        map.put(1, "one");
        assertNull(map.get(2));
        assertNull(map.get(-5));
    }

    @Test
    void overwriteKeepsSize() {
        IntObjectMap<String> map = new IntObjectMap<>();
        map.put(7, "a");
        map.put(7, "b");
        assertEquals("b", map.get(7));
        assertEquals(1, map.size());
    }

    @Test
    void resizeKeepsAllEntries() {
        IntObjectMap<Integer> map = new IntObjectMap<>(4);
        for (int i = 0; i < 500; i++) {
            map.put(i * 7919, i);
        }
        assertEquals(500, map.size());
        for (int i = 0; i < 500; i++) {
            assertEquals(i, map.get(i * 7919));
        }
        assertNull(map.get(123456789));
    }

    @Test
    void zeroKeyIsValid() {
        IntObjectMap<String> map = new IntObjectMap<>();
        map.put(0, "null-char");
        assertEquals("null-char", map.get(0));
    }

    @Test
    void negativePutIsRejected() {
        IntObjectMap<String> map = new IntObjectMap<>();
        assertThrows(IllegalArgumentException.class, () -> map.put(-1, "x"));
    }
}
