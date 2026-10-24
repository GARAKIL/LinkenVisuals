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
        this(new KeybindSetting("Anti Fly", "NONE", true), new KeybindSetting("Exp Scroll", "NONE", true),
            new KeybindSetting("Trap", "NONE", true), new KeybindSetting("Dark Pulse", "NONE", true));
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
        if (antiFly.matchesKey(key)) HelperActions.select(client, Items.FIREWORK_STAR, "Anti Fly");
        if (expScroll.matchesKey(key)) HelperActions.select(client, Items.FLOWER_BANNER_PATTERN, "Exp Scroll");
        if (trap.matchesKey(key)) HelperActions.select(client, Items.HEART_OF_THE_SEA, "Trap", "Ловушка");
        if (darkPulse.matchesKey(key)) {
            HelperActions.select(client, Items.FIREWORK_STAR, "Dark Pulse", "Тёмный Пульс");
        }
    }

    /** Donor mouse path: LMB/RMB binds select the same hotbar slots as keys. */
    public void onMouse(MinecraftClient client, int button) {
        if (!enabled()) return;
        if (antiFly.matchesMouse(button)) HelperActions.select(client, Items.FIREWORK_STAR, "Anti Fly");
        if (expScroll.matchesMouse(button)) HelperActions.select(client, Items.FLOWER_BANNER_PATTERN, "Exp Scroll");
        if (trap.matchesMouse(button)) HelperActions.select(client, Items.HEART_OF_THE_SEA, "Trap", "Ловушка");
        if (darkPulse.matchesMouse(button)) {
            HelperActions.select(client, Items.FIREWORK_STAR, "Dark Pulse", "Тёмный Пульс");
        }
    }
}
