package dev.linkenvisuals.waypoint;

import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WaypointStoreTest {

    @Test
    void addGetAndSortAreCaseInsensitive() {
        WaypointStore store = new WaypointStore();
        assertTrue(store.add("Base", 10, 64, -20));
        assertTrue(store.add("alpha", 1, 2, 3));
        assertEquals(2, store.size());
        assertTrue(store.get("BASE").isPresent());
        List<Waypoint> sorted = store.sorted();
        assertEquals("alpha", sorted.get(0).name());
        assertEquals("Base", sorted.get(1).name());
    }

    @Test
    void addRefusesDuplicateKeepingOriginal() {
        WaypointStore store = new WaypointStore();
        store.add("base", 10, 64, -20);
        assertFalse(store.add("BASE", 99, 99, 99), "дубликат (без учёта регистра) отклоняется, как у донора");
        assertEquals(10.0, store.get("base").orElseThrow().x());
    }

    @Test
    void addTrimsNameAndRejectsBlank() {
        WaypointStore store = new WaypointStore();
        store.add("  base  ", 1, 2, 3);
        assertEquals("base", store.get("base").orElseThrow().name());
        assertThrows(IllegalArgumentException.class, () -> store.add("   ", 1, 2, 3));
    }

    @Test
    void removeAndClearBehave() {
        WaypointStore store = new WaypointStore();
        store.add("a", 0, 0, 0);
        store.add("b", 0, 0, 0);
        assertTrue(store.remove(" A "));
        assertFalse(store.remove("missing"));
        assertEquals(1, store.size());
        store.clear();
        assertTrue(store.isEmpty());
    }

    @Test
    void jsonRoundTripKeepsEntriesAndVersion() {
        WaypointStore store = new WaypointStore();
        store.add("home", 1.5, 64.0, -3.25);
        store.add("mine", -100.0, 12.0, 200.0);
        String json = store.toJson();
        assertEquals(WaypointStore.CONFIG_VERSION,
            JsonParser.parseString(json).getAsJsonObject().get("configVersion").getAsInt());
        WaypointStore parsed = WaypointStore.parse(json);
        assertEquals(2, parsed.size());
        assertEquals(1.5, parsed.get("home").orElseThrow().x());
        assertEquals(-3.25, parsed.get("home").orElseThrow().z());
    }

    @Test
    void parseIsTolerantToStructure() {
        assertTrue(WaypointStore.parse("{}").isEmpty());
        assertTrue(WaypointStore.parse("{\"waypoints\": 5}").isEmpty());
        assertTrue(WaypointStore.parse(
            "{\"waypoints\": [1, {\"name\": \"x\"}, {\"name\": \"y\", \"x\": 1, \"y\": 2}]}").isEmpty(),
            "записи без координат и не-объекты пропускаются");
    }

    @Test
    void parseAcceptsLegacyAndNewerVersions() {
        WaypointStore legacy = WaypointStore.parse(
            "{\"waypoints\": [{\"name\": \"a\", \"x\": 1, \"y\": 2, \"z\": 3}]}");
        assertEquals(1, legacy.size(), "отсутствие configVersion — легаси-формат, читается");
        WaypointStore newer = WaypointStore.parse(
            "{\"configVersion\": 99, \"waypoints\": [{\"name\": \"a\", \"x\": 1, \"y\": 2, \"z\": 3}]}");
        assertEquals(1, newer.size(), "новая версия файла читается forward-compatible");
    }

    @Test
    void parseDeduplicatesRepeatedNames() {
        WaypointStore parsed = WaypointStore.parse(
            "{\"waypoints\": [{\"name\": \"a\", \"x\": 1, \"y\": 2, \"z\": 3},"
                + " {\"name\": \"A\", \"x\": 9, \"y\": 9, \"z\": 9}]}");
        assertEquals(1, parsed.size());
        assertEquals(1.0, parsed.get("a").orElseThrow().x());
    }
}
