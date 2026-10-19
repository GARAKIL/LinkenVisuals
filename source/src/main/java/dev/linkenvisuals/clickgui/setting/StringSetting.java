package dev.linkenvisuals.clickgui.setting;

/**
 * Simple string setting.
 */
public final class StringSetting extends Setting {
    private String value;

    public StringSetting(String name, String defaultValue) {
        super(name);
        this.value = defaultValue;
    }

    public String value() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public float height() {
        return 13.0f;
    }
}
