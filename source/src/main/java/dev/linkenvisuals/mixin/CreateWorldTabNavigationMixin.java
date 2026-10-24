package dev.linkenvisuals.mixin;

import dev.linkenvisuals.ui.linkmenu.LinkenCreateWorldSkin;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TabNavigationWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Suppresses the vanilla tab header (texture, separators and tab buttons) on
 * the LINKEN Create World screen so the custom tab rail is the only one shown.
 */
@Mixin(TabNavigationWidget.class)
public abstract class CreateWorldTabNavigationMixin {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void linkenvisuals$hideVanillaTabs(DrawContext context, int mouseX, int mouseY,
                                               float delta, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null && LinkenCreateWorldSkin.shouldSkin(client.currentScreen)) {
            ci.cancel();
        }
    }
}
