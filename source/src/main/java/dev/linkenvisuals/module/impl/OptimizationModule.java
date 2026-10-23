package dev.linkenvisuals.module.impl;

import dev.linkenvisuals.clickgui.Category;
import dev.linkenvisuals.clickgui.setting.BooleanSetting;
import dev.linkenvisuals.clickgui.setting.ModeSetting;
import dev.linkenvisuals.clickgui.setting.Setting;
import dev.linkenvisuals.module.Module;
import dev.linkenvisuals.module.Modules;
import dev.linkenvisuals.util.optimization.OcclusionCuller;
import dev.linkenvisuals.util.optimization.OptimizationRules;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

import java.util.List;

/**
 * Donor Optimization (safe 1.21.11 subset): tiered entity occlusion culling,
 * render-distance cap, particle distance culling and cloud culling. The donor
 * "fast transparency" path targeted a removed 1.21.4 getter and is not exposed
 * as a dead setting.
 */
public final class OptimizationModule extends Module {
    public final ModeSetting tier;
    public final BooleanSetting entityCulling;

    public OptimizationModule() {
        this(new ModeSetting("Tier", List.of("Low", "Medium", "Ultra"), 1),
            new BooleanSetting("Entity Culling", true));
    }

    private OptimizationModule(ModeSetting tier, BooleanSetting entityCulling) {
        super("optimization", "Optimization", "tiered entity, particle, cloud and render-distance culling",
            Category.VISUAL, List.<Setting>of(tier), List.<Setting>of(entityCulling));
        this.tier = tier;
        this.entityCulling = entityCulling;
        info().withClassicCategory(dev.linkenvisuals.clickgui.classic.ClassicCategory.WORLD);
    }

    public int tierIndex() {
        return OptimizationRules.tier(tier.value());
    }

    private static OptimizationModule activeModule() {
        OptimizationModule module = Modules.optimization();
        return module != null && module.enabled() ? module : null;
    }

    /** Mixin hook: clamps the effective view distance for Medium/Ultra. */
    public static int capEffectiveRenderDistance(int original) {
        OptimizationModule module = activeModule();
        return module == null ? original : OptimizationRules.capRenderDistance(original, module.tierIndex());
    }

    /** Mixin hook: culls vanilla particles beyond the tier radius. */
    public static boolean allowParticle(double x, double y, double z) {
        OptimizationModule module = activeModule();
        if (module == null) {
            return true;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        Camera camera = client.gameRenderer == null ? null : client.gameRenderer.getCamera();
        if (camera == null) {
            return true;
        }
        Vec3d cam = camera.getCameraPos();
        return OptimizationRules.allowParticle(x - cam.x, y - cam.y, z - cam.z, module.tierIndex());
    }

    /** Mixin hook: donor DDA occlusion culling for entities. */
    public static boolean shouldRenderEntity(boolean original, Entity entity,
                                             double camX, double camY, double camZ) {
        OptimizationModule module = activeModule();
        if (!original || module == null || !module.entityCulling.value()) {
            return original;
        }
        return OcclusionCuller.isVisible(entity, camX, camY, camZ, module.tierIndex());
    }

    /** Mixin hook: Ultra hides clouds entirely. */
    public static boolean cullClouds() {
        OptimizationModule module = activeModule();
        return module != null && OptimizationRules.cullClouds(module.tierIndex());
    }
}
