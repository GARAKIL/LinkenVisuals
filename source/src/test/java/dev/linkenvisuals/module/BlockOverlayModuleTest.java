package dev.linkenvisuals.module;

import dev.linkenvisuals.module.impl.BlockOverlayModule;
import dev.linkenvisuals.module.impl.BlockOverlayModule.FillMode;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** BlockOverlay module wiring: modes, shader names, settings, registration. */
class BlockOverlayModuleTest {
    @Test
    void nineModesWithMatchingShaderNames() {
        assertEquals(9, FillMode.values().length);
        List<String> names = List.of("aurora", "plasma", "cosmos", "cobweb", "fireworks",
            "galaxy", "nebula", "starfield", "stars");
        for (FillMode mode : FillMode.values()) {
            assertTrue(names.contains(mode.shaderName()),
                "unexpected shader name: " + mode.shaderName());
        }
    }

    @Test
    void defaultsMirrorDonor() {
        BlockOverlayModule module = new BlockOverlayModule();
        assertEquals("blockoverlay", module.id());
        assertEquals("Block Overlay", module.info().name());
        assertTrue(module.enabled(), "overlay should be on by default");
        assertTrue(module.fill.value());
        assertTrue(module.useShader.value());
        assertEquals(FillMode.AURORA, module.mode());
        assertEquals(70.0f, module.fillAlpha.value());
        assertEquals(255.0f, module.outlineAlpha.value());
        assertEquals(1.0f, module.shaderSpeed.value());
    }

    @Test
    void modeFollowsSettingIndex() {
        BlockOverlayModule module = new BlockOverlayModule();
        module.fillMode.set("Stars");
        assertEquals(FillMode.STARS, module.mode());
    }

    @Test
    void registersThroughModulesFacade() {
        BlockOverlayModule module = new BlockOverlayModule();
        Modules.register(module);
        assertSame(module, Modules.get("blockoverlay"));
    }
}
