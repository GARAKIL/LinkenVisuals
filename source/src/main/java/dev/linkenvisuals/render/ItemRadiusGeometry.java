package dev.linkenvisuals.render;

import java.nio.ByteBuffer;

/**
 * Thick world-space lines for ItemRadius. Twelve vertices per line (two
 * crossed quads), same triangle-list layout as CustomHitBox outlines.
 */
public final class ItemRadiusGeometry {
    public static final int LINE_VERTEX_COUNT = 12;
    public static final int VERTEX_BYTES = 16;

    private ItemRadiusGeometry() {
    }

    public static void appendLine(ByteBuffer data,
                                  double x1, double y1, double z1,
                                  double x2, double y2, double z2,
                                  double thickness, int argb) {
        double half = thickness * 0.5;
        if (Math.abs(x2 - x1) > 1.0E-6) {
            putEdgeQuad(data, x1, y1, z1, x2, y2, z2, 0.0, half, 0.0, argb);
            putEdgeQuad(data, x1, y1, z1, x2, y2, z2, 0.0, 0.0, half, argb);
        } else if (Math.abs(y2 - y1) > 1.0E-6) {
            putEdgeQuad(data, x1, y1, z1, x2, y2, z2, half, 0.0, 0.0, argb);
            putEdgeQuad(data, x1, y1, z1, x2, y2, z2, 0.0, 0.0, half, argb);
        } else {
            putEdgeQuad(data, x1, y1, z1, x2, y2, z2, half, 0.0, 0.0, argb);
            putEdgeQuad(data, x1, y1, z1, x2, y2, z2, 0.0, half, 0.0, argb);
        }
    }

    private static void putEdgeQuad(ByteBuffer data,
                                    double fx, double fy, double fz,
                                    double tx, double ty, double tz,
                                    double nx, double ny, double nz, int argb) {
        double ax = fx - nx, ay = fy - ny, az = fz - nz;
        double bx = tx - nx, by = ty - ny, bz = tz - nz;
        double cx = tx + nx, cy = ty + ny, cz = tz + nz;
        double dx = fx + nx, dy = fy + ny, dz = fz + nz;
        put(data, argb, ax, ay, az);
        put(data, argb, bx, by, bz);
        put(data, argb, cx, cy, cz);
        put(data, argb, ax, ay, az);
        put(data, argb, cx, cy, cz);
        put(data, argb, dx, dy, dz);
    }

    private static void put(ByteBuffer data, int argb,
                            double x, double y, double z) {
        data.putFloat((float) x).putFloat((float) y).putFloat((float) z);
        data.put((byte) (argb >> 16 & 0xFF)).put((byte) (argb >> 8 & 0xFF))
            .put((byte) (argb & 0xFF)).put((byte) (argb >>> 24));
    }
}
