package dev.linkenvisuals.ui.linkmenu;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SplashProgressTest {
    @Test
    void clampHandlesOutOfRangeAndNonFiniteValues() {
        assertEquals(0.0f, SplashProgress.clamp01(-1.0f));
        assertEquals(1.0f, SplashProgress.clamp01(2.0f));
        assertEquals(0.0f, SplashProgress.clamp01(Float.NaN));
        assertEquals(0.35f, SplashProgress.clamp01(0.35f), 1.0e-6f);
        assertEquals(0.0f, SplashProgress.displayProgress(-0.2f));
        assertEquals(1.0f, SplashProgress.displayProgress(4.0f));
    }

    @Test
    void fadeInReachesFullOpacity() {
        assertEquals(0.0f, SplashProgress.fadeIn(1000L, 1000L), 1.0e-6f);
        assertTrue(SplashProgress.fadeIn(1000L, 1100L) > 0.0f);
        assertEquals(1.0f, SplashProgress.fadeIn(1000L, 1000L + SplashProgress.FADE_IN_MS), 1.0e-6f);
        assertEquals(1.0f, SplashProgress.fadeIn(0L, 1000L), 1.0e-6f);
    }

    @Test
    void fadeOutAndExitAreMonotonic() {
        float previous = 2.0f;
        for (long elapsed = 0; elapsed <= SplashProgress.FADE_OUT_MS; elapsed += 50) {
            float alpha = SplashProgress.fadeOut(1000L, 1000L + elapsed);
            assertTrue(alpha <= previous + 1.0e-6f, "fade-out must not go up");
            previous = alpha;
        }
        assertEquals(0.0f, SplashProgress.fadeOut(1000L, 1000L + SplashProgress.FADE_OUT_MS), 1.0e-6f);

        assertEquals(0.0f, SplashProgress.exit(1000L, 1000L), 1.0e-6f);
        assertEquals(1.0f, SplashProgress.exit(1000L,
            1000L + SplashProgress.FADE_OUT_MS / 2L + SplashProgress.EXIT_MS), 1.0e-6f);
    }

    @Test
    void percentAndStageFollowProgress() {
        assertEquals(0, SplashProgress.percent(-1.0f));
        assertEquals(50, SplashProgress.percent(0.5f));
        assertEquals(100, SplashProgress.percent(4.0f));

        assertEquals("loading.splash.stage.reload", SplashProgress.stageKey(0.0f));
        assertEquals("loading.splash.stage.reload", SplashProgress.stageKey(0.33f));
        assertEquals("loading.splash.stage.models", SplashProgress.stageKey(0.34f));
        assertEquals("loading.splash.stage.finish", SplashProgress.stageKey(0.68f));
        assertEquals("loading.splash.stage.ready", SplashProgress.stageKey(1.0f));
    }

    @Test
    void finishRequiresCompletedTimestamp() {
        assertFalse(SplashProgress.finished(0L, 5000L));
        assertFalse(SplashProgress.finished(1000L, 1000L + SplashProgress.FADE_OUT_MS));
        assertTrue(SplashProgress.finished(1000L,
            1000L + SplashProgress.FADE_OUT_MS + SplashProgress.EXIT_MS));
    }
}
