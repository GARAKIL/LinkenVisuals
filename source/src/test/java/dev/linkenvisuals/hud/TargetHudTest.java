package dev.linkenvisuals.hud;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class TargetHudTest {
    @Test
    void hpFormattingMatchesDonorThresholds() {
        assertEquals("19.9", TargetHud.formatHp(19.94f));
        assertEquals("999.9", TargetHud.formatHp(999.94f));
        assertEquals("1.0K", TargetHud.formatHp(1000.0f));
        assertEquals("1.5K", TargetHud.formatHp(1499.0f));
        assertEquals("1.0M", TargetHud.formatHp(1_000_000.0f));
        assertEquals("1.5M", TargetHud.formatHp(1_499_999.0f));
    }

    @Test
    void defaultsUseDonorCardGeometry() {
        TargetHud hud = new TargetHud();
        assertEquals("targethud", hud.id());
        assertEquals(104.0f, hud.width(), 0.01f);
        assertEquals(30.0f, hud.height(), 0.01f);
    }
}
