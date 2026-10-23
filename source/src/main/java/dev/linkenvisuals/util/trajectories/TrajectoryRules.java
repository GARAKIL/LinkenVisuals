package dev.linkenvisuals.util.trajectories;

import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

/** Pure donor trajectory rules and impact-face basis. */
public final class TrajectoryRules {
    public static final double HIT_MARKER_OFFSET = 0.02;
    public static final double EXPLOSIVE_RADIUS = 4.0;

    private static final String[] EXPLOSIVE_KEYWORDS = {
        "взрыв", "взрывн", "бомб", "взрывчат",
        "boom", "blast", "explos", "tnt"
    };

    private TrajectoryRules() {
    }

    public static float bowPower(int useTicks) {
        float useTime = Math.max(0, useTicks) / 20.0f;
        return Math.min(1.0f, (useTime * useTime + useTime * 2.0f) / 3.0f);
    }

    public static double drag(boolean inWater) {
        return inWater ? 0.8 : 0.99;
    }

    public static double gravity(boolean bowOrCrossbow) {
        return bowOrCrossbow ? 0.05 : 0.03;
    }

    public static boolean explosiveKeyword(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        String lower = text.toLowerCase(java.util.Locale.ROOT);
        for (String keyword : EXPLOSIVE_KEYWORDS) {
            if (lower.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    public static Vec3d impactPosition(Vec3d hit, Direction side) {
        return hit.add(side.getOffsetX() * HIT_MARKER_OFFSET,
            side.getOffsetY() * HIT_MARKER_OFFSET,
            side.getOffsetZ() * HIT_MARKER_OFFSET);
    }

    public static Vec3d faceRight(Direction side) {
        if (side.getAxis() == Direction.Axis.Y) {
            return new Vec3d(1.0, 0.0, 0.0);
        }
        if (side.getAxis() == Direction.Axis.X) {
            return new Vec3d(0.0, 0.0, 1.0);
        }
        return new Vec3d(1.0, 0.0, 0.0);
    }

    public static Vec3d faceUp(Direction side) {
        return side.getAxis() == Direction.Axis.Y
            ? new Vec3d(0.0, 0.0, 1.0)
            : new Vec3d(0.0, 1.0, 0.0);
    }

    /** Donor simulation step: drag on all axes, gravity on Y. */
    public static Vec3d step(Vec3d motion, boolean inWater, boolean bowOrCrossbow) {
        double drag = drag(inWater);
        return new Vec3d(motion.x * drag,
            motion.y * drag - gravity(bowOrCrossbow),
            motion.z * drag);
    }

    public static float initialDistance(boolean bow) {
        return bow ? 1.0f : 0.4f;
    }

    public static float throwVelocity(String itemType) {
        return switch (itemType) {
            case "splash", "lingering" -> 0.5f;
            case "experience_bottle" -> 0.59f;
            case "trident" -> 2.0f;
            default -> 1.5f;
        };
    }

    public static float throwPitch(String itemType) {
        return switch (itemType) {
            case "splash", "lingering", "experience_bottle" -> 20.0f;
            default -> 0.0f;
        };
    }

}
