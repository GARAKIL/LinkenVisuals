package dev.linkenvisuals.pet;

import dev.linkenvisuals.LinkenVisuals;
import dev.linkenvisuals.clickgui.ModuleRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.Model;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.EntityRenderManager;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.ParrotEntityModel;
import net.minecraft.client.render.entity.state.AllayEntityRenderState;
import net.minecraft.client.render.entity.state.BatEntityRenderState;
import net.minecraft.client.render.entity.state.BeeEntityRenderState;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.entity.state.FoxEntityRenderState;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.render.entity.state.ParrotEntityRenderState;
import net.minecraft.client.render.entity.state.SlimeEntityRenderState;
import net.minecraft.client.render.entity.state.VexEntityRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.entity.mob.VexEntity;
import net.minecraft.entity.passive.AllayEntity;
import net.minecraft.entity.passive.BatEntity;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.entity.passive.FrogEntity;
import net.minecraft.entity.passive.ParrotEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.passive.PufferfishEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * Pet rig (Phase 5.6): renders one vanilla mob around the local player, ported from the donor's
 * {@code Raven} module.
 *
 * <p>Flyers (bat, parrot, raven, fairy, bee, vex, pufferfish) hover on the donor's "shoulder orbit":
 * they hold next to the left shoulder while the player is still/sprinting and swing out into a
 * smooth orbit while the player walks. Ground pets (pig, frog, slime, fox) use
 * {@link PetWanderController}: a fixed 20 Hz simulation with interpolated rendering, ballistic
 * steps and a proper leg swing driven once per simulated tick. The fox additionally has the donor's
 * sit/sleep idle states.</p>
 *
 * <p>Everything is local-only and render-only: the mob is created client-side, never added to the
 * world, never ticked by the server and never synced. Animations advance by ticking the fake entity
 * on the client (the donor's approach), while poses that the fake entity cannot express are patched
 * directly onto the vanilla render state. Network code from the donor (RemotePets and friends) is
 * deliberately not ported.</p>
 */
public final class PetRig {

    private static final PetRig INSTANCE = new PetRig();

    /** Raven pet texture (asset ported from the donor). */
    private static final Identifier VORON_TEXTURE = Identifier.of("linkenvisuals", "textures/pet/voron.png");

    // --- flyer orbit (port of the donor's updateShoulderOrbit state) ---
    private boolean flyInited;
    private Vec3d flyPos = Vec3d.ZERO;
    private Vec3d flyPrevPos = Vec3d.ZERO;
    private float flyYaw;
    private float flyPitch;
    private float orbitAng;
    private float orbitBlend = 1.0f;
    private boolean holdMode;
    private float wantHoldTime;
    private float wantOrbitTime;
    private float baseYawDeg = Float.NaN;
    private float moveSp;

    // --- ground wander ---
    private final PetWanderController walkerWander = new PetWanderController();
    private final PetWanderController foxWander = new PetWanderController();
    private boolean slimeAir;
    private double slimeJumpY;
    private double slimeJumpVel;
    private long slimeLastHopMs;

    // --- fox idle behaviour (port of FoxStateCompat) ---
    private int foxMode;
    private long foxModeSinceMs;
    private long foxLastMoveMs;
    private long foxLastIdleMs;
    private boolean foxFlagSitting;
    private boolean foxFlagSleeping;
    private boolean foxFlagWalking;
    private Direction foxFlagSleepDir = Direction.SOUTH;

    // --- runtime ---
    private PetSkins skin = PetSkins.NONE;
    private LivingEntity entity;
    private World lastWorld;
    private long lastFrameMs;
    private long lastAnimMs;
    private boolean animFailed;
    private long petBlockedUntilMs;
    private long lastFailureLogMs;
    private final PetUltimate ult = new PetUltimate();

    private PetRig() {
    }

    /** World-render hook (AFTER_ENTITIES, registered next to the other cosmetic hooks). */
    public static void render(WorldRenderContext context) {
        INSTANCE.frame(context);
    }

    /** Drops the fake entity and all motion state (world switch / reconnect). */
    public static void clear() {
        INSTANCE.resetAll();
    }

    private void resetAll() {
        ult.reset();
        resetMotion();
        entity = null;
        lastWorld = null;
        lastFrameMs = 0L;
        skin = PetSkins.NONE;
    }

    private void resetMotion() {
        flyInited = false;
        flyPos = Vec3d.ZERO;
        flyPrevPos = Vec3d.ZERO;
        flyYaw = 0.0f;
        flyPitch = 0.0f;
        orbitAng = 0.0f;
        orbitBlend = 1.0f;
        holdMode = false;
        wantHoldTime = 0.0f;
        wantOrbitTime = 0.0f;
        baseYawDeg = Float.NaN;
        moveSp = 0.0f;
        walkerWander.reset();
        foxWander.reset();
        slimeAir = false;
        slimeJumpY = 0.0;
        slimeJumpVel = 0.0;
        slimeLastHopMs = 0L;
        foxMode = 0;
        foxModeSinceMs = 0L;
        foxLastMoveMs = 0L;
        foxLastIdleMs = 0L;
        foxFlagSitting = false;
        foxFlagSleeping = false;
        foxFlagWalking = false;
        foxFlagSleepDir = Direction.SOUTH;
        lastAnimMs = 0L;
        animFailed = false;
    }

    private void frame(WorldRenderContext context) {
        long nowMs = System.currentTimeMillis();
        if (nowMs < petBlockedUntilMs) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (!ModuleRegistry.cosmetics().enabled() || client.world == null || client.player == null) {
            resetAll();
            return;
        }

        PetSkins want = PetSkins.fromLabel(ModuleRegistry.cosmeticSlot("pet"));
        if (want == PetSkins.NONE) {
            resetAll();
            return;
        }

        // Raven ULT trigger (the donor's configurable "raven scene" bind). Blocked while a screen
        // is open so typing in a GUI can never fire the scene.
        if (want == PetSkins.RAVEN) {
            if (client.currentScreen == null) {
                ult.pollKey(client, ModuleRegistry.cosmeticPetUlt().code());
            }
        } else {
            ult.reset();
        }

        // The donor keeps the pet visible in first person unless the user opts out; an active
        // ULT scene (and its return flight) keeps rendering in first person regardless.
        if (client.options != null && client.options.getPerspective() == Perspective.FIRST_PERSON
            && !ModuleRegistry.cosmeticPetFirstPerson().value()
            && !ult.active() && !ult.returnActive()) {
            return;
        }

        MatrixStack matrices = context.matrices();
        OrderedRenderCommandQueue queue = context.commandQueue();
        CameraRenderState cameraState = context.worldState() == null
            ? null : context.worldState().cameraRenderState;
        if (matrices == null || queue == null || cameraState == null) {
            return;
        }

        try {
            renderPet(client, matrices, queue, cameraState, want, nowMs, context.consumers());
        } catch (RuntimeException failed) {
            // Safety net: a broken pet must never take the render thread down. The failure is logged
            // (throttled) and the pet retries after a short pause.
            if (nowMs - lastFailureLogMs > 30000L) {
                lastFailureLogMs = nowMs;
                LinkenVisuals.LOGGER.warn("Pet rendering failed, retrying in 5 s: {}", failed.toString());
            }
            resetAll();
            petBlockedUntilMs = nowMs + 5000L;
        }
    }

    private void renderPet(MinecraftClient client, MatrixStack matrices, OrderedRenderCommandQueue queue,
                           CameraRenderState cameraState, PetSkins want, long nowMs,
                           VertexConsumerProvider consumers) {
        PlayerEntity player = client.player;
        World world = client.world;
        EntityRenderManager manager = client.getEntityRenderDispatcher();

        // World switch (portal / reconnect): snap instead of dragging the pet across the world.
        if (lastWorld != world) {
            resetMotion();
            entity = null;
            lastWorld = world;
        }
        if (skin != want) {
            skin = want;
            resetMotion();
            entity = null;
        }
        LivingEntity ent = ensureEntity(world, want);
        if (ent == null) {
            return;
        }

        float pt = client.getRenderTickCounter().getTickProgress(false);
        float dt = lastFrameMs == 0L
            ? 0.016f : MathHelper.clamp((nowMs - lastFrameMs) / 1000.0f, 0.001f, 0.06f);
        lastFrameMs = nowMs;
        double t = nowMs / 1000.0;

        Vec3d lerped = player.getLerpedPos(pt);
        double px = lerped.x;
        double py = lerped.y;
        double pz = lerped.z;

        Vec3d vel = player.getVelocity();
        float rawSp = (float) Math.sqrt(vel.x * vel.x + vel.z * vel.z);
        moveSp += (rawSp - moveSp) * (1.0f - (float) Math.exp(-dt / 0.10f));

        float bodyYawDeg = MathHelper.lerpAngleDegrees(pt, player.lastBodyYaw, player.bodyYaw);
        if (Float.isNaN(baseYawDeg)) {
            baseYawDeg = bodyYawDeg;
        }
        if (player.isSprinting() || moveSp > 0.11f) {
            baseYawDeg = lerpAngle(baseYawDeg, bodyYawDeg, 1.0f - (float) Math.exp(-dt / 0.14f));
        } else if (moveSp > 0.06f) {
            baseYawDeg = lerpAngle(baseYawDeg, bodyYawDeg, 1.0f - (float) Math.exp(-dt / 0.22f));
        }

        double yawRad = Math.toRadians(baseYawDeg);
        Vec3d forward = new Vec3d(-Math.sin(yawRad), 0.0, Math.cos(yawRad));
        // The donor's "right" vector actually points at the player's left; kept verbatim so the
        // shoulder offset and the orbit direction match the donor 1:1.
        Vec3d side = new Vec3d(forward.z, 0.0, -forward.x);

        float radiusBase = MathHelper.clamp(ModuleRegistry.cosmeticPetRadius().value(), 0.15f, 2.5f);
        float heightBase = MathHelper.clamp(ModuleRegistry.cosmeticPetHeight().value(), -0.5f, 3.0f);
        float speed = MathHelper.clamp(ModuleRegistry.cosmeticPetSpeed().value(), 0.05f, 5.0f);
        float scale = MathHelper.clamp(ModuleRegistry.cosmeticPetScale().value(), 0.15f, 2.0f);

        Vec3d eye = client.gameRenderer.getCamera().getCameraPos();

        switch (want.kind()) {
            case FLYER -> renderFlyer(manager, queue, cameraState, matrices, ent, player, pt, dt, px, py, pz,
                forward, side, radiusBase, heightBase, speed, scale, t, nowMs, eye, consumers);
            case PUFFER -> renderPuffer(manager, queue, cameraState, matrices, ent, player, pt, dt, px, py, pz,
                forward, side, radiusBase, heightBase, speed, scale, t, nowMs, eye);
            case WALKER -> renderWalker(manager, queue, cameraState, matrices, ent, world, player, pt, dt,
                radiusBase, speed, scale, nowMs, eye);
            case FOX -> renderFox(manager, queue, cameraState, matrices, ent, world, player, pt, px, py, pz,
                radiusBase, speed, scale, nowMs, eye);
            default -> {
            }
        }
    }

    // ------------------------------------------------------------------
    // Entity management
    // ------------------------------------------------------------------

    private LivingEntity ensureEntity(World world, PetSkins want) {
        if (entity != null && entity.getEntityWorld() == world) {
            return entity;
        }
        entity = createEntity(world, want);
        return entity;
    }

    private LivingEntity createEntity(World world, PetSkins want) {
        LivingEntity created = switch (want) {
            case BAT -> new BatEntity(EntityType.BAT, world);
            case PARROT, RAVEN -> new ParrotEntity(EntityType.PARROT, world);
            case FAIRY -> new AllayEntity(EntityType.ALLAY, world);
            case BEE -> new BeeEntity(EntityType.BEE, world);
            case VEX -> new VexEntity(EntityType.VEX, world);
            case FOX -> new FoxEntity(EntityType.FOX, world);
            case PIG -> new PigEntity(EntityType.PIG, world);
            case FROG -> new FrogEntity(EntityType.FROG, world);
            case PUFFERFISH -> new PufferfishEntity(EntityType.PUFFERFISH, world);
            case SLIME -> new SlimeEntity(EntityType.SLIME, world);
            default -> null;
        };
        if (created == null) {
            return null;
        }
        created.setNoGravity(true);
        created.setSilent(true);
        created.setInvisible(false);
        created.setInvulnerable(true);
        created.setCustomNameVisible(false);
        created.setPose(EntityPose.STANDING);
        created.setOnGround(false);
        created.noClip = true;
        if (created instanceof MobEntity mob) {
            mob.setAiDisabled(true);
        }
        if (created instanceof SlimeEntity slime) {
            slime.setSize(1, true);
        }
        if (created instanceof PufferfishEntity puffer) {
            puffer.setPuffState(0);
        }
        return created;
    }

    // ------------------------------------------------------------------
    // Flyers (bat / parrot / raven / fairy / bee / vex)
    // ------------------------------------------------------------------

    private void renderFlyer(EntityRenderManager manager, OrderedRenderCommandQueue queue,
                             CameraRenderState cameraState, MatrixStack matrices, LivingEntity ent,
                             PlayerEntity player, float pt, float dt, double px, double py, double pz,
                             Vec3d forward, Vec3d side, float radiusBase, float heightBase,
                             float speed, float scale, double t, long nowMs, Vec3d eye,
                             VertexConsumerProvider consumers) {
        updateShoulderOrbit(player, dt, px, py, pz, forward, side, radiusBase, heightBase, speed, t);

        // Raven ULT / return flight replaces the normal raven rendering for the frame.
        if (skin == PetSkins.RAVEN && ult.render(this, consumers, manager, queue, cameraState, matrices, ent,
                player, pt, dt, px, py, pz, forward, side, radiusBase, speed, scale, t, nowMs, eye)) {
            return;
        }

        float renderPitch = flyPitch;
        float tickMul = 1.0f;
        float renderScale = scale;
        switch (skin) {
            case FAIRY -> {
                renderScale *= 0.92f;
                tickMul = 0.90f;
                renderPitch = flyPitch * 0.55f;
            }
            case BEE -> {
                renderScale *= 0.95f;
                tickMul = 1.10f;
                renderPitch = flyPitch * 0.70f;
            }
            case VEX -> {
                renderScale *= 0.98f;
                tickMul = 1.25f;
                renderPitch = flyPitch * 0.75f;
            }
            case RAVEN -> renderScale *= 1.28f;
            default -> {
            }
        }

        applyPose(ent, flyPos, flyYaw, flyYaw, renderPitch, player.age, false, EntityPose.STANDING);

        if (ent instanceof BatEntity bat) {
            bat.setRoosting(false);
            // Bat wings are advanced by the entity tick; keep the donor's 3-tick cap.
            tickAnimation(ent, nowMs, 3, 1.0f);
        } else if (ent instanceof ParrotEntity parrot) {
            // Parrots/ravens are not ticked at all (donor parity): wings are driven analytically.
            animateParrotWings(parrot, t, skin == PetSkins.RAVEN ? 1.15f : 5.25f);
        } else {
            Vec3d vis = estimateVelocity(dt, 0.22 + 0.25 * speed);
            ent.setVelocity(vis.x, vis.y * 0.45, vis.z);
            tickAnimation(ent, nowMs, 6, tickMul);
        }
        // Re-snap after ticking so the render state reads the exact pose.
        applyPose(ent, flyPos, flyYaw, flyYaw, renderPitch, player.age, false, EntityPose.STANDING);

        draw(manager, queue, cameraState, matrices, ent, flyPos, pt, renderScale, eye, skin == PetSkins.RAVEN);
    }

    /**
     * Donor's constant wing flap: {@code flapAngle} in the vanilla render state is
     * {@code (sin(lerp(lastFlapProgress, flapProgress, pt)) + 1) * lerp(lastMaxWingDeviation,
     * maxWingDeviation, pt)}, so advancing these four entity fields drives a steady flap.
     */
    static void animateParrotWings(ParrotEntity bird, double t, float flapSpeed) {
        float flap = (float) (Math.sin(t * flapSpeed * 6.2) * 0.5 + 0.5);
        bird.lastMaxWingDeviation = bird.maxWingDeviation;
        bird.maxWingDeviation = 0.10f + flap * 0.24f;
        bird.lastFlapProgress = bird.flapProgress;
        bird.flapProgress = (float) (t * flapSpeed * 3.6);
    }

    private void updateShoulderOrbit(PlayerEntity player, float dt, double px, double py, double pz,
                                     Vec3d forward, Vec3d side, float radiusBase, float heightBase,
                                     float speed, double t) {
        boolean wantHold = player.isSprinting() || moveSp > 0.12f;
        boolean wantOrbit = !player.isSprinting() && moveSp < 0.07f;

        if (wantHold) {
            wantHoldTime += dt;
            wantOrbitTime = 0.0f;
        } else if (wantOrbit) {
            wantOrbitTime += dt;
            wantHoldTime = 0.0f;
        } else {
            wantHoldTime = Math.max(0.0f, wantHoldTime - dt * 0.75f);
            wantOrbitTime = Math.max(0.0f, wantOrbitTime - dt * 0.75f);
        }

        if (!holdMode && wantHoldTime >= 0.16f) {
            holdMode = true;
        }
        if (holdMode && wantOrbitTime >= 0.20f) {
            holdMode = false;
        }

        float blendTau = holdMode ? 0.30f : 0.36f;
        float blendA = 1.0f - (float) Math.exp(-dt / Math.max(0.06f, blendTau));
        orbitBlend += ((holdMode ? 0.0f : 1.0f) - orbitBlend) * blendA;
        float b = easeInOut(MathHelper.clamp(orbitBlend, 0.0f, 1.0f));

        float orbitAngVel = speed * 1.25f;
        orbitAng = wrapRad(orbitAng + (orbitAngVel * dt * (0.26f + 0.26f * (1.0f - b))));

        double ox = Math.cos(orbitAng) * radiusBase;
        double oz = Math.sin(orbitAng) * radiusBase;

        double orbitY = py + player.getStandingEyeHeight() * 0.35 + heightBase + Math.sin(t * 1.6) * 0.055;
        Vec3d orbitTarget = new Vec3d(
            px + side.x * ox + forward.x * oz,
            orbitY,
            pz + side.z * ox + forward.z * oz);

        Vec3d shoulderTarget = computeLeftShoulder(px, py, pz, player, forward, side, t);
        Vec3d target = lerpVec(shoulderTarget, orbitTarget, b);

        float posTau = 0.09f + 0.08f * (1.0f - b);
        float posA = 1.0f - (float) Math.exp(-dt / Math.max(0.05f, posTau));

        if (!flyInited || flyPos.squaredDistanceTo(target) > 256.0) {
            // First frame or a teleport/portal: snap instead of dragging across the world.
            flyPrevPos = target;
            flyPos = target;
            flyInited = true;
        } else {
            flyPrevPos = flyPos;
            flyPos = flyPos.add(target.subtract(flyPos).multiply(posA));
        }

        Vec3d vEst = flyPos.subtract(flyPrevPos);
        double inv = dt > 1.0e-4 ? (1.0 / dt) : 60.0;
        vEst = vEst.multiply(inv);

        float orbitYaw;
        if ((vEst.x * vEst.x + vEst.z * vEst.z) > 1.0e-8) {
            orbitYaw = (float) (MathHelper.atan2(vEst.z, vEst.x) * (180.0 / Math.PI)) - 90.0f;
        } else {
            orbitYaw = flyYaw;
        }
        flyYaw = lerpAngle(flyYaw, lerpAngle(baseYawDeg, orbitYaw, b), 1.0f - (float) Math.exp(-dt / 0.16f));

        float orbitPitch = (float) MathHelper.clamp(-vEst.y * 7.5, -16.0, 16.0);
        float holdPitch = (float) Math.sin(t * 1.45) * 2.0f;
        float targetPitch = holdPitch + (orbitPitch - holdPitch) * b;
        flyPitch += (targetPitch - flyPitch) * (1.0f - (float) Math.exp(-dt / 0.20f));
    }

    static Vec3d computeLeftShoulder(double px, double py, double pz, PlayerEntity player,
                                     Vec3d forward, Vec3d side, double t) {
        double baseY = py + player.getStandingEyeHeight() * 0.74 + 0.08;
        baseY += Math.sin(t * 2.9) * 0.030;
        double lr = -0.34;
        double back = 0.12;
        double up = Math.sin(t * 1.25) * 0.016;
        return new Vec3d(
            px + side.x * lr - forward.x * back,
            baseY + up,
            pz + side.z * lr - forward.z * back);
    }

    private Vec3d estimateVelocity(float dt, double vMax) {
        Vec3d v = flyPos.subtract(flyPrevPos);
        double inv = dt > 1.0e-4 ? (1.0 / dt) : 60.0;
        v = v.multiply(inv);
        double len = Math.sqrt(v.x * v.x + v.y * v.y + v.z * v.z);
        if (len > vMax && len > 1.0e-8) {
            v = v.multiply(vMax / len);
        }
        return v;
    }

    // ------------------------------------------------------------------
    // Pufferfish (orbit + puff cycle)
    // ------------------------------------------------------------------

    private void renderPuffer(EntityRenderManager manager, OrderedRenderCommandQueue queue,
                              CameraRenderState cameraState, MatrixStack matrices, LivingEntity ent,
                              PlayerEntity player, float pt, float dt, double px, double py, double pz,
                              Vec3d forward, Vec3d side, float radiusBase, float heightBase,
                              float speed, float scale, double t, long nowMs, Vec3d eye) {
        updateShoulderOrbit(player, dt, px, py, pz, forward, side, radiusBase, heightBase, speed, t);

        Vec3d vis = estimateVelocity(dt, 0.20 + 0.22 * speed);
        ent.setOnGround(false);
        ent.setVelocity(vis.x, vis.y * 0.40, vis.z);

        float s = (float) ((Math.sin(t * 1.35) + 1.0) * 0.5);
        int puff = s < 0.33f ? 0 : s < 0.66f ? 1 : 2;
        if (ent instanceof PufferfishEntity puffer) {
            puffer.setPuffState(puff);
        }

        applyPose(ent, flyPos, flyYaw, flyYaw, 0.0f, player.age, false, EntityPose.STANDING);
        tickAnimation(ent, nowMs, 6, 1.0f);
        applyPose(ent, flyPos, flyYaw, flyYaw, 0.0f, player.age, false, EntityPose.STANDING);

        draw(manager, queue, cameraState, matrices, ent, flyPos, pt, scale * 0.92f, eye, false);
    }

    // ------------------------------------------------------------------
    // Ground walkers (pig / frog / slime)
    // ------------------------------------------------------------------

    private void renderWalker(EntityRenderManager manager, OrderedRenderCommandQueue queue,
                              CameraRenderState cameraState, MatrixStack matrices, LivingEntity ent,
                              World world, PlayerEntity player, float pt, float dt, float radiusBase,
                              float speed, float scale, long nowMs, Vec3d eye) {
        double leash = MathHelper.clamp(radiusBase + 0.85, 1.2, 1.6);
        int steps = walkerWander.frame(world, player.getX(), player.getY(), player.getZ(), speed, leash,
            player.age);
        Vec3d wpos = walkerWander.posLerp(pt);
        if (wpos == null) {
            return;
        }

        boolean slime = ent instanceof SlimeEntity;
        double yOff = 0.02;
        if (ent instanceof FrogEntity) {
            yOff = 0.00;
        }
        if (slime) {
            yOff = 0.01;
            // Ballistic hop on top of the walk position (the donor's own slime physics).
            long periodMs = (long) MathHelper.clamp(520.0 - speed * 55.0, 340.0, 520.0);
            if (!slimeAir) {
                if (slimeLastHopMs == 0L) {
                    slimeLastHopMs = nowMs;
                }
                if (nowMs - slimeLastHopMs >= periodMs) {
                    slimeLastHopMs = nowMs;
                    slimeJumpVel = 0.34 + 0.08 * MathHelper.clamp(speed, 0.05f, 5.0f);
                    slimeAir = true;
                }
            }
            if (slimeAir) {
                slimeJumpVel += -2.10 * dt;
                slimeJumpY += slimeJumpVel * dt;
                if (slimeJumpY <= 0.0) {
                    slimeJumpY = 0.0;
                    slimeJumpVel = 0.0;
                    slimeAir = false;
                }
            }
        }

        double jumpY = slime ? slimeJumpY : 0.0;
        Vec3d finalPos = new Vec3d(wpos.x, wpos.y + yOff + jumpY, wpos.z);
        float bYaw = walkerWander.bodyYawLerp(pt);
        float hYaw = walkerWander.headYawLerp(pt);
        boolean onGround = !slime || !slimeAir;

        float renderScale = scale;
        if (ent instanceof PigEntity || ent instanceof FrogEntity) {
            renderScale *= 0.98f;
        }
        if (slime) {
            renderScale *= 0.72f;
        }

        applyPose(ent, finalPos, bYaw, hYaw, 0.0f, player.age, onGround, EntityPose.STANDING);
        tickAnimation(ent, nowMs, 6, 1.0f);
        // Restore the pose after the tick and swing the legs once per simulated tick with the
        // actual step distance: the swing frequency follows the speed exactly like vanilla mobs.
        applyPose(ent, finalPos, bYaw, hYaw, 0.0f, player.age, onGround, EntityPose.STANDING);
        for (int i = 0; i < steps; i++) {
            driveLimbs(ent, walkerWander.limbPosDelta());
        }

        draw(manager, queue, cameraState, matrices, ent, finalPos, pt, renderScale, eye, false);
    }

    // ------------------------------------------------------------------
    // Fox (walk / sit / sleep)
    // ------------------------------------------------------------------

    private void renderFox(EntityRenderManager manager, OrderedRenderCommandQueue queue,
                           CameraRenderState cameraState, MatrixStack matrices, LivingEntity ent,
                           World world, PlayerEntity player, float pt, double px, double py, double pz,
                           float radiusBase, float speed, float scale, long nowMs, Vec3d eye) {
        boolean playerMoving = player.isSprinting() || moveSp > 0.07f;
        updateFoxMode(playerMoving, nowMs);
        int mode = foxMode;

        double leash = MathHelper.clamp(radiusBase + 0.85, 1.2, 1.6);
        int steps = 0;
        if (mode == 0 || !foxWander.hasPos()) {
            steps = foxWander.frame(world, px, py, pz, speed, leash, player.age);
        }
        Vec3d wpos = mode == 0 ? foxWander.posLerp(pt) : foxWander.pos();
        if (wpos == null) {
            return;
        }
        double y0 = wpos.y;

        // Sitting/sleeping freezes the wander position, but the ground below may have risen since;
        // re-glue every frame so the pet never sinks into blocks.
        if (mode == 1 || mode == 2) {
            double ground = PetGroundSampler.sampleGroundY(world, wpos.x, wpos.z, py);
            if (!Double.isNaN(ground)) {
                y0 = ground;
            }
        }

        double yOff = 0.06;
        if (mode == 2) {
            float standH = ent.getDimensions(EntityPose.STANDING).height();
            float sleepH = ent.getDimensions(EntityPose.SLEEPING).height();
            yOff = 0.06 + Math.max(0.0, (standH - sleepH) * 0.5) - 0.30;
        }

        Vec3d finalPos = new Vec3d(wpos.x, y0 + yOff, wpos.z);
        float bYaw = mode == 0 ? foxWander.bodyYawLerp(pt) : foxWander.bodyYaw();
        float hYaw = mode == 0 ? foxWander.headYawLerp(pt) : foxWander.headYaw();
        Direction sleepDir = sleepDir(bYaw);
        float renderYaw = mode == 2 ? dirYaw(sleepDir) : bYaw;

        long idleFor = nowMs - foxLastMoveMs;
        foxFlagSleepDir = sleepDir;
        switch (mode) {
            case 1 -> {
                foxFlagSitting = true;
                foxFlagSleeping = idleFor > 38000L; // very long idle: the sitting fox dozes off
                foxFlagWalking = false;
            }
            case 2 -> {
                foxFlagSitting = false;
                foxFlagSleeping = true;
                foxFlagWalking = false;
            }
            default -> {
                foxFlagSitting = false;
                foxFlagSleeping = false;
                foxFlagWalking = steps > 0 || foxWander.limbPosDelta() > 0.01f;
            }
        }

        if (mode == 2) {
            applyPose(ent, finalPos, renderYaw, renderYaw, 0.0f, player.age, true, EntityPose.STANDING);
            ent.setVelocity(Vec3d.ZERO);
            tickAnimation(ent, nowMs, 2, 1.0f);
            applyPose(ent, finalPos, renderYaw, renderYaw, 0.0f, player.age, true, EntityPose.STANDING);
            driveLimbs(ent, 0.0f);
        } else if (mode == 1) {
            applyPose(ent, finalPos, bYaw, hYaw, 0.0f, player.age, true, EntityPose.SITTING);
            ent.setVelocity(Vec3d.ZERO);
            tickAnimation(ent, nowMs, 2, 1.0f);
            applyPose(ent, finalPos, bYaw, hYaw, 0.0f, player.age, true, EntityPose.SITTING);
            driveLimbs(ent, 0.0f);
        } else {
            applyPose(ent, finalPos, bYaw, hYaw, 0.0f, player.age, true, EntityPose.STANDING);
            tickAnimation(ent, nowMs, 2, 1.0f);
            applyPose(ent, finalPos, bYaw, hYaw, 0.0f, player.age, true, EntityPose.STANDING);
            for (int i = 0; i < steps; i++) {
                driveLimbs(ent, foxWander.limbPosDelta());
            }
        }

        draw(manager, queue, cameraState, matrices, ent, finalPos, pt, scale * 0.92f, eye, false);
    }

    /** Donor's FoxStateCompat.update: walk -> sit after 4.5 s idle -> sleep after 18 s idle. */
    private void updateFoxMode(boolean playerMoving, long nowMs) {
        if (foxLastMoveMs == 0L) {
            foxLastMoveMs = nowMs;
        }
        if (foxLastIdleMs == 0L) {
            foxLastIdleMs = nowMs;
        }
        if (playerMoving) {
            foxLastMoveMs = nowMs;
        } else {
            foxLastIdleMs = nowMs;
        }

        long idleFor = nowMs - foxLastMoveMs;
        long moveFor = nowMs - foxLastIdleMs;

        int want = idleFor > 18000L ? 2 : idleFor > 4500L ? 1 : 0;
        if (foxMode == 2 && playerMoving && moveFor > 650L) {
            want = 0;
        }
        if (foxMode == 1 && playerMoving && moveFor > 450L) {
            want = 0;
        }

        if (foxModeSinceMs == 0L) {
            foxModeSinceMs = nowMs;
        }
        long since = nowMs - foxModeSinceMs;
        if (want != foxMode) {
            long gate = foxMode == 2 ? 900L : 650L;
            if (since > gate) {
                foxMode = want;
                foxModeSinceMs = nowMs;
            }
        }
    }

    private static Direction sleepDir(float bodyYaw) {
        int qi = MathHelper.floor(bodyYaw * 4.0f / 360.0f + 0.5f) & 3;
        return switch (qi) {
            case 0 -> Direction.SOUTH;
            case 1 -> Direction.WEST;
            case 2 -> Direction.NORTH;
            default -> Direction.EAST;
        };
    }

    private static float dirYaw(Direction dir) {
        return switch (dir) {
            case SOUTH -> 0.0f;
            case WEST -> 90.0f;
            case NORTH -> 180.0f;
            case EAST -> 270.0f;
            default -> 0.0f;
        };
    }

    // ------------------------------------------------------------------
    // Shared render plumbing
    // ------------------------------------------------------------------

    private void draw(EntityRenderManager manager, OrderedRenderCommandQueue queue, CameraRenderState cameraState,
                      MatrixStack matrices, LivingEntity ent, Vec3d pos, float pt, float scale, Vec3d eye,
                      boolean raven) {
        matrices.push();
        matrices.translate(pos.x - eye.x, pos.y - eye.y, pos.z - eye.z);
        matrices.scale(scale, scale, scale);

        EntityRenderState state = manager.getAndUpdateRenderState(ent, pt);
        if (state != null) {
            patchState(state);
            if (raven) {
                submitRaven(queue, manager, matrices, ent, state,
                    RenderLayers.entityCutoutNoCull(VORON_TEXTURE), 0xFFFFFFFF);
            } else {
                // x/y/z are raw offsets added on top of the current matrix (vanilla subtracts the
                // camera position in its caller); the matrix is already camera-relative here.
                manager.render(state, cameraState, 0.0, 0.0, 0.0, matrices, queue);
            }
        }
        matrices.pop();
    }

    /** Raven skin: the parrot model with the donor's {@code voron.png} texture instead of vanilla. */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private void submitRaven(OrderedRenderCommandQueue queue, EntityRenderManager manager, MatrixStack matrices,
                             LivingEntity ent, EntityRenderState state, RenderLayer layer, int color) {
        EntityRenderer<?, ?> renderer = manager.getRenderer(ent);
        if (!(renderer instanceof LivingEntityRenderer livingRenderer)) {
            return;
        }
        Model model = livingRenderer.getModel();
        queue.submitModel(model, state, matrices, layer,
            LightmapTextureManager.MAX_LIGHT_COORDINATE, OverlayTexture.DEFAULT_UV, color, null);
    }

    /**
     * ULT ravens (hero + illusions): the raven skin's model, alpha-blended through the translucent
     * entity layer so the donor's per-bird fade-in / fade-out stays smooth.
     */
    void renderUltRaven(EntityRenderManager manager, OrderedRenderCommandQueue queue, MatrixStack matrices,
                        LivingEntity ent, Vec3d pos, float pt, float scale, Vec3d eye, float alpha) {
        matrices.push();
        matrices.translate(pos.x - eye.x, pos.y - eye.y, pos.z - eye.z);
        matrices.scale(scale, scale, scale);

        EntityRenderState state = manager.getAndUpdateRenderState(ent, pt);
        if (state != null) {
            patchState(state);
            int a = MathHelper.clamp(Math.round(alpha * 255.0f), 0, 255);
            submitRaven(queue, manager, matrices, ent, state,
                RenderLayers.entityTranslucent(VORON_TEXTURE), (a << 24) | 0xFFFFFF);
        }
        matrices.pop();
    }

    /** Return flight finished: pin the orbit state to the shoulder so normal flight continues seamlessly. */
    void snapFlyTo(Vec3d pos, float yaw, float pitch) {
        flyInited = true;
        flyPrevPos = pos;
        flyPos = pos;
        flyYaw = yaw;
        flyPitch = pitch;
    }

    private void patchState(EntityRenderState state) {
        state.invisible = false;
        state.onFire = false;
        state.displayName = null;
        state.nameLabelPos = null;
        state.light = LightmapTextureManager.MAX_LIGHT_COORDINATE;
        state.shadowRadius = 0.0f;
        state.shadowPieces.clear();
        state.outlineColor = EntityRenderState.NO_OUTLINE;
        if (state instanceof LivingEntityRenderState living) {
            living.hurt = false;
            living.invisibleToPlayer = false;
            living.deathTime = 0.0f;
        }
        patchSkinState(state);
    }

    /**
     * Poses the vanilla render state cannot read from the fake entity are patched directly. From
     * here on the render state fully determines what the model shows; the entity in the world
     * carries no visible state.
     */
    private void patchSkinState(EntityRenderState state) {
        switch (skin) {
            case BAT -> {
                if (state instanceof BatEntityRenderState bat) {
                    bat.roosting = false;
                }
            }
            case PARROT, RAVEN -> {
                if (state instanceof ParrotEntityRenderState parrot) {
                    parrot.variant = ParrotEntity.Variant.RED_BLUE;
                    parrot.parrotPose = ParrotEntityModel.Pose.FLYING;
                }
            }
            case FAIRY -> {
                if (state instanceof AllayEntityRenderState allay) {
                    allay.dancing = false;
                    allay.spinning = false;
                }
            }
            case BEE -> {
                if (state instanceof BeeEntityRenderState bee) {
                    bee.angry = false;
                    bee.stoppedOnGround = false;
                }
            }
            case VEX -> {
                if (state instanceof VexEntityRenderState vex) {
                    vex.charging = false;
                }
            }
            case SLIME -> {
                if (state instanceof SlimeEntityRenderState slime) {
                    slime.size = 1;
                }
            }
            case FOX -> {
                if (state instanceof FoxEntityRenderState fox) {
                    fox.type = FoxEntity.Variant.SNOW;
                    fox.sitting = foxFlagSitting;
                    fox.sleeping = foxFlagSleeping;
                    fox.sleepingDirection = foxFlagSleepDir;
                    fox.walking = foxFlagWalking;
                    fox.chasing = false;
                }
            }
            default -> {
            }
        }
    }

    static void applyPose(LivingEntity ent, Vec3d pos, float bodyYaw, float headYaw, float pitch,
                          int age, boolean onGround, EntityPose pose) {
        ent.setPos(pos.x, pos.y, pos.z);
        ent.lastX = pos.x;
        ent.lastY = pos.y;
        ent.lastZ = pos.z;
        ent.setYaw(bodyYaw);
        ent.lastYaw = bodyYaw;
        ent.bodyYaw = bodyYaw;
        ent.lastBodyYaw = bodyYaw;
        ent.headYaw = headYaw;
        ent.lastHeadYaw = headYaw;
        ent.setPitch(pitch);
        ent.lastPitch = pitch;
        ent.setOnGround(onGround);
        ent.setPose(pose);
        ent.age = age;
    }

    /**
     * Vanilla leg animation: {@code updateLimbs(min(dist * 4, 1), 0.4, 1.0)} once per simulated
     * tick; {@code posDeltaPerTick} is the horizontal step distance of one tick in blocks.
     */
    private static void driveLimbs(LivingEntity ent, float posDeltaPerTick) {
        float f = Math.min(posDeltaPerTick * 4.0f, 1.0f);
        ent.limbAnimator.updateLimbs(f, 0.4f, 1.0f);
    }

    /**
     * Advances the fake entity in fixed 50 ms steps like the donor did, so vanilla animation timers
     * keep running. A failing tick is logged once and freezes animations instead of spamming.
     */
    private void tickAnimation(LivingEntity ent, long nowMs, int capTicks, float mul) {
        if (animFailed) {
            return;
        }
        if (lastAnimMs == 0L) {
            lastAnimMs = nowMs;
        }
        long elapsed = nowMs - lastAnimMs;
        if (elapsed < 50L) {
            return;
        }
        long ticks = Math.min(capTicks, elapsed / 50L);
        int steps = Math.max(1, Math.min(10, (int) Math.floor(ticks * MathHelper.clamp(mul, 0.35f, 2.35f))));
        for (int i = 0; i < steps; i++) {
            ent.noClip = true;
            try {
                ent.tick();
            } catch (RuntimeException failed) {
                animFailed = true;
                LinkenVisuals.LOGGER.warn("Pet animation tick failed; animations frozen: {}",
                    failed.toString());
                break;
            }
        }
        lastAnimMs += ticks * 50L;
    }

    // ------------------------------------------------------------------
    // Small math helpers (donor parity)
    // ------------------------------------------------------------------

    private static float lerpAngle(float a, float b, float t) {
        return a + MathHelper.wrapDegrees(b - a) * MathHelper.clamp(t, 0.0f, 1.0f);
    }

    private static Vec3d lerpVec(Vec3d a, Vec3d b, float t) {
        return new Vec3d(a.x + (b.x - a.x) * t, a.y + (b.y - a.y) * t, a.z + (b.z - a.z) * t);
    }

    private static float easeInOut(float x) {
        return x * x * (3.0f - 2.0f * x);
    }

    private static float wrapRad(float r) {
        float tau = (float) (Math.PI * 2.0);
        r = r % tau;
        if (r > Math.PI) {
            r -= tau;
        }
        if (r < -Math.PI) {
            r += tau;
        }
        return r;
    }
}
