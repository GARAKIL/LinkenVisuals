package dev.linkenvisuals.cosmetic;

import org.joml.Vector3f;

/**
 * ChinaHat (Phase 5.6): procedural conical hat, donor's {@code ChinaHat}
 * adapted to our cosmetic system. Spinning cone with filled base, side
 * surface and rim outline. Classic style only (Layered/Halo variants
 * are follow-ups).
 */
public final class ChinaHat {
    private static final int SEGMENTS = 64;
    private static final float RADIUS = 0.62f;
    private static final float HEIGHT = 0.35f;
    private static final float SPIN_SPEED = 0.9f;

    private static final Vector3f a = new Vector3f();
    private static final Vector3f b = new Vector3f();
    private static final Vector3f c = new Vector3f();
    private static final Vector3f tip = new Vector3f();
    private static final Vector3f center = new Vector3f();
    private static final Vector3f n = new Vector3f(0.0f, 1.0f, 0.0f);

    private ChinaHat() {
    }

    public static void render(CosmeticContext ctx) {
        float s = ctx.size;
        float radius = RADIUS * s;
        float height = HEIGHT * s;
        float y = ctx.height + 0.28f * s;

        int deep = Material.deepen(ctx.color, 0.35f);
        int tipColor = Material.shift(ctx.color, 18.0f);
        float spin = ctx.time * SPIN_SPEED;

        tip.set(0.0f, y + height, 0.0f);
        center.set(0.0f, y, 0.0f);

        for (int i = 0; i < SEGMENTS; i++) {
            float from = spin + (float) (Math.PI * 2.0) * i / SEGMENTS;
            float to = spin + (float) (Math.PI * 2.0) * (i + 1) / SEGMENTS;

            a.set((float) Math.cos(from) * radius, y, (float) Math.sin(from) * radius);
            b.set((float) Math.cos(to) * radius, y, (float) Math.sin(to) * radius);

            // Base: triangle fan (center, a, b) as degenerate quad.
            int baseColor = Material.argb(Material.mix(deep, ctx.color, 0.5f), 0.7f);
            Material.bodyQuad(ctx, center, a, b, center, baseColor, baseColor, baseColor, baseColor);

            // Side: triangle (a, b, tip) as degenerate quad.
            c.set(tip);
            int sideBase = Material.argb(deep, 0.85f);
            int sideTip = Material.argb(tipColor, 1.0f);
            Material.bodyQuad(ctx, a, b, c, c, sideBase, sideBase, sideTip, sideTip);
        }

        // Rim outline.
        int rimRgb = Material.lift(ctx.color, 0.45f);
        for (int i = 0; i < SEGMENTS; i++) {
            float from = spin + (float) (Math.PI * 2.0) * i / SEGMENTS;
            float to = spin + (float) (Math.PI * 2.0) * (i + 1) / SEGMENTS;
            a.set((float) Math.cos(from) * radius, y, (float) Math.sin(from) * radius);
            b.set((float) Math.cos(to) * radius, y, (float) Math.sin(to) * radius);
            Material.rimSmooth(ctx, a, b, b, a, n, n, n, n, rimRgb, 0.9f);
        }
    }
}
