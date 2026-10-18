package dev.linkenvisuals.util;

import java.io.BufferedWriter;
import java.io.FilterWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;

/**
 * Crash-safe file writes: content goes to {@code <name>.tmp} in the same
 * directory and is then moved over the target with {@code ATOMIC_MOVE}
 * (falling back to a plain replace when the filesystem does not support it).
 * This prevents 0-byte corrupt files on game crash / power loss.
 */
public final class AtomicFiles {
    private AtomicFiles() {
    }

    /**
     * Returns a Writer that commits atomically on {@link Writer#close()}.
     */
    public static Writer openWriter(Path target) throws IOException {
        Path parent = target.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Path tmp = target.resolveSibling(target.getFileName().toString() + ".tmp");
        BufferedWriter buf =
            Files.newBufferedWriter(tmp, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
        return new FilterWriter(buf) {
            private boolean committed;

            @Override
            public void close() throws IOException {
                if (committed) {
                    return;
                }
                committed = true;
                super.close();
                try {
                    Files.move(tmp, target,
                        StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
                } catch (AtomicMoveNotSupportedException fallback) {
                    Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        };
    }

    /** Writes the whole string atomically. */
    public static void writeString(Path target, String content) throws IOException {
        try (Writer writer = openWriter(target)) {
            writer.write(content);
        }
    }
}
