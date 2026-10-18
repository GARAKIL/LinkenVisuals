package dev.linkenvisuals.hands;

/**
 * Pure clamps and limits for the first-person hand pose storage.
 * Kept free of Minecraft types so the invariants stay unit-testable headless.
 */
public final class HandPoseMath {
    /** Lateral reach limit on X/Y. */
    public static final float REACH = 0.3f;
    /** Depth limit toward the player model. */
    public static final float NEAR = -0.3f;
    /** Depth limit away from the player model. */
    public static final float FAR = 0.5f;
    /** Minimum scale factor. */
    public static final float MIN_SCALE = 0.2f;
    /** Maximum scale factor. */
    public static final float MAX_SCALE = 3.0f;

    private HandPoseMath() {
    }

    public static float clampReach(float value) {
        return Math.max(-REACH, Math.min(REACH, value));
    }

    public static float clampDepth(float value) {
        return Math.max(NEAR, Math.min(FAR, value));
    }

    public static float clampScale(float value) {
        return Math.max(MIN_SCALE, Math.min(MAX_SCALE, value));
    }
}
