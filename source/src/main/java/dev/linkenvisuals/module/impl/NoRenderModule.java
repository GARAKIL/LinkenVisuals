package dev.linkenvisuals.module.impl;

import dev.linkenvisuals.clickgui.Category;
import dev.linkenvisuals.clickgui.setting.BooleanSetting;
import dev.linkenvisuals.clickgui.setting.Setting;
import dev.linkenvisuals.module.Module;

import java.util.List;

/**
 * Donor NoRender: independent switches for vanilla overlays that are normally
 * impossible to disable from the client options.
 */
public final class NoRenderModule extends Module {
    public final BooleanSetting hurtCamera;
    public final BooleanSetting fire;
    public final BooleanSetting totem;
    public final BooleanSetting scoreboard;
    public final BooleanSetting bossBar;
    public final BooleanSetting particles;
    public final BooleanSetting armor;
    public final BooleanSetting inventoryBackground;

    public NoRenderModule() {
        this(new BooleanSetting("Hurt Camera", true),
            new BooleanSetting("Fire", true),
            new BooleanSetting("Totem Pop", true),
            new BooleanSetting("Scoreboard", false),
            new BooleanSetting("Boss Bar", false),
            new BooleanSetting("Particles", true),
            new BooleanSetting("Armor", false),
            new BooleanSetting("Inventory Background", false));
    }

    private NoRenderModule(BooleanSetting hurtCamera, BooleanSetting fire, BooleanSetting totem,
                           BooleanSetting scoreboard, BooleanSetting bossBar, BooleanSetting particles,
                           BooleanSetting armor, BooleanSetting inventoryBackground) {
        super("norender", "No Render", "hide vanilla overlays",
            Category.VISUAL,
            List.of(hurtCamera, fire, totem, scoreboard),
            List.<Setting>of(bossBar, particles, armor, inventoryBackground));
        this.hurtCamera = hurtCamera;
        this.fire = fire;
        this.totem = totem;
        this.scoreboard = scoreboard;
        this.bossBar = bossBar;
        this.particles = particles;
        this.armor = armor;
        this.inventoryBackground = inventoryBackground;
        info().withClassicCategory(dev.linkenvisuals.clickgui.classic.ClassicCategory.WORLD);
    }
}
