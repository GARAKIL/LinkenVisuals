package dev.linkenvisuals.ui.linkmenu;

/**
 * Pure timing and progress rules for the LINKEN splash/loading layer.
 *
 * <p>The class deliberately avoids Minecraft types so the fade and stage
 * transitions can be unit-tested without a client. All timestamps are
 * milliseconds from {@code Util.getMeasuringTimeMs()}.</p>
 */
public final class SplashProgress {
    public static final long FADE_IN_MS = 420L;
    public static final long FADE_OUT_MS = 520L;
    public static final long EXIT_MS = 360L;

    private SplashProgress() {
    }

    public static float clamp01(float value) {
        if (!Float.isFinite(value)) {
            return 0.0f;
        }
        return Math.max(0.0f, Math.min(1.0f, value));
    }

    /** Hermite smoothstep, clamped to the 0..1 domain. */
    public static float smoothstep(float value) {
        float t = clamp01(value);
        return t * t * (3.0f - 2.0f * t);
    }

    /** Fades the overlay in; a missing start timestamp means "already visible". */
    public static float fadeIn(long startedAt, long now) {
        if (startedAt <= 0L) {
            return 1.0f;
        }
        return smoothstep((now - startedAt) / (float) FADE_IN_MS);
    }

    /** Fades the overlay out after the reload has completed. */
    public static float fadeOut(long completedAt, long now) {
        if (completedAt <= 0L) {
            return 1.0f;
        }
        return 1.0f - smoothstep((now - completedAt) / (float) FADE_OUT_MS);
    }

    /** Slide-out amount from 0 (idle) to 1 (fully off-screen). */
    public static float exit(long completedAt, long now) {
        if (completedAt <= 0L) {
            return 0.0f;
        }
        long delay = Math.max(0L, FADE_OUT_MS / 2L);
        return smoothstep((now - completedAt - delay) / (float) EXIT_MS);
    }

    /** Clamps a raw progress value, treating NaN as zero. */
    public static float displayProgress(float raw) {
        return clamp01(raw);
    }

    /** Number of whole percent shown by the splash. */
    public static int percent(float raw) {
        return Math.round(displayProgress(raw) * 100.0f);
    }

    /** Translation key for the stage line of the resource reload. */
    public static String stageKey(float raw) {
        float progress = displayProgress(raw);
        if (progress < 0.34f) {
            return "loading.splash.stage.reload";
        }
        if (progress < 0.68f) {
            return "loading.splash.stage.models";
        }
        if (progress < 1.0f) {
            return "loading.splash.stage.finish";
        }
        return "loading.splash.stage.ready";
    }

    /** True once the fade-out has fully finished. */
    public static boolean finished(long completedAt, long now) {
        return completedAt > 0L && now - completedAt >= FADE_OUT_MS + EXIT_MS;
    }
}
