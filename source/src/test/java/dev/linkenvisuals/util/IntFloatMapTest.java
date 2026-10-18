package dev.linkenvisuals.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IntFloatMapTest {
    @Test
    void putAndGetRoundTrip() {
        IntFloatMap map = new IntFloatMap();
        map.put(0x41, 1.5f);
        map.put(0x044D, -0.25f);
        assertEquals(1.5f, map.get(0x41));
        assertEquals(-0.25f, map.get(0x044D));
        assertEquals(2, map.size());
    }

    @Test
    void missingKeyReadsZero() {
        IntFloatMap map = new IntFloatMap();
        map.put(1, 2.0f);
        assertEquals(0.0f, map.get(999));
        assertEquals(0.0f, map.get(-3));
    }

    @Test
    void resizeKeepsAllEntries() {
        IntFloatMap map = new IntFloatMap(4);
        for (int i = 0; i < 300; i++) {
            map.put(i, i * 0.5f);
        }
        assertEquals(300, map.size());
        for (int i = 0; i < 300; i++) {
            assertEquals(i * 0.5f, map.get(i));
        }
    }
}
