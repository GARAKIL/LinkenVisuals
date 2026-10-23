package dev.linkenvisuals.render;

import java.nio.ByteBuffer;

/**
 * Donor HitRange ribbon geometry: a flat ring built from two concentric
 * circles (inner/outer radius) instead of GL lines, whose width differs
 * between drivers. Kept pure so the vertex layout is unit-tested.
 */
public final class HitRangeGeometry {
    /** Donor ring resolution. */
    public static final int SIDES = 72;
    /** Donor hit range in blocks. */
    public static final float HIT_RADIUS = 3.32f;
    /** Keep the ring just above the block surface to avoid z-fighting. */
    public static final float Y_OFFSET = 0.02f;
    /** Vertex stride from {@code VertexFormats.POSITION_COLOR} (3 floats + 4 bytes). */
    public static final int VERTEX_BYTES = 16;

    private static final float TWO_PI = (float) (Math.PI * 2.0);

    private HitRangeGeometry() {
    }

    /** Vertices emitted by {@link #appendBand} for a ring with {@code sides} segments. */
    public static int bandVertexCount(int sides) {
        return Math.max(0, sides) * 6;
    }

    /** Donor rotation step: degrees per rendered frame, wrapped to [0, 360). */
    public static float advanceRotation(float angle, float speed) {
        float next = angle + speed;
        if (!Float.isFinite(next)) {
            return 0.0f;
        }
        next %= 360.0f;
        if (next < 0.0f) {
            next += 360.0f;
        }
        return next;
    }

    /**
     * Appends one triangle-list ring band (outer/inner radius) at
     * camera-relative {@code (cx, cy, cz)}. Vertices are POSITION_COLOR:
     * three floats followed by packed RGBA bytes, matching the flat world
     * pipeline.
     */
    public static void appendBand(ByteBuffer data, float cx, float cy, float cz,
                                  float radius, float halfWidth, double rotationRadians,
                                  int sides, int argb) {
        if (sides < 3) {
            throw new IllegalArgumentException("ring needs at least 3 sides");
        }
        float safeRadius = Math.max(0.01f, radius);
        float safeHalf = Math.max(0.0f, halfWidth);
        float outer = safeRadius + safeHalf;
        float inner = Math.max(0.01f, safeRadius - safeHalf);
        byte red = (byte) (argb >> 16 & 0xFF);
        byte green = (byte) (argb >> 8 & 0xFF);
        byte blue = (byte) (argb & 0xFF);
        byte alpha = (byte) (argb >>> 24);

        for (int i = 0; i < sides; i++) {
            double a0 = rotationRadians + TWO_PI * i / sides;
            double a1 = rotationRadians + TWO_PI * (i + 1) / sides;
            float cos0 = (float) Math.cos(a0);
            float sin0 = (float) Math.sin(a0);
            float cos1 = (float) Math.cos(a1);
            float sin1 = (float) Math.sin(a1);

            float outerX0 = cx + cos0 * outer;
            float outerZ0 = cz + sin0 * outer;
            float innerX0 = cx + cos0 * inner;
            float innerZ0 = cz + sin0 * inner;
            float outerX1 = cx + cos1 * outer;
            float outerZ1 = cz + sin1 * outer;
            float innerX1 = cx + cos1 * inner;
            float innerZ1 = cz + sin1 * inner;

            putVertex(data, outerX0, cy, outerZ0, red, green, blue, alpha);
            putVertex(data, innerX0, cy, innerZ0, red, green, blue, alpha);
            putVertex(data, outerX1, cy, outerZ1, red, green, blue, alpha);

            putVertex(data, innerX0, cy, innerZ0, red, green, blue, alpha);
            putVertex(data, innerX1, cy, innerZ1, red, green, blue, alpha);
            putVertex(data, outerX1, cy, outerZ1, red, green, blue, alpha);
        }
    }

    private static void putVertex(ByteBuffer data, float x, float y, float z,
                                  byte red, byte green, byte blue, byte alpha) {
        data.putFloat(x).putFloat(y).putFloat(z);
        data.put(red).put(green).put(blue).put(alpha);
    }
}
