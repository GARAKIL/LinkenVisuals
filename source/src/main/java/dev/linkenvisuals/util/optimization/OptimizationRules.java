package dev.linkenvisuals.util.optimization;

/**
 * Pure donor Optimization tables (Low/Medium/Ultra). Kept separate from
 * Minecraft so the caps can be unit-tested in both build flavors.
 */
public final class OptimizationRules {
    /** Donor render-distance caps; 0 means "do not cap". */
    private static final int[] RENDER_DISTANCE_CAP = {0, 12, 8};
    /** Donor particle cull radii squared: 48, 32 and 22 blocks. */
    private static final double[] PARTICLE_DISTANCE_SQ = {2304.0, 1024.0, 484.0};

    private OptimizationRules() {
    }

    public static int tier(String mode) {
        if (mode == null) {
            return 1;
        }
        return switch (mode.trim().toLowerCase(java.util.Locale.ROOT)) {
            case "low" -> 0;
            case "ultra" -> 2;
            default -> 1;
        };
    }

    public static int renderDistanceCap(int tier) {
        return RENDER_DISTANCE_CAP[clamp(tier)];
    }

    public static int capRenderDistance(int original, int tier) {
        int cap = renderDistanceCap(tier);
        return cap > 0 ? Math.min(original, cap) : original;
    }

    public static double particleDistanceSq(int tier) {
        return PARTICLE_DISTANCE_SQ[clamp(tier)];
    }

    public static boolean allowParticle(double dx, double dy, double dz, int tier) {
        double distanceSq = dx * dx + dy * dy + dz * dz;
        return distanceSq <= particleDistanceSq(tier);
    }

    public static boolean cullClouds(int tier) {
        return clamp(tier) == 2;
    }

    private static int clamp(int tier) {
        return Math.max(0, Math.min(2, tier));
    }
}
