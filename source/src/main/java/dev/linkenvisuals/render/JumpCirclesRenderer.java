package dev.linkenvisuals.render;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.linkenvisuals.LinkenVisuals;
import dev.linkenvisuals.module.Modules;
import dev.linkenvisuals.module.impl.JumpCirclesModule;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.render.Camera;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.OptionalDouble;
import java.util.OptionalInt;

/**
 * JumpCircles renderer (Phase 5.7): expanding rings on jump.
 * Circles spawn when the player leaves the ground, expand and fade.
 * Drawn as line rings at END_MAIN using the shared line pipeline.
 */
public final class JumpCirclesRenderer {
    private static final int SEGMENTS = 48;
    private static final long FADE_IN_MS = 500L;
    private static final long FADE_OUT_MS = 500L;

    private record Circle(Vec3d pos, long spawnTime) {}

    private static final List<Circle> circles = new ArrayList<>();
    private static boolean wasOnGround = true;

    private JumpCirclesRenderer() {
    }

    public static void clear() {
        circles.clear();
        wasOnGround = true;
    }

    /** Called each client tick to detect jumps. */
    public static void tick() {
        JumpCirclesModule module = Modules.jumpCircles();
        if (module == null || !module.enabled()) {
            if (!circles.isEmpty()) {
                circles.clear();
            }
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return;
        }
        boolean onGround = client.player.isOnGround();
        boolean jumpPressed = client.options.jumpKey.isPressed();
        if (wasOnGround && !onGround && jumpPressed) {
            Vec3d pos = new Vec3d(client.player.getX(),
                client.player.getBlockPos().getY() + 0.01, client.player.getZ());
            circles.add(new Circle(pos, System.currentTimeMillis()));
        }
        wasOnGround = onGround;
    }

    /** Called at END_MAIN to render rings. */
    public static void render(WorldRenderContext context) {
        JumpCirclesModule module = Modules.jumpCircles();
        if (module == null || !module.enabled() || circles.isEmpty()) {
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        Framebuffer main = client.getFramebuffer();
        Camera camera = client.gameRenderer.getCamera();
        if (main == null || main.getColorAttachmentView() == null
            || main.getDepthAttachmentView() == null || camera == null) {
            return;
        }

        Vec3d camPos = camera.getCameraPos();
        long now = System.currentTimeMillis();
        long lifetimeMs = (long) (module.lifetime.value() * 1000f);
        float baseRadius = module.size.value();

        // Count segments for buffer sizing.
        int totalSegments = 0;
        Iterator<Circle> it = circles.iterator();
        while (it.hasNext()) {
            Circle c = it.next();
            long age = now - c.spawnTime();
            if (age >= lifetimeMs) {
                it.remove();
            } else {
                totalSegments += SEGMENTS;
            }
        }
        if (totalSegments == 0) {
            return;
        }

        ByteBuffer data = MemoryUtil.memAlloc(totalSegments * 2 * 28);
        try {
            for (Circle c : circles) {
                long age = now - c.spawnTime();
                float progress = (float) age / lifetimeMs;
                // Expand: radius grows with progress.
                float radius = baseRadius * (0.5f + progress * 1.5f);
                // Alpha: fade in then out.
                float alpha;
                if (age < FADE_IN_MS) {
                    alpha = (float) age / FADE_IN_MS;
                } else if (age > lifetimeMs - FADE_OUT_MS) {
                    alpha = (float) (lifetimeMs - age) / FADE_OUT_MS;
                } else {
                    alpha = 1.0f;
                }
                if (alpha <= 0.01f) {
                    continue;
                }

                double cx = c.pos().x - camPos.x;
                double cy = c.pos().y - camPos.y;
                double cz = c.pos().z - camPos.z;
                float r = 1.0f, g = 1.0f, b = 1.0f; // white, could use theme color

                for (int i = 0; i < SEGMENTS; i++) {
                    double a0 = Math.PI * 2.0 * i / SEGMENTS;
                    double a1 = Math.PI * 2.0 * (i + 1) / SEGMENTS;
                    float x0 = (float) (cx + Math.cos(a0) * radius);
                    float z0 = (float) (cz + Math.sin(a0) * radius);
                    float x1 = (float) (cx + Math.cos(a1) * radius);
                    float z1 = (float) (cz + Math.sin(a1) * radius);
                    float y = (float) cy;
                    putVertex(data, x0, y, z0, r, g, b, alpha);
                    putVertex(data, x1, y, z1, r, g, b, alpha);
                }
            }
            data.flip();
            if (data.remaining() == 0) {
                return;
            }

            CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
            GpuBuffer verts = RenderSystem.getDevice().createBuffer(
                () -> "linkenvisuals:jumpcircles", GpuBuffer.USAGE_VERTEX, data);
            GpuBufferSlice transforms = Pipelines.writeWorldTransforms();
            try (RenderPass pass = encoder.createRenderPass(() -> "linkenvisuals:jumpcircles",
                main.getColorAttachmentView(), OptionalInt.empty(),
                main.getDepthAttachmentView(), OptionalDouble.empty())) {
                pass.setPipeline(BlockOverlayPipelines.LINES);
                Pipelines.bindWorldUniforms(pass, transforms);
                pass.setVertexBuffer(0, verts);
                pass.draw(0, data.remaining() / 28);
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
