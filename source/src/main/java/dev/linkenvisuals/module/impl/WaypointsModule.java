package dev.linkenvisuals.module.impl;

import dev.linkenvisuals.clickgui.Category;
import dev.linkenvisuals.module.Module;

import java.util.List;

/**
 * WayPoints (Phase 5.7): waypoint markers in the world, managed by the client
 * command {@code /way} (add/list/remove/clear). Donor's {@code WayPoints}
 * (852 lines: module + 2 managers + GPS + chat event parsing) simplified:
 * storage + world markers. Toggling the module only hides/shows markers; it
 * never erases waypoints (the donor manager is app-level state too).
 */
public final class WaypointsModule extends Module {
    public WaypointsModule() {
        super("waypoints", "WayPoints", "world waypoint markers", Category.PLAYER, List.of(), List.of());
    }
}
