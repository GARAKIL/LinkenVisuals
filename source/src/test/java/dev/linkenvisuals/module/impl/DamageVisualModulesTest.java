package dev.linkenvisuals.module.impl;

import dev.linkenvisuals.clickgui.Category;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Locks the donor DamageParticles/FortniteDamage module defaults. */
class DamageVisualModulesTest {
    @Test
    void damageParticlesDefaultsMatchTheDonor() {
        DamageParticlesModule module = new DamageParticlesModule();
        assertEquals("damageparticles", module.id());
        assertEquals(Category.VISUAL, module.info().category());
        assertFalse(module.enabled());
        assertEquals(30.0f, module.count.value(), 1.0e-6f);
        assertEquals(30.0f, module.size.value(), 1.0e-6f);
        assertTrue(module.spawnOnAttack.value());
        assertTrue(module.spawnOnThrow.value());
        assertEquals("Bloom", module.mode.current());
        assertEquals(0.5f, module.particleSize.value(), 1.0e-6f);
    }

    @Test
    void fortniteDamageDefaultsMatchTheDonor() {
        FortniteDamageModule module = new FortniteDamageModule();
        assertEquals("fortnitedamage", module.id());
        assertEquals(Category.VISUAL, module.info().category());
        assertFalse(module.enabled());
        assertEquals("Fortnite", module.style.current());
        assertEquals(0.032f, module.size.value(), 1.0e-6f);
        assertEquals(850.0f, module.lifetime.value(), 1.0e-6f);
        assertEquals(0.42f, module.rise.value(), 1.0e-6f);
        assertFalse(module.themeColor.value());
        assertTrue(module.critColor.value());
    }

    @Test
    void slidersKeepTheDonorRanges() {
        DamageParticlesModule particles = new DamageParticlesModule();
        particles.count.setValue(0.0f);
        assertEquals(10.0f, particles.count.value(), 1.0e-6f);
        particles.count.setValue(99.0f);
        assertEquals(60.0f, particles.count.value(), 1.0e-6f);

        FortniteDamageModule numbers = new FortniteDamageModule();
        numbers.lifetime.setValue(10.0f);
        assertEquals(300.0f, numbers.lifetime.value(), 1.0e-6f);
        numbers.lifetime.setValue(9999.0f);
        assertEquals(1800.0f, numbers.lifetime.value(), 1.0e-6f);
    }
}
