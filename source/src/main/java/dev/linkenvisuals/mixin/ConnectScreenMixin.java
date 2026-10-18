package dev.linkenvisuals.mixin;

import dev.linkenvisuals.LinkenVisuals;
import dev.linkenvisuals.render.BackgroundPipeline;
import dev.linkenvisuals.render.CirclePipeline;
import dev.linkenvisuals.render.GlowPipeline;
import dev.linkenvisuals.render.GlyphAtlas;
import dev.linkenvisuals.render.MsdfText;
import dev.linkenvisuals.render.NeonEdge;
import dev.linkenvisuals.render.PanelPipeline;
import dev.linkenvisuals.render.RoundedRectPipeline;
import dev.linkenvisuals.ui.style.MenuStyle;
import dev.linkenvisuals.ui.style.StyleManager;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.network.ClientConnection;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
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

    @Unique private static final Identifier LOGO = LinkenVisuals.id("textures/gui/logo.png");
    @Unique private static final Identifier TITLE_FONT_DATA = LinkenVisuals.id("fonts/msdf/sf_bold.json");
    @Unique private static final Identifier TITLE_FONT_ATLAS = LinkenVisuals.id("fonts/msdf/sf_bold.png");
    @Unique private static final Identifier TEXT_FONT_DATA = LinkenVisuals.id("fonts/msdf/sf_regular.json");
    @Unique private static final Identifier TEXT_FONT_ATLAS = LinkenVisuals.id("fonts/msdf/sf_regular.png");

    @Unique private GlyphAtlas linkenvisuals$titleFont;
    @Unique private GlyphAtlas linkenvisuals$textFont;

    protected ConnectScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void linkenvisuals$customRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!dev.linkenvisuals.ui.editor.MainMenuConfig.get().customLoadingScreen) {
            return;
        }
        if (linkenvisuals$titleFont == null) {
            dev.linkenvisuals.render.Pipelines.warmUp();
            linkenvisuals$titleFont = GlyphAtlas.load(TITLE_FONT_DATA, TITLE_FONT_ATLAS);
            linkenvisuals$textFont = GlyphAtlas.load(TEXT_FONT_DATA, TEXT_FONT_ATLAS);
        }
        long time = System.currentTimeMillis();
        MenuStyle style = StyleManager.getStyle();

        String statusStr = status != null ? status.getString() : "Connecting...";

        if (style == MenuStyle.VANILLA) {
            linkenvisuals$renderVanillaLoading(context, mouseX, mouseY, delta, time, statusStr);
        } else {
            BackgroundPipeline.draw(1.0f);
            context.fillGradient(0, 0, width, height, 0x66000000, 0x99020406);
            linkenvisuals$renderBetaLoading(context, mouseX, mouseY, time, statusStr);
        }

        // Render custom Cancel button
        float btnW = 120.0f;
        float btnH = 26.0f;
        float btnX = (width - btnW) * 0.5f;
        float btnY = height * 0.65f;

        boolean hov = mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH;
        if (style == MenuStyle.VANILLA) {
            dev.linkenvisuals.render.VanillaStylePipeline.renderButton(context, Math.round(btnX), Math.round(btnY), Math.round(btnW), Math.round(btnH),
                dev.linkenvisuals.i18n.I18n.get("loading.cancel"), dev.linkenvisuals.render.VanillaStylePipeline.getIconFor("quit"), true, hov);
        } else {
            RoundedRectPipeline.draw(context, btnX, btnY, btnW, btnH, hov ? 0xEE1E2433 : 0xDD12151D);
            RoundedRectPipeline.drawOutline(context, btnX, btnY, btnW, btnH, hov ? NeonEdge.cool() : 0xFF2F3648);
            if (linkenvisuals$titleFont != null) {
                MsdfText.drawCenteredOn(context, linkenvisuals$titleFont, dev.linkenvisuals.i18n.I18n.get("loading.cancel"), btnX + btnW * 0.5f, btnY + btnH * 0.5f, 7.5f, hov ? 0xFFFFFF : 0xC0C6D4);
            }
        }

        ci.cancel();
    }

    @Unique
    private void linkenvisuals$renderVanillaLoading(DrawContext context, int mouseX, int mouseY, float delta, long time, String statusStr) {
        try {
            this.renderPanoramaBackground(context, delta);
            context.fillGradient(0, 0, width, height, 0x1A000000, 0x30000000);
        } catch (Throwable t) {
            dev.linkenvisuals.render.VanillaStylePipeline.renderDirtBackground(context, width, height);
        }

        float cx = width * 0.5f;
        float cy = height * 0.42f;

        int iconSize = 24;
        context.drawTexture(RenderPipelines.GUI_TEXTURED, dev.linkenvisuals.render.VanillaStylePipeline.ICONS_MAIN,
            Math.round(cx - iconSize * 0.5f), Math.round(cy - 36.0f), 16.0f, 0.0f,
            iconSize, iconSize, 16, 16, 80, 16, 0xFFFFFFFF);

        context.drawCenteredTextWithShadow(textRenderer, dev.linkenvisuals.i18n.I18n.get("loading.connecting"), Math.round(cx), Math.round(cy), 0xFFFFFFFF);
        context.drawCenteredTextWithShadow(textRenderer, statusStr, Math.round(cx), Math.round(cy + 16.0f), 0xFFFFFFA0);

        int barW = 200;
        int barH = 12;
        int barX = Math.round(cx - barW * 0.5f);
        int barY = Math.round(cy + 34.0f);
        context.fill(barX, barY, barX + barW, barY + barH, 0xFF000000);
        dev.linkenvisuals.render.VanillaStylePipeline.drawBorder(context, barX, barY, barW, barH, 0xFF555555);

        float sweep = (float) ((time * 0.0009) % 1.0);
        int beamW = (int) (barW * 0.35f);
        int beamX = barX + 1 + (int) (sweep * (barW - 2 - beamW));
        context.fill(beamX, barY + 1, beamX + beamW, barY + barH - 1, 0xFF55FF55);
    }

    @Unique
    private void linkenvisuals$renderBetaLoading(DrawContext context, int mouseX, int mouseY, long time, String statusStr) {
        float cx = width * 0.5f;
        float cy = height * 0.42f;

        // Stable ambient glow - smooth slow breathing, 0 tremble
        float glowRadius = 75.0f + (float) Math.sin(time * 0.001) * 6.0f;
        GlowPipeline.radial(context, cx, cy - 20.0f, glowRadius, withAlpha(NeonEdge.cool(), 50));

        // Crisp stable logo - no jittering
        context.drawTexture(RenderPipelines.GUI_TEXTURED, LOGO,
            Math.round(cx - 23.0f), Math.round(cy - 43.0f), 0.0f, 0.0f,
            46, 46, 120, 120, 120, 120, 0xFFFFFFFF);

        if (linkenvisuals$titleFont != null) {
            MsdfText.drawCentered(context, linkenvisuals$titleFont, dev.linkenvisuals.i18n.I18n.get("loading.connecting"), cx, cy + 20.0f, 11.5f, 0xFFFFFFFF);
            MsdfText.drawCentered(context, linkenvisuals$textFont, statusStr, cx, cy + 36.0f, 8.2f, NeonEdge.cool());
        }

        // Sleek sliding indeterminate bar
        float barW = 200.0f;
        float barH = 4.0f;
        float barX = cx - barW * 0.5f;
        float barY = cy + 50.0f;
        RoundedRectPipeline.draw(context, barX, barY, barW, barH, 0xEE141822);
        RoundedRectPipeline.drawOutline(context, barX, barY, barW, barH, 0x44252E3E);

        float sweep = (float) ((time * 0.0009) % 1.0);
        float beamW = barW * 0.35f;
        float beamX = barX + sweep * (barW - beamW);
        RoundedRectPipeline.draw(context, beamX, barY, beamW, barH, NeonEdge.cool());
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubleClick) {
        if (!dev.linkenvisuals.ui.editor.MainMenuConfig.get().customLoadingScreen) {
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

    @Unique
    private static int withAlpha(int rgb, int alpha) {
        return Math.max(0, Math.min(255, alpha)) << 24 | rgb & 0xFFFFFF;
    }
}
