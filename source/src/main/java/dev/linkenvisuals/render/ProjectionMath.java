package dev.linkenvisuals.render;

import org.joml.Matrix4f;

/**
 * Pure projection helpers used by the donor AspectRatio module. Keeping the
 * matrix math separate makes the behavior testable without a Minecraft client.
 */
public final class ProjectionMath {
    private ProjectionMath() {
    }

    /**
     * Rebuilds the horizontal projection term for a forced aspect ratio while
     * preserving the vertical term (and therefore FOV, near and far planes).
     */
    public static Matrix4f withAspect(Matrix4f source, float aspectRatio) {
        if (source == null || !Float.isFinite(aspectRatio) || aspectRatio <= 0.01f) {
            return source;
        }
        Matrix4f copy = new Matrix4f(source);
        float vertical = copy.m11();
        if (!Float.isFinite(vertical) || Math.abs(vertical) < 1.0e-6f) {
            return copy;
        }
        copy.m00(vertical / aspectRatio);
        return copy;
    }
}
