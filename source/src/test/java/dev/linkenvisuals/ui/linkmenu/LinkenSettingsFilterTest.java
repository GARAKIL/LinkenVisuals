package dev.linkenvisuals.ui.linkmenu;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LinkenSettingsFilterTest {
    private static final LinkenSettingsOption RENDER = LinkenSettingsOption.toggle("Render Distance", () -> true, () -> {});
    private static final LinkenSettingsOption SOUND = LinkenSettingsOption.toggle("Master Volume", () -> false, () -> {});
    private static final LinkenSettingsOption CHAT = LinkenSettingsOption.display("Chat Opacity", () -> "80%");

    @Test
    void emptyQueryKeepsEveryRow() {
        assertEquals(List.of(RENDER, SOUND, CHAT),
            LinkenSettingsFilter.filter(List.of(RENDER, SOUND, CHAT), "  "));
    }

    @Test
    void filterIsCaseInsensitiveAndTrimmed() {
        assertEquals(List.of(SOUND), LinkenSettingsFilter.filter(List.of(RENDER, SOUND, CHAT), " volume "));
        assertEquals(List.of(CHAT), LinkenSettingsFilter.filter(List.of(RENDER, SOUND, CHAT), "opacity"));
    }

    @Test
    void unmatchedQueryIsEmpty() {
        assertEquals(List.of(), LinkenSettingsFilter.filter(List.of(RENDER, SOUND), "narrator"));
    }
}
