package dev.linkenvisuals.util.input;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Small layout-aware text editor shared by the custom LINKEN screens.
 *
 * <p>Supports caret/selection movement, Ctrl/Cmd+A/C/V/X, Ctrl/Cmd+Z/Y,
 * word deletion and clipboard shortcuts. Shortcut matching is delegated to
 * {@link LayoutKeys}, so the same physical keys work on US, AZERTY, QWERTZ and
 * Cyrillic layouts.</p>
 */
public final class TextInput {
    private String value = "";
    private int caret;
    private int anchor;
    private int maxLength = 256;
    private java.util.function.IntPredicate filter = codepoint -> !Character.isISOControl(codepoint);
    private final Deque<Snapshot> undo = new ArrayDeque<>();
    private final Deque<Snapshot> redo = new ArrayDeque<>();
    private String lastSnapshot;

    private record Snapshot(String value, int caret, int anchor) {
    }

    public String value() {
        return value;
    }

    public void setValue(String text) {
        value = text == null ? "" : text;
        caret = value.length();
        anchor = caret;
        undo.clear();
        redo.clear();
        lastSnapshot = null;
    }

    public void clear() {
        setValue("");
    }

    public int caret() {
        return caret;
    }

    public int selectionStart() {
        return Math.min(caret, anchor);
    }

    public int selectionEnd() {
        return Math.max(caret, anchor);
    }

    public boolean hasSelection() {
        return caret != anchor;
    }

    public String selectedText() {
        if (!hasSelection()) {
            return "";
        }
        return value.substring(selectionStart(), selectionEnd());
    }

    public boolean isEmpty() {
        return value.isEmpty();
    }

    public int length() {
        return value.length();
    }

    public TextInput maxLength(int maxLength) {
        this.maxLength = Math.max(1, maxLength);
        return this;
    }

    public TextInput filter(java.util.function.IntPredicate filter) {
        this.filter = filter == null
            ? codepoint -> !Character.isISOControl(codepoint) : filter;
        return this;
    }

    public boolean keyPressed(KeyInput input) {
        if (input == null) {
            return false;
        }
        boolean ctrl = input.hasCtrlOrCmd();
        boolean shift = input.hasShift();
        boolean alt = input.hasAlt();

        if (input.key() == GLFW.GLFW_KEY_INSERT) {
            if (shift) {
                paste();
                return true;
            }
            if (ctrl) {
                copy();
                return true;
            }
        }
        if (input.key() == GLFW.GLFW_KEY_DELETE && shift && !ctrl) {
            cut();
            return true;
        }
        if (ctrl && !alt) {
            if (LayoutKeys.matches(input, 'a') && !shift) {
                selectAll();
                return true;
            }
            if (LayoutKeys.matches(input, 'c') && !shift) {
                copy();
                return true;
            }
            if (LayoutKeys.matches(input, 'v') && !shift) {
                paste();
                return true;
            }
            if (LayoutKeys.matches(input, 'x') && !shift) {
                cut();
                return true;
            }
            if (LayoutKeys.matches(input, 'z')) {
                if (shift) {
                    redo();
                } else {
                    undo();
                }
                return true;
            }
            if (LayoutKeys.matches(input, 'y') && !shift) {
                redo();
                return true;
            }
        }

        return switch (input.key()) {
            case GLFW.GLFW_KEY_BACKSPACE -> {
                if (ctrl) {
                    deleteWordBackward();
                } else {
                    deleteBackward();
                }
                yield true;
            }
            case GLFW.GLFW_KEY_DELETE -> {
                if (ctrl) {
                    deleteWordForward();
                } else {
                    deleteForward();
                }
                yield true;
            }
            case GLFW.GLFW_KEY_LEFT -> {
                moveCaret(ctrl ? previousWord(caret) : caret - 1, shift);
                yield true;
            }
            case GLFW.GLFW_KEY_RIGHT -> {
                moveCaret(ctrl ? nextWord(caret) : caret + 1, shift);
                yield true;
            }
            case GLFW.GLFW_KEY_HOME -> {
                moveCaret(0, shift);
                yield true;
            }
            case GLFW.GLFW_KEY_END -> {
                moveCaret(value.length(), shift);
                yield true;
            }
            default -> false;
        };
    }

    public boolean charTyped(CharInput input) {
        if (input == null || !input.isValidChar() || Character.isISOControl(input.codepoint())) {
            return false;
        }
        insert(input.asString());
        return true;
    }

