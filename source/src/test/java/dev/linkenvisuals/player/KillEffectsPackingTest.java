package dev.linkenvisuals.player;

import dev.linkenvisuals.clickgui.ModuleRegistry;
import dev.linkenvisuals.render.KillShaderRenderer;
import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Verifies the exact byte layout written by {@link KillEffects#fillEvents}:
 * 32-byte header (untouched), MAX event vec4s, MAX phase vec4s, zero padding,
 * and the remaining bytes reserved for the inverse matrix.
 */
class KillEffectsPackingTest {
    private static final int MAX = 4;
    private static final int SIZE = KillShaderRenderer.UNIFORM_SIZE;

    @Test
    void packsOneActiveEventIntoStd140Layout() throws Exception {
        float scale = ModuleRegistry.killSize().value();
        float life = ModuleRegistry.killLife().value();
        setFloat("clock", null, 10.0f);
        setFloatArray("born", 0, 10.0f);
        setDoubleArray("atX", 0, 10.0);
        setDoubleArray("atY", 0, 20.0);
        setDoubleArray("atZ", 0, 30.0);
        setFloatArray("tall", 0, 2.0f);
        setFloatArray("wide", 0, 1.0f);

        ByteBuffer data = ByteBuffer.allocate(SIZE).order(ByteOrder.BIG_ENDIAN);
        int count = KillEffects.fillEvents(data, new Vec3d(1.0, 2.0, 3.0), MAX);

        assertEquals(1, count, "exactly one live event expected (age 0 < life " + life + ")");

        // Header (uScreen + uTint) is written by the caller and must stay untouched
        for (int i = 0; i < 8; i++) {
            assertEquals(0.0f, data.getFloat(i * 4), "header byte " + i * 4);
        }

        float height = 2.0f * scale;
        float lifted = height * 0.62f;
        assertEquals(9.0f, data.getFloat(32), 1.0e-5f, "event x = atX - eye.x");
        assertEquals(18.0f + lifted, data.getFloat(36), 1.0e-5f, "event y = atY - eye.y + lifted");
        assertEquals(27.0f, data.getFloat(40), 1.0e-5f, "event z = atZ - eye.z");
        assertEquals(Math.max(1.1f, height * 0.85f), data.getFloat(44), 1.0e-5f, "event radius");

        // Slots 1..MAX-1 are zero-padded
        for (int i = 48; i < 96; i += 4) {
            assertEquals(0.0f, data.getFloat(i), "event padding at " + i);
        }

        // Phase block at offset 32 + MAX * 16 = 96
        assertEquals(0.0f, data.getFloat(96), 1.0e-5f, "phase age");
        assertEquals(0.0f, data.getFloat(100), "phase funnel flag");
        assertEquals((2.0f * 3.4f + 5.5f) * scale, data.getFloat(104), 1.0e-5f, "phase height");
        assertEquals(0.0f, data.getFloat(108), "phase padding float");
        for (int i = 112; i < 160; i += 4) {
            assertEquals(0.0f, data.getFloat(i), "phase padding at " + i);
        }

        // Inverse matrix area (uInverse) is written by the caller
        for (int i = 160; i < SIZE; i += 4) {
            assertEquals(0.0f, data.getFloat(i), "matrix area at " + i);
        }
        assertEquals(SIZE, 160 + 64, "layout must be 32 header + 2 * MAX * 16 + mat4");
    }

    private static void setFloat(String name, Object receiver, float value) throws Exception {
        Field field = KillEffects.class.getDeclaredField(name);
        field.setAccessible(true);
        field.setFloat(receiver, value);
    }

    private static void setFloatArray(String name, int index, float value) throws Exception {
        Field field = KillEffects.class.getDeclaredField(name);
        field.setAccessible(true);
        ((float[]) field.get(null))[index] = value;
    }

    private static void setDoubleArray(String name, int index, double value) throws Exception {
        Field field = KillEffects.class.getDeclaredField(name);
        field.setAccessible(true);
        ((double[]) field.get(null))[index] = value;
    }
}
