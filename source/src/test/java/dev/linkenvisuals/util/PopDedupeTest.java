package dev.linkenvisuals.util;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PopDedupeTest {
    @Test
    void firstPopIsAccepted() {
        PopDedupe dedupe = new PopDedupe();
        assertTrue(dedupe.accept(UUID.randomUUID(), 1_000L));
    }

    @Test
    void duplicateInsideWindowIsRejectedAndBoundaryIsInclusive() {
        PopDedupe dedupe = new PopDedupe();
        UUID id = UUID.randomUUID();
        assertTrue(dedupe.accept(id, 1_000L));
        assertFalse(dedupe.accept(id, 1_050L));
        assertFalse(dedupe.accept(id, 1_100L));
        assertTrue(dedupe.accept(id, 1_101L));
    }

    @Test
    void playersAreIndependent() {
        PopDedupe dedupe = new PopDedupe();
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        assertTrue(dedupe.accept(first, 1_000L));
        assertTrue(dedupe.accept(second, 1_000L));
        assertFalse(dedupe.accept(first, 1_050L));
    }

    @Test
    void removeForgetsHistory() {
        PopDedupe dedupe = new PopDedupe();
        UUID id = UUID.randomUUID();
        assertTrue(dedupe.accept(id, 1_000L));
        dedupe.remove(id);
        assertTrue(dedupe.accept(id, 1_010L));
    }

    @Test
    void clearForgetsEverything() {
        PopDedupe dedupe = new PopDedupe();
        UUID id = UUID.randomUUID();
        assertTrue(dedupe.accept(id, 1_000L));
        dedupe.clear();
        assertTrue(dedupe.accept(id, 1_010L));
    }
}
