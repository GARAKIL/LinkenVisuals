package dev.linkenvisuals.cosmetic;

import org.joml.Vector3f;

/**
 * Spheres (Phase 5.6): three orbiting spheres around the player. Donor's
 * {@code Spheres} (670 lines: trails + SphereCache + per-player states)
 * simplified to our procedural system: 3 spheres at different phases,
 * solid shading via {@link Material}. No trails (follow-up).
 */
public final class Spheres {
    private static final int COUNT = 3;
    private static final int LAT_SEG = 12;
    private static final int LON_SEG = 16;
    private static final float ORBIT_R = 0.65f;
    private static final float SPHERE_R = 0.09f;

    private static final float[] PHASE = {0.0f, 2.0943951f, 4.1887903f};
    private static final float[] SPEED = {0.9f, -0.7f, 0.5f};
    private static final float[] TILT = {0.3f, -0.4f, 0.2f};

    private static final Vector3f a = new Vector3f();
    private static final Vector3f b = new Vector3f();
    private static final Vector3f c = new Vector3f();
    private static final Vector3f d = new Vector3f();
    private static final Vector3f center = new Vector3f();

    private Spheres() {
    }

    public static void render(CosmeticContext ctx) {
        float s = ctx.size;
        float orbitR = ORBIT_R * s;
        float sphereR = SPHERE_R * s;
        float y = ctx.height + 0.1f * s;

        for (int k = 0; k < COUNT; k++) {
            float angle = ctx.time * SPEED[k] + PHASE[k];
            float tilt = TILT[k];
            float cx = (float) Math.cos(angle) * orbitR;
            float cz = (float) Math.sin(angle) * orbitR;
            float cy = y + (float) Math.sin(angle * 0.7f) * tilt * orbitR;
            center.set(cx, cy, cz);

            int base = ctx.color;
            int deep = Material.deepen(base, 0.45f);
            int light = Material.lift(base, 0.3f);

            // Sphere via lat/long quads.
            for (int lat = 0; lat < LAT_SEG; lat++) {
                float theta0 = (float) Math.PI * lat / LAT_SEG;
                float theta1 = (float) Math.PI * (lat + 1) / LAT_SEG;
                float st0 = (float) Math.sin(theta0), ct0 = (float) Math.cos(theta0);
                float st1 = (float) Math.sin(theta1), ct1 = (float) Math.cos(theta1);

                for (int lon = 0; lon < LON_SEG; lon++) {
                    float phi0 = (float) (Math.PI * 2.0) * lon / LON_SEG;
                    float phi1 = (float) (Math.PI * 2.0) * (lon + 1) / LON_SEG;
                    float sp0 = (float) Math.sin(phi0), cp0 = (float) Math.cos(phi0);
                    float sp1 = (float) Math.sin(phi1), cp1 = (float) Math.cos(phi1);

                    a.set(center.x + sphereR * st0 * cp0, center.y + sphereR * ct0, center.z + sphereR * st0 * sp0);
                    b.set(center.x + sphereR * st0 * cp1, center.y + sphereR * ct0, center.z + sphereR * st0 * sp1);
                    c.set(center.x + sphereR * st1 * cp1, center.y + sphereR * ct1, center.z + sphereR * st1 * sp1);
                    d.set(center.x + sphereR * st1 * cp0, center.y + sphereR * ct1, center.z + sphereR * st1 * sp0);

                    // Simple vertical gradient: top lighter.
                    float t = lat / (float) LAT_SEG;
                    int color = Material.argb(Material.mix(light, deep, t), 0.95f);
                    Material.bodyQuad(ctx, a, b, c, d, color, color, color, color);
                }
            }
        }
    }
}
