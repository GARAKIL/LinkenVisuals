package dev.linkenvisuals.util.input;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class TextInputTest {
    @Test
    void typingAndUndoRedoFollowTheCaret() {
        TextInput input = new TextInput().maxLength(16);
        input.insert("a");
        input.insert("b");
        input.insert("c");
        assertEquals("abc", input.value());
        assertEquals(3, input.caret());

        input.undo();
        assertEquals("ab", input.value());
        input.redo();
        assertEquals("abc", input.value());
    }

    @Test
    void selectAllAndReplacementUseTheWholeValue() {
        TextInput input = new TextInput();
        input.insert("hello");
        input.selectAll();
        assertTrue(input.hasSelection());
        assertEquals("hello", input.selectedText());
        input.insert("x");
        assertEquals("x", input.value());
        assertEquals(1, input.caret());
    }

    @Test
    void wordDeletionAndHomeEndMoveTheCaret() {
        TextInput input = new TextInput();
        input.insert("hello big world");
        input.moveCaret(9, false);
        input.keyPressed(ctrl(org.lwjgl.glfw.GLFW.GLFW_KEY_BACKSPACE));
        assertEquals("hello world", input.value());

        input.keyPressed(new net.minecraft.client.input.KeyInput(
            org.lwjgl.glfw.GLFW.GLFW_KEY_HOME, 0, 0));
        assertEquals(0, input.caret());
        input.keyPressed(new net.minecraft.client.input.KeyInput(
            org.lwjgl.glfw.GLFW.GLFW_KEY_END, 0, 0));
        assertEquals(input.value().length(), input.caret());
    }

    @Test
    void filterAndLimitApplyToTypedAndPastedText() {
        TextInput input = new TextInput().maxLength(4)
            .filter(codepoint -> Character.isLetterOrDigit(codepoint) || codepoint == '_');
        input.insert("ab-cd_");
        assertEquals("abcd", input.value());
        assertFalse(input.value().contains("-"));
    }

    @Test
    void physicalShortcutStillWorksWhenLayoutNameIsUnavailable() {
        TextInput input = new TextInput();
        input.insert("abc");
        assertTrue(input.keyPressed(ctrl(org.lwjgl.glfw.GLFW.GLFW_KEY_A)));
        assertTrue(input.hasSelection());
        assertEquals(3, input.caret());
        assertEquals(0, input.selectionStart());
    }

    private static net.minecraft.client.input.KeyInput ctrl(int key) {
        return new net.minecraft.client.input.KeyInput(key, 0, org.lwjgl.glfw.GLFW.GLFW_MOD_CONTROL);
    }
}
