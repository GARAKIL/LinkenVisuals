package dev.linkenvisuals.module.impl;

import dev.linkenvisuals.clickgui.Category;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/** Locks the donor SelfNametags toggle (a StreamerMode self-label switch). */
class SelfNametagsModuleTest {
    @Test
    void defaultsMatchTheDonor() {
        SelfNametagsModule module = new SelfNametagsModule();
        assertEquals("selfnametags", module.id());
        assertEquals(Category.VISUAL, module.info().category());
        assertFalse(module.enabled());
    }
}
