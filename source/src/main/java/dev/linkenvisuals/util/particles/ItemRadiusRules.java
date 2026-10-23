package dev.linkenvisuals.util.particles;

import net.minecraft.util.math.MathHelper;

/** Pure donor ItemRadius splash-proximity rule. */
public final class ItemRadiusRules {
    private ItemRadiusRules() {
    }

    /** Donor: 100% at the impact centre, 0% at the radius edge. */
    public static int proximityPercent(double distance, double radius) {
        if (!Double.isFinite(distance) || !Double.isFinite(radius) || radius <= 0.0) {
            return 0;
        }
        return (int) Math.round(MathHelper.clamp(1.0 - distance / radius, 0.0, 1.0) * 100.0);
    }
}
