package dev.linkenvisuals.hands;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Locks the lightweight hand-motion subset: idle is neutral, movement stays
 * subtle, sprint amplifies, water lowers the hand and swing recoil is mirrored
 * for a left main arm.
 */
class HandMotionTest {
    @Test
    void idleHandIsNeutral() {
        HandMotion.Offset offset = HandMotion.offset(10.0f, 0.0f, false, false, 0.0, 0.0f, true);
        assertEquals(0.0f, offset.x(), 1.0e-6f);
        assertEquals(0.0f, offset.y(), 1.0e-6f);
        assertEquals(0.0f, offset.z(), 1.0e-6f);
        assertEquals(0.0f, offset.pitch(), 1.0e-6f);
        assertEquals(0.0f, offset.roll(), 1.0e-6f);
    }

    @Test
    void movementStaysSubtleAndSprintAmplifiesX() {
        HandMotion.Offset walk = HandMotion.offset(2.0f, 0.12f, false, false, 0.0, 0.0f, true);
        HandMotion.Offset sprint = HandMotion.offset(2.0f, 0.12f, true, false, 0.0, 0.0f, true);
        assertTrue(Math.abs(walk.x()) <= 0.01f, "walk sway must stay below one hundredth of a block");
        assertTrue(Math.abs(sprint.x()) <= 0.02f, "sprint sway must stay bounded");
        assertEquals(1.65f, sprint.x() / walk.x(), 0.02f);
    }

    @Test
    void waterLowersAndFallLiftsTheHand() {
        HandMotion.Offset water = HandMotion.offset(2.0f, 0.0f, false, true, 0.0, 0.0f, true);
        HandMotion.Offset fall = HandMotion.offset(2.0f, 0.0f, false, false, 2.0, 0.0f, true);
        assertTrue(water.y() < 0.0f);
        assertTrue(water.z() > 0.0f);
        assertTrue(fall.y() > 0.0f);
    }

    @Test
    void swingRollIsMirroredForLeftArm() {
        HandMotion.Offset right = HandMotion.offset(2.0f, 0.0f, false, false, 0.0, 0.5f, true);
        HandMotion.Offset left = HandMotion.offset(2.0f, 0.0f, false, false, 0.0, 0.5f, false);
        assertTrue(right.pitch() > 0.0f);
        assertEquals(-right.roll(), left.roll(), 1.0e-6f);
    }
}
