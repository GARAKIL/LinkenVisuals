package dev.linkenvisuals.render;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.linkenvisuals.module.Modules;
import dev.linkenvisuals.module.impl.CustomHitBoxModule;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.OptionalInt;

/**
 * CustomHitBox renderer: donor fill/outline boxes for visible entities at
 * {@code END_MAIN}. Reuses the flat world pipeline; no global render state is
 * changed. Visibility is block-raycast against the hitbox centre and fallback
 * points, cached for 120 ms.
 */
public final class CustomHitBoxRenderer {
    private static final Map<Integer, long[]> VISIBILITY_CACHE = new HashMap<>();
    private static boolean lastRenderVisible;

    private CustomHitBoxRenderer() {
    }

    public static void clear() {
        VISIBILITY_CACHE.clear();
        lastRenderVisible = false;
    }

    /** Autotest probe: true when the last frame submitted at least one box. */
    public static boolean lastRenderVisible() {
        return lastRenderVisible;
    }

    public static void render(WorldRenderContext context) {
        lastRenderVisible = false;
        CustomHitBoxModule module = Modules.customHitBox();
        if (module == null || !module.enabled()) {
            VISIBILITY_CACHE.clear();
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        Camera camera = client.gameRenderer.getCamera();
        Framebuffer main = client.getFramebuffer();
        if (client.player == null || client.world == null || camera == null || main == null
            || main.getColorAttachmentView() == null || main.getDepthAttachmentView() == null) {
            return;
        }

        float tickDelta = client.getRenderTickCounter().getTickProgress(true);
        int fillAlpha = CustomHitBoxRules.effectiveFillAlpha(module.fillAlpha.value(), 255);
        List<Box> boxes = collectBoxes(client, module, tickDelta);
        if (boxes.isEmpty()) {
            return;
        }

        int fillVertices = module.fill.value() && fillAlpha > 0
            ? boxes.size() * CustomHitBoxGeometry.filledVertexCount() : 0;
        int outlineVertices = boxes.size() * CustomHitBoxGeometry.outlineVertexCount();
        int totalVertices = fillVertices + outlineVertices;
        Vec3d cam = camera.getCameraPos();
        int rgb = module.color.rgb() & 0xFFFFFF;
        int fillColor = fillAlpha << 24 | rgb;
        int lineColor = 0xFF000000 | rgb;

        ByteBuffer data = MemoryUtil.memAlloc(
            totalVertices * CustomHitBoxGeometry.VERTEX_BYTES);
        try {
            if (fillVertices > 0) {
                for (Box box : boxes) {
                    CustomHitBoxGeometry.appendFilledBox(data, box, cam.x, cam.y, cam.z, fillColor);
                }
            }
            for (Box box : boxes) {
                CustomHitBoxGeometry.appendOutline(data, box, cam.x, cam.y, cam.z,
                    module.lineWidth.value(), lineColor);
            }
            data.flip();

            CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
            GpuBuffer buffer = RenderSystem.getDevice().createBuffer(
                () -> "linkenvisuals:custom_hitbox", GpuBuffer.USAGE_VERTEX, data);
            GpuBufferSlice transforms = Pipelines.writeWorldTransforms();
            try (RenderPass pass = encoder.createRenderPass(() -> "linkenvisuals:custom_hitbox",
                main.getColorAttachmentView(), OptionalInt.empty(),
                main.getDepthAttachmentView(), OptionalDouble.empty())) {
                pass.setPipeline(BlockOverlayPipelines.FLAT);
                Pipelines.bindWorldUniforms(pass, transforms);
                pass.setVertexBuffer(0, buffer);
                if (fillVertices > 0) {
                    pass.draw(0, fillVertices);
                }
                pass.draw(fillVertices, outlineVertices);
            } finally {
                buffer.close();
            }
            lastRenderVisible = true;
        } finally {
            MemoryUtil.memFree(data);
        }
    }

    private static List<Box> collectBoxes(MinecraftClient client, CustomHitBoxModule module,
                                          float tickDelta) {
        List<Box> boxes = new ArrayList<>();
        boolean firstPerson = client.options.getPerspective().isFirstPerson();
        Frustum frustum = client.worldRenderer.getCapturedFrustum();
        for (Entity entity : client.world.getEntities()) {
            boolean self = entity == client.player;
            if (self && (!module.showSelf.value() || firstPerson)) {
                continue;
            }
            if (entity instanceof ItemEntity && !module.showItems.value()) {
                continue;
            }
            if (entity instanceof ExperienceOrbEntity) {
                continue;
            }
            if (!entity.isAlive() || entity.isRemoved() || entity.isInvisible()) {
                continue;
            }
            if (entity instanceof LivingEntity living
                && living.hasStatusEffect(StatusEffects.INVISIBILITY)) {
                continue;
            }
            if (!CustomHitBoxRules.withinRenderDistance(client.player.squaredDistanceTo(entity))) {
                continue;
            }
            Vec3d lerped = entity.getLerpedPos(tickDelta);
            Box hitbox = entity.getBoundingBox()
                .offset(lerped.subtract(entity.getEntityPos()));
            if (frustum != null && !frustum.isVisible(hitbox)) {
                continue;
            }
            if (!isVisibleToPlayer(client, entity, hitbox, tickDelta)) {
                continue;
            }
            boxes.add(hitbox);
        }
        return boxes;
    }

    private static boolean isVisibleToPlayer(MinecraftClient client, Entity entity, Box hitbox,
                                             float tickDelta) {
        long now = System.currentTimeMillis();
        long[] cached = VISIBILITY_CACHE.get(entity.getId());
        if (cached != null && now < cached[0]) {
            return cached[1] != 0;
        }
        Vec3d eye = client.player.getCameraPosVec(tickDelta);
        boolean visible = computeVisibility(client.world, client.player, eye, hitbox);
        if (VISIBILITY_CACHE.size() > 512) {
            VISIBILITY_CACHE.clear();
        }
        VISIBILITY_CACHE.put(entity.getId(),
            new long[]{now + CustomHitBoxRules.VISIBILITY_CACHE_MS, visible ? 1 : 0});
        return visible;
    }

    private static boolean computeVisibility(World world, LivingEntity player, Vec3d eye, Box hitbox) {
        if (hasLineOfSight(world, player, eye, hitbox.getCenter())) {
            return true;
        }
        for (Vec3d point : visibilityPoints(hitbox)) {
            if (hasLineOfSight(world, player, eye, point)) {
                return true;
            }
        }
        return false;
    }

    private static Vec3d[] visibilityPoints(Box hitbox) {
        double centerX = (hitbox.minX + hitbox.maxX) * 0.5;
        double centerY = (hitbox.minY + hitbox.maxY) * 0.5;
        double centerZ = (hitbox.minZ + hitbox.maxZ) * 0.5;
        double insetX = Math.max(0.0, (hitbox.maxX - hitbox.minX) * 0.08);
        double insetZ = Math.max(0.0, (hitbox.maxZ - hitbox.minZ) * 0.08);
        double minX = hitbox.minX + insetX;
        double maxX = hitbox.maxX - insetX;
        double minZ = hitbox.minZ + insetZ;
        double maxZ = hitbox.maxZ - insetZ;
        double minY = hitbox.minY + 0.05;
        double maxY = hitbox.maxY - 0.05;
        return new Vec3d[]{
            new Vec3d(centerX, maxY, centerZ),
            new Vec3d(centerX, minY, centerZ),
            new Vec3d(minX, maxY, minZ),
            new Vec3d(minX, maxY, maxZ),
            new Vec3d(maxX, maxY, minZ),
            new Vec3d(maxX, maxY, maxZ),
            new Vec3d(minX, centerY, centerZ),
            new Vec3d(maxX, centerY, centerZ),
            new Vec3d(centerX, centerY, minZ),
            new Vec3d(centerX, centerY, maxZ)
        };
    }

    private static boolean hasLineOfSight(World world, LivingEntity player, Vec3d eye, Vec3d target) {
        if (world == null) {
            return false;
        }
        BlockHitResult hit = world.raycast(new RaycastContext(
            eye, target, RaycastContext.ShapeType.COLLIDER,
            RaycastContext.FluidHandling.NONE, player));
        if (hit.getType() == HitResult.Type.MISS) {
            return true;
        }
        if (hit.getType() == HitResult.Type.BLOCK) {
            double hitDistance = hit.getPos().squaredDistanceTo(eye);
            double targetDistance = target.squaredDistanceTo(eye);
            return hitDistance >= targetDistance - 1.0e-3;
        }
        return true;
    }

    public static void init() {
    }
}
