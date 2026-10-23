package dev.linkenvisuals.util.particles;

import net.minecraft.util.math.MathHelper;

/** Pure donor FortniteDamage animation math. */
public final class FortniteDamageRules {
    public static final long MAX_WAIT_MS = 750L;

    private FortniteDamageRules() {
    }

    public static float alpha(float progress) {
        float p = MathHelper.clamp(progress, 0.0f, 1.0f);
        if (p < 0.12f) {
            return easeOutCubic(p / 0.12f);
        }
        if (p > 0.72f) {
            return 1.0f - easeOutCubic((p - 0.72f) / 0.28f);
        }
        return 1.0f;
    }

    public static float scale(float progress) {
        float p = MathHelper.clamp(progress, 0.0f, 1.0f);
        if (p < 0.18f) {
            return 0.45f + 0.55f * easeOutBack(p / 0.18f);
        }
        return 1.0f;
    }

    public static double rise(float progress, float amount) {
        return amount * easeOutCubic(MathHelper.clamp(progress, 0.0f, 1.0f));
    }

    public static String text(float damage) {
        return String.valueOf(Math.max(1, Math.round(damage)));
    }

    public static float easeOutCubic(float value) {
        float clamped = MathHelper.clamp(value, 0.0f, 1.0f);
        return 1.0f - (float) Math.pow(1.0f - clamped, 3);
    }

    public static float easeOutBack(float value) {
        float c1 = 1.70158f;
        float c3 = c1 + 1.0f;
        float x = MathHelper.clamp(value, 0.0f, 1.0f);
        return 1.0f + c3 * (float) Math.pow(x - 1.0f, 3) + c1 * (float) Math.pow(x - 1.0f, 2);
    }
}
