package dev.linkenvisuals.module.impl;

import dev.linkenvisuals.clickgui.Category;
import dev.linkenvisuals.clickgui.setting.KeybindSetting;
import dev.linkenvisuals.clickgui.setting.Setting;
import dev.linkenvisuals.helper.HelperActions;
import dev.linkenvisuals.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Items;

import java.util.List;

/** Donor HwHelper: hotbar selection for HolyWorld helper items. */
public final class HwHelperModule extends Module {
    public final KeybindSetting stan;
    public final KeybindSetting trapka;
    public final KeybindSetting boomTrapka;
    public final KeybindSetting boomThing;

    public HwHelperModule() {
        this(new KeybindSetting("Stan", "NONE", true), new KeybindSetting("Trapka", "NONE", true),
            new KeybindSetting("Boom Trapka", "NONE", true), new KeybindSetting("Boom Thing", "NONE", true));
    }

    private HwHelperModule(KeybindSetting stan, KeybindSetting trapka,
                           KeybindSetting boomTrapka, KeybindSetting boomThing) {
        super("hwhelper", "Hw Helper", "select HolyWorld helper items",
            Category.MISC, List.of(), List.<Setting>of(stan, trapka, boomTrapka, boomThing));
        this.stan = stan;
        this.trapka = trapka;
        this.boomTrapka = boomTrapka;
        this.boomThing = boomThing;
    }

    public void onKey(MinecraftClient client, int key) {
        if (!enabled()) return;
        if (stan.matchesKey(key)) HelperActions.select(client, Items.NETHER_STAR, "Stan");
        if (trapka.matchesKey(key)) HelperActions.select(client, Items.POPPED_CHORUS_FRUIT, "Trapka");
        if (boomTrapka.matchesKey(key)) HelperActions.select(client, Items.PRISMARINE_SHARD, "Boom Trapka");
        if (boomThing.matchesKey(key)) HelperActions.select(client, Items.FIRE_CHARGE, "Boom Thing");
    }

    /** Donor mouse path: LMB/RMB binds select the same hotbar slots as keys. */
    public void onMouse(MinecraftClient client, int button) {
        if (!enabled()) return;
        if (stan.matchesMouse(button)) HelperActions.select(client, Items.NETHER_STAR, "Stan");
        if (trapka.matchesMouse(button)) HelperActions.select(client, Items.POPPED_CHORUS_FRUIT, "Trapka");
        if (boomTrapka.matchesMouse(button)) HelperActions.select(client, Items.PRISMARINE_SHARD, "Boom Trapka");
        if (boomThing.matchesMouse(button)) HelperActions.select(client, Items.FIRE_CHARGE, "Boom Thing");
    }
}
