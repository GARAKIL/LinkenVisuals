package dev.linkenvisuals.module.impl;

import dev.linkenvisuals.clickgui.Category;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Locks the donor Ft/Hw/Rw helper module ids and unbound defaults. */
class HelperModulesTest {
    @Test
    void ftHelperDefaults() {
        FtHelperModule module = new FtHelperModule();
        assertEquals("fthelper", module.id());
        assertEquals(Category.MISC, module.info().category());
        assertFalse(module.enabled());
        assertEquals(-1, module.desor.code());
        assertEquals(-1, module.snowball.code());
    }

    @Test
    void hwHelperDefaults() {
        HwHelperModule module = new HwHelperModule();
        assertEquals("hwhelper", module.id());
        assertEquals(Category.MISC, module.info().category());
        assertFalse(module.enabled());
        assertEquals(-1, module.stan.code());
        assertEquals(-1, module.boomThing.code());
    }

    @Test
    void rwHelperDefaults() {
        RwHelperModule module = new RwHelperModule();
        assertEquals("rwhelper", module.id());
        assertEquals(Category.MISC, module.info().category());
        assertFalse(module.enabled());
        assertEquals(-1, module.antiFly.code());
        assertTrue(module.trap.code() == -1);
    }
}
