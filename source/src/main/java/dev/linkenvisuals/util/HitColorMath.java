package dev.linkenvisuals.util;

/**
 * Pure tint math for {@code HitColor}: lerps the RGB channels of the model color
 * toward the configured hit color and keeps the original alpha. No Minecraft
 * dependencies, so the fade curve is unit-testable headless.
 */
public final class HitColorMath {
    private HitColorMath() {
    }

    /**
     * @param original  ARGB model color supplied by the renderer
     * @param chosen    RGB hit color (alpha ignored)
     * @param strength  hit fade strength in 0..1
     * @param maxTint   maximum tint factor in 0..1 (module opacity)
     * @return tinted ARGB color; {@code original} when nothing should change
     */
    public static int tint(int original, int chosen, float strength, float maxTint) {
        int alpha = original >>> 24;
        if (alpha == 0) {
            return original;
        }
        float factor = clamp(strength) * clamp(maxTint);
        if (factor <= 0.001f) {
            return original;
        }
        int red = lerp(original >> 16 & 0xFF, chosen >> 16 & 0xFF, factor);
        int green = lerp(original >> 8 & 0xFF, chosen >> 8 & 0xFF, factor);
        int blue = lerp(original & 0xFF, chosen & 0xFF, factor);
        return alpha << 24 | red << 16 | green << 8 | blue;
    }

    private static int lerp(int from, int to, float factor) {
        int value = Math.round(from + (to - from) * factor);
        return Math.max(0, Math.min(255, value));
    }

    private static float clamp(float value) {
        return Math.max(0.0f, Math.min(1.0f, value));
    }
}
