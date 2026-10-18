package dev.linkenvisuals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LegacyConfigMigrationTest {
    @Test
    void copiesLegacyFilesAndDirectories(@TempDir Path root) throws IOException {
        Path config = root.resolve("config");
        Path game = root.resolve("game");
        Files.createDirectories(config.resolve("strangevisuals").resolve("configs"));
        Files.createDirectories(game.resolve("strangevisuals"));
        Files.writeString(config.resolve("strangevisuals_config.json"), "{\"x\":1}");
        Files.writeString(config.resolve("strangevisuals_hands.json"), "{\"h\":1}");
        Files.writeString(config.resolve("strangevisuals").resolve("configs").resolve("p1.json"), "{}");
        Files.writeString(game.resolve("strangevisuals").resolve("bg.png"), "PNG");

        LegacyConfigMigration.Result result = LegacyConfigMigration.run(config, game);

        assertEquals(4, result.copied(), "two files + two directory trees must be migrated");
        assertTrue(result.clean(), () -> "unexpected failures: " + result.failures());
        assertEquals("{\"x\":1}", Files.readString(config.resolve("linkenvisuals_config.json")));
        assertEquals("{\"h\":1}", Files.readString(config.resolve("linkenvisuals_hands.json")));
        assertEquals("{}", Files.readString(
            config.resolve("linkenvisuals").resolve("configs").resolve("p1.json")));
        assertEquals("PNG", Files.readString(game.resolve("linkenvisuals").resolve("bg.png")));
        // Legacy originals are kept as a fallback
        assertTrue(Files.exists(config.resolve("strangevisuals_config.json")));
    }

    @Test
    void secondRunIsIdempotentAndNeverOverwrites(@TempDir Path root) throws IOException {
        Path config = root.resolve("config");
        Path game = root.resolve("game");
        Files.createDirectories(config);
        Files.createDirectories(game);
        Files.writeString(config.resolve("strangevisuals_config.json"), "legacy");

        LegacyConfigMigration.run(config, game);
        assertEquals(0, LegacyConfigMigration.run(config, game).copied(), "nothing left to copy");

        Files.writeString(config.resolve("linkenvisuals_config.json"), "user data");
        Files.writeString(config.resolve("strangevisuals_hands.json"), "legacy hands");
        LegacyConfigMigration.run(config, game);
        assertEquals("user data", Files.readString(config.resolve("linkenvisuals_config.json")),
            "existing current files must never be overwritten");
        assertFalse(Files.exists(config.resolve("strangevisuals_config.json.tmp")));
    }

    @Test
    void freshInstallDoesNothing(@TempDir Path root) {
        LegacyConfigMigration.Result result = LegacyConfigMigration.run(
            root.resolve("config"), root.resolve("game"));
        assertEquals(0, result.copied());
        assertTrue(result.clean());
        assertFalse(Files.exists(root.resolve("config").resolve("linkenvisuals_config.json")));
    }
}
