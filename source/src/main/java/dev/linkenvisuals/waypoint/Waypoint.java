package dev.linkenvisuals.waypoint;

/**
 * A named world position (Phase 5.7).
 * Donor: {@code ru.method.linkenvisuals.utils.waypoint.Waypoint} (name + x/y/z doubles).
 */
public record Waypoint(String name, double x, double y, double z) {
}
