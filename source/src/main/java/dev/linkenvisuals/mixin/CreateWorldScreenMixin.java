package dev.linkenvisuals.mixin;

import dev.linkenvisuals.ui.linkmenu.LinkenCreateWorldSkin;
import dev.linkenvisuals.ui.style.MenuStyle;
import dev.linkenvisuals.ui.style.StyleManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * LINKEN new-world screen. Vanilla keeps the functional tabs, fields and
 * world-creation logic; the widget mixins hide its chrome and
 * {@link LinkenCreateWorldSkin} paints the LINKEN composition over the whole
 * screen.
 */
@Mixin(CreateWorldScreen.class)
public abstract class CreateWorldScreenMixin {
    @Inject(method = "renderDarkening", at = @At("HEAD"), cancellable = true)
    private void linkenvisuals$skipVanillaDarkening(DrawContext context, CallbackInfo ci) {
        if (StyleManager.getStyle() == MenuStyle.LINKEN) {
            ci.cancel();
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void linkenvisuals$renderSkin(DrawContext context, int mouseX, int mouseY,
                                          float delta, CallbackInfo ci) {
        LinkenCreateWorldSkin.render(context, (CreateWorldScreen) (Object) this,
            mouseX, mouseY, delta);
    }
}
