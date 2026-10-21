package dev.linkenvisuals.clickgui.link;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LinkGuiThemeTest {
    @AfterEach
    void resetTheme() {
        LinkGuiTheme.apply("MIDNIGHT");
    }

    @Test
    void presetsExposeDistinctAccentsAndKnownNames() {
        for (LinkGuiTheme.Preset preset : LinkGuiTheme.Preset.values()) {
            assertEquals(preset, LinkGuiTheme.Preset.byName(preset.name()));
            assertEquals(preset, LinkGuiTheme.Preset.byName(preset.label()));
            assertNotEquals(preset.accent(), preset.accentDim());
        }
        assertEquals(LinkGuiTheme.Preset.MIDNIGHT, LinkGuiTheme.Preset.byName(null));
        assertEquals(LinkGuiTheme.Preset.MIDNIGHT, LinkGuiTheme.Preset.byName("missing"));
    }

    @Test
    void applyUpdatesAccentAndCycleWraps() {
        LinkGuiTheme.apply("OCEAN");
        assertEquals(LinkGuiTheme.Preset.OCEAN, LinkGuiTheme.preset());
        assertEquals(LinkGuiTheme.Preset.OCEAN.accent(), LinkGuiTheme.ACCENT);
        assertEquals("OCEAN", LinkGuiTheme.themeLabel());

        LinkGuiTheme.apply("EMERALD");
        assertEquals("LIGHT", LinkGuiTheme.cycle());
        assertEquals(LinkGuiTheme.Preset.LIGHT, LinkGuiTheme.preset());
        assertEquals("FROST", LinkGuiTheme.cycle());
        assertEquals(LinkGuiTheme.Preset.FROST, LinkGuiTheme.preset());
        assertEquals("MIDNIGHT", LinkGuiTheme.cycle());
        assertEquals(LinkGuiTheme.Preset.MIDNIGHT, LinkGuiTheme.preset());
        assertEquals(LinkGuiTheme.Preset.MIDNIGHT.accent(), LinkGuiTheme.ACCENT);
    }

    @Test
    void lightPresetsSwitchTheMaterialTokensAndDarkPresetsRestoreThem() {
        LinkGuiTheme.apply("LIGHT");
        assertTrue(LinkGuiTheme.isLight());
        assertNotEquals(LinkGuiTheme.Preset.MIDNIGHT.accent(), LinkGuiTheme.ACCENT);
        assertTrue((LinkGuiTheme.TEXT & 0xFFFFFF) < 0x808080);
        assertTrue((LinkGuiTheme.PANEL & 0xFFFFFF) > 0x808080);

        LinkGuiTheme.apply("FROST");
        assertTrue(LinkGuiTheme.isLight());
        assertEquals(LinkGuiTheme.Preset.FROST.accent(), LinkGuiTheme.ACCENT);

        LinkGuiTheme.apply("MIDNIGHT");
        assertFalse(LinkGuiTheme.isLight());
        assertTrue((LinkGuiTheme.TEXT & 0xFFFFFF) > 0x808080);
        assertTrue((LinkGuiTheme.PANEL & 0xFFFFFF) < 0x808080);
    }

    @Test
    void lightPresetsExposeNames() {
        assertEquals(LinkGuiTheme.Preset.LIGHT, LinkGuiTheme.Preset.byName("light"));
        assertEquals(LinkGuiTheme.Preset.FROST, LinkGuiTheme.Preset.byName("Frost"));
    }
}
