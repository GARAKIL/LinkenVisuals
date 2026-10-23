package dev.linkenvisuals.module.impl;

import dev.linkenvisuals.clickgui.Category;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Locks the donor ItemRadius defaults and item switches. */
class ItemRadiusModuleTest {
    @Test
    void defaultsMatchTheDonor() {
        ItemRadiusModule module = new ItemRadiusModule();
        assertEquals("itemradius", module.id());
        assertEquals(Category.VISUAL, module.info().category());
        assertFalse(module.enabled());
        assertTrue(module.enderEye.value());
        assertTrue(module.sugar.value());
        assertTrue(module.netheriteScrap.value());
        assertFalse(module.dragonTrap.value());
        assertTrue(module.driedKelp.value());
        assertTrue(module.snowball.value());
        assertTrue(module.explosivePotion.value());
        assertEquals(4.0f, module.explosivePotionRadius.value(), 1.0e-6f);
        assertTrue(module.proximityColor.value());
    }

    @Test
    void potionRadiusStaysInDonorRange() {
        ItemRadiusModule module = new ItemRadiusModule();
        module.explosivePotionRadius.setValue(0.0f);
        assertEquals(1.0f, module.explosivePotionRadius.value(), 1.0e-6f);
        module.explosivePotionRadius.setValue(60.0f);
        assertEquals(30.0f, module.explosivePotionRadius.value(), 1.0e-6f);
    }

    @Test
    void dragonTrapIsAnIndependentToggle() {
        ItemRadiusModule module = new ItemRadiusModule();
        module.dragonTrap.setValue(true);
        assertTrue(module.dragonTrap.value());
        module.netheriteScrap.setValue(false);
        assertTrue(module.dragonTrap.value(), "donor predicate hides it only in the UI, not in state");
    }
}
