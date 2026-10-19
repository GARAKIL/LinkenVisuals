package dev.linkenvisuals.module.impl;

import dev.linkenvisuals.clickgui.Category;
import dev.linkenvisuals.clickgui.setting.KeySetting;
import dev.linkenvisuals.clickgui.setting.Setting;
import dev.linkenvisuals.clickgui.setting.SliderSetting;
import dev.linkenvisuals.module.Module;
import org.lwjgl.glfw.GLFW;

import java.util.List;

/**
 * Zoom (Phase 5.7): smooth FOV zoom on key hold.
 * Donor's {@code Zoom} (91 lines) ported.
 */
public final class ZoomModule extends Module {
    public final KeySetting key;
    public final SliderSetting multiplier;
    public final SliderSetting smoothSpeed;

    private float progressPrev;
    private float progress;

    public ZoomModule() {
        this(new KeySetting("Key", GLFW.GLFW_KEY_C),
             new SliderSetting("Multiplier", 0.05f, 1.0f, 0.01f, 0.80f, ""),
             new SliderSetting("Smoothness", 0.01f, 1.0f, 0.01f, 0.89f, ""));
    }

    private ZoomModule(KeySetting key, SliderSetting multiplier, SliderSetting smoothSpeed) {
        super("zoom", "Zoom", "smooth FOV zoom",
            Category.VISUAL,
            List.of(),
            List.<Setting>of(key, multiplier, smoothSpeed));
        this.key = key;
        this.multiplier = multiplier;
        this.smoothSpeed = smoothSpeed;
    }

    @Override
    protected void onDisable() {
        progressPrev = 0.0f;
        progress = 0.0f;
    }

    public void tick(boolean zoomKeyDown) {
        float target = zoomKeyDown ? 1.0f : 0.0f;
        progressPrev = progress;
        float smoothness = smoothSpeed.value();
        float step = 1.0f - smoothness; // higher smoothness = slower
        progress += (target - progress) * Math.max(0.01f, step);
        progress = Math.max(0.0f, Math.min(1.0f, progress));
    }

    public float getFovMultiplier(float tickDelta) {
        float p = progressPrev + (progress - progressPrev) * tickDelta;
        p = Math.max(0.0f, Math.min(1.0f, p));
        float target = 1.0f - (1.0f - multiplier.value()) * p;
        return target;
    }

    public boolean isZoomKeyDown() {
        // Checked via GLFW in the mixin/tick; simplified here.
        return false;
    }
}
