package dev.linkenvisuals.module.impl;

import dev.linkenvisuals.clickgui.Category;
import dev.linkenvisuals.clickgui.setting.Setting;
import dev.linkenvisuals.clickgui.setting.SliderSetting;
import dev.linkenvisuals.module.Module;
import dev.linkenvisuals.render.MotionBlurRenderer;

import java.util.List;

/**
 * Motion Blur (Phase 5.4): frame-blend persistence of vision. Strength is the
 * share of the previous frame (5..90%, default 50%): higher = longer trails.
 * Only the world smears; HUD stays sharp (runs at END_MAIN).
 */
public final class MotionBlurModule extends Module {
    public final SliderSetting strength;

    public MotionBlurModule() {
        this(new SliderSetting("Strength", 5.0f, 90.0f, 5.0f, 50.0f, "%"));
    }

    private MotionBlurModule(SliderSetting strength) {
        super("motionblur", "Motion Blur", "frame-blend persistence of vision",
            Category.VISUAL,
            List.of(),
            List.<Setting>of(strength));
        this.strength = strength;
    }

    @Override
    protected void onDisable() {
        MotionBlurRenderer.shutdown();
    }
}
