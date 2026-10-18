package dev.linkenvisuals.clickgui;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.linkenvisuals.util.AtomicFiles;
import dev.linkenvisuals.util.ProfileNames;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Util;

import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages serialization and deserialization of module states, keybindings,
 * and individual settings across multiple configuration profiles in
 * {@code config/linkenvisuals/configs/<name>.json}.
 * <p>
 * Supports creating, loading, overwriting, and deleting profiles,
 * as well as opening the configs directory.
 */
public final class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve("linkenvisuals_config.json");
    private static final Path CONFIGS_DIR = FabricLoader.getInstance().getConfigDir().resolve("linkenvisuals").resolve("configs");

    /** Schema version stamped into every config file; bump on format changes. */
    public static final int CONFIG_VERSION = 1;

    private static String activeProfile = "default";

    private ConfigManager() {
    }

    public static synchronized String getActiveProfile() {
        return activeProfile;
    }

    public static synchronized List<String> getProfiles() {
        List<String> list = new ArrayList<>();
        try {
            if (Files.isDirectory(CONFIGS_DIR)) {
                try (var stream = Files.list(CONFIGS_DIR)) {
                    stream.filter(p -> p.getFileName().toString().toLowerCase().endsWith(".json"))
                        .map(p -> {
                            String fn = p.getFileName().toString();
                            return fn.substring(0, fn.length() - 5);
                        })
                        .sorted(String.CASE_INSENSITIVE_ORDER)
                        .forEach(list::add);
                }
            }
        } catch (Exception e) {
            dev.linkenvisuals.LinkenVisuals.LOGGER.error("Failed to list config profiles: {}", e.getMessage());
        }
        if (list.isEmpty()) {
            list.add("default");
        }
        return list;
    }

    public static synchronized void reloadProfiles() {
        ModuleRegistry.syncConfigProfiles();
    }

    public static void openFolder() {
        try {
            if (!Files.exists(CONFIGS_DIR)) {
                Files.createDirectories(CONFIGS_DIR);
            }
            Util.getOperatingSystem().open(CONFIGS_DIR.toFile());
        } catch (Exception e) {
            dev.linkenvisuals.LinkenVisuals.LOGGER.error("Failed to open configs folder: {}", e.getMessage());
        }
    }

    public static synchronized boolean createProfile(String name) {
        String clean = sanitizeName(name);
        if (clean.isEmpty()) {
            return false;
        }
        return saveProfile(clean);
    }

    public static synchronized boolean deleteProfile(String name) {
        String clean = sanitizeName(name);
        if (clean.isEmpty()) {
            return false;
        }
        List<String> profiles = getProfiles();
        if (profiles.size() <= 1) {
            return false;
        }
        Path file = profilePath(clean);
        if (file == null) {
            return false;
        }
        try {
            Files.deleteIfExists(file);
            if (activeProfile.equalsIgnoreCase(clean)) {
                List<String> remaining = getProfiles();
                String fallback = remaining.isEmpty() ? "default" : remaining.get(0);
                loadProfile(fallback);
            } else {
                ModuleRegistry.syncConfigProfiles();
            }
            return true;
        } catch (Exception e) {
            dev.linkenvisuals.LinkenVisuals.LOGGER.error("Failed to delete profile {}: {}", clean, e.getMessage());
            return false;
        }
    }

    public static synchronized boolean saveProfile(String name) {
        String clean = sanitizeName(name);
        if (clean.isEmpty()) {
            clean = "default";
        }
        try {
            if (!Files.exists(CONFIGS_DIR)) {
                Files.createDirectories(CONFIGS_DIR);
            }
            JsonObject root = new JsonObject();
            root.addProperty("name", clean);
            root.addProperty("configVersion", CONFIG_VERSION);
            root.add("modules", buildModulesJson());

            Path file = profilePath(clean);
            if (file == null) {
                dev.linkenvisuals.LinkenVisuals.LOGGER.warn("Rejected unsafe profile name: {}", clean);
                return false;
            }
            try (Writer writer = newAtomicWriter(file)) {
                GSON.toJson(root, writer);
            }
            activeProfile = clean;
            saveGlobal();
            ModuleRegistry.syncConfigProfiles();
            return true;
        } catch (Exception e) {
            dev.linkenvisuals.LinkenVisuals.LOGGER.error("Failed to save profile {}: {}", clean, e.getMessage());
            return false;
        }
    }

    public static synchronized boolean loadProfile(String name) {
        String clean = sanitizeName(name);
        if (clean.isEmpty()) {
            return false;
        }
        Path file = profilePath(clean);
        if (file == null || !Files.isRegularFile(file)) {
            return false;
        }
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            JsonElement elem = JsonParser.parseReader(reader);
            if (elem.isJsonObject()) {
                JsonObject obj = elem.getAsJsonObject();
                checkConfigVersion(obj, "profile " + clean);
                JsonObject modules = obj.has("modules") && obj.get("modules").isJsonObject()
                    ? obj.getAsJsonObject("modules") : obj;
                applyModulesJson(modules);
                activeProfile = clean;
                saveGlobal();
                ModuleRegistry.syncConfigProfiles();
                return true;
            }
        } catch (Exception e) {
            dev.linkenvisuals.LinkenVisuals.LOGGER.error("Failed to load profile {}: {}", clean, e.getMessage());
        }
        return false;
    }

    public static synchronized void load() {
        try {
            if (!Files.exists(CONFIGS_DIR)) {
                Files.createDirectories(CONFIGS_DIR);
            }
        } catch (Exception e) {
            dev.linkenvisuals.LinkenVisuals.LOGGER.error("Failed to create configs directory: {}", e.getMessage());
        }

        JsonObject root = null;
        if (Files.isRegularFile(CONFIG_FILE)) {
            try (Reader reader = Files.newBufferedReader(CONFIG_FILE, StandardCharsets.UTF_8)) {
                JsonElement rootElement = JsonParser.parseReader(reader);
                if (rootElement.isJsonObject()) {
                    root = rootElement.getAsJsonObject();
                    checkConfigVersion(root, "main config");
                }
            } catch (Exception e) {
                dev.linkenvisuals.LinkenVisuals.LOGGER.error("Failed to read main config: {}", e.getMessage());
            }
        }

        if (root != null) {
            if (root.has("activeProfile") && root.get("activeProfile").isJsonPrimitive()) {
                activeProfile = sanitizeName(root.get("activeProfile").getAsString());
                if (activeProfile.isEmpty()) {
                    activeProfile = "default";
                }
            }
            if (root.has("menuStyle") && root.get("menuStyle").isJsonPrimitive()) {
                dev.linkenvisuals.ui.style.StyleManager.setStyleSilently(
                    dev.linkenvisuals.ui.style.MenuStyle.fromName(root.get("menuStyle").getAsString()));
            }
            if (root.has("language") && root.get("language").isJsonPrimitive()) {
                dev.linkenvisuals.i18n.I18n.setLanguageOverride(root.get("language").getAsString());
            }
            if (root.has("classicGuiX") && root.get("classicGuiX").isJsonPrimitive()) {
                dev.linkenvisuals.clickgui.classic.ClassicGuiState.savedX = root.get("classicGuiX").getAsFloat();
            }
            if (root.has("classicGuiY") && root.get("classicGuiY").isJsonPrimitive()) {
                dev.linkenvisuals.clickgui.classic.ClassicGuiState.savedY = root.get("classicGuiY").getAsFloat();
            }
        }

        List<String> existingProfiles = getProfiles();
        Path activeProfilePath = profilePath(activeProfile);
        if (activeProfilePath == null) {
            activeProfile = "default";
            activeProfilePath = profilePath(activeProfile);
        }

        // Migrate legacy config if configs folder was empty
        if (activeProfilePath == null || !Files.exists(activeProfilePath)) {
            if (root != null && root.has("modules") && root.get("modules").isJsonObject()) {
                JsonObject defObj = new JsonObject();
                defObj.addProperty("name", "default");
                defObj.addProperty("configVersion", CONFIG_VERSION);
                defObj.add("modules", root.getAsJsonObject("modules"));
                Path defPath = profilePath("default");
                if (defPath != null) {
                    try (Writer writer = newAtomicWriter(defPath)) {
                        GSON.toJson(defObj, writer);
                    } catch (Exception e) {
                        dev.linkenvisuals.LinkenVisuals.LOGGER.error("Failed to migrate legacy config: {}", e.getMessage());
                    }
                }
                activeProfile = "default";
                activeProfilePath = profilePath("default");
            } else if (!existingProfiles.isEmpty()) {
                Path first = profilePath(existingProfiles.get(0));
                if (first != null && Files.exists(first)) {
                    activeProfile = existingProfiles.get(0);
                    activeProfilePath = first;
                } else {
                    activeProfile = "default";
                    saveProfile("default");
                    activeProfilePath = profilePath("default");
                }
            } else {
                activeProfile = "default";
                saveProfile("default");
                activeProfilePath = profilePath("default");
            }
        }

        if (activeProfilePath != null && Files.isRegularFile(activeProfilePath)) {
            try (Reader reader = Files.newBufferedReader(activeProfilePath, StandardCharsets.UTF_8)) {
                JsonElement profElem = JsonParser.parseReader(reader);
                if (profElem.isJsonObject()) {
                    JsonObject profObj = profElem.getAsJsonObject();
                    checkConfigVersion(profObj, "profile " + activeProfile);
                    JsonObject modulesObj = profObj.has("modules") && profObj.get("modules").isJsonObject()
                        ? profObj.getAsJsonObject("modules") : profObj;
                    applyModulesJson(modulesObj);
                }
            } catch (Exception e) {
                dev.linkenvisuals.LinkenVisuals.LOGGER.error("Failed to load profile {}: {}", activeProfile, e.getMessage());
            }
        }

        ModuleRegistry.syncConfigProfiles();
    }

    public static synchronized void save() {
        saveProfile(activeProfile);
    }

    private static void saveGlobal() {
        try {
            JsonObject root = new JsonObject();
            root.addProperty("configVersion", CONFIG_VERSION);
            root.addProperty("activeProfile", activeProfile);
            root.addProperty("menuStyle", dev.linkenvisuals.ui.style.StyleManager.getStyle().name());
            root.addProperty("language", dev.linkenvisuals.i18n.I18n.getLanguageOverride());
            if (dev.linkenvisuals.clickgui.classic.ClassicGuiState.savedX >= 0.0F) {
                root.addProperty("classicGuiX", dev.linkenvisuals.clickgui.classic.ClassicGuiState.savedX);
            }
            if (dev.linkenvisuals.clickgui.classic.ClassicGuiState.savedY >= 0.0F) {
                root.addProperty("classicGuiY", dev.linkenvisuals.clickgui.classic.ClassicGuiState.savedY);
            }

            if (CONFIG_FILE.getParent() != null) {
                Files.createDirectories(CONFIG_FILE.getParent());
            }
            try (Writer writer = newAtomicWriter(CONFIG_FILE)) {
                GSON.toJson(root, writer);
            }
        } catch (Exception e) {
            dev.linkenvisuals.LinkenVisuals.LOGGER.error("Failed to save global configuration: {}", e.getMessage());
        }
    }

    private static JsonObject buildModulesJson() {
        return ConfigJson.build(ModuleRegistry.all());
    }

    private static void applyModulesJson(JsonObject modulesObj) {
        ConfigJson.apply(ModuleRegistry.all(), modulesObj);
    }

    /** Warns when a file was written by a newer build than this one (unknown fields are ignored). */
    private static void checkConfigVersion(JsonObject root, String source) {
        if (!root.has("configVersion") || !root.get("configVersion").isJsonPrimitive()
            || !root.getAsJsonPrimitive("configVersion").isNumber()) {
            return;
        }
        int fileVersion = root.get("configVersion").getAsInt();
        if (fileVersion > CONFIG_VERSION) {
            dev.linkenvisuals.LinkenVisuals.LOGGER.warn(
                "{} was written by a newer build (configVersion {} > {}); unknown settings are ignored",
                source, fileVersion, CONFIG_VERSION);
        }
    }

    public static String sanitizeName(String name) {
        return ProfileNames.sanitize(name);
    }

    /**
     * Resolves a profile name to a path strictly inside {@link #CONFIGS_DIR}.
     * Returns {@code null} if the resolved path would escape the configs dir (CWE-22).
     */
    private static Path profilePath(String cleanName) {
        if (cleanName == null || cleanName.isEmpty()) {
            return null;
        }
        Path base = CONFIGS_DIR.toAbsolutePath().normalize();
        Path resolved = base.resolve(cleanName + ".json").normalize();
        if (!resolved.startsWith(base)) {
            return null;
        }
        return resolved;
    }

    /**
     * Returns a Writer that commits atomically: JSON is first written to
     * {@code target.json.tmp} in the same directory, then moved over the target with
     * ATOMIC_MOVE (fallback to plain replace when the FS does not support it).
     * This prevents 0-byte corrupt configs on game crash / power loss.
     */
    private static Writer newAtomicWriter(Path target) throws java.io.IOException {
        return AtomicFiles.openWriter(target);
    }
}
