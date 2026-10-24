package dev.linkenvisuals.clickgui.setting;

import dev.linkenvisuals.clickgui.KeyNames;

/**
 * Keyboard bind, optionally extended with donor-style mouse buttons for the
 * Ft/Hw/Rw helper actions. Key-only instances keep the historical behaviour;
 * {@code mouseAllowed} settings can store LMB/RMB instead of a GLFW key.
 */
public final class KeybindSetting extends Setting {
    private String label;
    private String raw;
    private int code = -1;
    private int mouseButton = -1;
    private boolean listening;
    private final boolean mouseAllowed;

    public KeybindSetting(String name, String label) {
        this(name, label, false);
    }

    public KeybindSetting(String name, String label, boolean mouseAllowed) {
        super(name);
        this.mouseAllowed = mouseAllowed;
        this.label = normalizeLabel(label);
        this.raw = this.label;
        int storedMouse = parseMouseToken(label);
        if (mouseAllowed && storedMouse >= 0) {
            applyMouse(storedMouse);
        } else if (storedMouse >= 0) {
            // A stale mouse label on a key-only control would be a dead bind.
            this.label = "NONE";
            this.raw = "NONE";
        } else {
            this.code = KeyNames.codeOf(this.label);
        }
    }

    public String label() {
        return listening ? "..." : label;
    }

    /** Raw stored bind label (never {@code "..."}); used by config persistence. */
    public String raw() {
        return raw;
    }

    /** GLFW key code of the current bind, or {@code -1} for unbound/mouse binds. */
    public int code() {
        return code;
    }

    /** GLFW mouse button of the current bind, or {@code -1} when this is a key bind. */
    public int button() {
        return mouseButton;
    }

    public boolean mouse() {
        return mouseButton >= 0;
    }

    public boolean mouseAllowed() {
        return mouseAllowed;
    }

    public boolean matchesKey(int key) {
        return !mouse() && code >= 0 && code == key;
    }

    public boolean matchesMouse(int button) {
        return mouse() && mouseButton == button;
    }

    public boolean listening() {
        return listening;
    }

    public void arm() {
        listening = true;
    }

    public void resolve(String keyName) {
        int parsedMouse = parseMouseToken(keyName);
        if (mouseAllowed && parsedMouse >= 0) {
            resolveMouse(parsedMouse);
            return;
        }
        if (!mouseAllowed && parsedMouse >= 0) {
            clear();
            return;
        }
        label = normalizeLabel(keyName);
        raw = label;
        code = KeyNames.codeOf(label);
        mouseButton = -1;
        listening = false;
    }

    /** Records a real mouse bind: button {@code 0} = LMB, {@code 1} = RMB. */
    public void resolveMouse(int button) {
        if (!mouseAllowed || button < 0) {
            return;
        }
        applyMouse(button);
        listening = false;
    }

    public void clear() {
        label = "NONE";
        raw = "NONE";
        code = -1;
        mouseButton = -1;
        listening = false;
    }

    private void applyMouse(int button) {
        mouseButton = button;
        code = -1;
        label = mouseLabel(button);
        raw = "MOUSE" + (button + 1);
    }

    /** Accepts the donor {@code M1/M2}, new {@code MOUSE1/MOUSE2} and LMB/RMB labels. */
    private static int parseMouseToken(String value) {
        if (value == null) {
            return -1;
        }
        String token = value.trim().toUpperCase(java.util.Locale.ROOT);
        if (token.isEmpty() || "NONE".equals(token) || "...".equals(token)) {
            return -1;
        }
        if ("LMB".equals(token) || "MOUSE1".equals(token) || "M1".equals(token)) {
            return 0;
        }
        if ("RMB".equals(token) || "MOUSE2".equals(token) || "M2".equals(token)) {
            return 1;
        }
        if (token.startsWith("MOUSE")) {
            return parseButtonIndex(token.substring(5));
        }
        if (token.startsWith("M")) {
            return parseButtonIndex(token.substring(1));
        }
        return -1;
    }

    private static int parseButtonIndex(String value) {
        try {
            int number = Integer.parseInt(value);
            return number >= 1 && number <= 8 ? number - 1 : -1;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static String mouseLabel(int button) {
        return switch (button) {
            case 0 -> "LMB";
            case 1 -> "RMB";
            default -> "M" + (button + 1);
        };
    }

    private static String normalizeLabel(String value) {
        return value == null || value.isBlank() ? "NONE" : value.trim();
    }

    @Override
    public float height() {
        return 13.0f;
    }
}
