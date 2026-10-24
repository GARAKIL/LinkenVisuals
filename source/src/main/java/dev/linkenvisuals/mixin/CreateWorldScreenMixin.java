package dev.linkenvisuals.mixin;

import dev.linkenvisuals.i18n.I18n;
import dev.linkenvisuals.ui.linkmenu.LinkenMenuChrome;
import dev.linkenvisuals.ui.style.MenuStyle;
import dev.linkenvisuals.ui.style.StyleManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * LINKEN new-world shell. Vanilla keeps the functional tabs and world settings;
 * this mixin removes the flat vanilla darkening and adds the LV footer mark so
 * the screen reads as part of the Linken family instead of a vanilla fallback.
 */
@Mixin(CreateWorldScreen.class)
public abstract class CreateWorldScreenMixin {
    @Inject(method = "renderDarkening", at = @At("HEAD"), cancellable = true)
    private void linkenvisuals$skipVanillaDarkening(DrawContext context, CallbackInfo ci) {
        if (StyleManager.getStyle() == MenuStyle.LINKEN) {
            ci.cancel();
        }
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void linkenvisuals$drawPanel(DrawContext context, int mouseX, int mouseY,
                                         float delta, CallbackInfo ci) {
        if (StyleManager.getStyle() != MenuStyle.LINKEN) {
            return;
        }
        int width = context.getScaledWindowWidth();
        int height = context.getScaledWindowHeight();
        float panelW = Math.min(280.0f, width - 40.0f);
        float panelH = Math.min(140.0f, height * 0.60f);
        float x = (width - panelW) * 0.5f;
        float y = Math.max(36.0f, Math.min(60.0f, height * 0.16f));
        LinkenMenuChrome.roundedFill(context, x, y, panelW, panelH, 10.0f,
            LinkenMenuChrome.rgba(0x0B0F0D, 232), 1.0f);
        LinkenMenuChrome.roundedOutline(context, x, y, panelW, panelH, 10.0f, 1.0f,
            LinkenMenuChrome.rgba(0xFFFFFF, 22, 1.0f), 1.0f);
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void linkenvisuals$drawChrome(DrawContext context, int mouseX, int mouseY,
                                          float delta, CallbackInfo ci) {
        if (StyleManager.getStyle() != MenuStyle.LINKEN) {
            return;
        }
        int width = context.getScaledWindowWidth();
        int height = context.getScaledWindowHeight();
        context.fill(0, height - 2, width, height,
            LinkenMenuChrome.rgba(LinkenMenuChrome.ACCENT, 110));
        float size = 15.0f;
        float y = height - 25.0f;
        LinkenMenuChrome.drawLogo(context, 14.0f, y, size, 0.85f);
        if (width > 520) {
            LinkenMenuChrome.drawSpaced(context, dev.linkenvisuals.ui.linkmenu.LinkenFonts.headingLight(),
                I18n.get("linkenvisuals.world_setup"), 14.0f + size + 7.0f, y + 7.0f, 8.0f, 1.8f,
                LinkenMenuChrome.rgba(LinkenMenuChrome.MUTED2, 0.85f));
        }
        LinkenMenuChrome.drawRightText(context, dev.linkenvisuals.ui.linkmenu.LinkenFonts.mono(),
            LinkenMenuChrome.version(), width - 14.0f, y + 6.0f, 8.5f,
            LinkenMenuChrome.rgba(LinkenMenuChrome.MUTED3, 0.7f));
    }
}
