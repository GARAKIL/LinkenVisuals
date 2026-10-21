package dev.linkenvisuals.ui.linkmenu;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LinkenPackSearchTest {
    @Test
    void emptyQueryMatchesEveryPack() {
        assertTrue(LinkenPackSearch.matches("Vanilla", "The default look", "vanilla", ""));
        assertTrue(LinkenPackSearch.matches("Vanilla", "The default look", "vanilla", null));
        assertTrue(LinkenPackSearch.matches("Vanilla", "The default look", "vanilla", "   "));
    }

    @Test
    void matchesNameCaseInsensitively() {
        assertTrue(LinkenPackSearch.matches("Fresh Animations", "", "fresh_animations", "FRESH"));
        assertTrue(LinkenPackSearch.matches("Fresh Animations", "", "fresh_animations", "anim"));
    }

    @Test
    void matchesDescriptionAndId() {
        assertTrue(LinkenPackSearch.matches("Pack", "Better leaves and grass", "pack", "leaves"));
        assertTrue(LinkenPackSearch.matches("Pack", "", "file/my_pack.zip", "my_pack"));
    }

    @Test
    void rejectsUnrelatedQueries() {
        assertFalse(LinkenPackSearch.matches("Vanilla", "The default look", "vanilla", "shaders"));
    }

    @Test
    void initialsUseAtMostTwoWords() {
        assertEquals("FA", LinkenPackSearch.initials("Fresh Animations"));
        assertEquals("V", LinkenPackSearch.initials("Vanilla"));
        assertEquals("RP", LinkenPackSearch.initials("   "));
        assertEquals("RP", LinkenPackSearch.initials(null));
    }
}
