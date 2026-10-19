package dev.linkenvisuals.cosmetic.character;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Parser for the donor's {@code characters/rudy.mesh} format (big-endian header {@code 0x52554459}
 * "RUDY", asset author: amogusstrikesback2, CC-BY-4.0). Pure data code without Minecraft
 * dependencies so the format invariants are unit-tested on the plain JVM.
 *
 * <p>Layout: header, part count (1..64), then per part: attachment index (0..5), material/texture
 * index, vertex count (multiple of 3) and the vertex data as a triangle soup with
 * {@code x, y, z, nx, ny, nz, u, v} per vertex. Positions are relative to the attachment part's
 * own pivot (see {@code RudyCharacter.pose}).</p>
 */
public final class RudyMesh {
    /** Big-endian ASCII "RUDY". */
    public static final int MAGIC = 0x52554459;
    public static final int MAX_PARTS = 64;
    /** Part indices map to head, body, right arm, left arm, right leg, left leg. */
    public static final int PART_COUNT = 6;
    public static final int MAX_VERTICES = 100_000;
    public static final int VERTEX_FLOATS = 8;
    public static final int TRIANGLE_FLOATS = VERTEX_FLOATS * 3;

    /** One rigid mesh: attachment part, material/texture index and a triangle soup. */
    public record Part(int part, int material, float[] vertices) {}

    private RudyMesh() {
    }

    public static Part[] parse(InputStream stream, int materialCount) throws IOException {
        DataInputStream input = new DataInputStream(stream);
        if (input.readInt() != MAGIC) {
            throw new IOException("invalid Rudy mesh header");
        }
        int count = input.readInt();
        if (count < 1 || count > MAX_PARTS) {
            throw new IOException("invalid Rudy mesh part count " + count);
        }
        Part[] parts = new Part[count];
        for (int i = 0; i < count; i++) {
            int part = input.readInt();
            int material = input.readInt();
            int vertices = input.readInt();
            if (part < 0 || part >= PART_COUNT) {
                throw new IOException("invalid Rudy mesh part " + part);
            }
            if (material < 0 || material >= materialCount) {
                throw new IOException("invalid Rudy mesh material " + material);
            }
            if (vertices < 3 || vertices > MAX_VERTICES || vertices % 3 != 0) {
                throw new IOException("invalid Rudy mesh vertex count " + vertices);
            }
            float[] data = new float[vertices * VERTEX_FLOATS];
            for (int v = 0; v < data.length; v++) {
                float value = input.readFloat();
                if (!Float.isFinite(value)) {
                    throw new IOException("non-finite Rudy mesh vertex in part " + i);
                }
                data[v] = value;
            }
            parts[i] = new Part(part, material, data);
        }
        return parts;
    }
}
