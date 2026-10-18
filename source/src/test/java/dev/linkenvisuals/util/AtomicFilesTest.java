package dev.linkenvisuals.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class AtomicFilesTest {
    @Test
    void writesAndCleansUpTempFile(@TempDir Path dir) throws IOException {
        Path target = dir.resolve("nested").resolve("config.json");
        AtomicFiles.writeString(target, "{\"a\":1}");

        assertEquals("{\"a\":1}", Files.readString(target, StandardCharsets.UTF_8));
        assertFalse(Files.exists(dir.resolve("nested").resolve("config.json.tmp")),
            "temporary file must be moved onto the target");
    }

    @Test
    void overwritesExistingFileAtomically(@TempDir Path dir) throws IOException {
        Path target = dir.resolve("config.json");
        Files.writeString(target, "old");
        AtomicFiles.writeString(target, "new");
        assertEquals("new", Files.readString(target, StandardCharsets.UTF_8));
        assertFalse(Files.exists(dir.resolve("config.json.tmp")));
    }

    @Test
    void writerCommitsOnlyOnClose(@TempDir Path dir) throws IOException {
        Path target = dir.resolve("config.json");
        java.io.Writer writer = AtomicFiles.openWriter(target);
        writer.write("partial");
        assertFalse(Files.exists(target), "target must not exist before close");
        writer.close();
        assertEquals("partial", Files.readString(target, StandardCharsets.UTF_8));
        writer.close();
        assertEquals("partial", Files.readString(target, StandardCharsets.UTF_8),
            "double close must be harmless");
    }
}
