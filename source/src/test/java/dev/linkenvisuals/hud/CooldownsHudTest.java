package dev.linkenvisuals.hud;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class CooldownsHudTest {
    @Test
    void timeFormatRoundsUpToWholeSeconds() {
        assertEquals("0:00", CooldownTracker.formatTime(0.0f));
        assertEquals("0:01", CooldownTracker.formatTime(1.0f));
        assertEquals("0:01", CooldownTracker.formatTime(20.0f));
        assertEquals("0:02", CooldownTracker.formatTime(21.0f));
        assertEquals("1:00", CooldownTracker.formatTime(1200.0f));
        assertEquals("1:01", CooldownTracker.formatTime(1201.0f));
    }
}
