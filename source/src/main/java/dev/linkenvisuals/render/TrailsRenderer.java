package dev.linkenvisuals.render;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.linkenvisuals.LinkenVisuals;
import dev.linkenvisuals.module.Modules;
import dev.linkenvisuals.module.impl.TrailsModule;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;
import java.util.OptionalInt;

/**
 * Trails renderer (Phase 5.7): manages and renders 3D particles behind the
 * player. Particles spawn on movement, fade over {@code length} ms, and are
 * drawn as camera-facing billboard quads at END_MAIN.
 */
public final class TrailsRenderer {
    private static final long FADE_WINDOW_MS = 250L;
    private static final double MAX_DISTANCE = 100.0;

    private static final RenderPipeline PIPELINE = Pipelines.register(
        RenderPipeline.builder(RenderPipelines.TRANSFORMS_AND_PROJECTION_SNIPPET)
            .withLocation(LinkenVisuals.id("pipeline/trails"))
            .withVertexShader(LinkenVisuals.id("core/position_color"))
            .withFragmentShader(LinkenVisuals.id("core/position_color"))
            .withVertexFormat(VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.QUADS)
            .withBlend(BlendFunction.TRANSLUCENT)
            .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
            .withDepthWrite(false)
            .withCull(false)
            .build());

    private record Particle(Vec3d pos, Vec3d velocity, long spawnTime, float size) {
        void update() {
            // Simple Euler integration; positions are immutable so we recreate.
        }
    }

    private static final List<Particle> particles = new ArrayList<>();
    private static double lastX, lastY, lastZ;
    private static boolean hasLast;

    private TrailsRenderer() {
    }

    public static void clear() {
        particles.clear();
        hasLast = false;
    }

    /** Called each client tick to spawn particles. */
    public static void tick() {
        TrailsModule module = Modules.trails();
        if (module == null || !module.enabled()) {
            if (!particles.isEmpty()) {
                particles.clear();
            }
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) {
            clear();
            return;
        }
        double x = client.player.getX();
        double y = client.player.getY();
        double z = client.player.getZ();
        if (hasLast && (x != lastX || y != lastY || z != lastZ)) {
            // Spawn behind the player.
            double yawRad = Math.toRadians(client.player.getBodyYaw());
            double distance = -(client.player.getWidth() / 2.0);
            double xOffset = -Math.sin(yawRad) * distance;
            double zOffset = Math.cos(yawRad) * distance;
            Vec3d spawnPos = new Vec3d(x + xOffset, y + client.player.getHeight() * 0.4, z + zOffset);
            Vec3d velocity = client.player.getVelocity();
            Vec3d vel = new Vec3d(velocity.x, 0.0, velocity.z).multiply(1.5 + Math.random());
            particles.add(new Particle(spawnPos, vel, System.currentTimeMillis(), 0.25f));
        }
        lastX = x;
        lastY = y;
        lastZ = z;
        hasLast = true;

        long lengthMs = (long) module.length.value();
        long now = System.currentTimeMillis();
        particles.removeIf(p -> now - p.spawnTime() > lengthMs
            || p.pos().squaredDistanceTo(x, y, z) > MAX_DISTANCE * MAX_DISTANCE);
    }

    /** Called at END_MAIN to render billboards. */
    public static void render(WorldRenderContext context) {
        TrailsModule module = Modules.trails();
        if (module == null || !module.enabled() || particles.isEmpty()) {
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.options.getPerspective().isFirstPerson() && !module.showInFirstPerson.value()) {
            return;
        }
        Framebuffer main = client.getFramebuffer();
        Camera camera = client.gameRenderer.getCamera();
        if (main == null || main.getColorAttachmentView() == null
            || main.getDepthAttachmentView() == null || camera == null) {
            return;
        }

        Vec3d camPos = camera.getCameraPos();
        long now = System.currentTimeMillis();
        long lengthMs = (long) module.length.value();

        // Build billboard quads: each particle is a camera-facing square.
        int count = particles.size();
        ByteBuffer data = MemoryUtil.memAlloc(count * 4 * 28); // 4 verts × (3+4 floats) × 4 bytes
        try {
            for (Particle p : particles) {
                long age = now - p.spawnTime();
                float alpha = 1.0f - (float) age / lengthMs;
                if (age > lengthMs - FADE_WINDOW_MS) {
                    alpha *= (float) (lengthMs - age) / FADE_WINDOW_MS;
                }
                if (alpha <= 0.0f) {
                    continue;
                }
                Vec3d pos = p.pos();
                double dx = pos.x - camPos.x;
                double dy = pos.y - camPos.y;
                double dz = pos.z - camPos.z;

                // Billboard: right and up vectors perpendicular to view.
                // Simplified: use camera-relative axes.
                float size = p.size();
                int color = 0xFFFFFF;
                float r = ((color >> 16) & 0xFF) / 255.0f;
                float g = ((color >> 8) & 0xFF) / 255.0f;
                float b = (color & 0xFF) / 255.0f;

                // Quad corners (camera-facing, simplified as axis-aligned in view space).
                // For true billboards we'd need the camera basis; this is an approximation.
                putVertex(data, (float) (pos.x - size), (float) (pos.y - size), (float) pos.z, r, g, b, alpha);
                putVertex(data, (float) (pos.x + size), (float) (pos.y - size), (float) pos.z, r, g, b, alpha);
                putVertex(data, (float) (pos.x + size), (float) (pos.y + size), (float) pos.z, r, g, b, alpha);
                putVertex(data, (float) (pos.x - size), (float) (pos.y + size), (float) pos.z, r, g, b, alpha);
            }
            data.flip();
            if (data.remaining() == 0) {
                return;
            }

            CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
            GpuBuffer verts = RenderSystem.getDevice().createBuffer(
                () -> "linkenvisuals:trails", GpuBuffer.USAGE_VERTEX, data);
            GpuBufferSlice transforms = Pipelines.writeWorldTransforms();
            try (RenderPass pass = encoder.createRenderPass(() -> "linkenvisuals:trails",
                main.getColorAttachmentView(), OptionalInt.empty(),
                main.getDepthAttachmentView(), OptionalDouble.empty())) {
                pass.setPipeline(PIPELINE);
                Pipelines.bindWorldUniforms(pass, transforms);
                pass.setVertexBuffer(0, verts);
                pass.draw(0, (data.remaining() / 28));
            } finally {
                verts.close();
            }
        } finally {
            MemoryUtil.memFree(data);
        }
    }

    private static void putVertex(ByteBuffer data, float x, float y, float z,
                                  float r, float g, float b, float a) {
        data.putFloat(x).putFloat(y).putFloat(z);
        data.putFloat(r).putFloat(g).putFloat(b).putFloat(a);
    }

    public static void init() {
    }
}
