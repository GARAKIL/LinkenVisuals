package dev.linkenvisuals.module.impl;

import dev.linkenvisuals.clickgui.Category;
import dev.linkenvisuals.clickgui.setting.Setting;
import dev.linkenvisuals.clickgui.setting.SliderSetting;
import dev.linkenvisuals.clickgui.setting.BooleanSetting;
import dev.linkenvisuals.module.Module;
import dev.linkenvisuals.render.TrailsRenderer;

import java.util.List;

/**
 * Trails (Phase 5.7): 3D particle trail behind the player.
 * Donor's {@code Trails} (258 lines) simplified: no bloom texture,
 * colored billboard quads with fade-out.
 */
public final class TrailsModule extends Module {
    public final SliderSetting length;
    public final BooleanSetting showInFirstPerson;

    public TrailsModule() {
        this(new SliderSetting("Length", 500.0f, 3000.0f, 100.0f, 1500.0f, "ms"),
             new BooleanSetting("First Person", true));
    }

    private TrailsModule(SliderSetting length, BooleanSetting showInFirstPerson) {
        super("trails", "Trails", "particle trail behind player",
            Category.VISUAL,
            List.of(),
            List.<Setting>of(length, showInFirstPerson));
        this.length = length;
        this.showInFirstPerson = showInFirstPerson;
    }

    @Override
    protected void onEnable() {
        TrailsRenderer.clear();
    }

    @Override
    protected void onDisable() {
        TrailsRenderer.clear();
    }
}
