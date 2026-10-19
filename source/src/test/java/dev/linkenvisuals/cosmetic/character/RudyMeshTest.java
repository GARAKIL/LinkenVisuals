package dev.linkenvisuals.cosmetic.character;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RudyMeshTest {
    @Test
    void parsesTriangleSoups() throws IOException {
        RudyMesh.Part[] parts = RudyMesh.parse(stream(out -> {
            out.writeInt(RudyMesh.MAGIC);
            out.writeInt(2);
            writeMesh(out, 0, 0, triangle(1f, 2f, 3f));
            writeMesh(out, 5, 1, triangle(-1f, -2f, -3f));
        }), 2);

        assertEquals(2, parts.length);
        assertEquals(0, parts[0].part());
        assertEquals(0, parts[0].material());
        assertEquals(24, parts[0].vertices().length);
        assertEquals(1f, parts[0].vertices()[0]);
        assertEquals(5, parts[1].part());
        assertEquals(1, parts[1].material());
        assertEquals(-3f, parts[1].vertices()[2]);
    }

    @Test
    void rejectsWrongHeader() {
        assertThrows(IOException.class, () -> RudyMesh.parse(stream(out -> out.writeInt(0)), 2));
    }

    @Test
    void rejectsPartCountOutOfRange() {
        assertThrows(IOException.class, () -> RudyMesh.parse(stream(out -> {
            out.writeInt(RudyMesh.MAGIC);
            out.writeInt(0);
        }), 2));
    }

    @Test
    void rejectsUnknownPartIndex() {
        assertThrows(IOException.class, () -> RudyMesh.parse(stream(out -> {
            out.writeInt(RudyMesh.MAGIC);
            out.writeInt(1);
            writeMesh(out, 6, 0, triangle(0f, 0f, 0f));
        }), 2));
    }

    @Test
    void rejectsUnknownMaterial() {
        assertThrows(IOException.class, () -> RudyMesh.parse(stream(out -> {
            out.writeInt(RudyMesh.MAGIC);
            out.writeInt(1);
            writeMesh(out, 0, 2, triangle(0f, 0f, 0f));
        }), 2));
    }

    @Test
    void rejectsVertexCountNotDivisibleByThree() {
        assertThrows(IOException.class, () -> RudyMesh.parse(stream(out -> {
            out.writeInt(RudyMesh.MAGIC);
            out.writeInt(1);
            writeMesh(out, 0, 0, new float[RudyMesh.VERTEX_FLOATS * 4]);
        }), 2));
    }

    @Test
    void rejectsNonFiniteVertex() {
        assertThrows(IOException.class, () -> RudyMesh.parse(stream(out -> {
            out.writeInt(RudyMesh.MAGIC);
            out.writeInt(1);
            float[] data = triangle(0f, 0f, 0f);
            data[3] = Float.NaN;
            writeMesh(out, 0, 0, data);
        }), 2));
    }

    @Test
    void rejectsTruncatedStream() {
        assertThrows(IOException.class, () -> RudyMesh.parse(stream(out -> {
            out.writeInt(RudyMesh.MAGIC);
            out.writeInt(1);
            out.writeInt(0);
        }), 2));
    }

    private static void writeMesh(DataOutputStream out, int part, int material, float[] data) throws IOException {
        out.writeInt(part);
        out.writeInt(material);
        out.writeInt(data.length / RudyMesh.VERTEX_FLOATS);
        for (float value : data) {
            out.writeFloat(value);
        }
    }

    private static float[] triangle(float x, float y, float z) {
        float[] data = new float[RudyMesh.VERTEX_FLOATS * 3];
        for (int i = 0; i < 3; i++) {
            data[i * RudyMesh.VERTEX_FLOATS] = x + i;
            data[i * RudyMesh.VERTEX_FLOATS + 1] = y + i;
            data[i * RudyMesh.VERTEX_FLOATS + 2] = z + i;
            data[i * RudyMesh.VERTEX_FLOATS + 3] = 0f;
            data[i * RudyMesh.VERTEX_FLOATS + 4] = 1f;
            data[i * RudyMesh.VERTEX_FLOATS + 5] = 0f;
            data[i * RudyMesh.VERTEX_FLOATS + 6] = 0.5f;
            data[i * RudyMesh.VERTEX_FLOATS + 7] = 0.25f;
        }
        return data;
    }

    private static ByteArrayInputStream stream(IoWriter writer) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bytes);
        try {
            writer.write(out);
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
        return new ByteArrayInputStream(bytes.toByteArray());
    }

    @FunctionalInterface
    private interface IoWriter {
        void write(DataOutputStream out) throws IOException;
    }
}
