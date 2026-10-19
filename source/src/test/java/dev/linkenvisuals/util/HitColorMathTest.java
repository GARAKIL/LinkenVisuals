package dev.linkenvisuals.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HitColorMathTest {
    @Test
    void leavesColorUntouchedWhenStrengthIsZero() {
        assertEquals(0xFF102030, HitColorMath.tint(0xFF102030, 0xFF0000, 0.0f, 1.0f));
    }

    @Test
    void leavesColorUntouchedWhenOpacityIsZero() {
        assertEquals(0xFF102030, HitColorMath.tint(0xFF102030, 0xFF0000, 1.0f, 0.0f));
    }

    @Test
    void preservesOriginalAlphaWhenTinting() {
        int tinted = HitColorMath.tint(0x80102030, 0xFFFFFF, 1.0f, 1.0f);
        assertEquals(0x80, tinted >>> 24, "alpha must not be replaced by the hit color");
    }

    @Test
    void fullStrengthReachesConfiguredColor() {
        assertEquals(0xFFFF0000, HitColorMath.tint(0xFF102030, 0xFF0000, 1.0f, 1.0f));
    }

    @Test
    void halfStrengthLerpsRgbChannels() {
        int tinted = HitColorMath.tint(0xFF000000, 0xFFFFFFFF, 1.0f, 0.5f);
        assertEquals(0xFF808080, tinted, "black lerped 50% toward white is mid grey");
    }

    @Test
    void clampsOutOfRangeInputs() {
        assertEquals(0xFFFF0000, HitColorMath.tint(0xFF102030, 0xFF0000, 5.0f, 5.0f));
        assertEquals(0xFF102030, HitColorMath.tint(0xFF102030, 0xFF0000, -1.0f, 1.0f));
    }

    @Test
    void transparentModelColorIsNotTinted() {
        int original = 0x00102030;
        assertEquals(original, HitColorMath.tint(original, 0xFFFFFF, 1.0f, 1.0f));
    }
}
