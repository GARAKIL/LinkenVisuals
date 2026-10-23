package dev.linkenvisuals.ui.style;

import dev.linkenvisuals.BuildFlavor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StyleFlavorTest {
    @Test
    void strangeExposesOnlyTheStrangeFamily() {
        StyleFlavor.Rules rules = StyleFlavor.rulesFor(StyleFlavor.Family.STRANGE);

        assertEquals("Classic", rules.defaultGuiStyle());
        assertEquals(java.util.List.of("Classic", "Beta"), rules.guiStyles());
        assertTrue(rules.allowsGuiStyle("Classic"));
        assertTrue(rules.allowsGuiStyle("Beta"));
        assertFalse(rules.allowsGuiStyle("Linken"));
        assertEquals("Classic", rules.sanitizeGuiStyle("Linken"));

        assertEquals(MenuStyle.VANILLA, rules.defaultMenuStyle());
        assertEquals(java.util.List.of(MenuStyle.BETA, MenuStyle.VANILLA), rules.menuStyles());
        assertEquals(MenuStyle.VANILLA, rules.parseMenuStyle("Linken"));
        assertEquals(MenuStyle.VANILLA, rules.parseMenuStyle(""));
        assertEquals(MenuStyle.BETA, rules.parseMenuStyle("Beta"));
        assertEquals(MenuStyle.VANILLA, rules.parseMenuStyle("vanilla"));
        assertEquals(MenuStyle.VANILLA, rules.nextMenuStyle(MenuStyle.BETA));
        assertEquals(MenuStyle.BETA, rules.nextMenuStyle(MenuStyle.VANILLA));

        assertEquals(java.util.List.of("Strange"), rules.hudStyles());
        assertEquals("Strange", rules.sanitizeHudStyle("Card"));
        assertEquals("Strange", rules.sanitizeHudStyle("Minimal"));
        assertEquals(java.util.List.of("Rounded"), rules.hudShapes());
        assertEquals("Rounded", rules.sanitizeHudShape("Pill"));
    }

    @Test
    void linkenExposesOnlyTheLinkenFamily() {
        StyleFlavor.Rules rules = StyleFlavor.rulesFor(StyleFlavor.Family.LINKEN);

        assertEquals("Linken", rules.defaultGuiStyle());
        assertEquals(java.util.List.of("Linken"), rules.guiStyles());
        assertTrue(rules.allowsGuiStyle("Linken"));
        assertFalse(rules.allowsGuiStyle("Classic"));
        assertFalse(rules.allowsGuiStyle("Beta"));
        assertEquals("Linken", rules.sanitizeGuiStyle("Classic"));

        assertEquals(MenuStyle.LINKEN, rules.defaultMenuStyle());
        assertEquals(java.util.List.of(MenuStyle.LINKEN), rules.menuStyles());
        assertEquals(MenuStyle.LINKEN, rules.parseMenuStyle("Beta"));
        assertEquals(MenuStyle.LINKEN, rules.parseMenuStyle("Linken"));
        assertEquals(MenuStyle.LINKEN, rules.nextMenuStyle(MenuStyle.LINKEN));

        assertEquals(8, rules.hudStyles().size());
        assertEquals("Card", rules.defaultHudStyle());
        assertEquals("Card", rules.sanitizeHudStyle("Strange"));
        assertEquals("Minimal", rules.sanitizeHudStyle("Minimal"));
        assertEquals(java.util.List.of("Rounded", "Pill", "Sharp"), rules.hudShapes());
        assertEquals("Rounded", rules.sanitizeHudShape("Strange"));
    }

    @Test
    void activeFamilyMatchesTheGeneratedBuildFlavor() {
        assertEquals(BuildFlavor.STRANGE, StyleFlavor.isStrange());
        assertEquals(BuildFlavor.STRANGE ? StyleFlavor.Family.STRANGE : StyleFlavor.Family.LINKEN,
            StyleFlavor.family());
        assertEquals(BuildFlavor.NAME, StyleFlavor.flavorName());
    }
}
