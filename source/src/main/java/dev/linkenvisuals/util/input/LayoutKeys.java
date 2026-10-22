package dev.linkenvisuals.util.input;

import net.minecraft.client.input.KeyInput;
import org.lwjgl.glfw.GLFW;

/**
 * Layout-aware shortcut matching.
 *
 * <p>GLFW key codes are based on the US physical layout, so vanilla
 * {@code Ctrl+A/C/V/X/Z/Y} only fires on the US positions. This helper accepts
 * both the physical US key and the character printed on the current layout
 * (via {@link GLFW#glfwGetKeyName}), including the common Cyrillic aliases for
 * the shortcut keys. It never throws when GLFW is not ready.</p>
 */
public final class LayoutKeys {
    private LayoutKeys() {
    }

    public static boolean matches(KeyInput input, char latin) {
        if (input == null) {
            return false;
        }
        char target = Character.toLowerCase(latin);
        if (target < 'a' || target > 'z') {
            return false;
        }
        int physical = GLFW.GLFW_KEY_A + (target - 'a');
        if (input.key() == physical) {
            return true;
        }
        char printed = printedLetter(input.key(), input.scancode());
        if (printed == target) {
            return true;
        }
        return cyrillicAlias(printed) == target;
    }

    public static char printedLetter(int key, int scancode) {
        try {
            String name = GLFW.glfwGetKeyName(key, scancode);
            if (name == null || name.isEmpty()) {
                return 0;
            }
            int first = name.codePointAt(0);
            if (Character.charCount(first) != name.length()) {
                return 0;
            }
            return Character.toLowerCase((char) first);
        } catch (RuntimeException ignored) {
            return 0;
        }
    }

    /** Printed Cyrillic letters that sit on the physical shortcut positions. */
    static char cyrillicAlias(char c) {
        return switch (c) {
            case 'а' -> 'a';
            case 'с' -> 'c';
            case 'м' -> 'v';
            case 'ч' -> 'x';
            case 'я' -> 'z';
            case 'н' -> 'y';
            default -> 0;
        };
    }
}
