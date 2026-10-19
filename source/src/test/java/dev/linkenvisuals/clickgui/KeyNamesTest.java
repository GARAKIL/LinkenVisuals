package dev.linkenvisuals.clickgui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Round-trips between GLFW key codes and the stored bind labels. */
class KeyNamesTest {

    @Test
    void codeOfInvertsOfForRepresentativeKeys() {
        int[] codes = {
            65, 90,           // A, Z
            48, 57,           // 0, 9
            290, 314,         // F1, F25
            32,               // SPACE
            340,              // LEFT_SHIFT
            324,              // NUM 4
            334,              // NUM +
            39,               // '
            96,               // `
        };
        for (int code : codes) {
            String name = KeyNames.of(code);
            assertEquals(code, KeyNames.codeOf(name), "label " + name);
        }
    }

    @Test
    void unknownAndClearedLabelsResolveToNone() {
        assertEquals(-1, KeyNames.codeOf(null));
        assertEquals(-1, KeyNames.codeOf(""));
        assertEquals(-1, KeyNames.codeOf("NONE"));
        assertEquals(-1, KeyNames.codeOf("..."));
        assertEquals(-1, KeyNames.codeOf("nonsense"));
    }

    @Test
    void fallbackKeyLabelsCarryTheCode() {
        // KeyNames.of falls back to "KEY <n>" for anything outside the named table.
        assertEquals(300, KeyNames.codeOf(KeyNames.of(300)));
    }
}
