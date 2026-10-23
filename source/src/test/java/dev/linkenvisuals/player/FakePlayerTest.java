package dev.linkenvisuals.player;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Locks the fake-player damage curve: the same 0.2 + 0.8 * charge^2 shape as
 * vanilla, with the 1.5x crit multiplier applied after the curve.
 */
class FakePlayerTest {
    @Test
    void fullChargeMatchesWeaponDamage() {
        assertEquals(8.0f, FakePlayer.damageFor(8.0f, 1.0f, false), 1.0e-6f);
    }

    @Test
    void critAddsFiftyPercent() {
        assertEquals(12.0f, FakePlayer.damageFor(8.0f, 1.0f, true), 1.0e-6f);
    }

    @Test
    void zeroChargeKeepsTheVanillaMinimumShare() {
        assertEquals(1.6f, FakePlayer.damageFor(8.0f, 0.0f, false), 1.0e-6f);
    }

    @Test
    void chargeIsClampedAndDamageNeverDropsBelowHalfHeart() {
        assertEquals(0.5f, FakePlayer.damageFor(0.1f, 0.0f, false), 1.0e-6f);
        assertEquals(8.0f, FakePlayer.damageFor(8.0f, 4.0f, false), 1.0e-6f);
    }
}
