package dev.linkenvisuals.module.impl;

import dev.linkenvisuals.clickgui.Category;
import dev.linkenvisuals.clickgui.setting.BooleanSetting;
import dev.linkenvisuals.clickgui.setting.ColorSetting;
import dev.linkenvisuals.clickgui.setting.ModeSetting;
import dev.linkenvisuals.clickgui.setting.Setting;
import dev.linkenvisuals.clickgui.setting.SliderSetting;
import dev.linkenvisuals.module.Module;
import dev.linkenvisuals.ui.Theme;

import java.util.List;

/**
 * Donor DamageParticles: the legacy impact burst plus the decorative
 * attack/projectile modes. Kept separate from the ambient {@code Particles}
 * module because the donor exposes it as its own Visual module.
 */
public final class DamageParticlesModule extends Module {
    public final ColorSetting color;
    public final SliderSetting count;
    public final SliderSetting size;
    public final BooleanSetting spawnOnAttack;
    public final BooleanSetting spawnOnThrow;
    public final ModeSetting mode;
    public final SliderSetting particleSize;

    public DamageParticlesModule() {
        this(new ColorSetting("Color", Theme.DEFAULT_ACCENT),
            new SliderSetting("Count", 10.0f, 60.0f, 1.0f, 30.0f, null),
            new SliderSetting("Size", 10.0f, 55.0f, 1.0f, 30.0f, null),
            new BooleanSetting("Spawn On Attack", true),
            new BooleanSetting("Spawn On Throw", true),
            new ModeSetting("Mode", List.of("Bloom", "Star", "Snow", "Firefly", "Dollar", "Cube"), 0),
            new SliderSetting("Particle Size", 0.0f, 1.0f, 0.1f, 0.5f, null));
    }

    private DamageParticlesModule(ColorSetting color, SliderSetting count,
                                  SliderSetting size, BooleanSetting spawnOnAttack,
                                  BooleanSetting spawnOnThrow, ModeSetting mode,
                                  SliderSetting particleSize) {
        super("damageparticles", "Damage Particles", "impact and projectile particles on hit",
            Category.VISUAL,
            List.<Setting>of(color, count, size, spawnOnAttack),
            List.<Setting>of(spawnOnThrow, mode, particleSize));
        this.color = color;
        this.count = count;
        this.size = size;
        this.spawnOnAttack = spawnOnAttack;
        this.spawnOnThrow = spawnOnThrow;
        this.mode = mode;
        this.particleSize = particleSize;
        info().withClassicCategory(dev.linkenvisuals.clickgui.classic.ClassicCategory.WORLD);
    }

    @Override
    protected void onDisable() {
        dev.linkenvisuals.render.DamageParticlesRenderer.clear();
    }
}
