package dev.linkenvisuals.hud;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Edit-mode flag round-trip. The flag is plain static state and must not
 * require a Minecraft bootstrap to exercise.
 */
final class HudManagerEditModeTest {
    @Test
    void editFlagRoundTrip() {
        HudManager.setEditing(false);
        assertFalse(HudManager.isEditing());
        HudManager.setEditing(true);
        assertTrue(HudManager.isEditing());
        HudManager.setEditing(false);
        assertFalse(HudManager.isEditing());
    }
}
