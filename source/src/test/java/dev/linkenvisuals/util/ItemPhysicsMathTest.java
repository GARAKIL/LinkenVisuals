package dev.linkenvisuals.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ItemPhysicsMathTest {
    @Test
    void spinIsZeroAtTimeZero() {
        assertEquals(0.0f, ItemPhysicsMath.spin(0.0f, 1.0f), 1.0e-6f);
    }

    @Test
    void spinScalesLinearlyWithTimeAndSpeed() {
        float one = ItemPhysicsMath.spin(1.0f, 1.0f);
        float two = ItemPhysicsMath.spin(2.0f, 1.0f);
        float fast = ItemPhysicsMath.spin(1.0f, 2.0f);
        assertEquals(one * 2.0f, two, 1.0e-5f);
        assertEquals(one * 2.0f, fast, 1.0e-5f);
        assertEquals(ItemPhysicsMath.SPIN_RAD_PER_SECOND, one, 1.0e-6f);
    }

    @Test
    void sideLitItemsSinkLower() {
        assertTrue(ItemPhysicsMath.groundOffsetY(true) < ItemPhysicsMath.groundOffsetY(false));
    }

    @Test
    void additionalOffsetPushesItemsFurtherBack() {
        float plain = ItemPhysicsMath.groundOffsetZ(false, false, 0.0f);
        float raised = ItemPhysicsMath.groundOffsetZ(false, true, 0.0f);
        assertTrue(raised < plain);
        assertEquals(-0.04f, plain, 1.0e-6f);
        assertEquals(-0.14f, raised, 1.0e-6f);
    }

    @Test
    void uniqueOffsetSpreadsDepthDeterministically() {
        float first = ItemPhysicsMath.groundOffsetZ(false, false, 0.5f);
        float second = ItemPhysicsMath.groundOffsetZ(false, false, 0.5f);
        assertEquals(first, second, 0.0f, "same input must give the same offset");
        assertTrue(first < ItemPhysicsMath.groundOffsetZ(false, false, 0.0f));
    }

    @Test
    void sideLitIgnoresAdditionalOffset() {
        assertEquals(-0.08f, ItemPhysicsMath.groundOffsetZ(true, true, 0.3f), 1.0e-6f);
    }
}
