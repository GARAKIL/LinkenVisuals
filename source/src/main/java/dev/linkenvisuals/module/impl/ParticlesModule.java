package dev.linkenvisuals.module.impl;

import dev.linkenvisuals.clickgui.Category;
import dev.linkenvisuals.clickgui.setting.BooleanSetting;
import dev.linkenvisuals.clickgui.setting.ColorSetting;
import dev.linkenvisuals.clickgui.setting.ModeSetting;
import dev.linkenvisuals.clickgui.setting.Setting;
import dev.linkenvisuals.clickgui.setting.SliderSetting;
import dev.linkenvisuals.module.Module;
import dev.linkenvisuals.render.ParticlesRenderer;
import dev.linkenvisuals.ui.Theme;

import java.util.List;

/**
 * Donor ambient Particles: five textured billboard modes plus a wireframe cube
 * mode, spawned around the player with the donor life/speed/drag values.
 */
public final class ParticlesModule extends Module {
    public final SliderSetting maxParticles;
    public final SliderSetting particleSize;
    public final ColorSetting particleColor;
    public final ModeSetting mode;
    public final BooleanSetting cubeGlow;
    public final SliderSetting cubeGlowSize;
    public final SliderSetting cubeRotateSpeed;

    public ParticlesModule() {
        this(new SliderSetting("Max Particles", 0.0f, 15000.0f, 50.0f, 600.0f, null),
            new SliderSetting("Size", 0.04f, 2.5f, 0.01f, 0.25f, null),
            new ColorSetting("Color", Theme.DEFAULT_ACCENT),
            new ModeSetting("Mode", List.of("Bloom", "Star", "Snow", "Firefly", "Dollar", "Cube"), 0),
            new BooleanSetting("Cube Glow", true),
            new SliderSetting("Cube Glow Size", 0.5f, 4.0f, 0.1f, 2.0f, "x"),
            new SliderSetting("Cube Rotate Speed", 0.5f, 6.0f, 0.1f, 2.0f, null));
    }

    private ParticlesModule(SliderSetting maxParticles, SliderSetting particleSize,
                            ColorSetting particleColor, ModeSetting mode,
                            BooleanSetting cubeGlow, SliderSetting cubeGlowSize,
                            SliderSetting cubeRotateSpeed) {
        super("particles", "Particles", "ambient particles around the player",
            Category.VISUAL,
            List.<Setting>of(maxParticles, particleSize, particleColor, mode),
            List.<Setting>of(cubeGlow, cubeGlowSize, cubeRotateSpeed));
        this.maxParticles = maxParticles;
        this.particleSize = particleSize;
        this.particleColor = particleColor;
        this.mode = mode;
        this.cubeGlow = cubeGlow;
        this.cubeGlowSize = cubeGlowSize;
        this.cubeRotateSpeed = cubeRotateSpeed;
        info().withClassicCategory(dev.linkenvisuals.clickgui.classic.ClassicCategory.WORLD);
    }

    @Override
    protected void onDisable() {
        ParticlesRenderer.clear();
    }
}
