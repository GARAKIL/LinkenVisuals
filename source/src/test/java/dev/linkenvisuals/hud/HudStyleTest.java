package dev.linkenvisuals.hud;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class HudStyleTest {
    @Test
    void shapeRadiusRespectsShapeAndSmallSurfaces() {
        assertEquals(2.0f, HudStyle.radius(HudStyle.Shape.SHARP, 100.0f, 20.0f), 0.01f);
        assertEquals(10.0f, HudStyle.radius(HudStyle.Shape.PILL, 100.0f, 20.0f), 0.01f);
        assertEquals(5.0f, HudStyle.radius(HudStyle.Shape.ROUNDED, 100.0f, 20.0f), 0.01f);
        assertEquals(3.0f, HudStyle.radius(HudStyle.Shape.ROUNDED, 6.0f, 12.0f), 0.01f);
        assertEquals(3.0f, HudStyle.radius(HudStyle.Shape.PILL, 6.0f, 12.0f), 0.01f);
    }
}
