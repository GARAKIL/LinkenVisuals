package dev.linkenvisuals.module.impl;

import dev.linkenvisuals.clickgui.Category;
import dev.linkenvisuals.clickgui.setting.BooleanSetting;
import dev.linkenvisuals.clickgui.setting.ColorSetting;
import dev.linkenvisuals.clickgui.setting.Setting;
import dev.linkenvisuals.clickgui.setting.SliderSetting;
import dev.linkenvisuals.module.Module;
import dev.linkenvisuals.ui.Theme;

import java.util.List;

/**
 * Donor CustomHitBox: filled/outlined boxes around visible entities with a
 * 128-block distance cap, LOS raycasts with a 120 ms cache, and self/item
 * switches.
 */
public final class CustomHitBoxModule extends Module {
    public final BooleanSetting fill;
    public final SliderSetting fillAlpha;
    public final SliderSetting lineWidth;
    public final ColorSetting color;
    public final BooleanSetting showSelf;
    public final BooleanSetting showItems;

    public CustomHitBoxModule() {
        this(new BooleanSetting("Fill", true),
            new SliderSetting("Fill Alpha", 0.0f, 255.0f, 1.0f, 90.0f, null),
            new SliderSetting("Line Width", 1.0f, 8.0f, 0.5f, 1.5f, null),
            new ColorSetting("Color", Theme.DEFAULT_ACCENT),
            new BooleanSetting("Show Self", false),
            new BooleanSetting("Show Items", false));
    }

    private CustomHitBoxModule(BooleanSetting fill, SliderSetting fillAlpha,
                               SliderSetting lineWidth, ColorSetting color,
                               BooleanSetting showSelf, BooleanSetting showItems) {
        super("customhitbox", "Custom HitBox", "filled and outlined entity hitboxes",
            Category.VISUAL,
            List.<Setting>of(fill, fillAlpha, lineWidth, color),
            List.<Setting>of(showSelf, showItems));
        this.fill = fill;
        this.fillAlpha = fillAlpha;
        this.lineWidth = lineWidth;
        this.color = color;
        this.showSelf = showSelf;
        this.showItems = showItems;
        info().withClassicCategory(dev.linkenvisuals.clickgui.classic.ClassicCategory.WORLD);
    }
}
