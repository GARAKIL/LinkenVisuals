package dev.linkenvisuals.render;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.linkenvisuals.module.Modules;
import dev.linkenvisuals.module.impl.ItemRadiusModule;
import dev.linkenvisuals.util.ColorMath;
import dev.linkenvisuals.util.particles.ItemRadiusRules;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.block.Blocks;
import dev.linkenvisuals.player.FakePlayer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;
import net.minecraft.world.RaycastContext;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.Set;

/**
 * Donor ItemRadius renderer: circles, the netherite-scrap box, the dried-kelp
 * staircase wall and the simulated splash-potion impact radius. Geometry is
 * camera-relative triangle-list data on the proven flat END_MAIN pipeline.
 */
public final class ItemRadiusRenderer {
    private static final int SIDES = 96;
    private static final double CIRCLE_LINE = 4.0;
    private static final double BOX_LINE = 8.0;
    private static final double WALL_LINE = 8.0;
    private static final int BUFFER_BYTES = 1 << 21;
    private static final double HALF_LINE_SCALE = 0.006;
    private static final float[][] LABEL_OUTLINE = {
        {-1.0f, 0.0f}, {1.0f, 0.0f}, {0.0f, -1.0f}, {0.0f, 1.0f},
        {-0.7f, -0.7f}, {-0.7f, 0.7f}, {0.7f, -0.7f}, {0.7f, 0.7f}
    };

    private static Vec3d cachedPotionImpact;
    private static int potionCacheAge = -1;
    private static float potionCacheYaw = Float.NaN;
    private static float potionCachePitch = Float.NaN;
    private static boolean lastRenderVisible;
    private static Vec3d lastPotionImpact;
    private static double lastPotionRadius;
    private static boolean lastLabelVisible;
    private static int lastLabelPercent;
    private static int lastLabelCandidates;
    private static float lastLabelDepth = -1.0f;

    public static int lastLabelCandidates() {
        return lastLabelCandidates;
    }

    public static float lastLabelDepth() {
        return lastLabelDepth;
    }

    public static Vec3d lastPotionImpactValue() {
        return lastPotionImpact == null ? null : new Vec3d(lastPotionImpact.x, lastPotionImpact.y, lastPotionImpact.z);
    }

    public static double lastPotionRadiusValue() {
        return lastPotionRadius;
    }

    public static int lastLabelPercent() {
        return lastLabelPercent;
    }

    private ItemRadiusRenderer() {
    }

    public static void clear() {
        cachedPotionImpact = null;
        potionCacheAge = -1;
        potionCacheYaw = Float.NaN;
        potionCachePitch = Float.NaN;
        lastRenderVisible = false;
        lastPotionImpact = null;
        lastPotionRadius = 0.0;
        lastLabelVisible = false;
    }

    public static boolean lastRenderVisible() {
        return lastRenderVisible;
    }

    public static boolean lastLabelVisible() {
        return lastLabelVisible;
    }

