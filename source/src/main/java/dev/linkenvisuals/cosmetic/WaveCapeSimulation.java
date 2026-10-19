package dev.linkenvisuals.cosmetic;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.util.math.MathHelper;

/**
 * Wave cape physics (Phase 5.6): rope simulation, chain of {@link #SEGMENTS}
 * points connected by unit-length sticks. Point 0 is pinned to the player;
 * the rest fall under gravity and are pulled by constraint iterations —
 * the cape behaves like cloth, not a rigid plate.
 *
 * <p>Math ported 1:1 from the donor's {@code CapeSimulation} (itself from
 * WaveCapes): same gravity/iterations/bend limit/height multiplier, same pass
 * order. Scalar math (no {@code clone()} per op) saves ~2000 allocations per
 * tick per player with identical results.
 *
 * <p>Ticked on the client thread from {@link WaveCape#render}, read on the
 * render thread via {@link #lerpX}/{@link #lerpY} — like vanilla prev/cur.
 */
public final class WaveCapeSimulation {
    /** Segments in the cape: 16 px of texture → 16 links, 1 px per link. */
    public static final int SEGMENTS = 16;

    private static final float GRAVITY = 25f;
    private static final int ITERATIONS = 30;
    private static final float MAX_BEND = 5f;
    private static final float HEIGHT_MULTIPLIER = 6f;
    private static final float DELTA_TIME = 50f / 1000f;
    private static final float STICK_LENGTH = 1f;
    private static final float RAD_TO_DEG = 57.2958f;

    private final float[] x = new float[SEGMENTS];
    private final float[] y = new float[SEGMENTS];
    private final float[] prevX = new float[SEGMENTS];
    private final float[] prevY = new float[SEGMENTS];
    private boolean built;
    private double lastPlayerX;
    private double lastPlayerZ;
    private boolean hasLastPlayerPos;

    /** Whether the chain is ready (otherwise there is nothing to render). */
    public boolean isBuilt() {
        return built;
    }

    public void build(AbstractClientPlayerEntity player) {
        for (int i = 0; i < SEGMENTS; i++) {
            x[i] = 0f;
            y[i] = -i;
            prevX[i] = 0f;
            prevY[i] = -i;
        }
        built = true;
        for (int i = 0; i < 10; i++) {
            tick(player);
        }
    }

    /** Interpolated point position between ticks — like vanilla prevCapeX/capeX. */
    public float lerpX(int index, float tickDelta) {
        return MathHelper.lerp(tickDelta, prevX[index], x[index]);
    }

    public float lerpY(int index, float tickDelta) {
        return MathHelper.lerp(tickDelta, prevY[index], y[index]);
    }

    /** Simulation step: drag the root after the player and solve constraints. Call once per tick. */
    public void tick(AbstractClientPlayerEntity player) {
        if (!built) {
            return;
        }
        prevX[0] = x[0];
        prevY[0] = y[0];

        // Cape lag behind the player, projected onto body facing: positive
        // value = player moved forward, cape should fly back.
        // (Vanilla capeX/capeZ were removed in 1.21.11; track movement ourselves.)
        double dx = 0.0;
        double dz = 0.0;
        if (hasLastPlayerPos) {
            dx = lastPlayerX - player.getX();
            dz = lastPlayerZ - player.getZ();
        }
        lastPlayerX = player.getX();
        lastPlayerZ = player.getZ();
        hasLastPlayerPos = true;
        float bodyYaw = player.getBodyYaw();
        double sin = MathHelper.sin(bodyYaw * 0.017453292f);
        double cos = -MathHelper.cos(bodyYaw * 0.017453292f);
        // Small kick on jump/fall so the cape doesn't fold into itself (air under it).
        double fallHack = MathHelper.clamp(y[0] - (player.getY() * HEIGHT_MULTIPLIER), 0.0, 1.0);

        x[0] += (float) ((dx * sin + dz * cos) + fallHack);
        y[0] = (float) (player.getY() * HEIGHT_MULTIPLIER + (player.isInSneakingPose() ? -4f : 0f));

        step();
    }

