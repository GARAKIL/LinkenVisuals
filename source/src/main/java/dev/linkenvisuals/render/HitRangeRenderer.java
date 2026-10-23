package dev.linkenvisuals.render;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.linkenvisuals.module.Modules;
import dev.linkenvisuals.module.impl.HitRangeModule;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.OptionalDouble;
import java.util.OptionalInt;

/**
 * HitRange renderer: donor rotating ring at the vanilla hit radius. Drawn at
 * {@code END_MAIN} through the proven flat world pipeline as a triangle-list
 * ribbon, so both the glow and the core band have real geometric width.
 */
public final class HitRangeRenderer {
    private static final int BASE_ALPHA = 200;
    private static final int NEAR_ALPHA = 220;
    private static final int NEAR_RGB = 0xFF2D2D;
    private static final float GLOW_MULTIPLIER = 2.6f;
    private static final float GLOW_ALPHA = 0.22f;

    private static float rotationAngle;
    private static boolean lastRenderVisible;

    private HitRangeRenderer() {
    }

    /** Resets animation state (disconnect/resource reload). */
    public static void clear() {
        rotationAngle = 0.0f;
        lastRenderVisible = false;
    }

    /** Autotest probe: true when the last frame actually submitted ring geometry. */
    public static boolean lastRenderVisible() {
        return lastRenderVisible;
    }

    public static void render(WorldRenderContext context) {
        lastRenderVisible = false;
        HitRangeModule module = Modules.hitRange();
        if (module == null || !module.enabled()) {
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        Camera camera = client.gameRenderer.getCamera();
        Framebuffer main = client.getFramebuffer();
        if (client.player == null || client.world == null || camera == null || main == null
            || main.getColorAttachmentView() == null || main.getDepthAttachmentView() == null) {
            return;
        }

        boolean enemyNear = isEnemyNear(client);
        rotationAngle = HitRangeGeometry.advanceRotation(rotationAngle, module.rotationSpeed.value());

        float tickDelta = client.getRenderTickCounter().getTickProgress(true);
        Vec3d origin = client.player.getLerpedPos(tickDelta);
        Vec3d cam = camera.getCameraPos();
        float cx = (float) (origin.x - cam.x);
        float cy = (float) (origin.y + HitRangeGeometry.Y_OFFSET - cam.y);
        float cz = (float) (origin.z - cam.z);

        int rgb = enemyNear ? NEAR_RGB : (module.color.rgb() & 0xFFFFFF);
        int alpha = enemyNear ? NEAR_ALPHA : BASE_ALPHA;
        int base = alpha << 24 | rgb;
        int glowAlpha = Math.max(0, Math.round(alpha * GLOW_ALPHA));
        int glow = glowAlpha << 24 | rgb;
        float halfWidth = module.thickness.value() * 0.045f;
        double rotation = Math.toRadians(rotationAngle);

        int sides = HitRangeGeometry.SIDES;
        int vertices = HitRangeGeometry.bandVertexCount(sides) * 2;
        ByteBuffer data = MemoryUtil.memAlloc(vertices * HitRangeGeometry.VERTEX_BYTES);
        try {
            HitRangeGeometry.appendBand(data, cx, cy, cz, HitRangeGeometry.HIT_RADIUS,
                halfWidth * GLOW_MULTIPLIER, rotation, sides, glow);
            HitRangeGeometry.appendBand(data, cx, cy, cz, HitRangeGeometry.HIT_RADIUS,
                halfWidth, rotation, sides, base);
            data.flip();

            RenderPipeline pipeline = module.depthTest.value()
                ? BlockOverlayPipelines.FLAT
                : BlockOverlayPipelines.FLAT_NO_DEPTH;
            CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
            GpuBuffer buffer = RenderSystem.getDevice().createBuffer(
                () -> "linkenvisuals:hit_range", GpuBuffer.USAGE_VERTEX, data);
            GpuBufferSlice transforms = Pipelines.writeWorldTransforms();
            try (RenderPass pass = encoder.createRenderPass(() -> "linkenvisuals:hit_range",
                main.getColorAttachmentView(), OptionalInt.empty(),
                main.getDepthAttachmentView(), OptionalDouble.empty())) {
                pass.setPipeline(pipeline);
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

    /** Mirrors the donor: nearby, visible, non-spectator players turn the ring red. */
    private static boolean isEnemyNear(MinecraftClient client) {
        PlayerEntity self = client.player;
        if (self == null || client.world == null) {
            return false;
        }
        double radiusSq = (double) HitRangeGeometry.HIT_RADIUS * HitRangeGeometry.HIT_RADIUS;
        for (PlayerEntity player : client.world.getPlayers()) {
            if (player == self || player.isInvisible() || player.isSpectator()) {
                continue;
            }
            if (player.squaredDistanceTo(self) <= radiusSq) {
                return true;
            }
        }
        return false;
    }

    public static void init() {
    }
}
