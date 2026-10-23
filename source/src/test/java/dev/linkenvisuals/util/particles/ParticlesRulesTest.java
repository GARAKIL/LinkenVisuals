package dev.linkenvisuals.util.particles;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ParticlesRulesTest {
    @Test
    void spawnAccumulatorMatchesTheDonorRate() {
        double accumulator = 0.0;
        accumulator = ParticlesRules.nextAccumulator(accumulator);
        assertEquals(2.1, accumulator, 1.0e-9);
        assertEquals(2, ParticlesRules.spawnCount(accumulator));
        accumulator -= 2;
        assertEquals(0, ParticlesRules.spawnCount(accumulator));

        assertEquals(0, ParticlesRules.maxCount(-10.0f));
        assertEquals(600, ParticlesRules.maxCount(600.4f));
        assertEquals(99999, ParticlesRules.maxCount(99999.0f));
    }

    @Test
    void alphaFadesInAndOutAcrossTheLifetime() {
        assertEquals(0, ParticlesRules.alpha(100, 100, 255, 0.0f));
        assertEquals(255, ParticlesRules.alpha(50, 100, 255, 0.0f));
        assertEquals(0, ParticlesRules.alpha(0, 100, 255, 0.0f));
        assertEquals(90, ParticlesRules.alpha(50, 100, 90, 0.0f));
        assertEquals(0.5f, ParticlesRules.lifeProgress(50, 100, 0.0f), 1.0e-6f);
        assertEquals(1.0f, ParticlesRules.lifeProgress(0, 100, 0.0f), 1.0e-6f);
    }

    @Test
    void billboardSizeKeepsTheDonorFloorAndShrinksWithLife() {
        float early = ParticlesRules.billboardSize(0.25f, 0.22f, 0.0f);
        float late = ParticlesRules.billboardSize(0.25f, 0.22f, 1.0f);
        assertTrue(early > late);
        assertEquals(Math.max(0.04f, 0.25f * 0.22f), early, 1.0e-6f);
        assertEquals(Math.max(0.04f, 0.25f * 0.22f * 0.75f), late, 1.0e-6f);
        assertEquals(0.04f, ParticlesRules.billboardSize(0.01f, 0.01f, 0.0f), 1.0e-6f);
    }

    @Test
    void cubeHalfSizeScalesWithAlphaAndNeverGoesNegative() {
        assertEquals(0.5f, ParticlesRules.cubeHalfSize(1.0f, 255), 1.0e-6f);
        assertEquals(0.25f, ParticlesRules.cubeHalfSize(1.0f, 128), 1.0e-3f);
        assertEquals(0.01f, ParticlesRules.cubeHalfSize(0.0f, 0), 1.0e-6f);
    }
}
