package dev.linkenvisuals.module.impl;

import dev.linkenvisuals.clickgui.Category;
import dev.linkenvisuals.clickgui.setting.BooleanSetting;
import dev.linkenvisuals.clickgui.setting.ColorSetting;
import dev.linkenvisuals.clickgui.setting.ModeSetting;
import dev.linkenvisuals.clickgui.setting.Setting;
import dev.linkenvisuals.clickgui.setting.SliderSetting;
import dev.linkenvisuals.module.Module;

import java.util.List;

/**
 * Donor FortniteDamage: floating damage numbers at the hit position with the
 * donor timing, rise and crit colour. Only one donor style exists; the control
 * is kept so the module remains a faithful port.
 */
public final class FortniteDamageModule extends Module {
    public final ModeSetting style;
    public final SliderSetting size;
    public final SliderSetting lifetime;
    public final SliderSetting rise;
    public final BooleanSetting themeColor;
    public final ColorSetting color;
    public final BooleanSetting critColor;

    public FortniteDamageModule() {
        this(new ModeSetting("Style", List.of("Fortnite"), 0),
            new SliderSetting("Size", 0.012f, 0.08f, 0.001f, 0.032f, null),
            new SliderSetting("Lifetime", 300.0f, 1800.0f, 50.0f, 850.0f, "ms"),
            new SliderSetting("Rise", 0.0f, 1.5f, 0.05f, 0.42f, null),
            new BooleanSetting("Theme Color", false),
            new ColorSetting("Color", 0xFFFFFF),
            new BooleanSetting("Crit Color", true));
    }

    private FortniteDamageModule(ModeSetting style, SliderSetting size,
                                 SliderSetting lifetime, SliderSetting rise,
                                 BooleanSetting themeColor, ColorSetting color,
                                 BooleanSetting critColor) {
        super("fortnitedamage", "Fortnite Damage", "floating damage numbers on hit",
            Category.VISUAL,
            List.<Setting>of(style, size, lifetime, rise),
            List.<Setting>of(themeColor, color, critColor));
        this.style = style;
        this.size = size;
        this.lifetime = lifetime;
        this.rise = rise;
        this.themeColor = themeColor;
        this.color = color;
        this.critColor = critColor;
        info().withClassicCategory(dev.linkenvisuals.clickgui.classic.ClassicCategory.WORLD);
    }

    @Override
    protected void onDisable() {
        dev.linkenvisuals.render.FortniteDamageRenderer.clear();
    }
}
