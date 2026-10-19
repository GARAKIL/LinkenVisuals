package dev.linkenvisuals.pet;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Timeline and per-bird alpha math of the raven ULT scene (ported donor numbers). */
class PetUltimateTest {

    @Test
    void phaseWindowsMatchDonorTimeline() {
        assertEquals(19000L, PetUltimate.ULT_MS);
        assertEquals(42, PetUltimate.ULT_RAVENS);
        assertEquals(0.0f, PetUltimate.phaseProgress(0L, 0L, PetUltimate.ULT_ASCEND_MS));
        assertEquals(1.0f, PetUltimate.phaseProgress(
            PetUltimate.ULT_ASCEND_MS, 0L, PetUltimate.ULT_ASCEND_MS));
        // Pull-in starts exactly when ascend ends.
        assertEquals(0.0f, PetUltimate.phaseProgress(
            PetUltimate.ULT_ASCEND_MS, PetUltimate.ULT_ASCEND_MS, PetUltimate.ULT_IN_MS));
        // The donor's windows: disperse opens at 17 s but the scene cuts at 19 s (donor overlap:
        // the disperse never plays out completely). Locked in so a future retune stays conscious.
        long disperseStart = PetUltimate.ULT_ASCEND_MS + PetUltimate.ULT_IN_MS
            + PetUltimate.ULT_HOLD_MS + PetUltimate.ULT_BREAK_MS;
        assertEquals(17000L, disperseStart);
        assertTrue(PetUltimate.phaseProgress(19000L, disperseStart, PetUltimate.ULT_DISPERSE_MS) < 1.0f);
    }

    @Test
    void attachAndDetachMatchDonorFormulas() {
        assertEquals(0.0f, PetUltimate.attachOf(0.0f, 0.0f));
        assertEquals(1.0f, PetUltimate.attachOf(1.0f, 0.0f));
        assertEquals(0.55f, PetUltimate.attachOf(0.5f, 0.0f), 1.0e-6f);
        assertEquals(0.0f, PetUltimate.detachOf(0.0f, 0.0f));
        assertEquals(0.475f, PetUltimate.detachOf(0.5f, 0.0f), 1.0e-6f);
        // Break alone reaches only 0.95; the disperse phase supplies the rest (donor formula).
        assertEquals(0.95f, PetUltimate.detachOf(1.0f, 0.0f), 1.0e-6f);
        // Both are clamped (overlapping windows cannot overshoot).
        assertEquals(1.0f, PetUltimate.attachOf(1.0f, 1.0f));
        assertEquals(1.0f, PetUltimate.detachOf(1.0f, 1.0f));
    }

    @Test
    void birdAlphaSpawnsStaggeredAndFadesOut() {
        // Before its own spawn delay a bird is still fully transparent.
        assertEquals(0.0f, PetUltimate.birdAlpha(0, 0L, 0.0f, 1.0f));
        // Once its spawn fade has elapsed the bird is at its "rest" alpha: the donor keeps a
        // slight permanent per-index fade (i * 0.0085), so even at rest later birds sit below 1.
        long spawned = PetUltimate.SPAWN_DELAY_MS
            + (long) (3 * PetUltimate.SPAWN_STEP_MS) + (long) PetUltimate.SPAWN_FADE_MS;
        float rest = 1.0f - PetUltimate.easeInOut(3 * 0.0085f);
        assertEquals(rest, PetUltimate.birdAlpha(3, spawned, 0.0f, 1.0f), 1.0e-5f);
        // Later birds spawn later.
        assertTrue(PetUltimate.birdAlpha(40, spawned, 0.0f, 1.0f)
            < PetUltimate.birdAlpha(0, spawned, 0.0f, 1.0f));
        // During disperse the later birds fade out sooner.
        assertTrue(PetUltimate.fadeOut(20, 0.5f) < PetUltimate.fadeOut(0, 0.5f));
        assertEquals(0.0f, PetUltimate.fadeOut(0, 1.0f));
        // The global "Pet Alpha" setting scales the whole result.
        assertEquals(0.5f * rest, PetUltimate.birdAlpha(3, spawned, 0.0f, 0.5f), 1.0e-5f);
    }

    @Test
    void easeInOutIsAClampedSmoothstep() {
        assertEquals(0.0f, PetUltimate.easeInOut(0.0f));
        assertEquals(1.0f, PetUltimate.easeInOut(1.0f));
        assertEquals(0.5f, PetUltimate.easeInOut(0.5f), 1.0e-6f);
        assertEquals(0.0f, PetUltimate.easeInOut(-3.0f));
        assertEquals(1.0f, PetUltimate.easeInOut(7.0f));
    }
}
