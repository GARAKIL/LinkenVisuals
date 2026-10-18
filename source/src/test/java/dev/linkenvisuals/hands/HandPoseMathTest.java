package dev.linkenvisuals.hands;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HandPoseMathTest {
    @Test
    void reachIsClampedSymmetrically() {
        assertEquals(HandPoseMath.REACH, HandPoseMath.clampReach(999.0f));
        assertEquals(-HandPoseMath.REACH, HandPoseMath.clampReach(-999.0f));
        assertEquals(0.1f, HandPoseMath.clampReach(0.1f));
    }

    @Test
    void depthUsesAsymmetricBounds() {
        assertEquals(HandPoseMath.FAR, HandPoseMath.clampDepth(999.0f));
        assertEquals(HandPoseMath.NEAR, HandPoseMath.clampDepth(-999.0f));
        assertEquals(0.25f, HandPoseMath.clampDepth(0.25f));
    }

    @Test
    void scaleIsClamped() {
        assertEquals(HandPoseMath.MIN_SCALE, HandPoseMath.clampScale(0.0f));
        assertEquals(HandPoseMath.MAX_SCALE, HandPoseMath.clampScale(99.0f));
        assertEquals(1.0f, HandPoseMath.clampScale(1.0f));
    }
}
