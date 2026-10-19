package dev.linkenvisuals.pet;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Random;

/**
 * Ground-pet "brain" (Phase 5.6, ported from the donor's {@code PetWanderController}).
 *
 * <p>Movement runs on a fixed 20 Hz tick while rendering interpolates
 * ({@code lerp(prev, cur, tickDelta)}), so the pet never jitters between frames. Legs are driven once
 * per elapsed tick with the actual step distance, which makes the swing frequency follow the movement
 * speed exactly like vanilla mobs. One-block steps are cleared with a ballistic jump (rise ~1.3 blocks)
 * instead of sliding up. Behaviour: idle near the player, catch up with hysteresis when left behind,
 * teleport when very far away.</p>
 *
 * <p>The ground probe is injectable so the simulation is unit-testable without a live world.</p>
 */
public final class PetWanderController {

    /** Ground probe; may be called with a null world by tests. */
    @FunctionalInterface
    public interface GroundProbe {
        double sample(World world, double x, double z, double fallbackY);
    }

    private static final double TICK_DT = 0.05;

    private final GroundProbe probe;
    private final Random rnd = new Random();

    private Vec3d pos;
    private Vec3d prevPos;
    private float bodyYaw;
    private float prevBodyYaw;
    private float headYaw;
    private float prevHeadYaw;
    private float speedBps;
    private float limbPosDelta;

    private Vec3d wanderTarget;
    private int wanderCooldownTicks;
    private boolean following;
    private int lastAge = -1;

    private boolean jumping;
    private double jumpVy;
    private double jumpTargetY;

    public PetWanderController() {
        this(PetGroundSampler::sampleGroundY);
    }

    public PetWanderController(GroundProbe probe) {
        this.probe = probe;
    }

    public boolean hasPos() {
        return pos != null;
    }

    public Vec3d pos() {
        return pos;
    }

    public float bodyYaw() {
        return bodyYaw;
    }

    public float headYaw() {
        return headYaw;
    }

    public Vec3d posLerp(float tickDelta) {
        if (pos == null) {
            return null;
        }
        if (prevPos == null) {
            return pos;
        }
        return new Vec3d(lerp(prevPos.x, pos.x, tickDelta), lerp(prevPos.y, pos.y, tickDelta),
            lerp(prevPos.z, pos.z, tickDelta));
    }

    public float bodyYawLerp(float tickDelta) {
        return lerpAngle(prevBodyYaw, bodyYaw, tickDelta);
    }

    public float headYawLerp(float tickDelta) {
        return lerpAngle(prevHeadYaw, headYaw, tickDelta);
    }

    /** Horizontal distance covered by the last simulated tick, used to drive the leg animation. */
    public float limbPosDelta() {
        return limbPosDelta;
    }

    public void reset() {
        pos = null;
        prevPos = null;
        wanderTarget = null;
        wanderCooldownTicks = 0;
        speedBps = 0f;
        limbPosDelta = 0f;
        following = false;
        lastAge = -1;
        jumping = false;
        jumpVy = 0.0;
        jumpTargetY = 0.0;
    }

    /**
     * Call once per frame. Advances the simulation by the ticks elapsed since the last call
     * ({@code age} is the player's tick counter). Returns the number of ticks that were simulated,
     * so the caller can swing the legs that many times.
     */
    public int frame(World world, double px, double py, double pz, float baseSpeed, double leash, int age) {
        if (pos == null) {
            seed(world, px, py, pz);
            lastAge = age;
            return 0;
        }
        if (age == lastAge) {
            return 0;
        }
        int steps = lastAge < 0 ? 1 : age - lastAge;
        if (steps < 1) {
            lastAge = age;
            return 0;
        }
        steps = Math.min(2, steps);
        lastAge = age;
        for (int i = 0; i < steps; i++) {
            tickStep(world, px, py, pz, baseSpeed, leash);
        }
        return steps;
    }

    private void seed(World world, double px, double py, double pz) {
        double g = probe.sample(world, px, pz, py);
        pos = new Vec3d(px, Double.isNaN(g) ? py : g, pz);
        prevPos = pos;
        bodyYaw = headYaw = prevBodyYaw = prevHeadYaw = 0f;
        speedBps = 0f;
        limbPosDelta = 0f;
        jumping = false;
    }

