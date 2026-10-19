package dev.linkenvisuals.module;

import dev.linkenvisuals.clickgui.ModuleRegistry;
import dev.linkenvisuals.module.impl.BlockOverlayModule;
import dev.linkenvisuals.module.impl.ChatHelperModule;
import dev.linkenvisuals.module.impl.CrosshairModule;
import dev.linkenvisuals.module.impl.FastExpModule;
import dev.linkenvisuals.module.impl.FreelookModule;
import dev.linkenvisuals.module.impl.FullbrightModule;
import dev.linkenvisuals.module.impl.HandSwapModule;
import dev.linkenvisuals.module.impl.HitColorModule;
import dev.linkenvisuals.module.impl.HitSoundModule;
import dev.linkenvisuals.module.impl.ItemPhysicsModule;
import dev.linkenvisuals.module.impl.JumpCirclesModule;
import dev.linkenvisuals.module.impl.MineHelperModule;
import dev.linkenvisuals.module.impl.MotionBlurModule;
import dev.linkenvisuals.module.impl.SwingAnimationModule;
import dev.linkenvisuals.module.impl.TimeChangerModule;
import dev.linkenvisuals.module.impl.TotemCounterModule;
import dev.linkenvisuals.module.impl.TotemTrackerModule;
import dev.linkenvisuals.module.impl.TrailsModule;
import dev.linkenvisuals.module.impl.UseTrackerModule;
import dev.linkenvisuals.module.impl.WaypointsModule;
import dev.linkenvisuals.module.impl.ZoomModule;

/**
 * Typed facade over object-based modules. Per-module accessors are added here
 * as modules are ported (Phase 5), e.g. {@code wayPoints()}. Legacy static
 * accessors stay in {@link ModuleRegistry} until migrated one by one.
 */
public final class Modules {
    private static BlockOverlayModule blockOverlay;
    private static MotionBlurModule motionBlur;
    private static FullbrightModule fullbright;
    private static TrailsModule trails;
    private static JumpCirclesModule jumpCircles;
    private static CrosshairModule crosshair;
    private static ZoomModule zoom;
    private static TimeChangerModule timeChanger;
    private static HitSoundModule hitSound;
    private static HandSwapModule handSwap;
    private static FastExpModule fastExp;
    private static MineHelperModule mineHelper;
    private static ChatHelperModule chatHelper;
    private static TotemCounterModule totemCounter;
    private static TotemTrackerModule totemTracker;
    private static WaypointsModule waypoints;
    private static UseTrackerModule useTracker;
    private static SwingAnimationModule swingAnimation;
    private static HitColorModule hitColor;
    private static FreelookModule freelook;
    private static ItemPhysicsModule itemPhysics;

    private Modules() {
    }

    public static void register(Module module) {
        ModuleRegistry.registerModule(module);
    }

    public static Module get(String id) {
        return ModuleRegistry.module(id);
    }

    public static BlockOverlayModule blockOverlay() {
        return blockOverlay;
    }

    public static MotionBlurModule motionBlur() {
        return motionBlur;
    }

    public static FullbrightModule fullbright() {
        return fullbright;
    }

    public static TrailsModule trails() {
        return trails;
    }

    public static JumpCirclesModule jumpCircles() {
        return jumpCircles;
    }

    public static CrosshairModule crosshair() {
        return crosshair;
    }

    public static ZoomModule zoom() {
        return zoom;
    }

    public static TimeChangerModule timeChanger() {
        return timeChanger;
    }

    public static HitSoundModule hitSound() {
        return hitSound;
    }

    public static HandSwapModule handSwap() {
        return handSwap;
    }

    public static FastExpModule fastExp() {
        return fastExp;
    }

    public static MineHelperModule mineHelper() {
        return mineHelper;
    }

    public static ChatHelperModule chatHelper() {
        return chatHelper;
    }

    public static TotemCounterModule totemCounter() {
        return totemCounter;
    }

    public static TotemTrackerModule totemTracker() {
        return totemTracker;
    }

    public static WaypointsModule waypoints() {
        return waypoints;
    }

    public static UseTrackerModule useTracker() {
        return useTracker;
    }

    public static SwingAnimationModule swingAnimation() {
        return swingAnimation;
    }

    public static HitColorModule hitColor() {
        return hitColor;
    }

    public static FreelookModule freelook() {
        return freelook;
    }

    public static ItemPhysicsModule itemPhysics() {
        return itemPhysics;
    }

    /**
     * Registers built-in object modules. Called once from mod init.
     */
    public static void init() {
        blockOverlay = new BlockOverlayModule();
        register(blockOverlay);
        motionBlur = new MotionBlurModule();
        register(motionBlur);
        fullbright = new FullbrightModule();
        register(fullbright);
        trails = new TrailsModule();
        register(trails);
        jumpCircles = new JumpCirclesModule();
        register(jumpCircles);
        crosshair = new CrosshairModule();
        register(crosshair);
        zoom = new ZoomModule();
        register(zoom);
        timeChanger = new TimeChangerModule();
        register(timeChanger);
        hitSound = new HitSoundModule();
        register(hitSound);
        handSwap = new HandSwapModule();
        register(handSwap);
        fastExp = new FastExpModule();
        register(fastExp);
        mineHelper = new MineHelperModule();
        register(mineHelper);
        chatHelper = new ChatHelperModule();
        register(chatHelper);
        totemCounter = new TotemCounterModule();
        register(totemCounter);
        totemTracker = new TotemTrackerModule();
        register(totemTracker);
        waypoints = new WaypointsModule();
        register(waypoints);
        useTracker = new UseTrackerModule();
        register(useTracker);
        swingAnimation = new SwingAnimationModule();
        register(swingAnimation);
        hitColor = new HitColorModule();
        register(hitColor);
        freelook = new FreelookModule();
        register(freelook);
        itemPhysics = new ItemPhysicsModule();
        register(itemPhysics);
    }
}
