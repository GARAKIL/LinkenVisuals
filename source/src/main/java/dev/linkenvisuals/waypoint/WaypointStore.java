package dev.linkenvisuals.waypoint;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Pure waypoint storage (no Minecraft classes, unit-testable).
 * <p>
 * Donor semantics (from {@code ru.method.linkenvisuals.managers.misc.WaypointManager}
 * and {@code commands.impl.WaypointCommand}): names are unique case-insensitively,
 * and {@code add} refuses a duplicate instead of silently replacing it.
 */
public final class WaypointStore {
    /** Version of the on-disk format written by {@link #toJson()}. */
    public static final int CONFIG_VERSION = 1;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final List<Waypoint> waypoints = new ArrayList<>();

    public boolean isEmpty() {
        return waypoints.isEmpty();
    }

    public int size() {
        return waypoints.size();
    }

    /** Live read-only view for per-frame rendering; no defensive copies. */
    public List<Waypoint> view() {
        return Collections.unmodifiableList(waypoints);
    }

    /** Sorted copy for chat listing (case-insensitive by name, donor order). */
    public List<Waypoint> sorted() {
        List<Waypoint> copy = new ArrayList<>(waypoints);
        copy.sort(Comparator.comparing(Waypoint::name, String.CASE_INSENSITIVE_ORDER));
        return copy;
    }

    public Optional<Waypoint> get(String name) {
        for (Waypoint waypoint : waypoints) {
            if (waypoint.name().equalsIgnoreCase(name)) {
                return Optional.of(waypoint);
            }
        }
        return Optional.empty();
    }

    /**
     * Adds a waypoint; the name is trimmed first.
     *
     * @return {@code false} if a waypoint with this name (case-insensitive) already exists
     * @throws IllegalArgumentException if the trimmed name is blank
     */
    public boolean add(String name, double x, double y, double z) {
        String trimmed = name.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Waypoint name must not be blank");
        }
        if (get(trimmed).isPresent()) {
            return false;
        }
        waypoints.add(new Waypoint(trimmed, x, y, z));
        return true;
    }

    public boolean remove(String name) {
        return waypoints.removeIf(waypoint -> waypoint.name().equalsIgnoreCase(name.trim()));
    }

    public void clear() {
        waypoints.clear();
    }

    /** Serializes to the on-disk JSON: {@code configVersion} + entries. */
    public String toJson() {
        JsonObject root = new JsonObject();
        root.addProperty("configVersion", CONFIG_VERSION);
        JsonArray array = new JsonArray();
        for (Waypoint waypoint : waypoints) {
            JsonObject entry = new JsonObject();
            entry.addProperty("name", waypoint.name());
            entry.addProperty("x", waypoint.x());
            entry.addProperty("y", waypoint.y());
            entry.addProperty("z", waypoint.z());
            array.add(entry);
        }
        root.add("waypoints", array);
        return GSON.toJson(root);
    }

    /**
     * Tolerant parser: structural garbage yields an empty store, unusable entries are skipped
     * (explicit shape checks, no swallowed exceptions). Missing {@code configVersion} is treated
     * as legacy v1; a newer version still loads (forward-compatible read, unknown fields ignored).
     * JSON syntax errors propagate to the caller, which logs and falls back to defaults.
     */
    public static WaypointStore parse(String json) {
        WaypointStore store = new WaypointStore();
        JsonElement root = JsonParser.parseString(json);
        if (!root.isJsonObject()) {
            return store;
        }
        JsonElement entries = root.getAsJsonObject().get("waypoints");
        if (entries == null || !entries.isJsonArray()) {
            return store;
        }
        for (JsonElement element : entries.getAsJsonArray()) {
            if (!element.isJsonObject()) {
                continue;
            }
            JsonObject entry = element.getAsJsonObject();
            String name = string(entry, "name");
            Double x = number(entry, "x");
            Double y = number(entry, "y");
            Double z = number(entry, "z");
            if (name == null || name.trim().isEmpty() || x == null || y == null || z == null) {
                continue;
            }
            store.add(name, x, y, z);
        }
        return store;
    }

    private static String string(JsonObject entry, String key) {
        JsonElement element = entry.get(key);
        if (element == null || !element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString()) {
            return null;
        }
        return element.getAsString();
    }

    private static Double number(JsonObject entry, String key) {
        JsonElement element = entry.get(key);
        if (element == null || !element.isJsonPrimitive() || !element.getAsJsonPrimitive().isNumber()) {
            return null;
        }
        return element.getAsDouble();
    }
}
