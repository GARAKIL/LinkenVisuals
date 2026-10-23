package dev.linkenvisuals.hud;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class LowArmorNotificationsHudTest {
    @Test
    void stageThresholdsMatchTheDonor() {
        assertEquals(0, LowArmorNotificationsHud.stageFor(100));
        assertEquals(0, LowArmorNotificationsHud.stageFor(50));
        assertEquals(1, LowArmorNotificationsHud.stageFor(49));
        assertEquals(1, LowArmorNotificationsHud.stageFor(25));
        assertEquals(2, LowArmorNotificationsHud.stageFor(24));
        assertEquals(2, LowArmorNotificationsHud.stageFor(0));
        assertEquals(0, LowArmorNotificationsHud.stageFor(-1));
    }
}
