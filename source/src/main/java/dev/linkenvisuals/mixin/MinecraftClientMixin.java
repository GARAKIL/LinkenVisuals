package dev.linkenvisuals.mixin;

import dev.linkenvisuals.ui.CustomMultiplayerScreen;
import dev.linkenvisuals.ui.MainMenuScreen;
import dev.linkenvisuals.ui.linkmenu.LinkenMainMenuScreen;
import dev.linkenvisuals.ui.style.MenuStyle;
import dev.linkenvisuals.ui.style.StyleManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
    @ModifyVariable(method = "setScreen", at = @At("HEAD"), argsOnly = true)
    private Screen linkenvisuals$redirectTitleScreen(Screen screen) {
        if (screen == null) {
            return null;
        }
        MenuStyle style = StyleManager.getStyle();
        return switch (style) {
            case VANILLA -> linkenvisuals$vanilla(screen);
            case BETA -> linkenvisuals$beta(screen);
            case LINKEN -> linkenvisuals$linken(screen);
        };
    }

    private static Screen linkenvisuals$vanilla(Screen screen) {
        if (screen instanceof MainMenuScreen || screen instanceof LinkenMainMenuScreen) {
            return new TitleScreen();
        }
        if (screen instanceof CustomMultiplayerScreen custom) {
            return new MultiplayerScreen(custom.getParentScreen() != null
                ? custom.getParentScreen() : new TitleScreen());
        }
        return screen;
    }

    private static Screen linkenvisuals$beta(Screen screen) {
        if (screen instanceof LinkenMainMenuScreen) {
            return new MainMenuScreen();
        }
        if (screen instanceof TitleScreen && !(screen instanceof MainMenuScreen)) {
            return new MainMenuScreen();
        }
        return screen;
    }

    private static Screen linkenvisuals$linken(Screen screen) {
        if (screen instanceof MainMenuScreen) {
            return new LinkenMainMenuScreen();
        }
        if (screen instanceof TitleScreen) {
            return new LinkenMainMenuScreen();
        }
        return screen;
    }
}
