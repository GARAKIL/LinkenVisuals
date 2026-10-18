package dev.linkenvisuals.mixin;

import dev.linkenvisuals.ui.CustomMultiplayerScreen;
import dev.linkenvisuals.ui.MainMenuScreen;
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
        if (StyleManager.getStyle() == MenuStyle.VANILLA) {
            if (screen instanceof MainMenuScreen) {
                return new TitleScreen();
            }
            if (screen instanceof CustomMultiplayerScreen custom) {
                return new MultiplayerScreen(custom.getParentScreen() != null ? custom.getParentScreen() : new TitleScreen());
            }
            return screen;
        } else if (StyleManager.getStyle() == MenuStyle.BETA) {
            if (screen instanceof TitleScreen && !(screen instanceof MainMenuScreen)) {
                return new MainMenuScreen();
            }
            return screen;
        }
        return screen;
    }
}
