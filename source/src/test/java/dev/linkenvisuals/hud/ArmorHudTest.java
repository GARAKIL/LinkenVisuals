package dev.linkenvisuals.hud;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ArmorHudTest {
    @Test
    void durabilityPercentClampsAndRounds() {
        assertEquals(100, ArmorHud.durabilityPercent(100, 0));
        assertEquals(50, ArmorHud.durabilityPercent(100, 50));
        assertEquals(0, ArmorHud.durabilityPercent(100, 100));
        assertEquals(0, ArmorHud.durabilityPercent(100, 150));
        assertEquals(-1, ArmorHud.durabilityPercent(0, 0));
    }

    @Test
    void criticalColourUsesRedFamily() {
        assertTrue((ArmorHud.durabilityColor(10, 255) & 0x00FF0000) != 0);
    }
}
