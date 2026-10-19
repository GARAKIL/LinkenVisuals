package dev.linkenvisuals.cosmetic;

import org.joml.Vector3f;

/**
 * Wave Cape (Phase 5.6): cloth-simulated cape using {@link WaveCapeSimulation}
 * (verlet rope physics, ported 1:1 from the donor's {@code CapeSimulation}).
 *
 * <p>Unlike the donor (which replaces the vanilla cape via 4 mixins), this is
 * a procedural cosmetic in our system: the simulation is ticked from
 * {@link #render} and drawn as a quad strip attached to the player's back.
 * No mixins, no render-state surgery.
 */
public final class WaveCape {
    private static final WaveCapeSimulation SIMULATION = new WaveCapeSimulation();
    private static final int SEGMENTS = WaveCapeSimulation.SEGMENTS;

    /** Cape width in blocks (vanilla: 10 px). */
    private static final float HALF_W = 5f / 16f;
    /** Segment height in blocks (1 px each). */
    private static final float SEG_H = 1f / 16f;

    private static final Vector3f a = new Vector3f();
    private static final Vector3f b = new Vector3f();
    private static final Vector3f c = new Vector3f();
    private static final Vector3f d = new Vector3f();
    private static final Vector3f n = new Vector3f(0.0f, 0.0f, 1.0f);

    private WaveCape() {
    }

    public static void render(CosmeticContext ctx) {
        if (ctx.player == null) {
            return;
        }
        if (!SIMULATION.isBuilt()) {
            SIMULATION.build(ctx.player);
        }
        // Tick once per render; the simulation is stable at render rate.
        SIMULATION.tick(ctx.player);

        float tickDelta = 1.0f; // render-thread; simulation already stepped
        float s = ctx.size;
        float halfW = HALF_W * s;

        // Cape root: behind the player's back, at shoulder height.
        // The simulation works in "cape space" (x = back offset, y = height*6).
        // We map it to world: each segment hangs down and back.
        float baseY = ctx.height + 0.45f * s;

        int deep = Material.deepen(ctx.color, 0.4f);
        int tip = Material.shift(ctx.color, 12.0f);

        for (int i = 0; i < SEGMENTS - 1; i++) {
            float x0 = SIMULATION.lerpX(i, tickDelta) / 6f * s;
            float y0 = SIMULATION.lerpY(i, tickDelta) / 6f * s;
            float x1 = SIMULATION.lerpX(i + 1, tickDelta) / 6f * s;
            float y1 = SIMULATION.lerpY(i + 1, tickDelta) / 6f * s;

            // Segment quad: width across X, positioned by simulation.
            // x is "back offset" (negative = behind), y is height.
            a.set(-halfW, baseY + y0, x0 - 0.12f * s);
            b.set(halfW, baseY + y0, x0 - 0.12f * s);
            c.set(halfW, baseY + y1, x1 - 0.12f * s);
            d.set(-halfW, baseY + y1, x1 - 0.12f * s);

            float t0 = i / (float) SEGMENTS;
            float t1 = (i + 1) / (float) SEGMENTS;
            int colorTop = Material.argb(Material.mix(ctx.color, deep, t0 * 0.5f), 0.95f);
            int colorBottom = Material.argb(Material.mix(tip, deep, t1 * 0.3f), 0.95f);
            Material.bodyQuad(ctx, a, b, c, d, colorTop, colorTop, colorBottom, colorBottom);
        }
    }
}
