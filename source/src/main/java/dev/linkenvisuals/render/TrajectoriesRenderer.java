package dev.linkenvisuals.render;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.linkenvisuals.module.Modules;
import dev.linkenvisuals.module.impl.TrajectoriesModule;
import dev.linkenvisuals.util.trajectories.TrajectoryRules;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.render.Camera;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.EggItem;
import net.minecraft.item.EnderPearlItem;
import net.minecraft.item.ExperienceBottleItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.LingeringPotionItem;
import net.minecraft.item.SnowballItem;
import net.minecraft.item.SplashPotionItem;
import net.minecraft.item.TridentItem;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.enchantment.Enchantment;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;

/**
 * Donor Trajectories: projectile simulation, oriented hit marker and optional
 * explosive radius. Server boss-bar heuristics are not ported; item names and
 * status effects still provide the donor explosive keywords.
 */
public final class TrajectoriesRenderer {
    private static final Map<Float, Impact> CACHE = new HashMap<>();

    private static int cacheAge = -1;
    private static float cacheYaw = Float.NaN;
    private static float cachePitch = Float.NaN;
    private static Item cacheItem;
    private static boolean explosiveCached;
    private static long explosiveExpires;
    private static Item explosiveItem;
    private static boolean lastRenderVisible;

    private TrajectoriesRenderer() {
    }

    public static void clear() {
        CACHE.clear();
        cacheAge = -1;
        cacheYaw = Float.NaN;
        cachePitch = Float.NaN;
        cacheItem = null;
        explosiveCached = false;
        explosiveExpires = 0L;
        explosiveItem = null;
        lastRenderVisible = false;
    }

    public static boolean lastRenderVisible() {
        return lastRenderVisible;
    }

    public static void render(WorldRenderContext context) {
        lastRenderVisible = false;
        TrajectoriesModule module = Modules.trajectories();
        if (module == null || !module.enabled()) {
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null
            || client.options.hudHidden
            || !client.options.getPerspective().isFirstPerson()) {
            return;
        }
        Hand hand = validHand(client);
        if (hand == null) {
            return;
        }
        ItemStack stack = hand == Hand.MAIN_HAND
            ? client.player.getMainHandStack() : client.player.getOffHandStack();
        Item item = stack.getItem();
        float tickDelta = client.getRenderTickCounter().getTickProgress(true);
        Vec3d eye = client.player.getLerpedPos(tickDelta)
            .add(0.0, client.player.getEyeHeight(client.player.getPose()) - 0.1, 0.0);
        float yaw = client.player.getYaw(tickDelta);
        float pitch = client.player.getPitch(tickDelta);

        if (item instanceof BowItem) {
            if (!client.player.isUsingItem() || client.player.getActiveItem().getItem() != item) {
                return;
            }
            simulateAndDraw(client, module, stack, item, 0.0f, yaw, pitch, tickDelta, eye);
        } else if (item instanceof CrossbowItem && hasMultishot(stack)) {
            simulateAndDraw(client, module, stack, item, -10.0f, yaw, pitch, tickDelta, eye);
            simulateAndDraw(client, module, stack, item, 0.0f, yaw, pitch, tickDelta, eye);
            simulateAndDraw(client, module, stack, item, 10.0f, yaw, pitch, tickDelta, eye);
        } else {
            simulateAndDraw(client, module, stack, item, 0.0f, yaw, pitch, tickDelta, eye);
        }
    }

    private static Hand validHand(MinecraftClient client) {
        if (validProjectile(client.player.getMainHandStack().getItem())) {
            return Hand.MAIN_HAND;
        }
        if (validProjectile(client.player.getOffHandStack().getItem())) {
            return Hand.OFF_HAND;
        }
        return null;
    }

    private static boolean validProjectile(Item item) {
        return item instanceof EnderPearlItem
            || item instanceof TridentItem
            || item instanceof CrossbowItem
            || item instanceof BowItem
            || item instanceof SplashPotionItem
            || item instanceof LingeringPotionItem
            || item instanceof ExperienceBottleItem
            || item instanceof SnowballItem
            || item instanceof EggItem;
    }

