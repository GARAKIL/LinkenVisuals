package dev.linkenvisuals.ui.style;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StyleRoutingTest {
    @Test
    void videoSettingsCyclesThroughAllThreeStyles() {
        assertEquals(MenuStyle.VANILLA, StyleRouting.nextStyle(MenuStyle.BETA));
        assertEquals(MenuStyle.LINKEN, StyleRouting.nextStyle(MenuStyle.VANILLA));
        assertEquals(MenuStyle.BETA, StyleRouting.nextStyle(MenuStyle.LINKEN));

        MenuStyle style = MenuStyle.BETA;
        for (int i = 0; i < 3; i++) {
            style = StyleRouting.nextStyle(style);
        }
        assertEquals(MenuStyle.BETA, style, "three steps must return to the starting style");
    }

    @Test
    void linkenNeverAppliesTheEditablePreset() {
        assertTrue(StyleRouting.shouldApplyPreset(MenuStyle.BETA));
        assertTrue(StyleRouting.shouldApplyPreset(MenuStyle.VANILLA));
        assertFalse(StyleRouting.shouldApplyPreset(MenuStyle.LINKEN));
    }

    @Test
    void unportedLinkenScreensUseVanillaChromeInsteadOfBeta() {
        assertFalse(StyleRouting.usesVanillaChrome(MenuStyle.BETA));
        assertTrue(StyleRouting.usesVanillaChrome(MenuStyle.VANILLA));
        assertTrue(StyleRouting.usesVanillaChrome(MenuStyle.LINKEN),
            "LINKEN fallback screens must not inherit Beta styling");
    }
}
