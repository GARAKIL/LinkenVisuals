package dev.linkenvisuals.util.particles;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Locks the donor splash-proximity percentage. */
class ItemRadiusRulesTest {
    @Test
    void percentFallsFromCentreToEdge() {
        assertEquals(100, ItemRadiusRules.proximityPercent(0.0, 4.0));
        assertEquals(50, ItemRadiusRules.proximityPercent(2.0, 4.0));
        assertEquals(0, ItemRadiusRules.proximityPercent(4.0, 4.0));
        assertEquals(0, ItemRadiusRules.proximityPercent(5.0, 4.0));
        assertEquals(0, ItemRadiusRules.proximityPercent(1.0, 0.0));
    }
}
