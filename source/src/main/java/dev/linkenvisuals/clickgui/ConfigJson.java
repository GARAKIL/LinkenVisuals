package dev.linkenvisuals.clickgui;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.linkenvisuals.clickgui.setting.BooleanSetting;
import dev.linkenvisuals.clickgui.setting.ColorSetting;
import dev.linkenvisuals.clickgui.setting.KeySetting;
import dev.linkenvisuals.clickgui.setting.KeybindSetting;
import dev.linkenvisuals.clickgui.setting.ModeSetting;
import dev.linkenvisuals.clickgui.setting.Setting;
import dev.linkenvisuals.clickgui.setting.SliderSetting;
import dev.linkenvisuals.clickgui.setting.StringSetting;

import java.util.List;

/**
 * Pure JSON (de)serialization of module states, keybindings and settings.
 * Free of Minecraft/Fabric dependencies so round-trips are unit-testable headless.
 * {@link ConfigManager} supplies the live module list.
 */
public final class ConfigJson {
    private ConfigJson() {
    }

    /** Serializes every module to a {@code modules} object. */
    public static JsonObject build(List<ModuleInfo> modules) {
        JsonObject modulesObj = new JsonObject();
        for (ModuleInfo module : modules) {
            JsonObject modJson = new JsonObject();
            modJson.addProperty("enabled", module.enabled());
            modJson.addProperty("keyCode", module.keyCode());
            modJson.addProperty("keybind", module.keybind());

            JsonObject settingsObj = new JsonObject();
            saveSettings(module.leftSettings(), settingsObj);
            saveSettings(module.rightSettings(), settingsObj);
            modJson.add("settings", settingsObj);

            modulesObj.add(module.name(), modJson);
        }
        return modulesObj;
    }

    /** Applies a previously built object back onto the same module list. */
    public static void apply(List<ModuleInfo> modules, JsonObject modulesObj) {
        if (modulesObj == null) {
            return;
        }
        for (ModuleInfo module : modules) {
            JsonObject modJson = null;
            if (modulesObj.has(module.name())) {
                modJson = modulesObj.getAsJsonObject(module.name());
            } else if ("Filters".equalsIgnoreCase(module.name())) {
                if (modulesObj.has("Фильтры")) {
                    modJson = modulesObj.getAsJsonObject("Фильтры");
                }
            }
            if (modJson == null) {
                continue;
            }
            if (modJson.has("enabled")) {
                module.toggleSilently(modJson.get("enabled").getAsBoolean());
            }
            if (modJson.has("keyCode")) {
                int code = modJson.get("keyCode").getAsInt();
                if (code >= 0) {
                    module.resolveBinding(code);
                } else {
                    module.clearBinding();
                }
            }

            if (modJson.has("settings") && modJson.get("settings").isJsonObject()) {
                JsonObject settingsObj = modJson.getAsJsonObject("settings");
                loadSettings(module.leftSettings(), settingsObj);
                loadSettings(module.rightSettings(), settingsObj);
            }
        }
    }

    private static void loadSettings(Iterable<Setting> settings, JsonObject settingsObj) {
        for (Setting setting : settings) {
            if (!settingsObj.has(setting.name())) {
                continue;
            }
            JsonElement val = settingsObj.get(setting.name());
            try {
                if (setting instanceof BooleanSetting bool) {
                    bool.setValue(val.getAsBoolean());
                } else if (setting instanceof SliderSetting slider) {
                    slider.setValue(val.getAsFloat());
                } else if (setting instanceof ModeSetting mode) {
                    if (val.isJsonPrimitive() && val.getAsJsonPrimitive().isNumber()) {
                        mode.select(val.getAsInt());
                    } else if (val.isJsonPrimitive() && val.getAsJsonPrimitive().isString()) {
                        String opt = val.getAsString();
                        int idx = mode.options().indexOf(opt);
                        if (idx >= 0) {
                            mode.select(idx);
                        }
                    }
                } else if (setting instanceof ColorSetting color) {
                    color.set(val.getAsInt());
                } else if (setting instanceof KeySetting key) {
                    key.setValue(val.getAsInt());
                } else if (setting instanceof KeybindSetting bind) {
                    bind.resolve(val.getAsString());
                } else if (setting instanceof StringSetting text) {
                    text.setValue(val.getAsString());
                }
            } catch (Exception e) {
                dev.linkenvisuals.LinkenVisuals.LOGGER.warn("Failed to apply setting '{}': {}", setting.name(), e.getMessage());
            }
        }
    }

    private static void saveSettings(Iterable<Setting> settings, JsonObject settingsObj) {
        for (Setting setting : settings) {
            if (setting instanceof BooleanSetting bool) {
                settingsObj.addProperty(setting.name(), bool.value());
            } else if (setting instanceof SliderSetting slider) {
                settingsObj.addProperty(setting.name(), slider.value());
            } else if (setting instanceof ModeSetting mode) {
                settingsObj.addProperty(setting.name(), mode.current());
            } else if (setting instanceof ColorSetting color) {
                settingsObj.addProperty(setting.name(), color.rgb());
            } else if (setting instanceof KeySetting key) {
                settingsObj.addProperty(setting.name(), key.value());
            } else if (setting instanceof KeybindSetting bind) {
                settingsObj.addProperty(setting.name(), bind.raw());
            } else if (setting instanceof StringSetting text) {
                settingsObj.addProperty(setting.name(), text.value());
            }
        }
    }
}
