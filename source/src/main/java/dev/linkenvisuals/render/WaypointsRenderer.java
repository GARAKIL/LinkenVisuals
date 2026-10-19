package dev.linkenvisuals.render;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.linkenvisuals.module.Modules;
import dev.linkenvisuals.module.impl.WaypointsModule;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.render.Camera;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;
import java.util.OptionalInt;

/**
 * Waypoints renderer (Phase 5.7): draws markers at waypoint positions.
 * Simplified: vertical beacon-like lines + name via existing infra.
 */
public final class WaypointsRenderer {
    public record Waypoint(String name, double x, double y, double z) {}

    private static final List<Waypoint> waypoints = new ArrayList<>();

    private WaypointsRenderer() {
    }

    public static void clear() {
        // Keep waypoints on disable; clear only via command.
    }

    public static void addWaypoint(String name, double x, double y, double z) {
        waypoints.removeIf(w -> w.name().equalsIgnoreCase(name));
        waypoints.add(new Waypoint(name, x, y, z));
    }

    public static boolean removeWaypoint(String name) {
        return waypoints.removeIf(w -> w.name().equalsIgnoreCase(name));
    }

    public static List<Waypoint> getWaypoints() {
        return List.copyOf(waypoints);
    }

    /** Called at END_MAIN to render markers. */
    public static void render(WorldRenderContext context) {
        WaypointsModule module = Modules.waypoints();
        if (module == null || !module.enabled() || waypoints.isEmpty()) {
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
        int totalVerts = waypoints.size() * 2; // vertical line per waypoint
        ByteBuffer data = MemoryUtil.memAlloc(totalVerts * 28);
        try {
            for (Waypoint w : waypoints) {
                float x = (float) (w.x() - camPos.x);
                float y0 = (float) (w.y() - camPos.y);
                float y1 = y0 + 3.0f; // 3-block tall marker
                float z = (float) (w.z() - camPos.z);
                // Cyan marker.
                putVertex(data, x, y0, z, 0.0f, 1.0f, 1.0f, 0.9f);
                putVertex(data, x, y1, z, 0.0f, 1.0f, 1.0f, 0.9f);
            }
            data.flip();
            if (data.remaining() == 0) {
                return;
            }

            CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
            GpuBuffer verts = RenderSystem.getDevice().createBuffer(
                () -> "linkenvisuals:waypoints", GpuBuffer.USAGE_VERTEX, data);
            try (RenderPass pass = encoder.createRenderPass(() -> "linkenvisuals:waypoints",
                main.getColorAttachmentView(), OptionalInt.empty(),
                main.getDepthAttachmentView(), OptionalDouble.empty())) {
                pass.setPipeline(BlockOverlayPipelines.LINES);
                RenderSystem.bindDefaultUniforms(pass);
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