    private void tickStep(World world, double px, double py, double pz, float baseSpeed, double leashRadius) {
        prevPos = pos;
        prevBodyYaw = bodyYaw;
        prevHeadYaw = headYaw;

        double dxp = px - pos.x;
        double dzp = pz - pos.z;
        double distToPlayer = Math.sqrt(dxp * dxp + dzp * dzp);

        // Teleport when left far behind.
        if (distToPlayer > 16.0) {
            double g = probe.sample(world, px, pz, py);
            pos = new Vec3d(px + (rnd.nextDouble() - 0.5), Double.isNaN(g) ? py : g, pz + (rnd.nextDouble() - 0.5));
            prevPos = pos;
            wanderTarget = null;
            following = false;
            speedBps = 0f;
            jumping = false;
            limbPosDelta = 0f;
            return;
        }

        double baseMove = 1.3 * MathHelper.clamp(baseSpeed, 0.4f, 3.0f);
        double followStart = Math.max(2.4, leashRadius + 1.0);
        double followStop = Math.max(1.1, leashRadius - 0.2);
        if (distToPlayer > followStart) {
            following = true;
        } else if (following && distToPlayer < followStop) {
            following = false;
        }

        Vec3d target;
        double moveGoal;
        if (following) {
            double inv = distToPlayer > 1.0e-4 ? 1.0 / distToPlayer : 0.0;
            double stop = followStop * 0.85;
            target = new Vec3d(px - dxp * inv * stop, py, pz - dzp * inv * stop);
            wanderTarget = null;
            // The further behind, the faster it runs: ~3.1 at the threshold up to a 7 blocks/s ceiling
            // (balances ~2.3 blocks behind a walking player and ~3 behind a sprinting one).
            double over = Math.max(0.0, distToPlayer - followStop);
            moveGoal = Math.min(baseMove * (2.4 + MathHelper.clamp(over * 1.2, 0.0, 3.6)), 7.0);
        } else {
            if (wanderTarget == null) {
                if (wanderCooldownTicks > 0) {
                    wanderCooldownTicks--;
                } else {
                    pickWander(world, px, py, pz, leashRadius);
                }
            } else if (horizDist(pos, wanderTarget) < 0.4) {
                wanderTarget = null;
                wanderCooldownTicks = 40 + rnd.nextInt(70);
            }
            target = wanderTarget;
            moveGoal = baseMove;
        }

        double dirX = 0;
        double dirZ = 0;
        double desiredSpeed = 0;
        if (target != null) {
            double dx = target.x - pos.x;
            double dz = target.z - pos.z;
            double d = Math.sqrt(dx * dx + dz * dz);
            if (d > 0.35) {
                dirX = dx / d;
                dirZ = dz / d;
                desiredSpeed = moveGoal;
            }
        }

        float tauSp = desiredSpeed > speedBps ? 0.25f : 0.20f;
        speedBps = approach(speedBps, (float) desiredSpeed, (float) TICK_DT, tauSp);

        double curG = groundOr(world, pos.x, pos.z, py);
        double aheadG = Double.NaN;
        boolean moving = (dirX != 0 || dirZ != 0) && speedBps > 0.05f;

        // Step on XZ with wall/cliff checks.
        if (moving) {
            double stepLen = speedBps * TICK_DT;
            double nx = pos.x + dirX * stepLen;
            double nz = pos.z + dirZ * stepLen;
            double ng = probe.sample(world, nx, nz, curG);
            aheadG = probe.sample(world, pos.x + dirX * 0.6, pos.z + dirZ * 0.6, curG);
            if (!Double.isNaN(ng) && ng - curG <= 1.05) {
                pos = new Vec3d(nx, pos.y, nz);
                bodyYaw = lerpAngle(bodyYaw, dirToYaw(dirX, dirZ), 1f - (float) Math.exp(-TICK_DT / 0.18));
            } else {
                if (!following) {
                    wanderTarget = null;
                    wanderCooldownTicks = 24;
                }
                speedBps = approach(speedBps, 0f, (float) TICK_DT, 0.12f);
            }
        }

        // Height: ballistic jump on one-block steps, otherwise a smooth follow of the ground.
        curG = probe.sample(world, pos.x, pos.z, py);
        if (!jumping && moving && !Double.isNaN(aheadG) && !Double.isNaN(curG)) {
            double rise = aheadG - curG;
            if (rise >= 0.55 && rise <= 1.2 && Math.abs(pos.y - curG) < 0.25) {
                jumping = true;
                // Discrete arc (vy0 = 0.50, g = 0.08) peaks at ~1.32 so the pet clears 1.2 and lands
                // on top of the block from above instead of jerking up at the end.
                jumpVy = 0.50;
                jumpTargetY = aheadG;
            }
        }
        if (jumping) {
            jumpVy -= 0.08;
            double ny = pos.y + jumpVy;
            if (jumpVy < 0 && ny <= jumpTargetY) {
                ny = jumpTargetY;
                jumping = false;
                jumpVy = 0.0;
            }
            pos = new Vec3d(pos.x, ny, pos.z);
        } else if (!Double.isNaN(curG)) {
            double tauY = curG > pos.y ? 0.08 : 0.14;
            double ny = pos.y + (curG - pos.y) * (1.0 - Math.exp(-TICK_DT / tauY));
            if (ny < curG - 0.08) {
                ny = curG;
            }
            pos = new Vec3d(pos.x, ny, pos.z);
        }

        // Head looks at the player; while standing the body slowly turns towards them too.
        float lookYaw = dirToYaw(px - pos.x, pz - pos.z);
        float rel = MathHelper.clamp(MathHelper.wrapDegrees(lookYaw - bodyYaw), -55f, 55f);
        headYaw = lerpAngle(headYaw, bodyYaw + rel, 1f - (float) Math.exp(-TICK_DT / 0.16));
        if (dirX == 0 && dirZ == 0) {
            float relB = MathHelper.clamp(MathHelper.wrapDegrees(lookYaw - bodyYaw), -35f, 35f);
            bodyYaw = lerpAngle(bodyYaw, bodyYaw + relB, 1f - (float) Math.exp(-TICK_DT / 0.6));
        }

        limbPosDelta = (float) horizDist(prevPos, pos);
    }

