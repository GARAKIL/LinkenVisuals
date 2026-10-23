package dev.linkenvisuals.render;

import net.minecraft.util.math.Box;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class CustomHitBoxGeometryTest {
    private static final int ARGB = 0x80112233;

    @Test
    void filledBoxEmitsSixCameraRelativeFaces() {
        Box box = new Box(1.0, 2.0, 3.0, 2.0, 3.0, 4.0);
        ByteBuffer data = ByteBuffer.allocate(
            CustomHitBoxGeometry.filledVertexCount() * CustomHitBoxGeometry.VERTEX_BYTES);
        CustomHitBoxGeometry.appendFilledBox(data, box, 0.5, 1.0, 1.5, ARGB);
        data.flip();

        int vertices = 0;
        while (data.remaining() >= CustomHitBoxGeometry.VERTEX_BYTES) {
            float x = data.getFloat();
            float y = data.getFloat();
            float z = data.getFloat();
            assertEquals(0x11, data.get() & 0xFF);
            assertEquals(0x22, data.get() & 0xFF);
            assertEquals(0x33, data.get() & 0xFF);
            assertEquals(0x80, data.get() & 0xFF);
            assertTrue(x >= 0.5f - 1.0e-5 && x <= 1.5f + 1.0e-5, "x " + x);
            assertTrue(y >= 1.0f - 1.0e-5 && y <= 2.0f + 1.0e-5, "y " + y);
            assertTrue(z >= 1.5f - 1.0e-5 && z <= 2.5f + 1.0e-5, "z " + z);
            vertices++;
        }
        assertEquals(CustomHitBoxGeometry.filledVertexCount(), vertices);
        assertEquals(0, data.remaining());
    }

    @Test
    void outlineIsTwelveThickEdgesAroundTheBox() {
        Box box = new Box(0.0, 0.0, 0.0, 1.0, 1.0, 1.0);
        ByteBuffer data = ByteBuffer.allocate(
            CustomHitBoxGeometry.outlineVertexCount() * CustomHitBoxGeometry.VERTEX_BYTES);
        CustomHitBoxGeometry.appendOutline(data, box, 0.0, 0.0, 0.0, 1.5, ARGB);
        data.flip();

        double half = CustomHitBoxRules.outlineThickness(1.5f) * 0.5;
        int vertices = 0;
        while (data.remaining() >= CustomHitBoxGeometry.VERTEX_BYTES) {
            float x = data.getFloat();
            float y = data.getFloat();
            float z = data.getFloat();
            data.position(data.position() + 4);
            assertTrue(x >= -half - 1.0e-5 && x <= 1.0 + half + 1.0e-5, "x " + x);
            assertTrue(y >= -half - 1.0e-5 && y <= 1.0 + half + 1.0e-5, "y " + y);
            assertTrue(z >= -half - 1.0e-5 && z <= 1.0 + half + 1.0e-5, "z " + z);
            vertices++;
        }
        assertEquals(CustomHitBoxGeometry.outlineVertexCount(), vertices);
        assertEquals(0, data.remaining());
    }

    @Test
    void distanceAndAlphaRulesMatchTheDonor() {
        assertTrue(CustomHitBoxRules.withinRenderDistance(0.0));
        assertTrue(CustomHitBoxRules.withinRenderDistance(128.0 * 128.0));
        assertFalse(CustomHitBoxRules.withinRenderDistance(128.0 * 128.0 + 0.001));
        assertFalse(CustomHitBoxRules.withinRenderDistance(Double.NaN));

        assertEquals(90, CustomHitBoxRules.effectiveFillAlpha(90.0f, 255));
        assertEquals(45, CustomHitBoxRules.effectiveFillAlpha(90.0f, 128));
        assertEquals(0, CustomHitBoxRules.effectiveFillAlpha(-5.0f, 255));
        assertEquals(255, CustomHitBoxRules.effectiveFillAlpha(300.0f, 255));

        assertEquals(0.006, CustomHitBoxRules.outlineThickness(1.0f), 1.0e-9);
        assertEquals(0.0035, CustomHitBoxRules.outlineThickness(0.1f), 1.0e-9);
        assertEquals(0.048, CustomHitBoxRules.outlineThickness(8.0f), 1.0e-9);
    }
}
