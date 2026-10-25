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

        assertEquals("Beta", rules.defaultGuiStyle());
        assertEquals(java.util.List.of("Beta", "Classic"), rules.guiStyles());
        assertTrue(rules.allowsGuiStyle("Classic"));
        assertTrue(rules.allowsGuiStyle("Beta"));
        assertFalse(rules.allowsGuiStyle("Linken"));
        assertEquals("Beta", rules.sanitizeGuiStyle("Linken"));

        assertEquals(MenuStyle.BETA, rules.defaultMenuStyle());
        assertEquals(java.util.List.of(MenuStyle.BETA, MenuStyle.VANILLA), rules.menuStyles());
        assertEquals(MenuStyle.BETA, rules.parseMenuStyle("Linken"));
        assertEquals(MenuStyle.BETA, rules.parseMenuStyle(""));
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

        assertEquals("Beta", rules.defaultGuiStyle());
        assertEquals(java.util.List.of("Beta", "Classic", "Linken"), rules.guiStyles());
        assertTrue(rules.allowsGuiStyle("Linken"));
        assertTrue(rules.allowsGuiStyle("Classic"));
        assertTrue(rules.allowsGuiStyle("Beta"));
        assertEquals("Classic", rules.sanitizeGuiStyle("Classic"));

        assertEquals(MenuStyle.BETA, rules.defaultMenuStyle());
        assertEquals(java.util.List.of(MenuStyle.BETA, MenuStyle.LINKEN, MenuStyle.VANILLA), rules.menuStyles());
        assertEquals(MenuStyle.BETA, rules.parseMenuStyle("Beta"));
        assertEquals(MenuStyle.LINKEN, rules.parseMenuStyle("Linken"));
        assertEquals(MenuStyle.LINKEN, rules.nextMenuStyle(MenuStyle.BETA));

        assertEquals(9, rules.hudStyles().size());
        assertEquals("Strange", rules.defaultHudStyle());
        assertEquals("Strange", rules.sanitizeHudStyle("Strange"));
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
