package dev.linkenvisuals.util;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Rejects duplicate totem-pop status packets for the same player inside a
 * short window: servers may broadcast two statuses for one pop within the same
 * tick. Time-based instead of world-tick based because day time is not
 * monotonic (sleep, time-change mods) while millisecond time is.
 */
public final class PopDedupe {
    /** Both packets of one pop arrive within one tick; 100 ms is a safe window. */
    public static final long WINDOW_MS = 100L;

    private final Map<UUID, Long> lastAccepted = new HashMap<>();

    /** {@code true} when this pop is new; {@code false} for a duplicate. */
    public boolean accept(UUID playerId, long nowMs) {
        Long last = lastAccepted.get(playerId);
        if (last != null && nowMs - last <= WINDOW_MS) {
            return false;
        }
        lastAccepted.put(playerId, nowMs);
        return true;
    }

    public void remove(UUID playerId) {
        lastAccepted.remove(playerId);
    }

    public void clear() {
        lastAccepted.clear();
    }
}
