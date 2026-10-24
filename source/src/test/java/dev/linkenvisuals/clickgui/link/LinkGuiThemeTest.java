package dev.linkenvisuals.clickgui.link;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import dev.linkenvisuals.clickgui.ModuleRegistry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LinkGuiThemeTest {
    private static void applyNow(String name) {
        LinkGuiTheme.apply(name);
        LinkGuiTheme.tick(1.0f);
    }

    @AfterEach
    void resetTheme() {
        applyNow("MIDNIGHT");
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
        applyNow("OCEAN");
        assertEquals(LinkGuiTheme.Preset.OCEAN, LinkGuiTheme.preset());
        assertEquals(LinkGuiTheme.Preset.OCEAN.accent(), LinkGuiTheme.ACCENT);
        assertEquals("OCEAN", LinkGuiTheme.themeLabel());

        applyNow("EMERALD");
        assertEquals("LIGHT", LinkGuiTheme.cycle());
        assertEquals(LinkGuiTheme.Preset.LIGHT, LinkGuiTheme.preset());
        assertEquals("FROST", LinkGuiTheme.cycle());
        assertEquals(LinkGuiTheme.Preset.FROST, LinkGuiTheme.preset());
        assertEquals("MIDNIGHT", LinkGuiTheme.cycle());
        assertEquals(LinkGuiTheme.Preset.MIDNIGHT, LinkGuiTheme.preset());
        LinkGuiTheme.tick(1.0f);
        assertEquals(LinkGuiTheme.Preset.MIDNIGHT.accent(), LinkGuiTheme.ACCENT);
    }

    @Test
    void themeSwitchInterpolatesInsteadOfSnapping() {
        applyNow("MIDNIGHT");
        int before = LinkGuiTheme.ACCENT;
        LinkGuiTheme.apply("OCEAN");
        int target = LinkGuiTheme.Preset.OCEAN.accent();
        assertNotEquals(target, LinkGuiTheme.ACCENT, "apply must not snap the displayed accent");
        LinkGuiTheme.tick(0.016f);
        assertNotEquals(before, LinkGuiTheme.ACCENT, "one frame must move the accent");
        LinkGuiTheme.tick(1.0f);
        assertEquals(target, LinkGuiTheme.ACCENT, "the transition must settle on the target");
    }

    @Test
    void lightPresetsSwitchTheMaterialTokensAndDarkPresetsRestoreThem() {
        applyNow("LIGHT");
        assertTrue(LinkGuiTheme.isLight());
        assertNotEquals(LinkGuiTheme.Preset.MIDNIGHT.accent(), LinkGuiTheme.ACCENT);
        assertTrue((LinkGuiTheme.TEXT & 0xFFFFFF) < 0x808080);
        assertTrue((LinkGuiTheme.PANEL & 0xFFFFFF) > 0x808080);

        applyNow("FROST");
        assertTrue(LinkGuiTheme.isLight());
        assertEquals(LinkGuiTheme.Preset.FROST.accent(), LinkGuiTheme.ACCENT);

        applyNow("MIDNIGHT");
        assertFalse(LinkGuiTheme.isLight());
        assertTrue((LinkGuiTheme.TEXT & 0xFFFFFF) > 0x808080);
        assertTrue((LinkGuiTheme.PANEL & 0xFFFFFF) < 0x808080);
    }

    @Test
    void lightPresetsExposeNames() {
        assertEquals(LinkGuiTheme.Preset.LIGHT, LinkGuiTheme.Preset.byName("light"));
        assertEquals(LinkGuiTheme.Preset.FROST, LinkGuiTheme.Preset.byName("Frost"));
    }

    @Test
    void linkenGuiStyleLocksTheStyleSwitcher() {
        assertTrue(ModuleRegistry.isGuiStyleLocked("Linken"));
        assertTrue(ModuleRegistry.isGuiStyleLocked("linken"));
        assertFalse(ModuleRegistry.isGuiStyleLocked("Beta"));
        assertFalse(ModuleRegistry.isGuiStyleLocked((String) null));
    }
}
