package dev.linkenvisuals.module.impl;

import dev.linkenvisuals.clickgui.Category;
import dev.linkenvisuals.clickgui.setting.BooleanSetting;
import dev.linkenvisuals.clickgui.setting.ModeSetting;
import dev.linkenvisuals.clickgui.setting.Setting;
import dev.linkenvisuals.clickgui.setting.SliderSetting;
import dev.linkenvisuals.module.Module;
import dev.linkenvisuals.render.TargetEspRenderer;

import java.util.List;

/**
 * TargetEsp (donor {@code TargetEsp}, 689 lines): an animated marker around the
 * entity under the crosshair. Four modes — Original, Ghosts, Circles, Crystals.
 * The effect is depth-tested and never drawn through walls; invisible players
 * without visible equipment are skipped.
 */
public final class TargetEspModule extends Module {
    public final ModeSetting mode;
    public final BooleanSetting showNpcs;
    public final SliderSetting size;
    public final SliderSetting ghostSpeed;
    public final SliderSetting crystalCount;

    public TargetEspModule() {
        this(new ModeSetting("Mode", List.of("Original", "Ghosts", "Circles", "Crystals"), 0),
             new BooleanSetting("Show NPCs", false),
             new SliderSetting("Size", 10.0f, 25.0f, 1.0f, 15.0f, ""),
             new SliderSetting("Ghost Speed", 0.2f, 3.0f, 0.05f, 1.0f, ""),
             new SliderSetting("Crystal Count", 4.0f, 40.0f, 1.0f, 14.0f, ""));
    }

    private TargetEspModule(ModeSetting mode, BooleanSetting showNpcs, SliderSetting size,
                            SliderSetting ghostSpeed, SliderSetting crystalCount) {
        super("targetesp", "Target ESP", "animated marker around the crosshair target",
            Category.VISUAL,
            List.of(),
            List.<Setting>of(mode, showNpcs, size, ghostSpeed, crystalCount));
        this.mode = mode;
        this.showNpcs = showNpcs;
        this.size = size;
        this.ghostSpeed = ghostSpeed;
        this.crystalCount = crystalCount;
    }

    @Override
    protected void onDisable() {
        TargetEspRenderer.reset();
    }
}
