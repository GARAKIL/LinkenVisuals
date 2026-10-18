package dev.linkenvisuals.module;

import dev.linkenvisuals.clickgui.ModuleRegistry;
import dev.linkenvisuals.module.impl.BlockOverlayModule;
import dev.linkenvisuals.module.impl.MotionBlurModule;

/**
 * Typed facade over object-based modules. Per-module accessors are added here
 * as modules are ported (Phase 5), e.g. {@code wayPoints()}. Legacy static
 * accessors stay in {@link ModuleRegistry} until migrated one by one.
 */
public final class Modules {
    private static BlockOverlayModule blockOverlay;
    private static MotionBlurModule motionBlur;

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

    /**
     * Registers built-in object modules. Called once from mod init.
     */
    public static void init() {
        blockOverlay = new BlockOverlayModule();
        register(blockOverlay);
        motionBlur = new MotionBlurModule();
        register(motionBlur);
    }
}
