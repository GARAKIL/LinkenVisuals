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
        assertTrue(module.desor.mouseAllowed(), "helper actions accept donor LMB/RMB binds");
        module.desor.resolveMouse(0);
        assertTrue(module.desor.matchesMouse(0));
        assertFalse(module.desor.matchesMouse(1));
        assertEquals("LMB", module.desor.label());
    }

    @Test
    void hwHelperDefaults() {
        HwHelperModule module = new HwHelperModule();
        assertEquals("hwhelper", module.id());
        assertEquals(Category.MISC, module.info().category());
        assertFalse(module.enabled());
        assertEquals(-1, module.stan.code());
        assertEquals(-1, module.boomThing.code());
        assertTrue(module.stan.mouseAllowed());
    }

    @Test
    void rwHelperDefaults() {
        RwHelperModule module = new RwHelperModule();
        assertEquals("rwhelper", module.id());
        assertEquals(Category.MISC, module.info().category());
        assertFalse(module.enabled());
        assertEquals(-1, module.antiFly.code());
        assertTrue(module.trap.code() == -1);
        assertTrue(module.trap.mouseAllowed());
    }
}
