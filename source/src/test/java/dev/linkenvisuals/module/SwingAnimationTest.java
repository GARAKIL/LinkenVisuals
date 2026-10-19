package dev.linkenvisuals.module;

import dev.linkenvisuals.clickgui.Category;
import dev.linkenvisuals.clickgui.setting.ModeSetting;
import dev.linkenvisuals.clickgui.setting.SliderSetting;
import dev.linkenvisuals.module.impl.SwingAnimationModule;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Locks the pure part of the swing animation: the curve is 0 at both ends and
 * 1 in the middle, and every mode produces a bounded angle that scales with
 * power. Guards against accidental NaN/Infinity from a bad formula.
 */
class SwingAnimationTest {
    @Test
    void curveIsZeroAtBothEndsAndOneInTheMiddle() {
        assertEquals(0.0f, SwingAnimationModule.curve(0.0f), 1.0e-6f);
        assertEquals(1.0f, SwingAnimationModule.curve(0.25f), 1.0e-5f);
        assertEquals(0.0f, SwingAnimationModule.curve(1.0f), 1.0e-5f);
    }

    @Test
    void curveClampsOutOfRangeProgress() {
        assertEquals(0.0f, SwingAnimationModule.curve(-3.0f), 1.0e-6f);
        assertEquals(0.0f, SwingAnimationModule.curve(7.0f), 1.0e-5f);
    }

    @Test
    void everyModeProducesFiniteAngles() {
        for (String mode : SwingAnimationModule.MODES) {
            for (float progress = 0.0f; progress <= 1.0f; progress += 0.05f) {
                float angle = SwingAnimationModule.xAngle(mode, progress, 5.0f);
                assertTrue(Float.isFinite(angle), mode + " produced " + angle);
            }
        }
    }

    @Test
    void powerScalesTheAngle() {
        float weak = SwingAnimationModule.xAngle("Classic", 0.5f, 2.5f);
        float strong = SwingAnimationModule.xAngle("Classic", 0.5f, 7.5f);
        assertEquals(3.0f, strong / weak, 1.0e-4f);
    }

    @Test
    void modeListMatchesTheSettingOptions() {
        ModeSetting setting = new SwingAnimationModule().mode;
        assertEquals(SwingAnimationModule.MODES, setting.options());
        assertTrue(SwingAnimationModule.MODES.size() == 7);
    }

    @Test
    void powerSliderDefaultsToNeutralBoost() {
        SliderSetting power = new SwingAnimationModule().power;
        assertEquals(5.0f, power.value(), 1.0e-6f);
    }

    @Test
    void categoryIsVisual() {
        assertEquals(Category.VISUAL, new SwingAnimationModule().info().category());
    }
}
