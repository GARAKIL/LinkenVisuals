package dev.linkenvisuals.render;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Locks the 12-vertex thick-line layout used by ItemRadius walls/boxes. */
class ItemRadiusGeometryTest {
    @Test
    void oneLineEmitsTwoCrossedQuads() {
        ByteBuffer data = ByteBuffer.allocate(ItemRadiusGeometry.LINE_VERTEX_COUNT
            * ItemRadiusGeometry.VERTEX_BYTES);
        ItemRadiusGeometry.appendLine(data, 0.0, 0.0, 0.0, 2.0, 1.0, 0.0, 0.04, 0x80FF0000);
        assertEquals(ItemRadiusGeometry.LINE_VERTEX_COUNT * ItemRadiusGeometry.VERTEX_BYTES,
            data.position());
    }

    @Test
    void everyVertexIsFiniteAndColorIsPacked() {
        ByteBuffer data = ByteBuffer.allocate(ItemRadiusGeometry.LINE_VERTEX_COUNT
            * ItemRadiusGeometry.VERTEX_BYTES);
        ItemRadiusGeometry.appendLine(data, -1.0, 2.0, 3.0, 4.0, -5.0, 6.0, 0.05, 0x7F12AB34);
        data.flip();
        while (data.remaining() >= ItemRadiusGeometry.VERTEX_BYTES) {
            float x = data.getFloat();
            float y = data.getFloat();
            float z = data.getFloat();
            assertTrue(Float.isFinite(x) && Float.isFinite(y) && Float.isFinite(z));
            int r = data.get() & 0xFF;
            int g = data.get() & 0xFF;
            int b = data.get() & 0xFF;
            int a = data.get() & 0xFF;
            assertEquals(0x12, r);
            assertEquals(0xAB, g);
            assertEquals(0x34, b);
            assertEquals(0x7F, a);
        }
    }
}
