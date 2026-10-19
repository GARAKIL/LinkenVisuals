package dev.linkenvisuals.clickgui.setting;

/**
 * Simple key setting storing a GLFW keycode.
 */
public final class KeySetting extends Setting {
    private int keyCode;

    public KeySetting(String name, int defaultKey) {
        super(name);
        this.keyCode = defaultKey;
    }

    public int value() {
        return keyCode;
    }

    public void setValue(int keyCode) {
        this.keyCode = keyCode;
    }
}
