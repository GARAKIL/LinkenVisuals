package dev.linkenvisuals.cosmetic;

import org.joml.Vector3f;

/**
 * Nimb (Phase 5.6): 3D torus halo above the head. Donor's {@code Nimb} (436
 * lines, TorusCache + per-node lighting + UserSyncManager) simplified to our
 * procedural system: torus geometry + {@link Material} lighting (which already
 * does lambert/facing). No network sync (forbidden).
 */
public final class Nimb {
    private static final int MAJOR_SEG = 48;
    private static final int MINOR_SEG = 12;
    private static final float MAJOR_R = 0.36f;
    private static final float TUBE_R = 0.06f;
    private static final float SPIN_SPEED = 0.5f;

    private static final Vector3f a = new Vector3f();
    private static final Vector3f b = new Vector3f();
    private static final Vector3f c = new Vector3f();
    private static final Vector3f d = new Vector3f();

    private Nimb() {
    }

    public static void render(CosmeticContext ctx) {
        float s = ctx.size;
        float majorR = MAJOR_R * s;
        float tubeR = TUBE_R * s;
        float y = ctx.height + 0.42f * s;
        float spin = ctx.time * SPIN_SPEED;

        int base = ctx.color;
        int deep = Material.deepen(base, 0.3f);

        for (int i = 0; i < MAJOR_SEG; i++) {
            float u0 = spin + (float) (Math.PI * 2.0) * i / MAJOR_SEG;
            float u1 = spin + (float) (Math.PI * 2.0) * (i + 1) / MAJOR_SEG;
            float cu0 = (float) Math.cos(u0), su0 = (float) Math.sin(u0);
            float cu1 = (float) Math.cos(u1), su1 = (float) Math.sin(u1);

            for (int j = 0; j < MINOR_SEG; j++) {
                float v0 = (float) (Math.PI * 2.0) * j / MINOR_SEG;
                float v1 = (float) (Math.PI * 2.0) * (j + 1) / MINOR_SEG;
                float cv0 = (float) Math.cos(v0), sv0 = (float) Math.sin(v0);
                float cv1 = (float) Math.cos(v1), sv1 = (float) Math.sin(v1);

                // Torus point: center circle (majorR) + tube circle (tubeR).
                a.set((majorR + tubeR * cv0) * cu0, y + tubeR * sv0, (majorR + tubeR * cv0) * su0);
                b.set((majorR + tubeR * cv0) * cu1, y + tubeR * sv0, (majorR + tubeR * cv0) * su1);
                c.set((majorR + tubeR * cv1) * cu1, y + tubeR * sv1, (majorR + tubeR * cv1) * su1);
                d.set((majorR + tubeR * cv1) * cu0, y + tubeR * sv1, (majorR + tubeR * cv1) * su0);

                float light = 0.7f + 0.3f * sv0; // simple top-light gradient
                int color = Material.argb(Material.mix(deep, base, light), 0.9f);
                Material.bodyQuad(ctx, a, b, c, d, color, color, color, color);
            }
        }
    }
}
