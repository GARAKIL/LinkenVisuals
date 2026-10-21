package dev.linkenvisuals.module;

import dev.linkenvisuals.clickgui.setting.BooleanSetting;
import dev.linkenvisuals.clickgui.setting.SliderSetting;
import dev.linkenvisuals.module.impl.SoundsModule;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SoundsModuleTest {
    @Test
    void defaultsKeepCombatCuesAudibleButConfigurable() {
        SoundsModule module = new SoundsModule();

        assertEquals("sounds", module.id());
        assertTrue(module.enabled(), "ported donor cues should work without an extra toggle");
        assertEquals(7, module.info().leftSettings().size() + module.info().rightSettings().size());

        SliderSetting volume = (SliderSetting) module.info().leftSettings().getFirst();
        assertEquals(130.0f, volume.value(), 0.001f);
        assertEquals(0.0f, volume.min(), 0.001f);
        assertEquals(200.0f, volume.max(), 0.001f);

        BooleanSetting kills = (BooleanSetting) module.info().leftSettings().get(1);
        assertTrue(kills.value());
    }
}
