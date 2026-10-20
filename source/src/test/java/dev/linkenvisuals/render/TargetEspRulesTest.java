package dev.linkenvisuals.render;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class TargetEspRulesTest {
    @Test
    void nakedInvisiblePlayerIsHidden() {
        assertTrue(TargetEspRules.hidesInvisiblePlayer(true, false));
    }

    @Test
    void equippedInvisiblePlayerStaysVisible() {
        assertFalse(TargetEspRules.hidesInvisiblePlayer(true, true));
    }

    @Test
    void visiblePlayerStaysVisible() {
        assertFalse(TargetEspRules.hidesInvisiblePlayer(false, false));
        assertFalse(TargetEspRules.hidesInvisiblePlayer(false, true));
    }

    @Test
    void rangeGateAcceptsBoundary() {
        assertTrue(TargetEspRules.withinRange(0.0));
        assertTrue(TargetEspRules.withinRange(TargetEspRules.MAX_RANGE));
        assertFalse(TargetEspRules.withinRange(TargetEspRules.MAX_RANGE + 0.001));
        assertFalse(TargetEspRules.withinRange(Double.NaN));
        assertFalse(TargetEspRules.withinRange(-1.0));
    }

    @Test
    void scaleMatchesDonorFormulaAt70Fov() {
        assertEquals(50.0, TargetEspRules.scaleAtDistance(10.0, 15.0f, 70.0f), 1.0e-6);
        assertEquals(5.0, TargetEspRules.scaleAtDistance(100.0, 15.0f, 70.0f), 1.0e-6);
    }

    @Test
    void scaleCompensatesWiderFov() {
        assertEquals(50.0 * 70.0 / 90.0, TargetEspRules.scaleAtDistance(10.0, 15.0f, 90.0f), 1.0e-6);
    }

    @Test
    void markerSizeNeverDropsBelowFloor() {
        assertEquals(4.5f, TargetEspRules.markerSizePx(16.0, 200.0, 15.0f), 1.0e-4);
        assertEquals(30.0f, TargetEspRules.markerSizePx(40.0, 100.0, 15.0f), 1.0e-4);
    }

    @Test
    void worldSizeCoversScreenFraction() {
        float size = TargetEspRules.worldSizeForPixels(270, 540, 10.0, 70.0f);
        assertEquals((float) (10.0 * Math.tan(Math.toRadians(35.0))), size, 1.0e-4);
    }

    @Test
    void worldSizeRejectsDegenerateInput() {
        assertEquals(0.0f, TargetEspRules.worldSizeForPixels(0.0f, 540, 10.0, 70.0f));
        assertEquals(0.0f, TargetEspRules.worldSizeForPixels(100.0f, 0, 10.0, 70.0f));
        assertEquals(0.0f, TargetEspRules.worldSizeForPixels(100.0f, 540, 0.0, 70.0f));
    }

    @Test
    void fadeStepMovesBothDirections() {
        assertEquals(1.0f, TargetEspRules.fadeStep(0.9f, true, 100.0f, 300), 1.0e-4);
        assertEquals(0.7f, TargetEspRules.fadeStep(0.8f, false, 30.0f, 300), 1.0e-4);
        assertEquals(0.0f, TargetEspRules.fadeStep(0.1f, false, 100.0f, 300), 1.0e-4);
    }

    @Test
    void easeOutCubicEndpointsAndMidpoint() {
        assertEquals(0.0f, TargetEspRules.easeOutCubic(0.0f), 1.0e-4);
        assertEquals(1.0f, TargetEspRules.easeOutCubic(1.0f), 1.0e-4);
        assertEquals(0.875f, TargetEspRules.easeOutCubic(0.5f), 1.0e-4);
        assertEquals(0.0f, TargetEspRules.easeOutCubic(-2.0f), 1.0e-4);
    }
}
