package dev.linkenvisuals.module.impl;

import dev.linkenvisuals.clickgui.Category;
import dev.linkenvisuals.clickgui.setting.KeybindSetting;
import dev.linkenvisuals.clickgui.setting.Setting;
import dev.linkenvisuals.helper.HelperActions;
import dev.linkenvisuals.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Items;

import java.util.List;

/** Donor FtHelper: hotbar selection for Funtime PvP helper items. */
public final class FtHelperModule extends Module {
    public final KeybindSetting desor;
    public final KeybindSetting trapka;
    public final KeybindSetting yavka;
    public final KeybindSetting bozhka;
    public final KeybindSetting plast;
    public final KeybindSetting snowball;

    public FtHelperModule() {
        this(new KeybindSetting("Desor", "NONE", true),
            new KeybindSetting("Trapka", "NONE", true),
            new KeybindSetting("Yavka", "NONE", true),
            new KeybindSetting("Bozhka", "NONE", true),
            new KeybindSetting("Plast", "NONE", true),
            new KeybindSetting("Snowball", "NONE", true));
    }

    private FtHelperModule(KeybindSetting desor, KeybindSetting trapka, KeybindSetting yavka,
                           KeybindSetting bozhka, KeybindSetting plast, KeybindSetting snowball) {
        super("fthelper", "Ft Helper", "select Funtime helper items",
            Category.MISC, List.of(), List.<Setting>of(desor, trapka, yavka, bozhka, plast, snowball));
        this.desor = desor;
        this.trapka = trapka;
        this.yavka = yavka;
        this.bozhka = bozhka;
        this.plast = plast;
        this.snowball = snowball;
    }

    public void onKey(MinecraftClient client, int key) {
        if (!enabled()) return;
        if (desor.matchesKey(key)) HelperActions.select(client, Items.ENDER_EYE, "Desor");
        if (trapka.matchesKey(key)) HelperActions.select(client, Items.NETHERITE_SCRAP, "Trapka");
        if (yavka.matchesKey(key)) HelperActions.select(client, Items.SUGAR, "Yavka");
        if (bozhka.matchesKey(key)) HelperActions.select(client, Items.PHANTOM_MEMBRANE, "Bozhka");
        if (plast.matchesKey(key)) HelperActions.select(client, Items.DRIED_KELP, "Plast");
        if (snowball.matchesKey(key)) HelperActions.select(client, Items.SNOWBALL, "Snowball");
    }

    /** Donor mouse path: LMB/RMB binds select the same hotbar slots as keys. */
    public void onMouse(MinecraftClient client, int button) {
        if (!enabled()) return;
        if (desor.matchesMouse(button)) HelperActions.select(client, Items.ENDER_EYE, "Desor");
        if (trapka.matchesMouse(button)) HelperActions.select(client, Items.NETHERITE_SCRAP, "Trapka");
        if (yavka.matchesMouse(button)) HelperActions.select(client, Items.SUGAR, "Yavka");
        if (bozhka.matchesMouse(button)) HelperActions.select(client, Items.PHANTOM_MEMBRANE, "Bozhka");
        if (plast.matchesMouse(button)) HelperActions.select(client, Items.DRIED_KELP, "Plast");
        if (snowball.matchesMouse(button)) HelperActions.select(client, Items.SNOWBALL, "Snowball");
    }
}
