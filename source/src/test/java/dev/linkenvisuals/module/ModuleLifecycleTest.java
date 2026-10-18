package dev.linkenvisuals.module;

import dev.linkenvisuals.clickgui.Category;
import dev.linkenvisuals.clickgui.ModuleRegistry;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Object-based module framework (Phase 4): registration, GUI-list wiring
 * and enable/disable lifecycle. Runs without Minecraft bootstrap:
 * {@code ModuleInfo.toggle()} is null-safe (NotificationManager guards
 * {@code MinecraftClient.getInstance()}).
 */
class ModuleLifecycleTest {
    private static final class RecordingModule extends Module {
        final AtomicInteger enabledCount = new AtomicInteger();
        final AtomicInteger disabledCount = new AtomicInteger();

        RecordingModule(String id) {
            super(id, "Test " + id, "lifecycle probe", Category.MISC, List.of(), List.of());
        }

        @Override
        protected void onEnable() {
            enabledCount.incrementAndGet();
        }

        @Override
        protected void onDisable() {
            disabledCount.incrementAndGet();
        }
    }

    @Test
    void registerExposesModuleAndGuiInfo() {
        RecordingModule mod = new RecordingModule("probe-register");
        Modules.register(mod);

        assertSame(mod, Modules.get("probe-register"));
        assertSame(mod, ModuleRegistry.module("probe-register"));
        assertTrue(ModuleRegistry.all().contains(mod.info()),
            "module info must join the ClickGUI/serialization list");
    }

    @Test
    void toggleFiresLifecycleOncePerDirection() {
        RecordingModule mod = new RecordingModule("probe-toggle");
        Modules.register(mod);

        mod.info().toggle();
        assertTrue(mod.enabled());
        assertEquals(1, mod.enabledCount.get());
        assertEquals(0, mod.disabledCount.get());

        mod.info().toggle();
        assertFalse(mod.enabled());
        assertEquals(1, mod.enabledCount.get());
        assertEquals(1, mod.disabledCount.get());
    }

    @Test
    void silentRestoreDoesNotFireLifecycle() {
        RecordingModule mod = new RecordingModule("probe-silent");
        Modules.register(mod);

        mod.info().toggleSilently(true);
        assertTrue(mod.enabled());
        assertEquals(0, mod.enabledCount.get());
        assertEquals(0, mod.disabledCount.get());
    }
}
