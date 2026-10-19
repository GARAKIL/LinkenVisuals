package dev.linkenvisuals.waypoint;

import dev.linkenvisuals.LinkenVisuals;
import dev.linkenvisuals.util.AtomicFiles;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * File persistence for {@link WaypointStore} (Phase 5.7).
 * <p>
 * Config: {@code config/linkenvisuals_waypoints.json}, atomic write via {@link AtomicFiles}
 * (tmp + ATOMIC_MOVE + fallback) with {@code configVersion} inside the file (D14 pattern).
 * The donor manager kept waypoints in memory only; persistence is our addition (D37).
 */
public final class WaypointStorage {
    private static final Path FILE =
        FabricLoader.getInstance().getConfigDir().resolve("linkenvisuals_waypoints.json");

    private static WaypointStore store = new WaypointStore();

    private WaypointStorage() {
    }

    /** The live store shared by the {@code /way} command and the renderer. */
    public static WaypointStore store() {
        return store;
    }

    public static void load() {
        if (!Files.isRegularFile(FILE)) {
            store = new WaypointStore();
            return;
        }
        try {
            store = WaypointStore.parse(Files.readString(FILE, StandardCharsets.UTF_8));
        } catch (IOException | RuntimeException e) {
            LinkenVisuals.LOGGER.warn("Failed to load waypoints, using defaults: {}", e.getMessage());
            store = new WaypointStore();
        }
    }

    public static void save() {
        try (Writer writer = AtomicFiles.openWriter(FILE)) {
            writer.write(store.toJson());
        } catch (IOException e) {
            LinkenVisuals.LOGGER.error("Failed to save waypoints: {}", e.getMessage());
        }
    }
}
