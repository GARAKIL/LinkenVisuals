package dev.linkenvisuals.mixin;

import dev.linkenvisuals.ui.linkmenu.LinkenCreateWorldSkin;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ClickableWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Hides every vanilla widget of the LINKEN Create World screen. The screen
 * keeps the widgets for input and narration; {@link LinkenCreateWorldSkin}
 * paints the LINKEN equivalents on the shared design canvas instead.
 */
@Mixin(ClickableWidget.class)
public abstract class CreateWorldWidgetRenderMixin {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void linkenvisuals$hideVanillaWidget(DrawContext context, int mouseX, int mouseY,
                                                 float delta, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null && LinkenCreateWorldSkin.shouldSkin(client.currentScreen)) {
            ci.cancel();
        }
    }
}
