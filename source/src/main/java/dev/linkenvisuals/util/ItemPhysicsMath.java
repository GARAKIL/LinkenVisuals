package dev.linkenvisuals.util;

/**
 * Pure math for {@code ItemPhysics}: deterministic spin and ground offsets.
 * Kept free of Minecraft types so the formulas are unit-testable headless.
 */
public final class ItemPhysicsMath {
    /** Radians per second at rotation speed 1.0. */
    public static final float SPIN_RAD_PER_SECOND = 5.0f;
    /** Donor constant: {@code 0.05 / (2 * PI)}. */
    public static final float RANDOM_Y_OFFSET_SCALE = (float) (0.05 / (Math.PI * 2.0));

    private ItemPhysicsMath() {
    }

    /**
     * Deterministic tumble angle. The donor accumulated a per-frame increment
     * from the render clock, which made the spin frame-rate dependent; this
     * version is a pure function of elapsed seconds.
     */
    public static float spin(float seconds, float speed) {
        return seconds * speed * SPIN_RAD_PER_SECOND;
    }

    /** Vertical offset so flat items do not sink into the block. */
    public static float groundOffsetY(boolean sideLit) {
        return sideLit ? -0.2f : 0.0f;
    }

    /** Depth offset, including the deterministic per-item random spread. */
    public static float groundOffsetZ(boolean sideLit, boolean additionalOffset, float uniqueOffset) {
        if (sideLit) {
            return -0.08f;
        }
        float base = additionalOffset ? -0.14f : -0.04f;
        return base - uniqueOffset * RANDOM_Y_OFFSET_SCALE;
    }
}
