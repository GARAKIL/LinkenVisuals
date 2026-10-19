package dev.linkenvisuals.module.impl;

import dev.linkenvisuals.clickgui.Category;
import dev.linkenvisuals.clickgui.setting.KeySetting;
import dev.linkenvisuals.clickgui.setting.Setting;
import dev.linkenvisuals.module.Module;
import org.lwjgl.glfw.GLFW;

import java.util.List;

/**
 * ElytraSwap (Phase 5.7): swaps chestplate with elytra.
 * Donor's {@code ElytraSwap} (331 lines) — inventory click logic is a follow-up.
 * Module stub registered.
 */
public final class ElytraSwapModule extends Module {
    public final KeySetting key;

    public ElytraSwapModule() {
        this(new KeySetting("Key", GLFW.GLFW_KEY_UNKNOWN));
    }

    private ElytraSwapModule(KeySetting key) {
        super("elytraswap", "Elytra Swap", "swap chestplate/elytra",
            Category.PLAYER,
            List.of(),
            List.<Setting>of(key));
        this.key = key;
    }
}
