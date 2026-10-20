package dev.linkenvisuals.ui.style;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MenuStyleTest {
    @Test
    void acceptsLinkenAliases() {
        assertEquals(MenuStyle.LINKEN, MenuStyle.fromName("linken"));
        assertEquals(MenuStyle.LINKEN, MenuStyle.fromName("link"));
        assertEquals(MenuStyle.LINKEN, MenuStyle.fromName("LINKEN"));
        assertEquals(MenuStyle.LINKEN, MenuStyle.fromName(" Linken "));
    }

    @Test
    void unknownOrBlankFallsBackToBeta() {
        assertEquals(MenuStyle.BETA, MenuStyle.fromName(null));
        assertEquals(MenuStyle.BETA, MenuStyle.fromName(""));
        assertEquals(MenuStyle.BETA, MenuStyle.fromName("   "));
        assertEquals(MenuStyle.BETA, MenuStyle.fromName("not-a-style"));
    }

    @Test
    void enumNamesRoundTripForEveryStyle() {
        for (MenuStyle style : MenuStyle.values()) {
            assertEquals(style, MenuStyle.fromName(style.name()));
            assertEquals(style, MenuStyle.fromName(style.displayName()));
        }
    }

    @Test
    void linkenHasStableDisplayName() {
        assertEquals("Linken", MenuStyle.LINKEN.displayName());
    }
}
