package dev.linkenvisuals.module.impl;

import dev.linkenvisuals.clickgui.Category;
import dev.linkenvisuals.clickgui.setting.BooleanSetting;
import dev.linkenvisuals.clickgui.setting.ModeSetting;
import dev.linkenvisuals.clickgui.setting.Setting;
import dev.linkenvisuals.clickgui.setting.SliderSetting;
import dev.linkenvisuals.hud.CustomVitalsHud;
import dev.linkenvisuals.module.Module;

import java.util.List;

/**
 * Donor CustomVitals: replaces the vanilla health/food/armour/air/XP bars with
 * a hotbar-anchored Linken bar. The drawing lives in {@link CustomVitalsHud};
 * this module only owns the donor settings so the HUD is toggleable and
 * persisted like every other module.
 */
public final class CustomVitalsModule extends Module {
    public final ModeSetting style;
    public final SliderSetting segments;
    public final BooleanSetting armorRow;

    public CustomVitalsModule() {
        this(new ModeSetting("Style", List.of("Segments", "Stack", "Bars"), 0),
            new SliderSetting("Segments", 3.0f, 10.0f, 1.0f, 8.0f, ""),
            new BooleanSetting("Armor Row", true));
    }

    private CustomVitalsModule(ModeSetting style, SliderSetting segments, BooleanSetting armorRow) {
        super("customvitals", "Custom Vitals", "HP, food, armour, air and XP above the hotbar",
            Category.VISUAL, List.of(style, segments), List.of(armorRow));
        this.style = style;
        this.segments = segments;
        this.armorRow = armorRow;
    }

    public int styleIndex() {
        return switch (style.current()) {
            case "Stack" -> 1;
            case "Bars" -> 2;
            default -> 0;
        };
    }

    public int segmentCount() {
        return Math.round(segments.value());
    }

    public boolean armorRow() {
        return armorRow.value();
    }

    public boolean isSegments() {
        return styleIndex() == 0;
    }
}