    private void pickWander(World world, double px, double py, double pz, double leash) {
        // Mostly idling, walking only occasionally.
        if (rnd.nextFloat() < 0.6f) {
            wanderTarget = null;
            wanderCooldownTicks = 50 + rnd.nextInt(80);
            return;
        }
        for (int i = 0; i < 12; i++) {
            double ang = rnd.nextDouble() * Math.PI * 2.0;
            double r = 0.9 + rnd.nextDouble() * Math.max(0.4, leash * 0.9);
            double tx = px + Math.cos(ang) * r;
            double tz = pz + Math.sin(ang) * r;
            double ddx = tx - pos.x;
            double ddz = tz - pos.z;
            if (ddx * ddx + ddz * ddz < 0.64) {
                continue; // prefer a real walk over a micro step
            }
            double g = probe.sample(world, tx, tz, py);
            if (!Double.isNaN(g) && Math.abs(g - py) <= 2.0) {
                wanderTarget = new Vec3d(tx, g, tz);
                wanderCooldownTicks = 60 + rnd.nextInt(80);
                return;
            }
        }
        wanderTarget = null;
        wanderCooldownTicks = 30;
    }

    private double groundOr(World world, double x, double z, double fallback) {
        double g = probe.sample(world, x, z, fallback);
        return Double.isNaN(g) ? fallback : g;
    }

    private static double horizDist(Vec3d a, Vec3d b) {
        double dx = a.x - b.x;
        double dz = a.z - b.z;
        return Math.sqrt(dx * dx + dz * dz);
    }

    private static double lerp(double a, double b, float t) {
        return a + (b - a) * t;
    }

    private static float approach(float cur, float want, float dt, float tau) {
        return cur + (want - cur) * (1f - (float) Math.exp(-dt / Math.max(0.02f, tau)));
    }

    private static float dirToYaw(double dx, double dz) {
        return (float) (MathHelper.atan2(dz, dx) * (180.0 / Math.PI)) - 90f;
    }

    private static float lerpAngle(float a, float b, float t) {
        float d = MathHelper.wrapDegrees(b - a);
        return a + d * MathHelper.clamp(t, 0f, 1f);
    }
}
