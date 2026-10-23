package dev.linkenvisuals.render;

import net.minecraft.util.math.Box;

import java.nio.ByteBuffer;

/**
 * Donor CustomHitBox geometry: six filled faces plus twelve thick edges drawn
 * as crossed quads. Camera-relative vertex data for the world pass.
 */
public final class CustomHitBoxGeometry {
    public static final int FILLED_VERTEX_COUNT = 36;
    public static final int OUTLINE_VERTEX_COUNT = 12 * 2 * 2 * 3;
    public static final int VERTEX_BYTES = 16;

    private CustomHitBoxGeometry() {
    }

    public static int filledVertexCount() {
        return FILLED_VERTEX_COUNT;
    }

    public static int outlineVertexCount() {
        return OUTLINE_VERTEX_COUNT;
    }

    public static void appendFilledBox(ByteBuffer data, Box box,
                                       double camX, double camY, double camZ, int argb) {
        putColoredBox(data, box.offset(-camX, -camY, -camZ), argb);
    }

    public static void appendOutline(ByteBuffer data, Box box,
                                     double camX, double camY, double camZ,
                                     double lineWidth, int argb) {
        Box b = box.offset(-camX, -camY, -camZ);
        double thickness = CustomHitBoxRules.outlineThickness((float) lineWidth);
        double x1 = b.minX, y1 = b.minY, z1 = b.minZ;
        double x2 = b.maxX, y2 = b.maxY, z2 = b.maxZ;

        appendEdge(data, x1, y1, z1, x2, y1, z1, thickness, argb);
        appendEdge(data, x1, y1, z1, x1, y2, z1, thickness, argb);
        appendEdge(data, x1, y1, z1, x1, y1, z2, thickness, argb);
        appendEdge(data, x2, y1, z1, x2, y2, z1, thickness, argb);
        appendEdge(data, x2, y1, z1, x2, y1, z2, thickness, argb);
        appendEdge(data, x1, y2, z1, x2, y2, z1, thickness, argb);
        appendEdge(data, x1, y2, z1, x1, y2, z2, thickness, argb);
        appendEdge(data, x1, y1, z2, x2, y1, z2, thickness, argb);
        appendEdge(data, x1, y1, z2, x1, y2, z2, thickness, argb);
        appendEdge(data, x2, y2, z1, x2, y2, z2, thickness, argb);
        appendEdge(data, x2, y1, z2, x2, y2, z2, thickness, argb);
        appendEdge(data, x1, y2, z2, x2, y2, z2, thickness, argb);
    }

    private static void putColoredBox(ByteBuffer data, Box box, int argb) {
        putColoredFace(data, argb, box.minX, box.minY, box.minZ, box.maxX, box.minY, box.minZ,
            box.maxX, box.maxY, box.minZ, box.minX, box.maxY, box.minZ);
        putColoredFace(data, argb, box.maxX, box.minY, box.maxZ, box.minX, box.minY, box.maxZ,
            box.minX, box.maxY, box.maxZ, box.maxX, box.maxY, box.maxZ);
        putColoredFace(data, argb, box.minX, box.minY, box.maxZ, box.minX, box.minY, box.minZ,
            box.minX, box.maxY, box.minZ, box.minX, box.maxY, box.maxZ);
        putColoredFace(data, argb, box.maxX, box.minY, box.minZ, box.maxX, box.minY, box.maxZ,
            box.maxX, box.maxY, box.maxZ, box.maxX, box.maxY, box.minZ);
        putColoredFace(data, argb, box.minX, box.maxY, box.minZ, box.maxX, box.maxY, box.minZ,
            box.maxX, box.maxY, box.maxZ, box.minX, box.maxY, box.maxZ);
        putColoredFace(data, argb, box.minX, box.minY, box.maxZ, box.maxX, box.minY, box.maxZ,
            box.maxX, box.minY, box.minZ, box.minX, box.minY, box.minZ);
    }

    private static void putColoredFace(ByteBuffer data, int argb,
                                       double x1, double y1, double z1, double x2, double y2, double z2,
                                       double x3, double y3, double z3, double x4, double y4, double z4) {
        putColored(data, argb, x1, y1, z1);
        putColored(data, argb, x2, y2, z2);
        putColored(data, argb, x3, y3, z3);
        putColored(data, argb, x1, y1, z1);
        putColored(data, argb, x3, y3, z3);
        putColored(data, argb, x4, y4, z4);
    }

    private static void appendEdge(ByteBuffer data,
                                   double fx, double fy, double fz,
                                   double tx, double ty, double tz,
                                   double thickness, int argb) {
        double half = thickness * 0.5;
        if (Math.abs(tx - fx) > 1.0E-6) {
            putEdgeQuad(data, fx, fy, fz, tx, ty, tz, 0.0, half, 0.0, argb);
            putEdgeQuad(data, fx, fy, fz, tx, ty, tz, 0.0, 0.0, half, argb);
        } else if (Math.abs(ty - fy) > 1.0E-6) {
            putEdgeQuad(data, fx, fy, fz, tx, ty, tz, half, 0.0, 0.0, argb);
            putEdgeQuad(data, fx, fy, fz, tx, ty, tz, 0.0, 0.0, half, argb);
        } else {
            putEdgeQuad(data, fx, fy, fz, tx, ty, tz, half, 0.0, 0.0, argb);
            putEdgeQuad(data, fx, fy, fz, tx, ty, tz, 0.0, half, 0.0, argb);
        }
    }

    private static void putEdgeQuad(ByteBuffer data,
                                    double fx, double fy, double fz,
                                    double tx, double ty, double tz,
                                    double nx, double ny, double nz, int argb) {
        double ax = fx - nx, ay = fy - ny, az = fz - nz;
        double bx = tx - nx, by = ty - ny, bz = tz - nz;
        double cx2 = tx + nx, cy2 = ty + ny, cz2 = tz + nz;
        double dx = fx + nx, dy = fy + ny, dz = fz + nz;
        putColored(data, argb, ax, ay, az);
        putColored(data, argb, bx, by, bz);
        putColored(data, argb, cx2, cy2, cz2);
        putColored(data, argb, ax, ay, az);
        putColored(data, argb, cx2, cy2, cz2);
        putColored(data, argb, dx, dy, dz);
    }

    private static void putColored(ByteBuffer data, int argb, double x, double y, double z) {
        data.putFloat((float) x).putFloat((float) y).putFloat((float) z);
        data.put((byte) (argb >> 16 & 0xFF)).put((byte) (argb >> 8 & 0xFF))
            .put((byte) (argb & 0xFF)).put((byte) (argb >>> 24));
    }
}
