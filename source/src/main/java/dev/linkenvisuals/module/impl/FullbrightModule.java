package dev.linkenvisuals.module.impl;

import dev.linkenvisuals.clickgui.Category;
import dev.linkenvisuals.module.Module;

import java.util.List;

/**
 * Fullbright (Phase 5.7): maximum brightness via lightmap clear.
 * Donor's {@code Fullbright} + {@code LightmapTextureManagerMixin} ported.
 */
public final class FullbrightModule extends Module {
    public FullbrightModule() {
        super("fullbright", "Fullbright", "maximum brightness",
            Category.VISUAL, List.of(), List.of());
    }
}
