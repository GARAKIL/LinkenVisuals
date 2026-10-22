package dev.linkenvisuals.mixin;

import dev.linkenvisuals.LinkenVisuals;
import dev.linkenvisuals.i18n.I18n;
import dev.linkenvisuals.ui.editor.MainMenuConfig;
import dev.linkenvisuals.ui.linkmenu.LinkenFonts;
import dev.linkenvisuals.ui.linkmenu.LinkenLoadingScreen;
import dev.linkenvisuals.ui.linkmenu.SplashProgress;
import dev.linkenvisuals.ui.style.MenuStyle;
import dev.linkenvisuals.ui.style.StyleManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.SplashOverlay;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Replaces the Mojang splash only for the LINKEN style. Beta and Vanilla keep
 * the original overlay untouched.
 */
@Mixin(SplashOverlay.class)
public abstract class SplashOverlayMixin {
    @Shadow private float progress;
    @Shadow private long reloadCompleteTime;
    @Shadow private long reloadStartTime;

    @Unique
    private static boolean linkenvisuals$failureLogged;

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void linkenvisuals$renderLinkenSplash(DrawContext context, int mouseX, int mouseY,
                                                  float delta, CallbackInfo ci) {
        if (StyleManager.getStyle() != MenuStyle.LINKEN || !MainMenuConfig.get().customLoadingScreen) {
            return;
        }
        if (!dev.linkenvisuals.render.Pipelines.resourcesReady()) {
            // The initial resource reload is still loading the custom core
            // shaders. Let vanilla draw the splash until they are available.
            return;
        }
        try {
            long now = Util.getMeasuringTimeMs();
            // SplashOverlay sets reloadStartTime in its own render method. We
            // cancel that method at HEAD, so manual resource reloads (resource
            // packs, F3+T) would otherwise keep reloadStartTime at -1 forever:
            // tick() would never set reloadCompleteTime and the overlay would
            // never be removed. Mirror the vanilla initialisation here.
            if (reloadStartTime <= 0L) {
                reloadStartTime = now;
            }
            long startedAt = reloadStartTime > 0L ? reloadStartTime : now;
            boolean complete = reloadCompleteTime > 0L;
            if (SplashProgress.finished(reloadCompleteTime, now)) {
                // Cancelling the vanilla render at HEAD also skips vanilla's
                // own setOverlay(null), so the LINKEN overlay has to perform
                // that hand-off once its fade-out is complete.
                MinecraftClient.getInstance().setOverlay(null);
                ci.cancel();
                return;
            }
            if (complete) {
                LinkenFonts.retryIfUnavailable();
            }
            float alpha = complete
                ? SplashProgress.fadeOut(reloadCompleteTime, now)
                : SplashProgress.fadeIn(startedAt, now);
            float exit = SplashProgress.exit(reloadCompleteTime, now);
            float clamped = SplashProgress.displayProgress(progress);
            String title = I18n.get("loading.splash.title");
            String subtitle = I18n.get(SplashProgress.stageKey(clamped));

            LinkenLoadingScreen.render(context, context.getScaledWindowWidth(), context.getScaledWindowHeight(),
                clamped, alpha, exit, title, subtitle);
            ci.cancel();
        } catch (Throwable failure) {
            if (!linkenvisuals$failureLogged) {
                linkenvisuals$failureLogged = true;
                LinkenVisuals.LOGGER.warn("LINKEN splash failed, falling back to vanilla: {}", failure.toString());
            }
        }
    }
}