    private static void simulateAndDraw(MinecraftClient client, TrajectoriesModule module,
                                        ItemStack stack, Item item, float yawOffset,
                                        float rawYaw, float pitch, float tickDelta, Vec3d eye) {
        boolean cacheValid = client.player.age == cacheAge
            && item == cacheItem
            && Math.abs(MathHelper.wrapDegrees(rawYaw - cacheYaw)) <= 0.1f
            && Math.abs(pitch - cachePitch) <= 0.1f;
        if (!cacheValid) {
            CACHE.clear();
            cacheAge = client.player.age;
            cacheItem = item;
            cacheYaw = rawYaw;
            cachePitch = pitch;
        }
        Impact impact = CACHE.get(yawOffset);
        if (impact == null) {
            impact = simulate(client, item, rawYaw + yawOffset, pitch, tickDelta, eye);
            CACHE.put(yawOffset, impact);
        }
        if (impact.point == null || impact.side == null) {
            return;
        }
        renderMarker(client, module, impact.point, impact.side);
        if (module.explosiveCircle.value() && hasExplosiveBuff(client, stack)) {
            renderExplosiveRadius(client, module, impact.point, impact.side);
        }
    }

    private static Impact simulate(MinecraftClient client, Item item, float yaw, float pitch,
                                   float tickDelta, Vec3d eye) {
        float distance = TrajectoryRules.initialDistance(item instanceof BowItem);
        double motionX = -MathHelper.sin(yaw * 0.017453292f)
            * MathHelper.cos(pitch * 0.017453292f) * distance;
        double motionY = -MathHelper.sin((pitch - throwPitch(item)) * 0.017453292f) * distance;
        double motionZ = MathHelper.cos(yaw * 0.017453292f)
            * MathHelper.cos(pitch * 0.017453292f) * distance;
        float power = item instanceof BowItem
            ? TrajectoryRules.bowPower(client.player.getItemUseTime()) : 1.0f;
        double length = Math.sqrt(motionX * motionX + motionY * motionY + motionZ * motionZ);
        if (length == 0.0) {
            return new Impact(null, null);
        }
        motionX /= length;
        motionY /= length;
        motionZ /= length;
        float velocity = (item instanceof BowItem ? power * 2.0f
            : item instanceof CrossbowItem ? 2.2f : 1.0f) * throwVelocity(item);
        motionX *= velocity;
        motionY *= velocity;
        motionZ *= velocity;
        Vec3d playerVelocity = client.player.getVelocity();
        motionX += playerVelocity.x;
        motionY += playerVelocity.y;
        motionZ += playerVelocity.z;

        double x = eye.x;
        double y = eye.y;
        double z = eye.z;
        boolean bowOrCrossbow = item instanceof BowItem || item instanceof CrossbowItem;
        for (int step = 0; step < 300; step++) {
            Vec3d current = new Vec3d(x, y, z);
            x += motionX;
            y += motionY;
            z += motionZ;
            boolean water = client.world.getBlockState(BlockPos.ofFloored(x, y, z)).isOf(Blocks.WATER);
            Vec3d stepped = TrajectoryRules.step(new Vec3d(motionX, motionY, motionZ),
                water, bowOrCrossbow);
            motionX = stepped.x;
            motionY = stepped.y;
            motionZ = stepped.z;
            Vec3d next = new Vec3d(x, y, z);
            BlockHitResult hit = client.world.raycast(new RaycastContext(
                current, next, RaycastContext.ShapeType.OUTLINE,
                RaycastContext.FluidHandling.NONE, client.player));
            if (hit != null && hit.getType() == HitResult.Type.BLOCK) {
                return new Impact(TrajectoryRules.impactPosition(hit.getPos(), hit.getSide()),
                    hit.getSide());
            }
            if (y <= client.world.getBottomY()) {
                break;
            }
        }
        return new Impact(null, null);
    }

    private static float throwVelocity(Item item) {
        String type = item == Items.SPLASH_POTION ? "splash"
            : item == Items.LINGERING_POTION ? "lingering"
            : item == Items.EXPERIENCE_BOTTLE ? "experience_bottle"
            : item instanceof TridentItem ? "trident" : "other";
        return TrajectoryRules.throwVelocity(type);
    }

    private static float throwPitch(Item item) {
        String type = item == Items.SPLASH_POTION ? "splash"
            : item == Items.LINGERING_POTION ? "lingering"
            : item == Items.EXPERIENCE_BOTTLE ? "experience_bottle" : "other";
        return TrajectoryRules.throwPitch(type);
    }

    private static boolean hasMultishot(ItemStack stack) {
        try {
            ItemEnchantmentsComponent enchantments = stack.getEnchantments();
            for (RegistryEntry<Enchantment> entry : enchantments.getEnchantments()) {
                if (entry.getKey().equals(Optional.of(Enchantments.MULTISHOT))) {
                    return enchantments.getLevel(entry) > 0;
                }
            }
        } catch (RuntimeException ignored) {
            // Enchantment components are optional; a malformed stack simply has no multishot.
        }
        return false;
    }

