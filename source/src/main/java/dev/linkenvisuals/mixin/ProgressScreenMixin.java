package dev.linkenvisuals.mixin;

import dev.linkenvisuals.i18n.I18n;
import dev.linkenvisuals.render.LoadingOverlay;
import dev.linkenvisuals.render.VanillaStylePipeline;
import dev.linkenvisuals.ui.editor.MainMenuConfig;
import dev.linkenvisuals.ui.style.StyleManager;
import dev.linkenvisuals.ui.style.StyleRouting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ProgressScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ProgressScreen.class)
public abstract class ProgressScreenMixin extends Screen {
    @Shadow private Text title;
    @Shadow private Text task;
    @Shadow private int progress;

    protected ProgressScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void linkenvisuals$customRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!MainMenuConfig.get().customLoadingScreen) {
            return;
        }
        long time = System.currentTimeMillis();

        String mainTitle = title != null ? title.getString() : I18n.get("loading.world");
        String subTask = task != null ? task.getString() : "";
        float pct = Math.max(0.0f, Math.min(100.0f, progress)) / 100.0f;
        float barProgress = progress >= 0 ? pct : -1.0f;
        float cy = height * 0.44f;

        if (StyleRouting.usesVanillaChrome(StyleManager.getStyle())) {
            LoadingOverlay.vanillaBackdrop(context, delta, width, height, this::linkenvisuals$panorama);
            LoadingOverlay.vanillaScene(context, textRenderer, width, height,
                VanillaStylePipeline.ICONS_SINGLEPLAYER, 0.0f, 0.0f, 16, 16, 64, 16,
                cy - 38.0f, cy, cy + 16.0f, cy + 34.0f, time,
                mainTitle, subTask.isBlank() ? null : subTask, barProgress);
        } else {
            LoadingOverlay.betaBackdrop(context, width, height);
            LoadingOverlay.betaScene(context, width, height, time,
                cy - 24.0f, cy - 47.0f, cy + 18.0f, cy + 34.0f,
                cy + 48.0f, 220.0f, 8.0f,
                mainTitle, subTask.isBlank() ? null : subTask, barProgress);
        }

        ci.cancel();
    }

    /** Passes the protected {@code Screen#renderPanoramaBackground} to the shared backdrop helper. */
    @Unique
    private void linkenvisuals$panorama(DrawContext context, float delta) {
        this.renderPanoramaBackground(context, delta);
    }
}
