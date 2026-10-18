package dev.linkenvisuals.mixin;

import dev.linkenvisuals.i18n.I18n;
import dev.linkenvisuals.render.LoadingOverlay;
import dev.linkenvisuals.render.VanillaStylePipeline;
import dev.linkenvisuals.ui.editor.MainMenuConfig;
import dev.linkenvisuals.ui.style.MenuStyle;
import dev.linkenvisuals.ui.style.StyleManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.LevelLoadingScreen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelLoadingScreen.class)
public abstract class LevelLoadingScreenMixin extends Screen {
    @Shadow private float loadProgress;

    protected LevelLoadingScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void linkenvisuals$customRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!MainMenuConfig.get().customLoadingScreen) {
            return;
        }
        long time = System.currentTimeMillis();

        String titleStr = I18n.get("loading.world");
        String subStr = I18n.get("loading.chunks");
        float barProgress = loadProgress > 0.0f ? loadProgress : -1.0f;
        float cy = height * 0.44f;

        if (StyleManager.getStyle() == MenuStyle.VANILLA) {
            LoadingOverlay.vanillaBackdrop(context, delta, width, height, this::linkenvisuals$panorama);
            LoadingOverlay.vanillaScene(context, textRenderer, width, height,
                VanillaStylePipeline.ICONS_SINGLEPLAYER, 0.0f, 0.0f, 16, 16, 64, 16,
                cy - 38.0f, cy, cy + 16.0f, cy + 34.0f, time,
                titleStr, subStr, barProgress);
        } else {
            LoadingOverlay.betaBackdrop(context, width, height);
            LoadingOverlay.betaScene(context, width, height, time,
                cy - 24.0f, cy - 47.0f, cy + 18.0f, cy + 34.0f,
                cy + 48.0f, 220.0f, 7.8f,
                titleStr, subStr, barProgress);
        }

        ci.cancel();
    }

    /** Passes the protected {@code Screen#renderPanoramaBackground} to the shared backdrop helper. */
    @Unique
    private void linkenvisuals$panorama(DrawContext context, float delta) {
        this.renderPanoramaBackground(context, delta);
    }
}
