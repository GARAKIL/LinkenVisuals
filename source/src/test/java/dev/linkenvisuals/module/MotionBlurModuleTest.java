package dev.linkenvisuals.module;

import dev.linkenvisuals.module.impl.MotionBlurModule;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** MotionBlur module wiring: id, defaults, registration. */
class MotionBlurModuleTest {
    @Test
    void defaultsMirrorDonor() {
        MotionBlurModule module = new MotionBlurModule();
        assertEquals("motionblur", module.id());
        assertEquals("Motion Blur", module.info().name());
        assertFalse(module.enabled(), "motion blur should be off by default");
        assertEquals(50.0f, module.strength.value());
        assertEquals(5.0f, module.strength.min());
        assertEquals(90.0f, module.strength.max());
    }

    @Test
    void registersThroughModulesFacade() {
        MotionBlurModule module = new MotionBlurModule();
        Modules.register(module);
        assertSame(module, Modules.get("motionblur"));
    }
}
