package dev.linkenvisuals.util.optimization;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class OptimizationRulesTest {
    @Test
    void tierNamesMapToDonorIndices() {
        assertEquals(0, OptimizationRules.tier("Low"));
        assertEquals(1, OptimizationRules.tier("Medium"));
        assertEquals(2, OptimizationRules.tier("Ultra"));
        assertEquals(2, OptimizationRules.tier(" ultra "));
        assertEquals(1, OptimizationRules.tier(null));
        assertEquals(1, OptimizationRules.tier("unknown"));
    }

    @Test
    void renderDistanceCapsMatchTheDonor() {
        assertEquals(0, OptimizationRules.renderDistanceCap(0));
        assertEquals(12, OptimizationRules.renderDistanceCap(1));
        assertEquals(8, OptimizationRules.renderDistanceCap(2));

        assertEquals(32, OptimizationRules.capRenderDistance(32, 0));
        assertEquals(12, OptimizationRules.capRenderDistance(32, 1));
        assertEquals(8, OptimizationRules.capRenderDistance(32, 2));
        assertEquals(8, OptimizationRules.capRenderDistance(8, 2));
    }

    @Test
    void particleCullRadiusUsesSquaredDistance() {
        assertEquals(2304.0, OptimizationRules.particleDistanceSq(0), 1.0e-9);
        assertEquals(1024.0, OptimizationRules.particleDistanceSq(1), 1.0e-9);
        assertEquals(484.0, OptimizationRules.particleDistanceSq(2), 1.0e-9);

        assertTrue(OptimizationRules.allowParticle(48.0, 0.0, 0.0, 0));
        assertFalse(OptimizationRules.allowParticle(48.1, 0.0, 0.0, 0));
        assertTrue(OptimizationRules.allowParticle(0.0, 0.0, 22.0, 2));
        assertFalse(OptimizationRules.allowParticle(0.0, 23.0, 0.0, 2));
    }

    @Test
    void cloudsAreCulledOnlyOnUltra() {
        assertFalse(OptimizationRules.cullClouds(0));
        assertFalse(OptimizationRules.cullClouds(1));
        assertTrue(OptimizationRules.cullClouds(2));
        assertTrue(OptimizationRules.cullClouds(9), "out-of-range tiers clamp to Ultra");
    }
}
