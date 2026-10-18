package dev.linkenvisuals.media;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MediaWatcherTest {
    @Test
    void parsesFullLine() {
        MediaWatcher.Track t = MediaWatcher.parse("Title\tArtist\tSpotify.exe\t1\t1234\t5678");
        assertEquals("Title", t.title());
        assertEquals("Artist", t.artist());
        assertEquals("Spotify.exe", t.app());
        assertTrue(t.playing());
        assertEquals(1234L, t.position());
        assertEquals(5678L, t.length());
        assertTrue(t.present());
    }

    @Test
    void trimsFieldsAndToleratesExtraParts() {
        MediaWatcher.Track t = MediaWatcher.parse(" T \t A \t app \t 0 \t 5 \t 6 \textra");
        assertEquals("T", t.title());
        assertEquals("A", t.artist());
        assertEquals("app", t.app());
        assertFalse(t.playing());
        assertEquals(5L, t.position());
    }

    @Test
    void blankOrShortLinesAreNone() {
        assertSame(MediaWatcher.Track.NONE, MediaWatcher.parse(null));
        assertSame(MediaWatcher.Track.NONE, MediaWatcher.parse(""));
        assertSame(MediaWatcher.Track.NONE, MediaWatcher.parse("   "));
        assertSame(MediaWatcher.Track.NONE, MediaWatcher.parse("a\tb\tc"));
        assertSame(MediaWatcher.Track.NONE, MediaWatcher.parse("a\tb\tc\td\te"));
    }

    @Test
    void blankTitleFallsBackToArtist() {
        MediaWatcher.Track t = MediaWatcher.parse("\tArtist\tapp\t0\t0\t0");
        assertEquals("Artist", t.title());
        assertEquals("", t.artist());
        assertTrue(t.present());
    }

    @Test
    void malformedNumbersAndFlagBecomeDefaults() {
        MediaWatcher.Track t = MediaWatcher.parse("T\tA\tapp\tjunk\tNaN\tXX");
        assertFalse(t.playing());
        assertEquals(0L, t.position());
        assertEquals(0L, t.length());
    }
}
