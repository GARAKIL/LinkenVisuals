package dev.linkenvisuals.module.impl;

import dev.linkenvisuals.clickgui.Category;
import dev.linkenvisuals.clickgui.setting.Setting;
import dev.linkenvisuals.clickgui.setting.SliderSetting;
import dev.linkenvisuals.module.Module;
import dev.linkenvisuals.render.JumpCirclesRenderer;

import java.util.List;

/**
 * JumpCircles (Phase 5.7): expanding rings on the ground when jumping.
 * Donor's {@code JumpCircles} (115 lines) ported: no texture, procedural
 * ring lines with fade.
 */
public final class JumpCirclesModule extends Module {
    public final SliderSetting size;
    public final SliderSetting lifetime;

    public JumpCirclesModule() {
        this(new SliderSetting("Size", 1.0f, 2.0f, 0.1f, 1.5f, ""),
             new SliderSetting("Lifetime", 1.0f, 10.0f, 0.1f, 4.0f, "s"));
    }

    private JumpCirclesModule(SliderSetting size, SliderSetting lifetime) {
        super("jumpcircles", "Jump Circles", "expanding rings on jump",
            Category.VISUAL,
            List.of(),
            List.<Setting>of(size, lifetime));
        this.size = size;
        this.lifetime = lifetime;
    }

    @Override
    protected void onDisable() {
        JumpCirclesRenderer.clear();
    }
}
