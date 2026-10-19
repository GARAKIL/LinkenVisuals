package dev.linkenvisuals.clickgui;

import com.google.gson.JsonObject;
import dev.linkenvisuals.clickgui.setting.BooleanSetting;
import dev.linkenvisuals.clickgui.setting.ColorSetting;
import dev.linkenvisuals.clickgui.setting.KeySetting;
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
    void roundTripsKeyAndStringSettings() {
        KeySetting key = new KeySetting("Key", 65);
        StringSetting text = new StringSetting("Nick", "default");
        ModuleInfo module = new ModuleInfo("TextModule", "", Category.MISC,
            List.of(), List.of(key, text));

        JsonObject json = ConfigJson.build(List.of(module));

        key.setValue(66);
        text.setValue("changed");
        ConfigJson.apply(List.of(module), json);

        assertEquals(65, key.value(), "key codes must survive the round-trip");
        assertEquals("default", text.value(), "string settings must survive the round-trip");
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
