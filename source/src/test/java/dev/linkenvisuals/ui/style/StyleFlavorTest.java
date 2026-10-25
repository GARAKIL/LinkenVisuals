package dev.linkenvisuals.ui.style;

import dev.linkenvisuals.BuildFlavor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StyleFlavorTest {
    @Test
    void strangeExposesStrangeFamilyWithLinkenOption() {
        StyleFlavor.Rules rules = StyleFlavor.rulesFor(StyleFlavor.Family.STRANGE);

        assertEquals("Beta", rules.defaultGuiStyle());
        assertEquals(java.util.List.of("Beta", "Classic", "Linken"), rules.guiStyles());
        assertTrue(rules.allowsGuiStyle("Classic"));
        assertTrue(rules.allowsGuiStyle("Beta"));
        assertTrue(rules.allowsGuiStyle("Linken"));
        assertEquals("Linken", rules.sanitizeGuiStyle("Linken"));

        assertEquals(MenuStyle.BETA, rules.defaultMenuStyle());
        assertEquals(java.util.List.of(MenuStyle.BETA, MenuStyle.VANILLA, MenuStyle.LINKEN), rules.menuStyles());
        assertEquals(MenuStyle.LINKEN, rules.parseMenuStyle("Linken"));
        assertEquals(MenuStyle.BETA, rules.parseMenuStyle(""));
        assertEquals(MenuStyle.BETA, rules.parseMenuStyle("Beta"));
        assertEquals(MenuStyle.VANILLA, rules.parseMenuStyle("vanilla"));
        assertEquals(MenuStyle.VANILLA, rules.nextMenuStyle(MenuStyle.BETA));
        assertEquals(MenuStyle.LINKEN, rules.nextMenuStyle(MenuStyle.VANILLA));
        assertEquals(MenuStyle.BETA, rules.nextMenuStyle(MenuStyle.LINKEN));

        assertEquals(java.util.List.of("Strange", "Linken"), rules.hudStyles());
        assertEquals("Strange", rules.defaultHudStyle());
        assertTrue(rules.allowsHudStyle("Strange"));
        assertTrue(rules.allowsHudStyle("Linken"));
        assertEquals("Strange", rules.sanitizeHudStyle("Strange"));
        assertEquals("Linken", rules.sanitizeHudStyle("Linken"));
        assertEquals(java.util.List.of("Rounded"), rules.hudShapes());
        assertEquals("Rounded", rules.defaultHudShape());
        assertEquals("Rounded", rules.sanitizeHudShape("Pill"));
    }

    @Test
    void linkenExposesLinkenFamilyWithStrangeOption() {
        StyleFlavor.Rules rules = StyleFlavor.rulesFor(StyleFlavor.Family.LINKEN);

        assertEquals("Linken", rules.defaultGuiStyle());
        assertEquals(java.util.List.of("Linken", "Beta", "Classic"), rules.guiStyles());
        assertTrue(rules.allowsGuiStyle("Linken"));
        assertTrue(rules.allowsGuiStyle("Beta"));
        assertTrue(rules.allowsGuiStyle("Classic"));
        assertEquals("Linken", rules.sanitizeGuiStyle("Linken"));

        assertEquals(MenuStyle.LINKEN, rules.defaultMenuStyle());
        assertEquals(java.util.List.of(MenuStyle.LINKEN, MenuStyle.BETA, MenuStyle.VANILLA), rules.menuStyles());
        assertTrue(rules.allowsMenuStyle(MenuStyle.LINKEN));
        assertTrue(rules.allowsMenuStyle(MenuStyle.BETA));
        assertTrue(rules.allowsMenuStyle(MenuStyle.VANILLA));
        assertEquals(MenuStyle.LINKEN, rules.parseMenuStyle("Linken"));
        assertEquals(MenuStyle.BETA, rules.nextMenuStyle(MenuStyle.LINKEN));
        assertEquals(MenuStyle.VANILLA, rules.nextMenuStyle(MenuStyle.BETA));
        assertEquals(MenuStyle.LINKEN, rules.nextMenuStyle(MenuStyle.VANILLA));

        assertEquals(java.util.List.of("Linken", "Strange"), rules.hudStyles());
        assertEquals("Linken", rules.defaultHudStyle());
        assertTrue(rules.allowsHudStyle("Linken"));
        assertTrue(rules.allowsHudStyle("Strange"));
        assertEquals("Linken", rules.sanitizeHudStyle("Linken"));
        assertEquals("Strange", rules.sanitizeHudStyle("Strange"));
        assertEquals(java.util.List.of("Rounded"), rules.hudShapes());
        assertEquals("Rounded", rules.defaultHudShape());
        assertEquals("Rounded", rules.sanitizeHudShape("Sharp"));
    }

    @Test
    void activeFamilyMatchesBuildFlavor() {
        assertEquals(BuildFlavor.STRANGE, StyleFlavor.isStrange());
        assertEquals(BuildFlavor.STRANGE ? StyleFlavor.Family.STRANGE : StyleFlavor.Family.LINKEN,
            StyleFlavor.family());
        assertEquals(BuildFlavor.NAME, StyleFlavor.flavorName());
    }
}
