package dev.linkenvisuals.hud;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class CoordinatesHudTest {
    @Test
    void bpsFormattingMatchesDonorTwoDecimals() {
        assertEquals("0.00", CoordinatesHud.formatBps(0.0));
        assertEquals("4.40", CoordinatesHud.formatBps(4.4));
        assertEquals("-4.40", CoordinatesHud.formatBps(-4.4));
        assertEquals("12.35", CoordinatesHud.formatBps(12.345));
        assertEquals("0.00", CoordinatesHud.formatBps(0.004));
    }

    @Test
    void coordsTextMatchesDonorSpacing() {
        assertEquals(" -12  64  -567", CoordinatesHud.coordsText(-12, 64, -567));
    }

    @Test
    void defaultsUseDonorCardHeight() {
        CoordinatesHud hud = new CoordinatesHud();
        assertEquals("coordinateshud", hud.id());
        assertEquals(17.0f, hud.height(), 0.01f);
    }
}
