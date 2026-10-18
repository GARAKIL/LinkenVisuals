package dev.linkenvisuals.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProfileNamesTest {
    @Test
    void keepsPlainNames() {
        assertEquals("Default", ProfileNames.sanitize("Default"));
        assertEquals("Мой конфиг 2", ProfileNames.sanitize("Мой конфиг 2"));
        assertEquals("pvp-loadout_1", ProfileNames.sanitize("  pvp-loadout_1  "));
    }

    @Test
    void stripsPathSeparatorsAndDotSegments() {
        assertEquals("etc_passwd", ProfileNames.sanitize("../etc/passwd"));
        assertEquals("evil", ProfileNames.sanitize("..\\evil"));
        assertEquals("", ProfileNames.sanitize(".."));
        assertEquals("", ProfileNames.sanitize("."));
        assertEquals("a__b", ProfileNames.sanitize("a..b"));
        assertEquals("My_Profile_Name", ProfileNames.sanitize("My/Profile\\Name"));
        assertEquals("name_", ProfileNames.sanitize("name. "));
    }

    @Test
    void stripsControlCharactersAndIllegalFsChars() {
        assertEquals("ab_cd", ProfileNames.sanitize("ab\u0001cd"));
        assertEquals("a_b", ProfileNames.sanitize("a*b"));
        assertEquals("a_b", ProfileNames.sanitize("a?b"));
        assertEquals("a_b_c", ProfileNames.sanitize("a<b>c"));
    }

    @Test
    void rejectsNullAndEmpty() {
        assertEquals("", ProfileNames.sanitize(null));
        assertEquals("", ProfileNames.sanitize("   "));
        assertEquals("", ProfileNames.sanitize("..."));
    }

    @Test
    void capsLength() {
        String longName = "x".repeat(200);
        String clean = ProfileNames.sanitize(longName);
        assertEquals(ProfileNames.MAX_LENGTH, clean.length());
        assertTrue(clean.chars().allMatch(c -> c == 'x'));
    }
}
