package dev.linkenvisuals.module.impl;

import dev.linkenvisuals.clickgui.Category;
import dev.linkenvisuals.clickgui.setting.Setting;
import dev.linkenvisuals.module.Module;

import java.util.List;

/**
 * Donor SelfNametags is a toggle, not a renderer: when enabled it runs the
 * StreamerMode self-name sanitizer on the local player's display name. Keeping
 * the donor split means the global StreamerMode and the self-only switch can be
 * enabled independently.
 */
public final class SelfNametagsModule extends Module {
    public SelfNametagsModule() {
        super("selfnametags", "Self Nametags", "sanitize your own nametag with Streamer Mode",
            Category.VISUAL, List.<Setting>of(), List.<Setting>of());
        info().withClassicCategory(dev.linkenvisuals.clickgui.classic.ClassicCategory.WORLD);
    }
}
