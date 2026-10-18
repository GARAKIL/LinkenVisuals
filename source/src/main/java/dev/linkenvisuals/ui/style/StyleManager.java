package dev.linkenvisuals.ui.style;

import dev.linkenvisuals.clickgui.ConfigManager;
import dev.linkenvisuals.clickgui.ModuleRegistry;
import dev.linkenvisuals.ui.CustomMultiplayerScreen;
import dev.linkenvisuals.ui.MainMenuScreen;
import dev.linkenvisuals.ui.editor.MainMenuConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;

/**
 * Global coordinator for the active UI menu style across LinkenVisuals custom screens.
 */
public final class StyleManager {
    private static MenuStyle currentStyle = MenuStyle.VANILLA;

    private StyleManager() {
    }

    public static MenuStyle getStyle() {
        return currentStyle;
    }

    public static void setStyle(MenuStyle style) {
        if (style != null && currentStyle != style) {
            currentStyle = style;
            applyStyle(style);
        }
    }

    public static void setStyleSilently(MenuStyle style) {
        if (style != null) {
            currentStyle = style;
        }
    }

    public static void applyStyle(MenuStyle style) {
        if (style == null) return;
        MainMenuConfig config = MainMenuConfig.get();
        config.applyPreset(style);
        MainMenuConfig.save();
        ModuleRegistry.syncFromMenuConfig();
        ConfigManager.save();

        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null) {
            if (style == MenuStyle.BETA) {
                if (client.currentScreen instanceof TitleScreen && !(client.currentScreen instanceof MainMenuScreen)) {
                    client.setScreen(new MainMenuScreen());
                } else if (client.currentScreen instanceof MultiplayerScreen && !(client.currentScreen instanceof CustomMultiplayerScreen)) {
                    client.setScreen(new CustomMultiplayerScreen(null));
                }
            } else if (style == MenuStyle.VANILLA) {
                if (client.currentScreen instanceof MainMenuScreen) {
                    client.setScreen(new TitleScreen());
                } else if (client.currentScreen instanceof CustomMultiplayerScreen) {
                    client.setScreen(new MultiplayerScreen(new TitleScreen()));
                }
            }
        }
    }
}
