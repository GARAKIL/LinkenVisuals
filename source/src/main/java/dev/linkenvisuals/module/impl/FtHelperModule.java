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
        this(new KeybindSetting("Desor", "NONE"),
            new KeybindSetting("Trapka", "NONE"),
            new KeybindSetting("Yavka", "NONE"),
            new KeybindSetting("Bozhka", "NONE"),
            new KeybindSetting("Plast", "NONE"),
            new KeybindSetting("Snowball", "NONE"));
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
        if (desor.code() == key) HelperActions.select(client, Items.ENDER_EYE, "Desor");
        if (trapka.code() == key) HelperActions.select(client, Items.NETHERITE_SCRAP, "Trapka");
        if (yavka.code() == key) HelperActions.select(client, Items.SUGAR, "Yavka");
        if (bozhka.code() == key) HelperActions.select(client, Items.PHANTOM_MEMBRANE, "Bozhka");
        if (plast.code() == key) HelperActions.select(client, Items.DRIED_KELP, "Plast");
        if (snowball.code() == key) HelperActions.select(client, Items.SNOWBALL, "Snowball");
    }
}
