package dev.linkenvisuals.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AnimatedStringTest {

    @Test
    void startsIdleAtTheInitialValue() {
        AnimatedString anim = new AnimatedString(150L, "visual");

        assertEquals("visual", anim.current());
        assertEquals("visual", anim.previous());
        assertFalse(anim.isAnimating());
        assertEquals(1.0f, anim.progress(), 1.0e-4f);
    }

    @Test
    void updateKeepsPreviousValueAndStartsCrossfade() {
        AnimatedString anim = new AnimatedString(150L, "old");

        anim.update("new");

        assertEquals("new", anim.current());
        assertEquals("old", anim.previous());
        assertTrue(anim.isAnimating());
        float progress = anim.progress();
        assertTrue(progress >= 0.0f && progress <= 1.0f, "progress outside 0..1: " + progress);
    }

    @Test
    void sameValueDoesNotRestartTheCrossfade() throws InterruptedException {
        AnimatedString anim = new AnimatedString(40L, "a");
        anim.update("b");
        Thread.sleep(60L);
        assertEquals(1.0f, anim.progress(), 1.0e-4f);
        assertFalse(anim.isAnimating());

        anim.update("b");

        assertFalse(anim.isAnimating());
        assertEquals("b", anim.previous());
    }

    @Test
    void finishesAfterTheDonorDurationAndResetsToIdle() throws InterruptedException {
        AnimatedString anim = new AnimatedString(40L, "before");
        anim.update("after");
        assertTrue(anim.isAnimating());

        Thread.sleep(60L);

        assertEquals(1.0f, anim.progress(), 1.0e-4f);
        assertFalse(anim.isAnimating());
        assertEquals("after", anim.current());
        assertEquals("after", anim.previous());
    }

    @Test
    void nullValuesAreNormalizedToEmptyStrings() {
        AnimatedString anim = new AnimatedString(150L, null);

        assertEquals("", anim.current());
        anim.update(null);
        assertFalse(anim.isAnimating());
    }
}
