package dev.linkenvisuals.clickgui;

import org.lwjgl.glfw.GLFW;

public final class KeyNames {
    private KeyNames() {
    }

    public static String of(int keyCode) {
        if (keyCode >= GLFW.GLFW_KEY_A && keyCode <= GLFW.GLFW_KEY_Z) {
            return String.valueOf((char) ('A' + keyCode - GLFW.GLFW_KEY_A));
        }
        if (keyCode >= GLFW.GLFW_KEY_0 && keyCode <= GLFW.GLFW_KEY_9) {
            return String.valueOf((char) ('0' + keyCode - GLFW.GLFW_KEY_0));
        }
        if (keyCode >= GLFW.GLFW_KEY_KP_0 && keyCode <= GLFW.GLFW_KEY_KP_9) {
            return "NUM " + (char) ('0' + keyCode - GLFW.GLFW_KEY_KP_0);
        }
        if (keyCode >= GLFW.GLFW_KEY_F1 && keyCode <= GLFW.GLFW_KEY_F25) {
            return "F" + (keyCode - GLFW.GLFW_KEY_F1 + 1);
        }
        return switch (keyCode) {
            case GLFW.GLFW_KEY_SPACE -> "SPACE";
            case GLFW.GLFW_KEY_APOSTROPHE -> "'";
            case GLFW.GLFW_KEY_COMMA -> ",";
            case GLFW.GLFW_KEY_MINUS -> "-";
            case GLFW.GLFW_KEY_PERIOD -> ".";
            case GLFW.GLFW_KEY_SLASH -> "/";
            case GLFW.GLFW_KEY_SEMICOLON -> ";";
            case GLFW.GLFW_KEY_EQUAL -> "=";
            case GLFW.GLFW_KEY_LEFT_BRACKET -> "[";
            case GLFW.GLFW_KEY_RIGHT_BRACKET -> "]";
            case GLFW.GLFW_KEY_BACKSLASH -> "\\";
            case GLFW.GLFW_KEY_GRAVE_ACCENT -> "`";
            case GLFW.GLFW_KEY_ENTER -> "ENTER";
            case GLFW.GLFW_KEY_TAB -> "TAB";
            case GLFW.GLFW_KEY_BACKSPACE -> "BACK";
            case GLFW.GLFW_KEY_INSERT -> "INS";
            case GLFW.GLFW_KEY_DELETE -> "DEL";
            case GLFW.GLFW_KEY_RIGHT -> "RIGHT";
            case GLFW.GLFW_KEY_LEFT -> "LEFT";
            case GLFW.GLFW_KEY_DOWN -> "DOWN";
            case GLFW.GLFW_KEY_UP -> "UP";
            case GLFW.GLFW_KEY_PAGE_UP -> "PGUP";
            case GLFW.GLFW_KEY_PAGE_DOWN -> "PGDN";
            case GLFW.GLFW_KEY_HOME -> "HOME";
            case GLFW.GLFW_KEY_END -> "END";
            case GLFW.GLFW_KEY_CAPS_LOCK -> "CAPS";
            case GLFW.GLFW_KEY_LEFT_SHIFT -> "LSHIFT";
            case GLFW.GLFW_KEY_RIGHT_SHIFT -> "RSHIFT";
            case GLFW.GLFW_KEY_LEFT_CONTROL -> "LCTRL";
            case GLFW.GLFW_KEY_RIGHT_CONTROL -> "RCTRL";
            case GLFW.GLFW_KEY_LEFT_ALT -> "LALT";
            case GLFW.GLFW_KEY_RIGHT_ALT -> "RALT";
            case GLFW.GLFW_KEY_KP_ADD -> "NUM +";
            case GLFW.GLFW_KEY_KP_SUBTRACT -> "NUM -";
            case GLFW.GLFW_KEY_KP_MULTIPLY -> "NUM *";
            case GLFW.GLFW_KEY_KP_DIVIDE -> "NUM /";
            case GLFW.GLFW_KEY_KP_DECIMAL -> "NUM .";
            case GLFW.GLFW_KEY_KP_ENTER -> "NUM ENTER";
            default -> "KEY " + keyCode;
        };
    }

