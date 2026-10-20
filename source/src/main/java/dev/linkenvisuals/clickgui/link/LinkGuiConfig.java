package dev.linkenvisuals.clickgui.link;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.linkenvisuals.LinkenVisuals;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Small persisted state for the LINKEN ClickGUI: selected palette and pinned
 * modules. It is intentionally independent from the Beta {@code ConfigManager}
 * so switching shells never rewrites the other style's profile list.
 */
public final class LinkGuiConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir()
        .resolve("linkenvisuals_linkgui.json");
    private static Data data;

    private LinkGuiConfig() {
    }

    private static final class Data {
        String theme = "MIDNIGHT";
        List<String> pins = new ArrayList<>();
    }

    private static synchronized Data data() {
        if (data != null) {
            return data;
        }
        data = new Data();
        try {
            if (Files.exists(FILE)) {
                String json = Files.readString(FILE, StandardCharsets.UTF_8);
                Data loaded = GSON.fromJson(json, Data.class);
                if (loaded != null) {
                    data = loaded;
                    if (data.pins == null) {
                        data.pins = new ArrayList<>();
                    }
                    if (data.theme == null || data.theme.isBlank()) {
                        data.theme = "MIDNIGHT";
                    }
                }
            }
        } catch (Throwable failure) {
            LinkenVisuals.LOGGER.warn("[LinkGui] failed to read {}", FILE, failure);
        }
        return data;
    }

    private static synchronized void save() {
        try {
            Files.createDirectories(FILE.getParent());
            Files.writeString(FILE, GSON.toJson(data()), StandardCharsets.UTF_8);
        } catch (Throwable failure) {
            LinkenVisuals.LOGGER.warn("[LinkGui] failed to write {}", FILE, failure);
        }
    }

    public static String theme() {
        return data().theme;
    }

    public static void setTheme(String name) {
        if (name == null || name.isBlank()) {
            return;
        }
        Data value = data();
        if (!name.equalsIgnoreCase(value.theme)) {
            value.theme = name.toUpperCase(java.util.Locale.ROOT);
            save();
        }
    }

    public static boolean pinned(String moduleName) {
        return moduleName != null && data().pins.contains(moduleName);
    }

    public static void togglePin(String moduleName) {
        if (moduleName == null || moduleName.isBlank()) {
            return;
        }
        Data value = data();
        if (!value.pins.remove(moduleName)) {
            value.pins.add(moduleName);
        }
        save();
    }

    public static List<String> pins() {
        return new ArrayList<>(data().pins);
    }
}
