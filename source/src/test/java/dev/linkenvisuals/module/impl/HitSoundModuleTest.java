package dev.linkenvisuals.module.impl;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Vanilla crit gate: full charge, falling, airborne and no water/blind/vehicle/sprint. */
class HitSoundModuleTest {
    @Test
    void fullFallingHitIsCritical() {
        assertTrue(HitSoundModule.meetsCritRules(1.0f, 1.0, false,
            false, false, false, false, false));
    }

    @Test
    void lowChargeOrGroundCancelsCritical() {
        assertFalse(HitSoundModule.meetsCritRules(0.5f, 1.0, false,
            false, false, false, false, false));
        assertFalse(HitSoundModule.meetsCritRules(1.0f, 1.0, true,
            false, false, false, false, false));
    }

    @Test
    void waterClimbBlindVehicleSprintCancelCritical() {
        assertFalse(HitSoundModule.meetsCritRules(1.0f, 1.0, false,
            true, false, false, false, false));
        assertFalse(HitSoundModule.meetsCritRules(1.0f, 1.0, false,
            false, true, false, false, false));
        assertFalse(HitSoundModule.meetsCritRules(1.0f, 1.0, false,
            false, false, true, false, false));
        assertFalse(HitSoundModule.meetsCritRules(1.0f, 1.0, false,
            false, false, false, true, false));
        assertFalse(HitSoundModule.meetsCritRules(1.0f, 1.0, false,
            false, false, false, false, true));
    }
}
