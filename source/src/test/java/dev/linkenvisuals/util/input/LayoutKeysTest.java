package dev.linkenvisuals.util.input;

import net.minecraft.client.input.KeyInput;
import org.junit.jupiter.api.Test;
import org.lwjgl.glfw.GLFW;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class LayoutKeysTest {
    @Test
    void physicalUsPositionsAlwaysMatch() {
        assertTrue(LayoutKeys.matches(new KeyInput(GLFW.GLFW_KEY_A, 0, GLFW.GLFW_MOD_CONTROL), 'a'));
        assertTrue(LayoutKeys.matches(new KeyInput(GLFW.GLFW_KEY_C, 0, GLFW.GLFW_MOD_CONTROL), 'c'));
        assertTrue(LayoutKeys.matches(new KeyInput(GLFW.GLFW_KEY_V, 0, GLFW.GLFW_MOD_CONTROL), 'v'));
        assertTrue(LayoutKeys.matches(new KeyInput(GLFW.GLFW_KEY_X, 0, GLFW.GLFW_MOD_CONTROL), 'x'));
        assertTrue(LayoutKeys.matches(new KeyInput(GLFW.GLFW_KEY_Z, 0, GLFW.GLFW_MOD_CONTROL), 'z'));
        assertTrue(LayoutKeys.matches(new KeyInput(GLFW.GLFW_KEY_Y, 0, GLFW.GLFW_MOD_CONTROL), 'y'));
    }

    @Test
    void unrelatedKeysDoNotMatch() {
        assertFalse(LayoutKeys.matches(new KeyInput(GLFW.GLFW_KEY_Q, 0, GLFW.GLFW_MOD_CONTROL), 'a'));
        assertFalse(LayoutKeys.matches(new KeyInput(GLFW.GLFW_KEY_1, 0, 0), 'z'));
    }

    @Test
    void cyrillicAliasesMapTheShortcutLetters() {
        assertTrue(LayoutKeys.cyrillicAlias('а') == 'a');
        assertTrue(LayoutKeys.cyrillicAlias('с') == 'c');
        assertTrue(LayoutKeys.cyrillicAlias('м') == 'v');
        assertTrue(LayoutKeys.cyrillicAlias('ч') == 'x');
        assertTrue(LayoutKeys.cyrillicAlias('я') == 'z');
        assertTrue(LayoutKeys.cyrillicAlias('н') == 'y');
    }
}
