package dev.linkenvisuals.mixin;

import dev.linkenvisuals.render.Fonts;
import dev.linkenvisuals.i18n.I18n;
import dev.linkenvisuals.render.GlyphAtlas;
import dev.linkenvisuals.render.LoadingOverlay;
import dev.linkenvisuals.render.MsdfText;
import dev.linkenvisuals.render.NeonEdge;
import dev.linkenvisuals.render.RoundedRectPipeline;
import dev.linkenvisuals.render.VanillaStylePipeline;
import dev.linkenvisuals.ui.editor.MainMenuConfig;
import dev.linkenvisuals.ui.style.MenuStyle;
import dev.linkenvisuals.ui.style.StyleManager;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.network.ClientConnection;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ConnectScreen.class)
public abstract class ConnectScreenMixin extends Screen {
    @Shadow private Text status;
    @Shadow private Screen parent;
    @Shadow private boolean connectingCancelled;
    @Shadow @Nullable private ClientConnection connection;

    protected ConnectScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void linkenvisuals$customRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!MainMenuConfig.get().customLoadingScreen) {
            return;
        }
        long time = System.currentTimeMillis();
        MenuStyle style = StyleManager.getStyle();

        String statusStr = status != null ? status.getString() : "Connecting...";
        String titleStr = I18n.get("loading.connecting");
        float cy = height * 0.42f;

        if (style == MenuStyle.VANILLA) {
            LoadingOverlay.vanillaBackdrop(context, delta, width, height, this::linkenvisuals$panorama);
            LoadingOverlay.vanillaScene(context, textRenderer, width, height,
                VanillaStylePipeline.ICONS_MAIN, 16.0f, 0.0f, 16, 16, 80, 16,
                cy - 36.0f, cy, cy + 16.0f, cy + 34.0f, time,
                titleStr, statusStr, -1.0f);
        } else {
            LoadingOverlay.betaBackdrop(context, width, height);
            LoadingOverlay.betaScene(context, width, height, time,
                cy - 20.0f, cy - 43.0f, cy + 20.0f, cy + 36.0f,
                cy + 50.0f, 200.0f, 8.2f,
                titleStr, statusStr, -1.0f);
        }

        // Render custom Cancel button
        float btnW = 120.0f;
        float btnH = 26.0f;
        float btnX = (width - btnW) * 0.5f;
        float btnY = height * 0.65f;

        boolean hov = mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH;
        if (style == MenuStyle.VANILLA) {
            VanillaStylePipeline.renderButton(context, Math.round(btnX), Math.round(btnY), Math.round(btnW), Math.round(btnH),
                I18n.get("loading.cancel"), VanillaStylePipeline.getIconForKey("loading.cancel"), true, hov);
        } else {
            RoundedRectPipeline.draw(context, btnX, btnY, btnW, btnH, hov ? 0xEE1E2433 : 0xDD12151D);
            RoundedRectPipeline.drawOutline(context, btnX, btnY, btnW, btnH, hov ? NeonEdge.cool() : 0xFF2F3648);
            GlyphAtlas titleFont = Fonts.titleFont();
            if (titleFont != null) {
                MsdfText.drawCenteredOn(context, titleFont, I18n.get("loading.cancel"), btnX + btnW * 0.5f, btnY + btnH * 0.5f, 7.5f, hov ? 0xFFFFFF : 0xC0C6D4);
            }
        }

        ci.cancel();
    }

    /** Passes the protected {@code Screen#renderPanoramaBackground} to the shared backdrop helper. */
    @Unique
    private void linkenvisuals$panorama(DrawContext context, float delta) {
        this.renderPanoramaBackground(context, delta);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubleClick) {
        if (!MainMenuConfig.get().customLoadingScreen) {
            return super.mouseClicked(click, doubleClick);
        }
        float btnW = 120.0f;
        float btnH = 26.0f;
        float btnX = (width - btnW) * 0.5f;
        float btnY = height * 0.65f;

        if (click.x() >= btnX && click.x() <= btnX + btnW && click.y() >= btnY && click.y() <= btnY + btnH) {
            this.connectingCancelled = true;
            if (this.connection != null) {
                this.connection.disconnect(Text.translatable("connect.aborted"));
            }
            if (client != null) {
                client.setScreen(this.parent);
            }
            return true;
        }

        return super.mouseClicked(click, doubleClick);
    }
}
