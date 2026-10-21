package dev.linkenvisuals.audio;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Guards the ported donor sound map: every event must stay registered, every
 * referenced OGG must ship in the jar, and the generic UI click must not fall
 * back to a vanilla event.
 */
class SoundAssetsTest {
    private static final Set<String> EXPECTED_EVENTS = Set.of(
        "ui.open", "ui.back", "ui.toggle_on", "ui.toggle_off", "ui.click",
        "enable", "disable", "critical", "clickgui_open", "bell", "crime", "nya", "uwu",
        "hellodragons", "krithealth_lechisy", "molodec_ubil", "unichtozjen", "zapen",
        "vcheli", "respawn", "next_raz_povezyo", "snes_totema", "snesli_totem",
        "multikill", "neostonovim", "dabkill", "dota_double_kill", "dota_triple_kill",
        "dota_mega_kill", "dota_monster_kill", "dota_rampage", "mainmenu/track",
        "cosmetic/itachi", "cosmetic/naruto_shadow_clones", "pet/ult");

    @Test
    void everyEventIsRegisteredAndEveryFileExists() throws Exception {
        JsonObject root;
        try (InputStream in = SoundAssetsTest.class.getResourceAsStream(
            "/assets/linkenvisuals/sounds.json")) {
            assertNotNull(in, "sounds.json must be on the runtime classpath");
            root = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8))
                .getAsJsonObject();
        }

        Set<String> actual = new HashSet<>(root.keySet());
        assertEquals(EXPECTED_EVENTS, actual, "donor sound event set changed unexpectedly");

        for (String event : actual) {
            JsonArray sounds = root.getAsJsonObject(event).getAsJsonArray("sounds");
            assertTrue(sounds != null && !sounds.isEmpty(), event + " has no sound entries");
            for (JsonElement element : sounds) {
                String name = element.isJsonObject()
                    ? element.getAsJsonObject().get("name").getAsString()
                    : element.getAsString();
                assertTrue(name.startsWith("linkenvisuals:"),
                    event + " must use a mod sound, found " + name);
                String path = name.substring("linkenvisuals:".length());
                try (InputStream ogg = SoundAssetsTest.class.getResourceAsStream(
                    "/assets/linkenvisuals/sounds/" + path + ".ogg")) {
                    assertNotNull(ogg, event + " references missing asset " + path + ".ogg");
                }
            }
        }
    }

    @Test
    void genericUiClickUsesTheDonorEnableCue() {
        try (InputStream in = SoundAssetsTest.class.getResourceAsStream(
            "/assets/linkenvisuals/sounds.json")) {
            assertNotNull(in);
            JsonObject root = JsonParser.parseReader(
                new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
            String click = root.getAsJsonObject("ui.click")
                .getAsJsonArray("sounds").get(0).getAsJsonObject()
                .get("name").getAsString();
            assertEquals("linkenvisuals:enable", click);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }
}
