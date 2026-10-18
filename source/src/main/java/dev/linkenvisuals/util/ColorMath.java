package dev.linkenvisuals.util;

/**
 * Pure ARGB/HSB-free color math shared by cosmetics, HUD and UI code.
 * All methods are allocation-free and depend on nothing but {@code java.lang.Math}.
 *
 * <p>Alpha conventions: {@link #argb(int, float)} builds a color with alpha from 0..1,
 * {@link #withAlpha(int, int)} with a byte alpha in 0..255 (both clamped);
 * {@link #mix(int, int, float)} interpolates RGB and drops alpha on purpose
 * (current visual behavior of the cosmetic material pipeline).
 */
public final class ColorMath {
    private ColorMath() {
    }

    /** Packs {@code rgb} with an alpha factor in 0..1 (clamped). */
    public static int argb(int rgb, float alpha) {
        int clamped = Math.max(0, Math.min(255, Math.round(alpha * 255.0f)));
        return clamped << 24 | rgb & 0xFFFFFF;
    }

    /** Packs {@code rgb} with a byte alpha in 0..255 (clamped). */
    public static int withAlpha(int rgb, int alpha) {
        return Math.max(0, Math.min(255, alpha)) << 24 | rgb & 0xFFFFFF;
    }

    /** Linear RGB interpolation toward white. */
    public static int lift(int rgb, float amount) {
        return mix(rgb, 0xFFFFFF, amount);
    }

    /** Linear RGB interpolation toward black. */
    public static int deepen(int rgb, float amount) {
        return mix(rgb, 0x000000, amount);
    }

    /** Linear RGB interpolation, {@code amount} clamped to 0..1. Alpha is dropped. */
    public static int mix(int from, int to, float amount) {
        float t = Math.max(0.0f, Math.min(1.0f, amount));
        int r = Math.round((from >> 16 & 0xFF) + ((to >> 16 & 0xFF) - (from >> 16 & 0xFF)) * t);
        int g = Math.round((from >> 8 & 0xFF) + ((to >> 8 & 0xFF) - (from >> 8 & 0xFF)) * t);
        int b = Math.round((from & 0xFF) + ((to & 0xFF) - (from & 0xFF)) * t);
        return r << 16 | g << 8 | b;
    }

    /** Hermite smoothstep with clamped input. */
    public static float smoothstep(float edge0, float edge1, float x) {
        float t = Math.max(0.0f, Math.min(1.0f, (x - edge0) / (edge1 - edge0)));
        return t * t * (3.0f - 2.0f * t);
    }

    /** Deterministic hash noise in [0, 1] for a given integer seed. */
    public static float noise(int seed) {
        int h = seed * 0x9E3779B1;
        h ^= h >>> 15;
        h *= 0x85EBCA6B;
        h ^= h >>> 13;
        return (h >>> 8 & 0xFFFF) / 65535.0f;
    }
}
