package dev.linkenvisuals.module.impl;

import dev.linkenvisuals.clickgui.Category;
import dev.linkenvisuals.clickgui.setting.BooleanSetting;
import dev.linkenvisuals.clickgui.setting.ColorSetting;
import dev.linkenvisuals.clickgui.setting.Setting;
import dev.linkenvisuals.module.Module;
import dev.linkenvisuals.ui.Theme;

import java.util.List;

/**
 * Donor Trajectories: simulated projectile arc and impact marker; the optional
 * explosive radius is shown for item names/status effects matching donor
 * explosive keywords. Server boss-bar heuristics are intentionally omitted.
 */
public final class TrajectoriesModule extends Module {
    public final BooleanSetting explosiveCircle;
    public final ColorSetting color;

    public TrajectoriesModule() {
        this(new BooleanSetting("Explosive Circle", true),
            new ColorSetting("Color", Theme.DEFAULT_ACCENT));
    }

    private TrajectoriesModule(BooleanSetting explosiveCircle, ColorSetting color) {
        super("trajectories", "Trajectories", "projectile arc with impact marker",
            Category.VISUAL,
            List.<Setting>of(explosiveCircle),
            List.<Setting>of(color));
        this.explosiveCircle = explosiveCircle;
        this.color = color;
        info().withClassicCategory(dev.linkenvisuals.clickgui.classic.ClassicCategory.WORLD);
    }
}
