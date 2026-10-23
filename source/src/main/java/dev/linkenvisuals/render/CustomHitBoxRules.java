package dev.linkenvisuals.render;

/** Pure donor constants/rules for the CustomHitBox module. */
public final class CustomHitBoxRules {
    /** Donor render distance: 128 blocks, not 64. */
    public static final double MAX_RENDER_DISTANCE = 128.0;
    /** Donor LOS cache: ~2-3 ticks. */
    public static final long VISIBILITY_CACHE_MS = 120L;
    /** Donor outline thickness floor and width scale. */
    public static final double MIN_OUTLINE_THICKNESS = 0.0035;
    public static final double OUTLINE_WIDTH_SCALE = 0.006;

    private CustomHitBoxRules() {
    }

    public static boolean withinRenderDistance(double squaredDistance) {
        return Double.isFinite(squaredDistance)
            && squaredDistance <= MAX_RENDER_DISTANCE * MAX_RENDER_DISTANCE;
    }

    /** Donor combines the slider with the theme colour's alpha. */
    public static int effectiveFillAlpha(float fillAlpha, int colorAlpha) {
        float value = fillAlpha * Math.max(0, Math.min(255, colorAlpha)) / 255.0f;
        return Math.max(0, Math.min(255, Math.round(value)));
    }

    public static double outlineThickness(float lineWidth) {
        return Math.max(MIN_OUTLINE_THICKNESS, lineWidth * OUTLINE_WIDTH_SCALE);
    }
}
