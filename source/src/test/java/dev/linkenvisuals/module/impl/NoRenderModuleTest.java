package dev.linkenvisuals.module.impl;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class NoRenderModuleTest {
    @Test
    void donorDefaultsAreIndependent() {
        NoRenderModule module = new NoRenderModule();

        assertFalse(module.enabled());
        assertTrue(module.hurtCamera.value());
        assertTrue(module.fire.value());
        assertTrue(module.totem.value());
        assertFalse(module.scoreboard.value());
        assertFalse(module.bossBar.value());
        assertTrue(module.particles.value());
        assertFalse(module.armor.value());
        assertFalse(module.inventoryBackground.value());
        assertTrue(module.info().leftSettings().size() == 4);
        assertTrue(module.info().rightSettings().size() == 4);
    }
}
