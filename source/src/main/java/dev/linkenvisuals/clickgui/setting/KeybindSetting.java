package dev.linkenvisuals.clickgui.setting;

import dev.linkenvisuals.clickgui.KeyNames;

public final class KeybindSetting extends Setting {
    private String label;
    private int code = -1;
    private boolean listening;

    public KeybindSetting(String name, String label) {
        super(name);
        this.label = label;
        this.code = KeyNames.codeOf(label);
    }

    public String label() {
        return listening ? "..." : label;
    }

    /** Raw stored bind label (never {@code "..."}); used by config persistence. */
    public String raw() {
        return label;
    }

    /** GLFW key code of the current bind, or {@code -1} when unbound. */
    public int code() {
        return code;
    }

    public boolean listening() {
        return listening;
    }

    public void arm() {
        listening = true;
    }

    public void resolve(String keyName) {
        label = keyName;
        code = KeyNames.codeOf(keyName);
        listening = false;
    }

    public void clear() {
        label = "NONE";
        code = -1;
        listening = false;
    }

    @Override
    public float height() {
        return 13.0f;
    }
}
