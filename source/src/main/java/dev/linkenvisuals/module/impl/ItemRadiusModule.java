package dev.linkenvisuals.module.impl;

import dev.linkenvisuals.clickgui.Category;
import dev.linkenvisuals.clickgui.setting.BooleanSetting;
import dev.linkenvisuals.clickgui.setting.ColorSetting;
import dev.linkenvisuals.clickgui.setting.Setting;
import dev.linkenvisuals.clickgui.setting.SliderSetting;
import dev.linkenvisuals.module.Module;

import java.util.List;

/**
 * Donor ItemRadius: world-space circles/boxes/walls showing the effect radius
 * of the item in hand. Defaults, ranges and item list mirror the donor.
 */
public final class ItemRadiusModule extends Module {
    public final BooleanSetting enderEye;
    public final ColorSetting enderEyeColor;
    public final BooleanSetting sugar;
    public final ColorSetting sugarColor;
    public final BooleanSetting netheriteScrap;
    public final ColorSetting netheriteScrapColor;
    public final BooleanSetting dragonTrap;
    public final BooleanSetting driedKelp;
    public final ColorSetting driedKelpColor;
    public final BooleanSetting snowball;
    public final ColorSetting snowballColor;
    public final BooleanSetting explosivePotion;
    public final SliderSetting explosivePotionRadius;
    public final ColorSetting explosivePotionColor;
    public final BooleanSetting proximityColor;
    public final ColorSetting proximityColorSetting;

    public ItemRadiusModule() {
        this(
            new BooleanSetting("Ender Eye", true),
            new ColorSetting("Ender Eye Colour", 0xFFFFFF),
            new BooleanSetting("Sugar", true),
            new ColorSetting("Sugar Colour", 0xFFFFFF),
            new BooleanSetting("Netherite Scrap", true),
            new ColorSetting("Netherite Scrap Colour", 0xFFFFFF),
            new BooleanSetting("Dragon Trap", false),
            new BooleanSetting("Dried Kelp", true),
            new ColorSetting("Dried Kelp Colour", 0xFFFFFF),
            new BooleanSetting("Snowball", true),
            new ColorSetting("Snowball Colour", 0xFFFFFF),
            new BooleanSetting("Explosive Potion", true),
            new SliderSetting("Potion Radius", 1.0f, 30.0f, 0.5f, 4.0f, "m"),
            new ColorSetting("Potion Colour", 0xFFFFFF),
            new BooleanSetting("Proximity Colour", true),
            new ColorSetting("Proximity Colour Value", 0xFF00FF));
    }

    private ItemRadiusModule(BooleanSetting enderEye, ColorSetting enderEyeColor,
                             BooleanSetting sugar, ColorSetting sugarColor,
                             BooleanSetting netheriteScrap, ColorSetting netheriteScrapColor,
                             BooleanSetting dragonTrap, BooleanSetting driedKelp,
                             ColorSetting driedKelpColor, BooleanSetting snowball,
                             ColorSetting snowballColor, BooleanSetting explosivePotion,
                             SliderSetting explosivePotionRadius, ColorSetting explosivePotionColor,
                             BooleanSetting proximityColor, ColorSetting proximityColorSetting) {
        super("itemradius", "Item Radius", "effect radius of the item in hand",
            Category.VISUAL,
            List.<Setting>of(enderEye, enderEyeColor, sugar, sugarColor, netheriteScrap,
                netheriteScrapColor, dragonTrap, driedKelp, driedKelpColor, snowball,
                snowballColor),
            List.<Setting>of(explosivePotion, explosivePotionRadius, explosivePotionColor,
                proximityColor, proximityColorSetting));
        this.enderEye = enderEye;
        this.enderEyeColor = enderEyeColor;
        this.sugar = sugar;
        this.sugarColor = sugarColor;
        this.netheriteScrap = netheriteScrap;
        this.netheriteScrapColor = netheriteScrapColor;
        this.dragonTrap = dragonTrap;
        this.driedKelp = driedKelp;
        this.driedKelpColor = driedKelpColor;
        this.snowball = snowball;
        this.snowballColor = snowballColor;
        this.explosivePotion = explosivePotion;
        this.explosivePotionRadius = explosivePotionRadius;
        this.explosivePotionColor = explosivePotionColor;
        this.proximityColor = proximityColor;
        this.proximityColorSetting = proximityColorSetting;
        info().withClassicCategory(dev.linkenvisuals.clickgui.classic.ClassicCategory.WORLD);
    }
}
