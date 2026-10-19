package dev.linkenvisuals.module.impl;

import dev.linkenvisuals.clickgui.Category;
import dev.linkenvisuals.module.Module;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * TotemTracker (Phase 5.7): tracks totem pops per player.
 * Donor's {@code TotemTracker} (88 lines) ported (simplified).
 */
public final class TotemTrackerModule extends Module {
    private final Map<UUID, Integer> popCounts = new HashMap<>();

    public TotemTrackerModule() {
        super("totemtracker", "Totem Tracker", "track totem pops",
            Category.PLAYER, List.of(), List.of());
    }

    @Override
    protected void onDisable() {
        popCounts.clear();
    }

    public void recordPop(UUID playerId) {
        popCounts.merge(playerId, 1, Integer::sum);
    }

    public int getPops(UUID playerId) {
        return popCounts.getOrDefault(playerId, 0);
    }

    public void clearPlayer(UUID playerId) {
        popCounts.remove(playerId);
    }
}
