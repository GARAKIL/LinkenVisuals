package dev.linkenvisuals.ui.linkmenu;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LinkenAnimationTest {
    @Test
    void initialValueIsZero() {
        LinkenAnimation anim = LinkenAnimation.hover();
        assertEquals(0.0f, anim.get(), 0.001f);
    }

    @Test
    void forwardProgressReachesOne() throws InterruptedException {
        LinkenAnimation anim = LinkenAnimation.hover();
        anim.update(true);
        Thread.sleep(150);
        assertEquals(1.0f, anim.get(), 0.001f);
    }

    @Test
    void reverseProgressReturnsToZero() throws InterruptedException {
        LinkenAnimation anim = LinkenAnimation.hover();
        anim.update(true);
        Thread.sleep(150);
        assertEquals(1.0f, anim.get(), 0.001f);
        anim.update(false);
        Thread.sleep(150);
        assertEquals(0.0f, anim.get(), 0.001f);
    }

    @Test
    void animationEasesOutCubically() throws InterruptedException {
        LinkenAnimation anim = new LinkenAnimation(300L);
        anim.update(true);
        Thread.sleep(60);
        float value = anim.get();
        // At t ~ 0.2, linear is 0.2, ease-out cubic (1 - 0.8^3) is 0.488.
        // It must be significantly ahead of linear progress due to ease-out.
        assertTrue(value > 0.25f, "Ease-out cubic should start fast, was " + value);
        assertTrue(value <= 1.0f);
    }
}
