package dev.linkenvisuals.clickgui;

import dev.linkenvisuals.clickgui.setting.KeybindSetting;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Locks the key/mouse bind representation used by the donor helper actions. */
class KeybindSettingTest {
    @Test
    void keyOnlyBehaviorIsUnchanged() {
        KeybindSetting bind = new KeybindSetting("Desor", "R");
        assertEquals("R", bind.label());
        assertEquals("R", bind.raw());
        assertEquals(82, bind.code());
        assertFalse(bind.mouse());
        assertTrue(bind.matchesKey(82));
        assertFalse(bind.matchesKey(83));
        assertFalse(bind.matchesMouse(0));

        bind.resolve("F");
        assertEquals(70, bind.code());
        assertEquals("F", bind.raw());

        bind.clear();
        assertEquals("NONE", bind.label());
        assertEquals(-1, bind.code());
        assertEquals("NONE", bind.raw());
    }

    @Test
    void mouseBindsUseLmbRmbLabelsAndPersistAsMouseTokens() {
        KeybindSetting bind = new KeybindSetting("Desor", "NONE", true);
        bind.resolveMouse(0);
        assertEquals("LMB", bind.label());
        assertEquals("MOUSE1", bind.raw());
        assertEquals(-1, bind.code());
        assertTrue(bind.mouse());
        assertTrue(bind.matchesMouse(0));
        assertFalse(bind.matchesMouse(1));
        assertFalse(bind.matchesKey(0));

        bind.resolveMouse(1);
        assertEquals("RMB", bind.label());
        assertEquals("MOUSE2", bind.raw());
        assertTrue(bind.matchesMouse(1));
    }

    @Test
    void parsesDonorAndConfigMouseTokens() {
        KeybindSetting bind = new KeybindSetting("Desor", "NONE", true);
        bind.resolve("M1");
        assertTrue(bind.matchesMouse(0));
        assertEquals("LMB", bind.label());

        bind.resolve("MOUSE2");
        assertTrue(bind.matchesMouse(1));
        assertEquals("RMB", bind.label());

        KeybindSetting loaded = new KeybindSetting("Trapka", "MOUSE1", true);
        assertTrue(loaded.matchesMouse(0));
        assertEquals("MOUSE1", loaded.raw());
    }

    @Test
    void keyOnlySettingsNeverExposeDeadMouseBinds() {
        KeybindSetting bind = new KeybindSetting("Key", "M1");
        assertEquals("NONE", bind.label(), "legacy mouse labels on key-only controls are cleared");
        assertEquals(-1, bind.code());

        bind.resolve("A");
        bind.resolve("MOUSE1");
        assertEquals("NONE", bind.label(), "a mouse token must not become a dead key label");
        assertEquals(-1, bind.code());
        assertFalse(bind.mouse());
    }

    @Test
    void listeningStateShowsEllipsisWithoutChangingRawValue() {
        KeybindSetting bind = new KeybindSetting("Desor", "NONE", true);
        bind.arm();
        assertEquals("...", bind.label());
        assertEquals("NONE", bind.raw());
        bind.resolveMouse(1);
        assertFalse(bind.listening());
        assertEquals("RMB", bind.label());
    }
}
