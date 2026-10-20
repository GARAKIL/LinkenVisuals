package dev.linkenvisuals.clickgui.link;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

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
        assertEquals("MIDNIGHT", LinkGuiTheme.cycle());
        assertEquals(LinkGuiTheme.Preset.MIDNIGHT, LinkGuiTheme.preset());
        assertEquals(LinkGuiTheme.Preset.MIDNIGHT.accent(), LinkGuiTheme.ACCENT);
    }
}