    /**
     * Reverse of {@link #of(int)}: maps a stored bind label back to its GLFW key code.
     * Cleared or unknown labels ("NONE", "", "...") and unparsable values resolve to {@code -1}.
     */
    public static int codeOf(String name) {
        if (name == null) {
            return -1;
        }
        String n = name.trim();
        if (n.isEmpty() || "NONE".equalsIgnoreCase(n) || "...".equals(n)) {
            return -1;
        }
        if (n.length() == 1) {
            char c = n.charAt(0);
            if (c >= 'A' && c <= 'Z') {
                return GLFW.GLFW_KEY_A + (c - 'A');
            }
            if (c >= '0' && c <= '9') {
                return GLFW.GLFW_KEY_0 + (c - '0');
            }
        }
        if (n.startsWith("NUM ")) {
            String rest = n.substring(4);
            if (rest.length() == 1) {
                char c = rest.charAt(0);
                if (c >= '0' && c <= '9') {
                    return GLFW.GLFW_KEY_KP_0 + (c - '0');
                }
            }
            return switch (rest) {
                case "+" -> GLFW.GLFW_KEY_KP_ADD;
                case "-" -> GLFW.GLFW_KEY_KP_SUBTRACT;
                case "*" -> GLFW.GLFW_KEY_KP_MULTIPLY;
                case "/" -> GLFW.GLFW_KEY_KP_DIVIDE;
                case "." -> GLFW.GLFW_KEY_KP_DECIMAL;
                case "ENTER" -> GLFW.GLFW_KEY_KP_ENTER;
                default -> -1;
            };
        }
        if (n.startsWith("F") && n.length() > 1) {
            int f = parseIndex(n.substring(1));
            if (f >= 1 && f <= 25) {
                return GLFW.GLFW_KEY_F1 + f - 1;
            }
            return -1;
        }
        if (n.startsWith("KEY ")) {
            return parseIndex(n.substring(4));
        }
        return switch (n) {
            case "SPACE" -> GLFW.GLFW_KEY_SPACE;
            case "'" -> GLFW.GLFW_KEY_APOSTROPHE;
            case "," -> GLFW.GLFW_KEY_COMMA;
            case "-" -> GLFW.GLFW_KEY_MINUS;
            case "." -> GLFW.GLFW_KEY_PERIOD;
            case "/" -> GLFW.GLFW_KEY_SLASH;
            case ";" -> GLFW.GLFW_KEY_SEMICOLON;
            case "=" -> GLFW.GLFW_KEY_EQUAL;
            case "[" -> GLFW.GLFW_KEY_LEFT_BRACKET;
            case "]" -> GLFW.GLFW_KEY_RIGHT_BRACKET;
            case "\\" -> GLFW.GLFW_KEY_BACKSLASH;
            case "`" -> GLFW.GLFW_KEY_GRAVE_ACCENT;
            case "ENTER" -> GLFW.GLFW_KEY_ENTER;
            case "TAB" -> GLFW.GLFW_KEY_TAB;
            case "BACK" -> GLFW.GLFW_KEY_BACKSPACE;
            case "INS" -> GLFW.GLFW_KEY_INSERT;
            case "DEL" -> GLFW.GLFW_KEY_DELETE;
            case "RIGHT" -> GLFW.GLFW_KEY_RIGHT;
            case "LEFT" -> GLFW.GLFW_KEY_LEFT;
            case "DOWN" -> GLFW.GLFW_KEY_DOWN;
            case "UP" -> GLFW.GLFW_KEY_UP;
            case "PGUP" -> GLFW.GLFW_KEY_PAGE_UP;
            case "PGDN" -> GLFW.GLFW_KEY_PAGE_DOWN;
            case "HOME" -> GLFW.GLFW_KEY_HOME;
            case "END" -> GLFW.GLFW_KEY_END;
            case "CAPS" -> GLFW.GLFW_KEY_CAPS_LOCK;
            case "LSHIFT" -> GLFW.GLFW_KEY_LEFT_SHIFT;
            case "RSHIFT" -> GLFW.GLFW_KEY_RIGHT_SHIFT;
            case "LCTRL" -> GLFW.GLFW_KEY_LEFT_CONTROL;
            case "RCTRL" -> GLFW.GLFW_KEY_RIGHT_CONTROL;
            case "LALT" -> GLFW.GLFW_KEY_LEFT_ALT;
            case "RALT" -> GLFW.GLFW_KEY_RIGHT_ALT;
            default -> -1;
        };
    }

    private static int parseIndex(String s) {
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
