package dev.linkenvisuals.module.impl;

import dev.linkenvisuals.clickgui.Category;
import dev.linkenvisuals.clickgui.setting.BooleanSetting;
import dev.linkenvisuals.clickgui.setting.ModeSetting;
import dev.linkenvisuals.clickgui.setting.Setting;
import dev.linkenvisuals.clickgui.setting.SliderSetting;
import dev.linkenvisuals.module.Module;

import java.util.List;

/**
 * BlockOverlay (Phase 5.2, ported from the donor): replaces the vanilla block
 * outline with an animated shader fill (nine effects) plus a glow outline.
 * First object-based module — exercises the Phase 4 framework.
 */
public final class BlockOverlayModule extends Module {
    public enum FillMode {
        AURORA,
        PLASMA,
        COSMOS,
        COBWEB,
        FIREWORKS,
        GALAXY,
        NEBULA,
        STARFIELD,
        STARS;

        /** Fragment shader name suffix, e.g. {@code "plasma"}. */
        public String shaderName() {
            return name().toLowerCase();
        }
    }

    public final BooleanSetting fill;
    public final BooleanSetting useShader;
    public final ModeSetting fillMode;
    public final SliderSetting fillAlpha;
    public final SliderSetting outlineAlpha;
    public final SliderSetting shaderSpeed;

    public BlockOverlayModule() {
        this(new BooleanSetting("Fill", true),
            new BooleanSetting("Use Shader", true),
            new ModeSetting("Mode", List.of("Aurora", "Plasma", "Cosmos", "Cobweb", "Fireworks",
                "Galaxy", "Nebula", "Starfield", "Stars"), 0),
            new SliderSetting("Fill Alpha", 0.0f, 255.0f, 1.0f, 70.0f, null),
            new SliderSetting("Outline Alpha", 0.0f, 255.0f, 1.0f, 255.0f, null),
            new SliderSetting("Shader Speed", 0.1f, 5.0f, 0.1f, 1.0f, "x"));
    }

    private BlockOverlayModule(BooleanSetting fill, BooleanSetting useShader, ModeSetting fillMode,
                               SliderSetting fillAlpha, SliderSetting outlineAlpha,
                               SliderSetting shaderSpeed) {
        super("blockoverlay", "Block Overlay", "animated shader outline for the targeted block",
            Category.VISUAL,
            List.<Setting>of(fill, useShader, fillMode),
            List.<Setting>of(fillAlpha, outlineAlpha, shaderSpeed));
        this.fill = fill;
        this.useShader = useShader;
        this.fillMode = fillMode;
        this.fillAlpha = fillAlpha;
        this.outlineAlpha = outlineAlpha;
        this.shaderSpeed = shaderSpeed;
        info().toggleSilently(true);
    }

    public FillMode mode() {
        return FillMode.values()[fillMode.index()];
    }
}