    private static boolean hasExplosiveBuff(MinecraftClient client, ItemStack stack) {
        long now = System.currentTimeMillis();
        if (stack.getItem() != explosiveItem || now >= explosiveExpires) {
            explosiveCached = hasExplosiveBuffUncached(client, stack);
            explosiveItem = stack.getItem();
            explosiveExpires = now + 200L;
        }
        return explosiveCached;
    }

    private static boolean hasExplosiveBuffUncached(MinecraftClient client, ItemStack stack) {
        if (TrajectoryRules.explosiveKeyword(stack.getName().getString())) {
            return true;
        }
        for (StatusEffectInstance effect : client.player.getStatusEffects()) {
            if (TrajectoryRules.explosiveKeyword(
                effect.getEffectType().value().getName().getString())) {
                return true;
            }
        }
        return false;
    }

    private static void renderMarker(MinecraftClient client, TrajectoriesModule module,
                                     Vec3d point, Direction side) {
        Framebuffer main = client.getFramebuffer();
        Camera camera = client.gameRenderer.getCamera();
        if (main == null || main.getColorAttachmentView() == null
            || main.getDepthAttachmentView() == null || camera == null) {
            return;
        }
        Vec3d cam = camera.getCameraPos();
        Vec3d right = TrajectoryRules.faceRight(side);
        Vec3d up = TrajectoryRules.faceUp(side);
        float x = (float) (point.x - cam.x);
        float y = (float) (point.y - cam.y);
        float z = (float) (point.z - cam.z);
        int color = 0xFF000000 | (module.color.rgb() & 0xFFFFFF);
        int sides = 72;
        int vertices = sides * 6 + 4 * 6;
        ByteBuffer data = MemoryUtil.memAlloc(vertices * 16);
        try {
            float radius = 0.34f;
            float halfWidth = 0.025f;
            for (int i = 0; i < sides; i++) {
                double a0 = Math.PI * 2.0 * i / sides;
                double a1 = Math.PI * 2.0 * (i + 1) / sides;
                float c0 = (float) Math.cos(a0);
                float s0 = (float) Math.sin(a0);
                float c1 = (float) Math.cos(a1);
                float s1 = (float) Math.sin(a1);
                putColored(data, color, x + (float) (right.x * c0 * (radius + halfWidth) + up.x * s0 * (radius + halfWidth)),
                    y + (float) (right.y * c0 * (radius + halfWidth) + up.y * s0 * (radius + halfWidth)),
                    z + (float) (right.z * c0 * (radius + halfWidth) + up.z * s0 * (radius + halfWidth)));
                putColored(data, color, x + (float) (right.x * c0 * (radius - halfWidth) + up.x * s0 * (radius - halfWidth)),
                    y + (float) (right.y * c0 * (radius - halfWidth) + up.y * s0 * (radius - halfWidth)),
                    z + (float) (right.z * c0 * (radius - halfWidth) + up.z * s0 * (radius - halfWidth)));
                putColored(data, color, x + (float) (right.x * c1 * (radius + halfWidth) + up.x * s1 * (radius + halfWidth)),
                    y + (float) (right.y * c1 * (radius + halfWidth) + up.y * s1 * (radius + halfWidth)),
                    z + (float) (right.z * c1 * (radius + halfWidth) + up.z * s1 * (radius + halfWidth)));
                putColored(data, color, x + (float) (right.x * c0 * (radius - halfWidth) + up.x * s0 * (radius - halfWidth)),
                    y + (float) (right.y * c0 * (radius - halfWidth) + up.y * s0 * (radius - halfWidth)),
                    z + (float) (right.z * c0 * (radius - halfWidth) + up.z * s0 * (radius - halfWidth)));
                putColored(data, color, x + (float) (right.x * c1 * (radius - halfWidth) + up.x * s1 * (radius - halfWidth)),
                    y + (float) (right.y * c1 * (radius - halfWidth) + up.y * s1 * (radius - halfWidth)),
                    z + (float) (right.z * c1 * (radius - halfWidth) + up.z * s1 * (radius - halfWidth)));
                putColored(data, color, x + (float) (right.x * c1 * (radius + halfWidth) + up.x * s1 * (radius + halfWidth)),
                    y + (float) (right.y * c1 * (radius + halfWidth) + up.y * s1 * (radius + halfWidth)),
                    z + (float) (right.z * c1 * (radius + halfWidth) + up.z * s1 * (radius + halfWidth)));
            }
            for (int i = 0; i < 4; i++) {
                double angle = Math.PI * 0.5 * i;
                float dx = (float) Math.cos(angle);
                float dy = (float) Math.sin(angle);
                float px = (float) (right.x * dx + up.x * dy);
                float py = (float) (right.y * dx + up.y * dy);
                float pz = (float) (right.z * dx + up.z * dy);
                float tx = (float) (-right.x * dy + up.x * dx);
                float ty = (float) (-right.y * dy + up.y * dx);
                float tz = (float) (-right.z * dy + up.z * dx);
                float inner = radius - 0.035f;
                float outer = radius + 0.085f;
                float half = 0.035f;
                putColored(data, color, x + px * inner + tx * half, y + py * inner + ty * half, z + pz * inner + tz * half);
                putColored(data, color, x + px * inner - tx * half, y + py * inner - ty * half, z + pz * inner - tz * half);
                putColored(data, color, x + px * outer + tx * half, y + py * outer + ty * half, z + pz * outer + tz * half);
                putColored(data, color, x + px * inner - tx * half, y + py * inner - ty * half, z + pz * inner - tz * half);
                putColored(data, color, x + px * outer - tx * half, y + py * outer - ty * half, z + pz * outer - tz * half);
                putColored(data, color, x + px * outer + tx * half, y + py * outer + ty * half, z + pz * outer + tz * half);
            }
            data.flip();
            GpuBuffer buffer = RenderSystem.getDevice().createBuffer(
                () -> "linkenvisuals:trajectory_marker", GpuBuffer.USAGE_VERTEX, data);
            GpuBufferSlice transforms = Pipelines.writeWorldTransforms();
            CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
            try (RenderPass pass = encoder.createRenderPass(() -> "linkenvisuals:trajectory_marker",
                main.getColorAttachmentView(), OptionalInt.empty(),
                main.getDepthAttachmentView(), OptionalDouble.empty())) {
                pass.setPipeline(BlockOverlayPipelines.FLAT);
                Pipelines.bindWorldUniforms(pass, transforms);
                pass.setVertexBuffer(0, buffer);
                pass.draw(0, vertices);
            } finally {
                buffer.close();
            }
            lastRenderVisible = true;
        } finally {
            MemoryUtil.memFree(data);
        }
    }

