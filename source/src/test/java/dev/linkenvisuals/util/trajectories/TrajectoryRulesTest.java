package dev.linkenvisuals.util.trajectories;

import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class TrajectoryRulesTest {
    @Test
    void bowPowerMatchesTheDonorCurve() {
        assertEquals(0.0f, TrajectoryRules.bowPower(0), 1.0e-6f);
        assertEquals(0.4166667f, TrajectoryRules.bowPower(10), 1.0e-6f);
        assertEquals(1.0f, TrajectoryRules.bowPower(20), 1.0e-6f);
        assertEquals(1.0f, TrajectoryRules.bowPower(200), 1.0e-6f);
    }

    @Test
    void waterAndGravityStepsMatchTheDonor() {
        assertEquals(0.99, TrajectoryRules.drag(false), 1.0e-9);
        assertEquals(0.8, TrajectoryRules.drag(true), 1.0e-9);
        assertEquals(0.05, TrajectoryRules.gravity(true), 1.0e-9);
        assertEquals(0.03, TrajectoryRules.gravity(false), 1.0e-9);

        Vec3d step = TrajectoryRules.step(new Vec3d(2.0, 2.0, 2.0), false, false);
        assertEquals(1.98, step.x, 1.0e-9);
        assertEquals(1.95, step.y, 1.0e-9);
        assertEquals(1.98, step.z, 1.0e-9);
        Vec3d wet = TrajectoryRules.step(new Vec3d(2.0, 2.0, 2.0), true, true);
        assertEquals(1.6, wet.x, 1.0e-9);
        assertEquals(1.55, wet.y, 1.0e-9);
    }

    @Test
    void explosiveKeywordsCoverDonorLanguages() {
        assertTrue(TrajectoryRules.explosiveKeyword("Explosive Bow"));
        assertTrue(TrajectoryRules.explosiveKeyword("BLAST protection"));
        assertTrue(TrajectoryRules.explosiveKeyword("взрывное зелье"));
        assertTrue(TrajectoryRules.explosiveKeyword("Бомба"));
        assertFalse(TrajectoryRules.explosiveKeyword("Diamond Sword"));
        assertFalse(TrajectoryRules.explosiveKeyword(""));
        assertFalse(TrajectoryRules.explosiveKeyword(null));
    }

    @Test
    void impactFaceBasisAndOffsetMatchTheDonor() {
        assertEquals(new Vec3d(1.0, 0.0, 0.0), TrajectoryRules.faceRight(Direction.UP));
        assertEquals(new Vec3d(0.0, 0.0, 1.0), TrajectoryRules.faceUp(Direction.UP));
        assertEquals(new Vec3d(0.0, 0.0, 1.0), TrajectoryRules.faceRight(Direction.EAST));
        assertEquals(new Vec3d(0.0, 1.0, 0.0), TrajectoryRules.faceUp(Direction.NORTH));

        Vec3d hit = new Vec3d(1.0, 2.0, 3.0);
        Vec3d offset = TrajectoryRules.impactPosition(hit, Direction.UP);
        assertEquals(1.0, offset.x, 1.0e-9);
        assertEquals(2.02, offset.y, 1.0e-9);
        assertEquals(3.0, offset.z, 1.0e-9);

        assertEquals(1.0f, TrajectoryRules.initialDistance(true), 1.0e-6f);
        assertEquals(0.4f, TrajectoryRules.initialDistance(false), 1.0e-6f);
        assertEquals(2.0f, TrajectoryRules.throwVelocity("trident"), 1.0e-6f);
        assertEquals(20.0f, TrajectoryRules.throwPitch("splash"), 1.0e-6f);
    }
}
