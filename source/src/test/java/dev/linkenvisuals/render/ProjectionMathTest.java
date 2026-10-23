package dev.linkenvisuals.render;

import org.joml.Matrix4f;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

final class ProjectionMathTest {
    @Test
    void aspectRatioRewritesOnlyTheHorizontalProjectionTerm() {
        Matrix4f source = new Matrix4f().setPerspective(
            (float) Math.toRadians(70.0), 16.0f / 9.0f, 0.05f, 256.0f);
        Matrix4f adjusted = ProjectionMath.withAspect(source, 4.0f / 3.0f);

        assertEquals(adjusted.m11() / (4.0f / 3.0f), adjusted.m00(), 1.0e-6f);
        assertEquals(source.m11(), adjusted.m11(), 1.0e-6f);
        assertEquals(source.m22(), adjusted.m22(), 1.0e-6f);
        assertEquals(source.m32(), adjusted.m32(), 1.0e-6f);
    }

    @Test
    void invalidRatiosReturnTheOriginalMatrix() {
        Matrix4f source = new Matrix4f();
        assertSame(source, ProjectionMath.withAspect(source, 0.0f));
        assertSame(source, ProjectionMath.withAspect(source, Float.NaN));
        assertNull(ProjectionMath.withAspect(null, 1.75f));
    }
}
