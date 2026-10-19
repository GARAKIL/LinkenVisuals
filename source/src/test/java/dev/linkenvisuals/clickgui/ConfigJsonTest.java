package dev.linkenvisuals.clickgui;

import com.google.gson.JsonObject;
import dev.linkenvisuals.clickgui.setting.BooleanSetting;
import dev.linkenvisuals.clickgui.setting.ColorSetting;
import dev.linkenvisuals.clickgui.setting.KeybindSetting;
import dev.linkenvisuals.clickgui.setting.ModeSetting;
import dev.linkenvisuals.clickgui.setting.SliderSetting;
import dev.linkenvisuals.clickgui.setting.StringSetting;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigJsonTest {
    @Test
    void roundTripsEverySettingType() {
        BooleanSetting flag = new BooleanSetting("Flag", false);
        SliderSetting size = new SliderSetting("Size", 0.5f, 3.0f, 0.05f, 1.0f, "x");
        ModeSetting style = new ModeSetting("Style", List.of("Soul", "Portal"), 0);
        ColorSetting tint = new ColorSetting("Tint", 0xFF6B6B);
        ModuleInfo module = new ModuleInfo("TestModule", "desc", Category.VISUAL,
            List.of(flag), List.of(size, style, tint));
        module.toggleSilently(true);
        module.resolveBinding(65);

        JsonObject json = ConfigJson.build(List.of(module));

        module.toggleSilently(false);
        module.clearBinding();
        flag.setValue(true);
        size.setValue(2.0f);
        style.select(1);
        tint.set(0x00FF00);

        ConfigJson.apply(List.of(module), json);

        assertTrue(module.enabled(), "enabled state must survive the round-trip");
        assertEquals(65, module.keyCode(), "keybind must survive the round-trip");
        assertFalse(flag.value());
        assertEquals(1.0f, size.value(), 1.0e-6f);
        assertEquals("Soul", style.current());
        assertEquals(0xFF6B6B, tint.rgb());
    }

    @Test
    void appliesLegacyCyrillicFiltersKey() {
        JsonObject legacy = new JsonObject();
        JsonObject filtersJson = new JsonObject();
        filtersJson.addProperty("enabled", true);
        legacy.add("Фильтры", filtersJson);

        ModuleInfo filters = new ModuleInfo("Filters", "", Category.VISUAL, List.of(), List.of());
        ConfigJson.apply(List.of(filters), legacy);

        assertTrue(filters.enabled(), "legacy 'Фильтры' section must still map to Filters");
    }

    @Test
    void roundTripsKeybindAndStringSettings() {
        KeybindSetting key = new KeybindSetting("Key", "A");
        StringSetting text = new StringSetting("Nick", "default");
        ModuleInfo module = new ModuleInfo("TextModule", "", Category.MISC,
            List.of(), List.of(key, text));

        JsonObject json = ConfigJson.build(List.of(module));

        key.resolve("B");
        text.setValue("changed");
        ConfigJson.apply(List.of(module), json);

        assertEquals("A", key.raw(), "bind labels must survive the round-trip");
        assertEquals(65, key.code(), "bind codes must resolve from the stored label");
        assertEquals("default", text.value(), "string settings must survive the round-trip");
    }

    @Test
    void keybindIgnoresLegacyNumericValues() {
        KeybindSetting key = new KeybindSetting("Key", "A");
        ModuleInfo module = new ModuleInfo("TextModule", "", Category.MISC,
            List.of(), List.of(key));

        JsonObject json = new JsonObject();
        JsonObject modJson = new JsonObject();
        JsonObject settings = new JsonObject();
        settings.addProperty("Key", 66); // legacy int written by the removed KeySetting
        modJson.add("settings", settings);
        json.add("TextModule", modJson);

        ConfigJson.apply(List.of(module), json);

        assertEquals("A", key.raw(), "numeric legacy values must be ignored, not shown as a label");
    }

    @Test
    void ignoresUnknownModulesAndNull() {
        ModuleInfo module = new ModuleInfo("Known", "", Category.VISUAL, List.of(), List.of());
        JsonObject json = new JsonObject();
        json.add("Unknown", new JsonObject());
        ConfigJson.apply(List.of(module), json);
        ConfigJson.apply(List.of(module), null);
        assertFalse(module.enabled());
    }
}