    /** HUD pass: donor "NN%" over each player inside the simulated splash radius. */
    public static void renderLabels(DrawContext context) {
        lastLabelVisible = false;
        lastLabelPercent = 0;
        lastLabelCandidates = 0;
        lastLabelDepth = -1.0f;
        ItemRadiusModule module = Modules.itemRadius();
        if (module == null || !module.enabled() || lastPotionImpact == null || lastPotionRadius <= 0.0) {
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        Camera camera = client.gameRenderer.getCamera();
        GlyphAtlas font = Fonts.titleFont();
        if (client.player == null || client.world == null || camera == null || font == null) {
            return;
        }
        int width = context.getScaledWindowWidth();
        int height = context.getScaledWindowHeight();
        float fov = (float) Math.toRadians(Math.max(30, Math.min(110,
            client.options.getFov().getValue())));
        float focal = (float) (height / (2.0 * Math.tan(fov * 0.5)));
        Quaternionf rotation = camera.getRotation();
        Vector3f right = rotation.transform(new Vector3f(1.0f, 0.0f, 0.0f));
        Vector3f up = rotation.transform(new Vector3f(0.0f, 1.0f, 0.0f));
        Vector3f forwardV = rotation.transform(new Vector3f(0.0f, 0.0f, -1.0f));
        Vec3d rightVec = new Vec3d(right.x(), right.y(), right.z());
        Vec3d upVec = new Vec3d(up.x(), up.y(), up.z());
        Vec3d forward = new Vec3d(forwardV.x(), forwardV.y(), forwardV.z());
        Vec3d cam = camera.getCameraPos();
        float tickDelta = client.getRenderTickCounter().getTickProgress(false);
        boolean visible = false;
        for (PlayerEntity player : nearbyPlayers(client)) {
            if (player == client.player) {
                continue;
            }
            lastLabelCandidates++;
            int pct = ItemRadiusRules.proximityPercent(
                Math.sqrt(player.squaredDistanceTo(lastPotionImpact)), lastPotionRadius);
            if (pct <= 0) {
                continue;
            }
            lastLabelPercent = pct;
            Vec3d head = player.getLerpedPos(tickDelta)
                .add(0.0, player.getHeight() + 0.35, 0.0).subtract(cam);
            float depth = (float) head.dotProduct(forward);
            lastLabelDepth = depth;
            if (depth <= 0.05f) {
                continue;
            }
            float sx = width * 0.5f + (float) head.dotProduct(rightVec) * focal / depth;
            float sy = height * 0.5f - (float) head.dotProduct(upVec) * focal / depth;
            float textSize = Math.max(4.0f, Math.min(10.0f, 0.27f * focal / depth));
            String text = pct + "%";
            float textWidth = font.width(text, textSize);
            if (sx < -textWidth || sx > width + textWidth
                || sy < -textSize || sy > height + textSize) {
                continue;
            }
            float x = sx - textWidth * 0.5f;
            int fill = ColorMath.withAlpha(0xFFFFFF, 255);
            int outline = ColorMath.withAlpha(0x000000, 235);
            for (float[] offset : LABEL_OUTLINE) {
                MsdfText.drawOn(context, font, text, x + offset[0] * 1.5f,
                    sy + offset[1] * 1.5f, textSize, outline);
            }
            MsdfText.drawOn(context, font, text, x, sy, textSize, fill);
            visible = true;
            lastLabelPercent = pct;
        }
        lastLabelVisible = visible;
    }

    public static void render(WorldRenderContext context) {
        lastRenderVisible = false;
        lastPotionImpact = null;
        lastPotionRadius = 0.0;
        ItemRadiusModule module = Modules.itemRadius();
        MinecraftClient client = MinecraftClient.getInstance();
        if (module == null || !module.enabled() || client.player == null || client.world == null) {
            return;
        }
        Camera camera = client.gameRenderer.getCamera();
        Framebuffer main = client.getFramebuffer();
        if (camera == null || main == null || main.getColorAttachmentView() == null
            || main.getDepthAttachmentView() == null) {
            return;
        }

        ByteBuffer data = MemoryUtil.memAlloc(BUFFER_BYTES);
        try {
            Vec3d cam = camera.getCameraPos();
            Vec3d playerPos = client.player.getEntityPos().add(0.0, -0.1, 0.0);
            if (module.enderEye.value() && isHolding(client.player, Items.ENDER_EYE)) {
                circle(data, cam, playerPos, 14.0, color(module.enderEyeColor.rgb()), module, client);
            }
            if (module.sugar.value() && isHolding(client.player, Items.SUGAR)) {
                circle(data, cam, playerPos, 14.0, color(module.sugarColor.rgb()), module, client);
            }
            if (module.netheriteScrap.value() && isHolding(client.player, Items.NETHERITE_SCRAP)) {
                double half = module.dragonTrap.value() ? 3.5 : 2.5;
                Vec3d center = blockCenter(client.player.getEntityPos()).add(0.0, half - 0.5, 0.0);
                Box box = new Box(center.x - half, center.y - half, center.z - half,
                    center.x + half, center.y + half, center.z + half);
                int argb = resolveColor(center, half, color(module.netheriteScrapColor.rgb()), module, client);
                CustomHitBoxGeometry.appendOutline(data, box, cam.x, cam.y, cam.z,
                    BOX_LINE, argb);
            }
            if (module.driedKelp.value() && isHolding(client.player, Items.DRIED_KELP)) {
                driedKelp(data, cam, client, color(module.driedKelpColor.rgb()), module);
            }
            if (module.snowball.value() && isHolding(client.player, Items.SNOWBALL)) {
                Vec3d center = projectedSnowballCenter(client);
                circle(data, cam, center, 12.0, color(module.snowballColor.rgb()), module, client);
            }
            if (module.explosivePotion.value() && isHolding(client.player, Items.SPLASH_POTION)) {
                Vec3d impact = potionImpact(client);
                if (impact != null) {
                    double radius = module.explosivePotionRadius.value();
                    lastPotionImpact = impact;
                    lastPotionRadius = radius;
                    circle(data, cam, impact, radius, color(module.explosivePotionColor.rgb()), module, client);
                }
            }
            if (data.position() == 0) {
                return;
            }
            data.flip();
            int vertices = data.remaining() / ItemRadiusGeometry.VERTEX_BYTES;
            CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
            GpuBuffer buffer = RenderSystem.getDevice().createBuffer(
                () -> "linkenvisuals:item_radius", GpuBuffer.USAGE_VERTEX, data);
            GpuBufferSlice transforms = Pipelines.writeWorldTransforms();
            try (RenderPass pass = encoder.createRenderPass(() -> "linkenvisuals:item_radius",
                main.getColorAttachmentView(), OptionalInt.empty(),
                main.getDepthAttachmentView(), OptionalDouble.empty())) {
                pass.setPipeline(BlockOverlayPipelines.FLAT_NO_DEPTH);
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

    private static void circle(ByteBuffer data, Vec3d cam, Vec3d center, double radius,
                               int argb, ItemRadiusModule module, MinecraftClient client) {
        int resolved = resolveColor(center, radius, argb, module, client);
        HitRangeGeometry.appendBand(data,
            (float) (center.x - cam.x), (float) (center.y - cam.y), (float) (center.z - cam.z),
            (float) radius, (float) (CIRCLE_LINE * HALF_LINE_SCALE), 0.0, SIDES, resolved);
    }

    private static void driedKelp(ByteBuffer data, Vec3d cam, MinecraftClient client,
                                  int argb, ItemRadiusModule module) {
        float yaw = Math.round(client.player.getYaw(1.0f) / 45.0f) * 45.0f;
        double dx = -Math.sin(Math.toRadians(yaw));
        double dz = Math.cos(Math.toRadians(yaw));
        float pitch = client.player.getPitch();
        Vec3d playerBlock = blockCenter(client.player.getEntityPos());
        if (pitch > 45.0f) {
            Vec3d center = playerBlock.add(0.0, -1.5, 0.0);
            Box box = new Box(center.x - 2.5, center.y - 1.0, center.z - 2.5,
                center.x + 2.5, center.y + 1.0, center.z + 2.5);
            CustomHitBoxGeometry.appendOutline(data, box, cam.x, cam.y, cam.z, BOX_LINE,
                resolveColor(center, 2.5, argb, module, client));
            return;
        }
        if (pitch < -45.0f) {
            Vec3d center = playerBlock.add(0.0, 2.5, 0.0);
            Box box = new Box(center.x - 2.5, center.y - 1.0, center.z - 2.5,
                center.x + 2.5, center.y + 1.0, center.z + 2.5);
            CustomHitBoxGeometry.appendOutline(data, box, cam.x, cam.y, cam.z, BOX_LINE,
                resolveColor(center, 2.5, argb, module, client));
            return;
        }
        double px = dz;
        double pz = -dx;
        Vec3d origin = playerBlock.add(dx * 3.0, 0.0, dz * 3.0);
        staircaseWall(data, cam, origin, px, pz, 2.5, 4.0,
            Math.floor(client.player.getY()), argb, module, client);
    }

    private static void staircaseWall(ByteBuffer data, Vec3d cam, Vec3d origin,
                                      double px, double pz, double halfLen, double height,
                                      double baseY, int argb, ItemRadiusModule module,
                                      MinecraftClient client) {
        int resolved = resolveColor(origin, halfLen, argb, module, client);
        double topY = baseY + height;
        Set<Long> cells = new HashSet<>();
        int minX = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int minZ = Integer.MAX_VALUE;
        int maxZ = Integer.MIN_VALUE;
        for (double t = -halfLen; t <= halfLen + 1.0E-6; t += 0.05) {
            int bx = (int) Math.floor(origin.x + px * t);
            int bz = (int) Math.floor(origin.z + pz * t);
            cells.add(cellKey(bx, bz));
            minX = Math.min(minX, bx);
            maxX = Math.max(maxX, bx);
            minZ = Math.min(minZ, bz);
            maxZ = Math.max(maxZ, bz);
        }
        for (long key : cells) {
            int bx = (int) (key >> 32);
            int bz = (int) (key & 0xFFFFFFFFL);
            if (!cells.contains(cellKey(bx - 1, bz))) {
                edge(data, cam, bx, baseY, bz, bx, baseY, bz + 1, resolved);
                edge(data, cam, bx, topY, bz, bx, topY, bz + 1, resolved);
            }
            if (!cells.contains(cellKey(bx + 1, bz))) {
                edge(data, cam, bx + 1, baseY, bz, bx + 1, baseY, bz + 1, resolved);
                edge(data, cam, bx + 1, topY, bz, bx + 1, topY, bz + 1, resolved);
            }
            if (!cells.contains(cellKey(bx, bz - 1))) {
                edge(data, cam, bx, baseY, bz, bx + 1, baseY, bz, resolved);
                edge(data, cam, bx, topY, bz, bx + 1, topY, bz, resolved);
            }
            if (!cells.contains(cellKey(bx, bz + 1))) {
                edge(data, cam, bx, baseY, bz + 1, bx + 1, baseY, bz + 1, resolved);
                edge(data, cam, bx, topY, bz + 1, bx + 1, topY, bz + 1, resolved);
            }
        }
        for (int vx = minX; vx <= maxX + 1; vx++) {
            for (int vz = minZ; vz <= maxZ + 1; vz++) {
                boolean c00 = cells.contains(cellKey(vx - 1, vz - 1));
                boolean c10 = cells.contains(cellKey(vx, vz - 1));
                boolean c01 = cells.contains(cellKey(vx - 1, vz));
                boolean c11 = cells.contains(cellKey(vx, vz));
                int count = (c00 ? 1 : 0) + (c10 ? 1 : 0) + (c01 ? 1 : 0) + (c11 ? 1 : 0);
                if (count == 1 || count == 3 || (count == 2 && ((c00 && c11) || (c10 && c01)))) {
                    edge(data, cam, vx, baseY, vz, vx, topY, vz, resolved);
                }
            }
        }
    }

    private static void edge(ByteBuffer data, Vec3d cam,
                             double x1, double y1, double z1,
                             double x2, double y2, double z2, int argb) {
        ItemRadiusGeometry.appendLine(data, x1 - cam.x, y1 - cam.y, z1 - cam.z,
            x2 - cam.x, y2 - cam.y, z2 - cam.z, WALL_LINE * HALF_LINE_SCALE, argb);
    }

    private static long cellKey(int x, int z) {
        return (((long) x) << 32) ^ (z & 0xFFFFFFFFL);
    }

    private static Vec3d projectedSnowballCenter(MinecraftClient client) {
        Vec3d eye = client.player.getEyePos();
        Vec3d rotation = client.player.getRotationVec(1.0f);
        float pitch = client.player.getPitch(1.0f);
        double scale = 1.0 + Math.abs(Math.sin(Math.toRadians(pitch)));
        double distance = Math.max(10.0, Math.min(50.0, 20.0 * scale));
        Vec3d projected = eye.add(rotation.multiply(distance));
        double y = client.player.getY();
        if (client.world != null) {
            y = client.world.getTopY(Heightmap.Type.MOTION_BLOCKING,
                (int) projected.x, (int) projected.z);
        }
        return new Vec3d(projected.x, y, projected.z);
    }

    private static Vec3d potionImpact(MinecraftClient client) {
        float yaw = client.player.getYaw();
        float pitch = client.player.getPitch();
        if (cachedPotionImpact != null && client.player.age == potionCacheAge
            && Math.abs(MathHelper.wrapDegrees(yaw - potionCacheYaw)) <= 0.5f
            && Math.abs(pitch - potionCachePitch) <= 0.5f) {
            return cachedPotionImpact;
        }
        potionCacheAge = client.player.age;
        potionCacheYaw = yaw;
        potionCachePitch = pitch;
        cachedPotionImpact = simulatePotionImpact(client, yaw, pitch);
        return cachedPotionImpact;
    }

    private static Vec3d simulatePotionImpact(MinecraftClient client, float yaw, float pitch) {
        if (client.world == null || client.player == null) {
            return null;
        }
        double radians = Math.toRadians(1.0);
        double mx = -Math.sin(yaw * radians) * Math.cos(pitch * radians);
        double my = -Math.sin((pitch - 20.0) * radians);
        double mz = Math.cos(yaw * radians) * Math.cos(pitch * radians);
        double norm = Math.sqrt(mx * mx + my * my + mz * mz);
        if (norm == 0.0) {
            return null;
        }
        double speed = 0.5;
        mx = mx / norm * speed;
        my = my / norm * speed;
        mz = mz / norm * speed;
        Vec3d velocity = client.player.getVelocity();
        mx += velocity.x;
        mz += velocity.z;
        if (!client.player.isOnGround()) {
            my += velocity.y;
        }
        double x = client.player.getX();
        double y = client.player.getEyeY() - 0.1;
        double z = client.player.getZ();
        for (int i = 0; i < 200; i++) {
            my -= 0.03;
            boolean water = client.world.getBlockState(
                net.minecraft.util.math.BlockPos.ofFloored(x, y, z)).isOf(Blocks.WATER);
            double drag = water ? 0.8 : 0.99;
            mx *= drag;
            my *= drag;
            mz *= drag;
            Vec3d current = new Vec3d(x, y, z);
            Vec3d next = current.add(mx, my, mz);
            BlockHitResult hit = client.world.raycast(new RaycastContext(
                current, next, RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE, client.player));
            if (hit != null && hit.getType() == HitResult.Type.BLOCK) {
                return hit.getPos();
            }
            x = next.x;
            y = next.y;
            z = next.z;
            if (y < client.world.getBottomY()) {
                return null;
            }
        }
        return null;
    }

    private static int resolveColor(Vec3d center, double radius, int base,
                                    ItemRadiusModule module, MinecraftClient client) {
        if (module.proximityColor.value() && hasPlayerInRadius(client, center, radius)) {
            return visibleColor(module.proximityColorSetting.rgb());
        }
        return visibleColor(base);
    }

    private static boolean hasPlayerInRadius(MinecraftClient client, Vec3d center, double radius) {
        if (client.world == null || client.player == null) {
            return false;
        }
        double squared = radius * radius;
        for (PlayerEntity player : nearbyPlayers(client)) {
            if (player == client.player) {
                continue;
            }
            if (player.squaredDistanceTo(center) <= squared) {
                return true;
            }
        }
        return false;
    }

    /** World players plus the client-side FakePlayer, which is not in {@code world.getPlayers()}. */
    private static List<? extends PlayerEntity> nearbyPlayers(MinecraftClient client) {
        List<? extends PlayerEntity> players = client.world == null
            ? List.of() : client.world.getPlayers();
        PlayerEntity dummy = FakePlayer.entity() instanceof PlayerEntity player ? player : null;
        if (dummy == null || players.contains(dummy)) {
            return players;
        }
        List<PlayerEntity> out = new ArrayList<>(players.size() + 1);
        out.addAll(players);
        out.add(dummy);
        return out;    }

    private static int visibleColor(int rgb) {
        return ColorMath.withAlpha(rgb & 0xFFFFFF, 210);
    }

    private static int color(int rgb) {
        return rgb & 0xFFFFFF;
    }

    private static boolean isHolding(PlayerEntity player, Item item) {
        return player.getMainHandStack().isOf(item) || player.getOffHandStack().isOf(item);
    }

    private static Vec3d blockCenter(Vec3d position) {
        return new Vec3d(Math.floor(position.x) + 0.5, Math.floor(position.y) + 0.5,
            Math.floor(position.z) + 0.5);
    }

    public static void init() {
    }
}
