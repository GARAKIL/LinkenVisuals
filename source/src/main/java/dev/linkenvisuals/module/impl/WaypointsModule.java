package dev.linkenvisuals.module.impl;

import dev.linkenvisuals.clickgui.Category;
import dev.linkenvisuals.module.Module;
import dev.linkenvisuals.render.WaypointsRenderer;

import java.util.List;

/**
 * WayPoints (Phase 5.7): waypoint markers in the world.
 * Donor's {@code WayPoints} (852 lines: module + 2 managers + GPS) simplified:
 * storage + world markers. No GPS navigation (follow-up).
 */
public final class WaypointsModule extends Module {
    public WaypointsModule() {
        super("waypoints", "WayPoints", "world waypoint markers",
            Category.PLAYER, List.of(), List.of());
    }

    @Override
    protected void onDisable() {
        WaypointsRenderer.clear();
    }
}