    private void step() {
        float gravityStep = GRAVITY * DELTA_TIME;

        // 1) Gravity (point 0 is pinned).
        for (int i = 1; i < SEGMENTS; i++) {
            float px = x[i];
            float py = y[i];
            y[i] = py - gravityStep;
            prevX[i] = px;
            prevY[i] = py;
        }

        // 2) Don't let the cape fall through the player: all points strictly "behind" the root.
        for (int i = 1; i < SEGMENTS; i++) {
            if (x[i] - x[0] > 0f) {
                x[i] = x[0] - 0.1f;
            }
        }

        // 3) Bend limit — not perfect, but saves the cape from folding into itself.
        for (int i = SEGMENTS - 2; i >= 1; i--) {
            double angle = angle(x[i], y[i], x[i - 1], y[i - 1], x[i + 1], y[i + 1]) * RAD_TO_DEG;
            if (angle > 360.0) {
                angle -= 360.0;
            }
            if (angle < -360.0) {
                angle += 360.0;
            }
            double abs = Math.abs(angle);
            if (abs < 180.0 - MAX_BEND) {
                replace(i, angle, 180.0 - MAX_BEND + 1.0);
            } else if (abs > 180.0 + MAX_BEND) {
                replace(i, angle, 180.0 + MAX_BEND - 1.0);
            }
        }

        // 4) Length-constraint iterations: pull stick ends toward their middle.
        float half = STICK_LENGTH / 2f;
        for (int iteration = 0; iteration < ITERATIONS; iteration++) {
            for (int s = SEGMENTS - 2; s >= 0; s--) {
                int a = s;
                int b = s + 1;
                float centreX = (x[a] + x[b]) / 2f;
                float centreY = (y[a] + y[b]) / 2f;
                float dirX = x[a] - x[b];
                float dirY = y[a] - y[b];
                float len = MathHelper.sqrt(dirX * dirX + dirY * dirY);
                if (len < 1.0E-4f) {
                    dirX = 0f;
                    dirY = 0f;
                } else {
                    dirX /= len;
                    dirY /= len;
                }
                if (a != 0) {                       // point 0 is pinned
                    x[a] = centreX + dirX * half;
                    y[a] = centreY + dirY * half;
                }
                x[b] = centreX - dirX * half;
                y[b] = centreY - dirY * half;
            }
        }

        // 5) Final length fix — otherwise the chain "springs" and stretches.
        for (int s = 0; s < SEGMENTS - 1; s++) {
            int a = s;
            int b = s + 1;
            float dirX = x[a] - x[b];
            float dirY = y[a] - y[b];
            float len = MathHelper.sqrt(dirX * dirX + dirY * dirY);
            if (len < 1.0E-4f) {
                dirX = 0f;
                dirY = 0f;
            } else {
                dirX /= len;
                dirY /= len;
            }
            x[b] = x[a] - dirX * STICK_LENGTH;
            y[b] = y[a] - dirY * STICK_LENGTH;
        }
    }

    /** Angle between "prev — current — next" points (in radians). */
    private static double angle(float midX, float midY, float prevPX, float prevPY, float nextX, float nextY) {
        return Math.atan2(nextY - midY, nextX - midX) - Math.atan2(prevPY - midY, prevPX - midX);
    }

    /** Rotates point i+1 around i so the bend angle becomes target (in degrees). */
    private void replace(int i, double angle, double target) {
        double theta = target / RAD_TO_DEG;
        if (angle < 0.0) {
            theta = -theta;
        }
        float dx = x[i - 1] - x[i];
        float dy = y[i - 1] - y[i];
        double cos = Math.cos(theta);
        double sin = Math.sin(theta);
        x[i + 1] = (float) ((dx * cos) - (dy * sin) + x[i]);
        y[i + 1] = (float) ((dx * sin) + (dy * cos) + y[i]);
    }
}
