package dev.linkenvisuals.module.impl;

import dev.linkenvisuals.clickgui.Category;
import dev.linkenvisuals.clickgui.setting.KeybindSetting;
import dev.linkenvisuals.clickgui.setting.Setting;
import dev.linkenvisuals.helper.HelperActions;
import dev.linkenvisuals.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Items;

import java.util.List;

/** Donor RwHelper: hotbar selection for ReallyWorld helper items. */
public final class RwHelperModule extends Module {
    public final KeybindSetting antiFly;
    public final KeybindSetting expScroll;
    public final KeybindSetting trap;
    public final KeybindSetting darkPulse;

    public RwHelperModule() {
        this(new KeybindSetting("Anti Fly", "NONE"), new KeybindSetting("Exp Scroll", "NONE"),
            new KeybindSetting("Trap", "NONE"), new KeybindSetting("Dark Pulse", "NONE"));
    }

    private RwHelperModule(KeybindSetting antiFly, KeybindSetting expScroll,
                           KeybindSetting trap, KeybindSetting darkPulse) {
        super("rwhelper", "Rw Helper", "select ReallyWorld helper items",
            Category.MISC, List.of(), List.<Setting>of(antiFly, expScroll, trap, darkPulse));
        this.antiFly = antiFly;
        this.expScroll = expScroll;
        this.trap = trap;
        this.darkPulse = darkPulse;
    }

    public void onKey(MinecraftClient client, int key) {
        if (!enabled()) return;
        if (antiFly.code() == key) HelperActions.select(client, Items.FIREWORK_STAR, "Anti Fly");
        if (expScroll.code() == key) HelperActions.select(client, Items.FLOWER_BANNER_PATTERN, "Exp Scroll");
        if (trap.code() == key) HelperActions.select(client, Items.HEART_OF_THE_SEA, "Trap", "Ловушка");
        if (darkPulse.code() == key) {
            HelperActions.select(client, Items.FIREWORK_STAR, "Dark Pulse", "Тёмный Пульс");
        }
    }
}
