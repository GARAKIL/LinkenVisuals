package dev.linkenvisuals.pet;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Simulation tests for the ported ground-pet brain (Phase 5.6). The world is null and the ground
 * probe is injected: exactly how the controller decouples from the game world in production.
 */
class PetWanderControllerTest {

    private static final double GROUND = 64.0;

    private static PetWanderController controller() {
        return new PetWanderController((world, x, z, fallback) -> GROUND);
    }

    @Test
    void seedsOnGroundOnFirstFrame() {
        PetWanderController walker = controller();
        assertFalse(walker.hasPos());

        int steps = walker.frame(null, 0.0, 66.0, 0.0, 1.0f, 1.5, 0);

        assertEquals(0, steps);
        assertTrue(walker.hasPos());
        assertNotNull(walker.pos());
        assertEquals(GROUND, walker.pos().y, 1.0e-6);
    }

    @Test
    void followsPlayerAfterTheyMoveAway() {
        PetWanderController walker = controller();
        walker.frame(null, 0.0, 66.0, 0.0, 1.15f, 1.5, 0);

        double px = 0.0;
        for (int age = 1; age <= 160; age++) {
            px = Math.min(8.0, age * 0.1);
            walker.frame(null, px, 66.0, 0.0, 1.15f, 1.5, age);
        }

        assertNotNull(walker.pos());
        double dx = walker.pos().x - px;
        double dz = walker.pos().z;
        double distance = Math.sqrt(dx * dx + dz * dz);
        assertTrue(distance < 3.0, "pet should have caught up, distance=" + distance);
    }

    @Test
    void teleportsWhenLeftFarBehind() {
        PetWanderController walker = controller();
        walker.frame(null, 0.0, 66.0, 0.0, 1.0f, 1.5, 0);

        walker.frame(null, 30.0, 66.0, 0.0, 1.0f, 1.5, 1);

        double dx = walker.pos().x - 30.0;
        double dz = walker.pos().z;
        double distance = Math.sqrt(dx * dx + dz * dz);
        assertTrue(distance <= 1.5, "teleport should land next to the player, distance=" + distance);
    }

    @Test
    void legsReportStepDistanceWhileWalking() {
        PetWanderController walker = controller();
        walker.frame(null, 0.0, 66.0, 0.0, 1.15f, 1.5, 0);
        // 4 blocks away: beyond the follow start radius, so the pet switches to catching up.
        walker.frame(null, 4.0, 66.0, 0.0, 1.15f, 1.5, 1);

        float best = 0.0f;
        for (int age = 2; age <= 60; age++) {
            walker.frame(null, 4.0, 66.0, 0.0, 1.15f, 1.5, age);
            best = Math.max(best, walker.limbPosDelta());
        }

        assertTrue(best > 0.01f, "limbPosDelta should be positive while walking, best=" + best);
    }

    @Test
    void resetClearsSeededState() {
        PetWanderController walker = controller();
        walker.frame(null, 0.0, 66.0, 0.0, 1.0f, 1.5, 0);
        assertTrue(walker.hasPos());

        walker.reset();

        assertFalse(walker.hasPos());
        assertNull(walker.posLerp(0.5f));
    }
}
