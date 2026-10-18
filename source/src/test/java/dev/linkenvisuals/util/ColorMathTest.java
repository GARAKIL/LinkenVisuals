package dev.linkenvisuals.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ColorMathTest {
    @Test
    void argbClampsAlpha() {
        assertEquals(0xFFFF0000, ColorMath.argb(0xFF0000, 1.0f));
        assertEquals(0x00123456, ColorMath.argb(0x123456, -5.0f));
        assertEquals(0xFF123456, ColorMath.argb(0x123456, 5.0f));
        assertEquals(0x80123456, ColorMath.argb(0x123456, 0.5f));
    }

    @Test
    void mixInterpolatesAndClamps() {
        assertEquals(0x000000, ColorMath.mix(0x000000, 0xFFFFFF, 0.0f));
        assertEquals(0xFFFFFF, ColorMath.mix(0x000000, 0xFFFFFF, 1.0f));
        assertEquals(0x808080, ColorMath.mix(0x000000, 0xFFFFFF, 0.5f));
        assertEquals(0x000000, ColorMath.mix(0x000000, 0xFFFFFF, -1.0f));
        assertEquals(0xFFFFFF, ColorMath.mix(0x000000, 0xFFFFFF, 2.0f));
    }

    @Test
    void liftAndDeepenMoveTowardWhiteAndBlack() {
        assertEquals(0xFFFFFF, ColorMath.lift(0x000000, 1.0f));
        assertEquals(0x000000, ColorMath.deepen(0xFFFFFF, 1.0f));
        assertEquals(0x000000, ColorMath.lift(0x000000, 0.0f));
        assertEquals(0x000000, ColorMath.deepen(0x000000, 1.0f));
    }

    @Test
    void smoothstepClampsAndCenters() {
        assertEquals(0.0f, ColorMath.smoothstep(0.0f, 1.0f, -5.0f));
        assertEquals(1.0f, ColorMath.smoothstep(0.0f, 1.0f, 5.0f));
        assertEquals(0.5f, ColorMath.smoothstep(0.0f, 1.0f, 0.5f), 1.0e-6f);
    }

    @Test
    void noiseStaysInUnitRangeAndIsDeterministic() {
        for (int seed = -1000; seed <= 1000; seed++) {
            float value = ColorMath.noise(seed);
            assertTrue(value >= 0.0f && value <= 1.0f, "noise out of range for seed " + seed);
        }
        assertEquals(ColorMath.noise(42), ColorMath.noise(42));
    }
}
