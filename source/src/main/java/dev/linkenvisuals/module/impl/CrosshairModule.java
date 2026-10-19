package dev.linkenvisuals.module.impl;

import dev.linkenvisuals.clickgui.Category;
import dev.linkenvisuals.clickgui.setting.ModeSetting;
import dev.linkenvisuals.clickgui.setting.Setting;
import dev.linkenvisuals.module.Module;

import java.util.List;

/**
 * Crosshair (Phase 5.7): custom crosshair styles.
 * Donor's {@code Crosshair} (261 lines) simplified: style + color.
 */
public final class CrosshairModule extends Module {
    public final ModeSetting style;

    public CrosshairModule() {
        this(new ModeSetting("Style", List.of("Cross", "Dot", "Circle"), 0));
    }

    private CrosshairModule(ModeSetting style) {
        super("crosshair", "Crosshair", "custom crosshair",
            Category.VISUAL,
            List.of(),
            List.<Setting>of(style));
        this.style = style;
    }
}