    public void insert(String text) {
        if (text == null || text.isEmpty()) {
            return;
        }
        StringBuilder allowed = new StringBuilder(text.length());
        for (int offset = 0; offset < text.length(); ) {
            int codepoint = text.codePointAt(offset);
            if (filter.test(codepoint)) {
                allowed.appendCodePoint(codepoint);
            }
            offset += Character.charCount(codepoint);
        }
        if (allowed.isEmpty()) {
            return;
        }
        snapshot();
        deleteSelectionInternal();
        int room = maxLength - value.length();
        if (room <= 0) {
            return;
        }
        String clipped = allowed.length() > room ? allowed.substring(0, room) : allowed.toString();
        value = value.substring(0, caret) + clipped + value.substring(caret);
        caret += clipped.length();
        anchor = caret;
    }

    public void selectAll() {
        anchor = 0;
        caret = value.length();
    }

    public void moveCaret(int position, boolean select) {
        int clamped = Math.max(0, Math.min(value.length(), position));
        caret = clamped;
        if (!select) {
            anchor = clamped;
        }
    }

    public void copy() {
        String selected = selectedText();
        if (selected.isEmpty()) {
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null && client.keyboard != null) {
            client.keyboard.setClipboard(selected);
        }
    }

    public void cut() {
        if (!hasSelection()) {
            return;
        }
        copy();
        snapshot();
        deleteSelectionInternal();
    }

    public void paste() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null && client.keyboard != null) {
            insert(client.keyboard.getClipboard());
        }
    }

    public void undo() {
        if (undo.isEmpty()) {
            return;
        }
        Snapshot current = new Snapshot(value, caret, anchor);
        Snapshot previous = undo.pop();
        redo.push(current);
        restore(previous);
        lastSnapshot = null;
    }

    public void redo() {
        if (redo.isEmpty()) {
            return;
        }
        Snapshot current = new Snapshot(value, caret, anchor);
        Snapshot next = redo.pop();
        undo.push(current);
        restore(next);
        lastSnapshot = null;
    }

    private void restore(Snapshot snapshot) {
        value = snapshot.value();
        caret = Math.min(snapshot.caret(), value.length());
        anchor = Math.min(snapshot.anchor(), value.length());
    }

    private void snapshot() {
        if (value.equals(lastSnapshot)) {
            return;
        }
        undo.push(new Snapshot(value, caret, anchor));
        while (undo.size() > 64) {
            undo.removeLast();
        }
        redo.clear();
        lastSnapshot = value;
    }

    private void deleteSelectionInternal() {
        if (!hasSelection()) {
            return;
        }
        int start = selectionStart();
        int end = selectionEnd();
        value = value.substring(0, start) + value.substring(end);
        caret = start;
        anchor = start;
    }

    private void deleteBackward() {
        if (hasSelection()) {
            snapshot();
            deleteSelectionInternal();
            return;
        }
        if (caret <= 0) {
            return;
        }
        snapshot();
        value = value.substring(0, caret - 1) + value.substring(caret);
        caret--;
        anchor = caret;
    }

    private void deleteForward() {
        if (hasSelection()) {
            snapshot();
            deleteSelectionInternal();
            return;
        }
        if (caret >= value.length()) {
            return;
        }
        snapshot();
        value = value.substring(0, caret) + value.substring(caret + 1);
        anchor = caret;
    }

    private void deleteWordBackward() {
        if (hasSelection()) {
            snapshot();
            deleteSelectionInternal();
            return;
        }
        int start = previousWord(caret);
        while (start > 0 && Character.isWhitespace(value.charAt(start - 1))) {
            start--;
        }
        if (start >= caret) {
            return;
        }
        snapshot();
        value = value.substring(0, start) + value.substring(caret);
        caret = start;
        anchor = start;
    }

    private void deleteWordForward() {
        if (hasSelection()) {
            snapshot();
            deleteSelectionInternal();
            return;
        }
        int end = nextWord(caret);
        if (end <= caret) {
            return;
        }
        snapshot();
        value = value.substring(0, caret) + value.substring(end);
        anchor = caret;
    }

    private int previousWord(int from) {
        int index = Math.max(0, Math.min(value.length(), from));
        while (index > 0 && Character.isWhitespace(value.charAt(index - 1))) {
            index--;
        }
        while (index > 0 && !Character.isWhitespace(value.charAt(index - 1))) {
            index--;
        }
        return index;
    }

    private int nextWord(int from) {
        int index = Math.max(0, Math.min(value.length(), from));
        while (index < value.length() && !Character.isWhitespace(value.charAt(index))) {
            index++;
        }
        while (index < value.length() && Character.isWhitespace(value.charAt(index))) {
            index++;
        }
        return index;
    }
}
