package dev.linkenvisuals.module.impl;

import dev.linkenvisuals.clickgui.Category;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Locks the donor CustomVitals style mapping and neutral defaults. */
class CustomVitalsModuleTest {
    @Test
    void defaultsMatchTheDonor() {
        CustomVitalsModule module = new CustomVitalsModule();
        assertEquals("customvitals", module.id());
        assertEquals(Category.VISUAL, module.info().category());
        assertEquals("Segments", module.style.current());
        assertEquals(8.0f, module.segments.value(), 1.0e-6f);
        assertTrue(module.armorRow());
        assertTrue(module.isSegments());
    }

    @Test
    void styleIndexMapsAllThreeModes() {
        CustomVitalsModule module = new CustomVitalsModule();
        assertEquals(0, module.styleIndex());
        module.style.select(1);
        assertEquals(1, module.styleIndex());
        module.style.select(2);
        assertEquals(2, module.styleIndex());
    }

    @Test
    void segmentCountUsesTheSlider() {
        CustomVitalsModule module = new CustomVitalsModule();
        module.segments.setValue(3.0f);
        assertEquals(3, module.segmentCount());
        module.segments.setValue(10.0f);
        assertEquals(10, module.segmentCount());
    }
}
