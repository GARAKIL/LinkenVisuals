package dev.linkenvisuals.render;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class HitRangeGeometryTest {
    private static final int ARGB = 0x80AABBCC;

    @Test
    void vertexCountMatchesTheTriangleRibbon() {
        assertEquals(432, HitRangeGeometry.bandVertexCount(72));
        assertEquals(18, HitRangeGeometry.bandVertexCount(3));
        assertEquals(0, HitRangeGeometry.bandVertexCount(0));
        assertEquals(0, HitRangeGeometry.bandVertexCount(-4));
    }

    @Test
    void bandVerticesStayOnTheInnerAndOuterCircles() {
        int sides = 8;
        float radius = 3.32f;
        float half = 0.09f;
        ByteBuffer data = ByteBuffer.allocate(
            HitRangeGeometry.bandVertexCount(sides) * HitRangeGeometry.VERTEX_BYTES);

        HitRangeGeometry.appendBand(data, 12.0f, 0.02f, -7.0f, radius, half, 0.0,
            sides, ARGB);
        data.flip();

        float outer = radius + half;
        float inner = radius - half;
        boolean sawOuter = false;
        boolean sawInner = false;
        for (int i = 0; i < sides * 6; i++) {
            float x = data.getFloat();
            float y = data.getFloat();
            float z = data.getFloat();
            assertEquals(0xAA, data.get() & 0xFF);
            assertEquals(0xBB, data.get() & 0xFF);
            assertEquals(0xCC, data.get() & 0xFF);
            assertEquals(0x80, data.get() & 0xFF);
            assertEquals(0.02f, y, 1.0e-6f);
            float distance = (float) Math.hypot(x - 12.0f, z + 7.0f);
            boolean onOuter = Math.abs(distance - outer) < 1.0e-4f;
            boolean onInner = Math.abs(distance - inner) < 1.0e-4f;
            assertTrue(onOuter || onInner, "vertex off the band: " + distance);
            sawOuter |= onOuter;
            sawInner |= onInner;
        }
        assertTrue(sawOuter);
        assertTrue(sawInner);
        assertEquals(0, data.remaining());
    }

    @Test
    void everyBandTriangleHasArea() {
        int sides = 12;
        ByteBuffer data = ByteBuffer.allocate(
            HitRangeGeometry.bandVertexCount(sides) * HitRangeGeometry.VERTEX_BYTES);
        HitRangeGeometry.appendBand(data, 0.0f, 0.0f, 0.0f, 3.32f, 0.09f, 0.35,
            sides, 0xFF102030);
        data.flip();

        float[] xs = new float[3];
        float[] zs = new float[3];
        for (int triangle = 0; triangle < sides * 2; triangle++) {
            for (int vertex = 0; vertex < 3; vertex++) {
                xs[vertex] = data.getFloat();
                data.getFloat();
                zs[vertex] = data.getFloat();
                data.position(data.position() + 4);
            }
            float areaTwice = Math.abs((xs[1] - xs[0]) * (zs[2] - zs[0])
                - (xs[2] - xs[0]) * (zs[1] - zs[0]));
            assertTrue(areaTwice > 1.0e-7f, "degenerate ring triangle " + triangle);
        }
        assertEquals(0, data.remaining());
    }

    @Test
    void rotationWrapsIntoZeroTo360() {
        assertEquals(0.4f, HitRangeGeometry.advanceRotation(0.0f, 0.4f), 1.0e-6f);
        assertEquals(0.3f, HitRangeGeometry.advanceRotation(359.9f, 0.4f), 1.0e-4f);
        assertEquals(359.4f, HitRangeGeometry.advanceRotation(-1.0f, 0.4f), 1.0e-4f);
        assertEquals(0.0f, HitRangeGeometry.advanceRotation(Float.NaN, 0.4f), 1.0e-6f);
    }

    @Test
    void oversizedHalfWidthStillProducesFiniteInnerRadius() {
        ByteBuffer data = ByteBuffer.allocate(3 * 6 * HitRangeGeometry.VERTEX_BYTES);
        HitRangeGeometry.appendBand(data, 0.0f, 0.0f, 0.0f, 3.32f, 10.0f, 0.0,
            3, 0xFFFFFFFF);
        data.flip();
        while (data.remaining() >= HitRangeGeometry.VERTEX_BYTES) {
            float x = data.getFloat();
            float y = data.getFloat();
            float z = data.getFloat();
            data.position(data.position() + 4);
            assertTrue(Float.isFinite(x) && Float.isFinite(y) && Float.isFinite(z));
        }
        assertEquals(0, data.remaining());
    }

    @Test
    void aRingNeedsAtLeastThreeSides() {
        ByteBuffer data = ByteBuffer.allocate(64);
        assertThrows(IllegalArgumentException.class,
            () -> HitRangeGeometry.appendBand(data, 0.0f, 0.0f, 0.0f, 1.0f, 0.1f, 0.0,
                2, ARGB));
    }
}
