package dev.linkenvisuals.util.particles;

import net.minecraft.util.math.MathHelper;

/** Pure donor Particles math, testable without Minecraft bootstrap. */
public final class ParticlesRules {
    public static final float SPAWN_RATE = 42.0f;
    public static final int MAX_ADD_PER_TICK = 36;
    public static final int MIN_LIFE_TICKS = 50;
    public static final int MAX_LIFE_TICKS = 150;

    private ParticlesRules() {
    }

    public static double nextAccumulator(double accumulator) {
        return accumulator + SPAWN_RATE / 20.0;
    }

    public static int spawnCount(double accumulator) {
        return Math.max(0, (int) accumulator);
    }

    public static int maxCount(float setting) {
        return Math.max(0, Math.round(setting));
    }

    public static int alpha(int life, int maxLife, int baseAlpha, float tickDelta) {
        float ratio = MathHelper.clamp((life - tickDelta) / (float) Math.max(1, maxLife), 0.0f, 1.0f);
        return MathHelper.clamp((int) Math.round(baseAlpha * Math.sin(ratio * Math.PI)), 0, 255);
    }

    public static float lifeProgress(int life, int maxLife, float tickDelta) {
        return 1.0f - MathHelper.clamp((life - tickDelta) / (float) Math.max(1, maxLife), 0.0f, 1.0f);
    }

    public static float billboardSize(float setting, float particleSize, float progress) {
        return Math.max(0.04f, setting * particleSize * (1.0f - progress * 0.25f));
    }

    public static float cubeHalfSize(float finalSize, int alpha) {
        return Math.max(0.01f, finalSize * 0.5f * (alpha / 255.0f));
    }
}
