package dev.linkenvisuals.pet;

import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;

/**
 * Surface scanner for ground pets (Phase 5.6, ported from the donor's {@code PetGroundSampler}).
 *
 * <p>Scans the block column downwards and returns the Y of the walkable top surface. Unlike a naive
 * scan it refuses to tunnel into walls (a solid block above the fallback ceiling yields {@link Double#NaN}),
 * treats the water surface as ground (ground pets swim on top instead of walking the bottom) and keeps
 * surrounding columns as a fallback so pets do not freeze at cliff corners.</p>
 */
public final class PetGroundSampler {

    /** Water counts as ~8/9 of a full block below its top edge (vanilla source height). */
    private static final double WATER_SURFACE = 0.90;

    private PetGroundSampler() {
    }

    /** Ground below (x, z) with neighbouring columns as a fallback; NaN when there is none in range. */
    public static double sampleGroundY(World world, double x, double z, double fallbackY) {
        if (world == null) {
            return Double.NaN;
        }

        int ix = MathHelper.floor(x);
        int iz = MathHelper.floor(z);

        double maxTopY = fallbackY + 1.25;
        int minY = world.getBottomY();
        int startY = startScanY(world, fallbackY, minY);

        double g0 = column(world, ix, iz, startY, 90, minY, maxTopY);
        if (!Double.isNaN(g0)) {
            return g0;
        }

        double g1 = column(world, ix + 1, iz, startY, 90, minY, maxTopY);
        double g2 = column(world, ix, iz + 1, startY, 90, minY, maxTopY);
        double g3 = column(world, ix + 1, iz + 1, startY, 90, minY, maxTopY);

        double best = Double.NaN;
        if (!Double.isNaN(g1)) {
            best = g1;
        }
        if (!Double.isNaN(g2)) {
            best = Double.isNaN(best) ? g2 : Math.max(best, g2);
        }
        if (!Double.isNaN(g3)) {
            best = Double.isNaN(best) ? g3 : Math.max(best, g3);
        }
        if (!Double.isNaN(best)) {
            return best;
        }

        return column(world, ix, iz, startY, 170, minY, maxTopY);
    }

    /**
     * Height used to position a pet. The donor dropped its bilinear 4-column average here because it
     * sank pets into blocks on stairs; smoothing is done by the render-side position lerp instead.
     */
    public static double sampleGroundYSmooth(World world, double x, double z, double fallbackY) {
        return sampleGroundY(world, x, z, fallbackY);
    }

    private static double column(World world, int x, int z, int startY, int depth, int minY, double maxTopY) {
        int endY = Math.max(minY + 1, startY - Math.max(12, depth));
        BlockPos.Mutable m = new BlockPos.Mutable();
        double waterTop = Double.NaN;

        for (int y = startY; y >= endY; y--) {
            m.set(x, y, z);
            BlockState st = world.getBlockState(m);

            if (Double.isNaN(waterTop)) {
                FluidState fluid = st.getFluidState();
                if (!fluid.isEmpty()) {
                    waterTop = y + WATER_SURFACE;
                }
            }
            if (st.isAir()) {
                continue;
            }

            VoxelShape shape = st.getCollisionShape(world, m);
            if (shape.isEmpty()) {
                continue;
            }

            double top = y + shape.getMax(Direction.Axis.Y);
            double bottom = y + shape.getMin(Direction.Axis.Y);

            // Entirely above the ceiling (canopy/overhang): not ground, keep scanning below.
            if (bottom > maxTopY) {
                continue;
            }
            // Rises past the ceiling: wall/slope, do not scan through it.
            if (top > maxTopY) {
                return Double.NaN;
            }
            // Water above this solid block: swim on the surface.
            if (!Double.isNaN(waterTop) && waterTop <= maxTopY && waterTop >= top) {
                return waterTop;
            }
            return top + 0.015;
        }

        if (!Double.isNaN(waterTop) && waterTop <= maxTopY) {
            return waterTop;
        }
        return Double.NaN;
    }

    private static int startScanY(World world, double fallbackY, int minY) {
        int maxY = minY + world.getHeight();
        return MathHelper.clamp(MathHelper.floor(fallbackY) + 3, minY + 2, maxY - 2);
    }
}
