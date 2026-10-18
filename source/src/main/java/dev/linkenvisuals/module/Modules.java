package dev.linkenvisuals.module;

import dev.linkenvisuals.clickgui.ModuleRegistry;

/**
 * Typed facade over object-based modules. Per-module accessors are added here
 * as modules are ported (Phase 5), e.g. {@code wayPoints()}. Legacy static
 * accessors stay in {@link ModuleRegistry} until migrated one by one.
 */
public final class Modules {
    private Modules() {
    }

    public static void register(Module module) {
        ModuleRegistry.registerModule(module);
    }

    public static Module get(String id) {
        return ModuleRegistry.module(id);
    }

    /**
     * Registers built-in object modules. Called once from mod init.
     * The first object-based modules land with the Phase 5 ports.
     */
    public static void init() {
    }
}
