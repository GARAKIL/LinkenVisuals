package dev.linkenvisuals.util.particles;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Locks the donor DamageParticles timing/size formulas. */
class DamageParticlesRulesTest {
    @Test
    void legacySizeKeepsTheDonorFloorAndShrinksWithLife() {
        assertEquals(0.04f, DamageParticlesRules.legacySize(10.0f, 0.5f, 1.0f), 1.0e-6f);
        float early = DamageParticlesRules.legacySize(55.0f, 1.0f, 0.0f);
        float late = DamageParticlesRules.legacySize(55.0f, 1.0f, 1.0f);
        assertTrue(early > late);
        assertEquals(1.0f, early, 1.0e-6f);
        assertEquals(0.42f, late, 1.0e-6f);
    }

    @Test
    void legacyAlphaFadesAndGroundDimsIt() {
        assertEquals(1.0f, DamageParticlesRules.legacyAlpha(0.0f, 1.0f, 1.0f, false), 1.0e-6f);
        assertEquals(0.0f, DamageParticlesRules.legacyAlpha(1.0f, 1.0f, 1.0f, false), 1.0e-6f);
        assertEquals(0.85f, DamageParticlesRules.legacyAlpha(0.0f, 1.0f, 1.0f, true), 1.0e-6f);
    }

    @Test
    void decorativeAlphaHasFadeInAndOut() {
        assertEquals(0.0f, DamageParticlesRules.decorativeAlpha(0L, 350L, 650L), 1.0e-6f);
        assertEquals(1.0f, DamageParticlesRules.decorativeAlpha(600L, 350L, 650L), 1.0e-6f);
        assertTrue(DamageParticlesRules.decorativeAlpha(900L, 350L, 650L) < 1.0f);
    }

    @Test
    void decorativeSizeMatchesDonorFormula() {
        assertEquals(0.05f, DamageParticlesRules.decorativeSize(0.0f), 1.0e-6f);
        assertEquals(0.15f, DamageParticlesRules.decorativeSize(0.5f), 1.0e-6f);
        assertEquals(0.25f, DamageParticlesRules.decorativeSize(1.0f), 1.0e-6f);
    }
}
