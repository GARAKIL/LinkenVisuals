package dev.linkenvisuals.mixin;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Guards against mixin classes that compile but are never applied because the
 * config forgot to list them (the D97 NoRender regression).
 */
final class MixinRegistrationTest {
    @Test
    void everyMixinClassIsRegisteredInTheConfig() throws IOException {
        Path mixinDir = Path.of("src/main/java/dev/linkenvisuals/mixin");
        Path config = Path.of("src/main/resources/linkenvisuals.mixins.json");
        Set<String> registered = new HashSet<>();

        JsonObject root = JsonParser.parseString(
            Files.readString(config, StandardCharsets.UTF_8)).getAsJsonObject();
        collect(root, "client", registered);
        collect(root, "server", registered);
        collect(root, "mixins", registered);

        try (Stream<Path> files = Files.list(mixinDir)) {
            files.map(path -> path.getFileName().toString())
                .filter(name -> name.endsWith("Mixin.java"))
                .map(name -> name.substring(0, name.length() - ".java".length()))
                .forEach(name -> assertTrue(registered.contains(name),
                    "mixin is not listed in linkenvisuals.mixins.json: " + name));
        }
    }

    private static void collect(JsonObject root, String key, Set<String> target) {
        JsonElement element = root.get(key);
        if (element == null || !element.isJsonArray()) {
            return;
        }
        JsonArray array = element.getAsJsonArray();
        for (JsonElement entry : array) {
            target.add(entry.getAsString());
        }
    }
}
