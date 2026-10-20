package dev.linkenvisuals.render;

/**
 * Pure rules and formulas for TargetEsp. Kept free of Minecraft types so the
 * target-retention, invisibility, sizing and easing contracts are unit-tested
 * without a game bootstrap.
 */
public final class TargetEspRules {
    /** Donor {@code MAX_RANGE}: the crosshair target must be within 30 blocks. */
    public static final double MAX_RANGE = 30.0;
    /** Donor {@code TARGET_DELAY}: the halo stays for 1 s after the crosshair leaves. */
    public static final long TARGET_DELAY_MS = 1000L;
    /** Donor {@code Animation(300, ...)}: fade in/out duration. */
    public static final int FADE_MS = 300;

    private TargetEspRules() {
    }

    /**
     * Donor {@code shouldHidePlayer}: an invisible player is only hidden when
     * nothing of their equipment is visible. A naked invisible player therefore
     * gets no ghost, while an invisible player holding/wearing something does.
     */
    public static boolean hidesInvisiblePlayer(boolean invisible, boolean hasVisibleEquipment) {
        return invisible && !hasVisibleEquipment;
    }

    /** Range gate; NaN and negative distances are rejected. */
    public static boolean withinRange(double distance) {
        return !Double.isNaN(distance) && distance >= 0.0 && distance <= MAX_RANGE;
    }

    /**
     * Donor {@code WorldUtils.getScale}: screen-space marker size for the given
     * camera distance, size setting and vertical FOV.
     */
    public static double scaleAtDistance(double distance, float sizePx, float fov) {
        double safeDistance = Math.max(distance, 0.05);
        double safeFov = fov <= 0.0f ? 70.0 : fov;
        return Math.max(10.0, 1000.0 / safeDistance)
            * (sizePx / 30.0)
            / (safeFov == 70.0 ? 1.0 : safeFov / 70.0);
    }

    /**
     * Donor {@code finalSize = max(maxSize - distance * 0.1, size * 0.3)}.
     * Unlike the donor this never returns NaN (the donor can return it when the
     * distance is zero).
     */
    public static float markerSizePx(double maxSize, double horizontalDistance, float sizeSetting) {
        double attenuated = (Double.isNaN(maxSize) ? 0.0 : maxSize)
            - Math.max(0.0, horizontalDistance) * 0.1;
        return Math.max((float) attenuated, sizeSetting * 0.3f);
    }

    /**
     * Converts a size in GUI-scaled pixels into world units at the given camera
     * distance, so the world-space billboard covers the same fraction of the
     * screen as the donor's 2D marker.
     */
    public static float worldSizeForPixels(float pixels, int scaledHeight, double distance, float fovDegrees) {
        if (scaledHeight <= 0 || pixels <= 0.0f || !(distance > 0.0) || Double.isNaN(distance)) {
            return 0.0f;
        }
        double fov = fovDegrees <= 0.0f ? 70.0 : fovDegrees;
        double halfHeight = Math.tan(Math.toRadians(fov) * 0.5);
        double fraction = (double) pixels / scaledHeight;
        return (float) (fraction * 2.0 * distance * halfHeight);
    }

    /** Donor {@code crystalEaseOutCubic}. Input is clamped to 0..1. */
    public static float easeOutCubic(float t) {
        float clamped = Math.max(0.0f, Math.min(1.0f, t));
        float c = 1.0f - clamped;
        return 1.0f - c * c * c;
    }

    /**
     * Moves the raw 0..1 animation by {@code deltaMs / fadeMs} toward the target
     * state. Kept pure so the fade timing is testable.
     */
    public static float fadeStep(float current, boolean retained, float deltaMs, int fadeMs) {
        float step = Math.max(0.0f, deltaMs) / Math.max(1, fadeMs);
        float clamped = Math.max(0.0f, Math.min(1.0f, current));
        return retained ? Math.min(1.0f, clamped + step) : Math.max(0.0f, clamped - step);
    }
}
