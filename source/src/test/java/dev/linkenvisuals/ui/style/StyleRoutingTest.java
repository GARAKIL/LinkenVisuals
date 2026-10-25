package dev.linkenvisuals.ui.style;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StyleRoutingTest {
    @Test
    void videoSettingsCyclesThroughActiveStyles() {
        assertEquals(MenuStyle.VANILLA, StyleRouting.nextStyle(MenuStyle.BETA));
        assertEquals(MenuStyle.BETA, StyleRouting.nextStyle(MenuStyle.VANILLA));
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
