package dev.linkenvisuals.util.particles;

import net.minecraft.util.math.MathHelper;

/** Pure donor DamageParticles math, testable without a Minecraft client. */
public final class DamageParticlesRules {
    public static final long LEGACY_LIFETIME_MS = 2800L;
    public static final long DECORATIVE_LIFETIME_MS = 2000L;
    public static final int ATTACK_BURST = 35;
    public static final int THROW_BURST = 4;
    public static final int MAX_ATTACK_PARTICLES = 420;
    public static final int MAX_THROW_PARTICLES = 480;

    private DamageParticlesRules() {
    }

    public static float legacySize(float setting, float multiplier, float progress) {
        float base = MathHelper.clamp(setting / 55.0f, 0.15f, 1.2f);
        return Math.max(0.04f, base * multiplier * (1.0f - progress * 0.58f));
    }

    public static float legacyAlpha(float progress, float baseAlpha, float multiplier, boolean onGround) {
        float fade = 1.0f - MathHelper.clamp(progress, 0.0f, 1.0f);
        if (onGround) {
            fade *= 0.85f;
        }
        return MathHelper.clamp(fade * baseAlpha * multiplier, 0.0f, 1.0f);
    }

    public static float decorativeAlpha(long ageMs, long fadeInMs, long fadeOutStartMs) {
        if (ageMs < 0) {
            return 0.0f;
        }
        if (ageMs < fadeInMs) {
            return easeOutQuad((float) ageMs / (float) Math.max(1, fadeInMs));
        }
        if (ageMs >= fadeOutStartMs) {
            return 1.0f - easeOutQuad((float) (ageMs - fadeOutStartMs) / 450.0f);
        }
        return 1.0f;
    }

    public static float decorativeSize(float setting) {
        return 0.05f + MathHelper.clamp(setting, 0.0f, 1.0f) * 0.2f;
    }

    public static float easeOutQuad(float value) {
        float clamped = MathHelper.clamp(value, 0.0f, 1.0f);
        return clamped * (2.0f - clamped);
    }
}
