package dev.linkenvisuals.module.impl;

import dev.linkenvisuals.clickgui.Category;
import dev.linkenvisuals.clickgui.setting.BooleanSetting;
import dev.linkenvisuals.clickgui.setting.ColorSetting;
import dev.linkenvisuals.clickgui.setting.Setting;
import dev.linkenvisuals.clickgui.setting.SliderSetting;
import dev.linkenvisuals.module.Module;

import java.util.List;

/**
 * Donor HitRange: a rotating ring at the 3.32-block hit radius that turns red
 * when a visible opponent is inside it. All values mirror the donor module.
 */
public final class HitRangeModule extends Module {
    public final ColorSetting color;
    public final SliderSetting thickness;
    public final SliderSetting rotationSpeed;
    public final BooleanSetting depthTest;

    public HitRangeModule() {
        this(new ColorSetting("Color", 0x50B4FF),
            new SliderSetting("Thickness", 0.5f, 8.0f, 0.1f, 2.0f, null),
            new SliderSetting("Rotation Speed", 0.05f, 2.0f, 0.05f, 0.4f, null),
            new BooleanSetting("Depth Test", true));
    }

    private HitRangeModule(ColorSetting color, SliderSetting thickness,
                           SliderSetting rotationSpeed, BooleanSetting depthTest) {
        super("hitrange", "Hit Range", "rotating hit-distance ring, red near players",
            Category.VISUAL,
            List.<Setting>of(color, thickness, rotationSpeed),
            List.<Setting>of(depthTest));
        this.color = color;
        this.thickness = thickness;
        this.rotationSpeed = rotationSpeed;
        this.depthTest = depthTest;
        info().withClassicCategory(dev.linkenvisuals.clickgui.classic.ClassicCategory.WORLD);
    }
}
