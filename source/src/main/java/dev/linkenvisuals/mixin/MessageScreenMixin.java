package dev.linkenvisuals.mixin;

import dev.linkenvisuals.i18n.I18n;
import dev.linkenvisuals.render.LoadingOverlay;
import dev.linkenvisuals.render.VanillaStylePipeline;
import dev.linkenvisuals.ui.editor.MainMenuConfig;
import dev.linkenvisuals.ui.style.StyleManager;
import dev.linkenvisuals.ui.style.StyleRouting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.MessageScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.NarratedMultilineTextWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MessageScreen.class)
public abstract class MessageScreenMixin extends Screen {
    @Shadow private NarratedMultilineTextWidget textWidget;

    protected MessageScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void linkenvisuals$hideVanillaTextWidget(CallbackInfo ci) {
        if (!MainMenuConfig.get().customLoadingScreen) {
            return;
        }
        if (this.textWidget != null) {
            this.textWidget.visible = false;
        }
    }

    @Inject(method = "renderBackground", at = @At("HEAD"), cancellable = true)
    private void linkenvisuals$customRenderSaving(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!MainMenuConfig.get().customLoadingScreen) {
            return;
        }
        long time = System.currentTimeMillis();

        String titleText = this.getTitle() != null && !this.getTitle().getString().isBlank()
            ? this.getTitle().getString() : I18n.get("saving.title");
        String subText = I18n.get("saving.subtitle");
        float cy = height * 0.44f;

        if (StyleRouting.usesVanillaChrome(StyleManager.getStyle())) {
            LoadingOverlay.vanillaBackdrop(context, delta, width, height, this::linkenvisuals$panorama);
            LoadingOverlay.vanillaScene(context, textRenderer, width, height,
                VanillaStylePipeline.ICONS_VIDEO, 0.0f, 40.0f, 20, 20, 240, 60,
                cy - 38.0f, cy, cy + 16.0f, cy + 34.0f, time,
                titleText, subText, -1.0f);
            ci.cancel();
            return;
        }

        LoadingOverlay.betaBackdrop(context, width, height);
        LoadingOverlay.betaScene(context, width, height, time,
            cy - 24.0f, cy - 47.0f, cy + 18.0f, cy + 34.0f,
            cy + 48.0f, 220.0f, 7.8f,
            titleText, subText, -1.0f);

        ci.cancel();
    }

    /** Passes the protected {@code Screen#renderPanoramaBackground} to the shared backdrop helper. */
    @Unique
    private void linkenvisuals$panorama(DrawContext context, float delta) {
        this.renderPanoramaBackground(context, delta);
    }
}