    private static void renderExplosiveRadius(MinecraftClient client, TrajectoriesModule module,
                                              Vec3d point, Direction side) {
        Framebuffer main = client.getFramebuffer();
        Camera camera = client.gameRenderer.getCamera();
        if (main == null || main.getColorAttachmentView() == null
            || main.getDepthAttachmentView() == null || camera == null) {
            return;
        }
        Vec3d cam = camera.getCameraPos();
        float y = (float) (point.y + (side == Direction.UP ? 0.02 : 0.01) - cam.y);
        int argb = 120 << 24 | (module.color.rgb() & 0xFFFFFF);
        int sides = HitRangeGeometry.SIDES;
        int vertices = HitRangeGeometry.bandVertexCount(sides);
        ByteBuffer data = MemoryUtil.memAlloc(vertices * 16);
        try {
            HitRangeGeometry.appendBand(data, (float) (point.x - cam.x), y,
                (float) (point.z - cam.z), (float) TrajectoryRules.EXPLOSIVE_RADIUS,
                0.03f, 0.0, sides, argb);
            data.flip();
            GpuBuffer buffer = RenderSystem.getDevice().createBuffer(
                () -> "linkenvisuals:trajectory_radius", GpuBuffer.USAGE_VERTEX, data);
            GpuBufferSlice transforms = Pipelines.writeWorldTransforms();
            CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
            try (RenderPass pass = encoder.createRenderPass(() -> "linkenvisuals:trajectory_radius",
                main.getColorAttachmentView(), OptionalInt.empty(),
                main.getDepthAttachmentView(), OptionalDouble.empty())) {
                pass.setPipeline(BlockOverlayPipelines.FLAT);
                Pipelines.bindWorldUniforms(pass, transforms);
                pass.setVertexBuffer(0, buffer);
                pass.draw(0, vertices);
            } finally {
                buffer.close();
            }
        } finally {
            MemoryUtil.memFree(data);
        }
    }

    private static void putColored(ByteBuffer data, int argb, float x, float y, float z) {
        data.putFloat(x).putFloat(y).putFloat(z);
        data.put((byte) (argb >> 16 & 0xFF)).put((byte) (argb >> 8 & 0xFF))
            .put((byte) (argb & 0xFF)).put((byte) (argb >>> 24));
    }

    public static void init() {
    }

    private record Impact(Vec3d point, Direction side) {
    }
}
