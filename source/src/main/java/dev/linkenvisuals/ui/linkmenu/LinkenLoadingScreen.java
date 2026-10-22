package dev.linkenvisuals.ui.linkmenu;

import dev.linkenvisuals.LinkenVisuals;
import dev.linkenvisuals.render.GlowPipeline;
import dev.linkenvisuals.render.GlyphAtlas;
import dev.linkenvisuals.render.RoundedRectPipeline;
import net.minecraft.SharedConstants;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

/**
 * Full-screen LINKEN loading scene shared by the splash overlay and the four
 * vanilla loading screens. Geometry lives on the same 1920x1080 design canvas
 * as the main menu, so the composition scales cleanly from 720p to 4K.
 */
public final class LinkenLoadingScreen {
    private static final float DESIGN_WIDTH = 1920.0f;
    private static final float DESIGN_HEIGHT = 1080.0f;
    private static final Identifier BACKGROUND = LinkenVisuals.id("textures/linkmenu/bg-tower.png");
    private static final float BACKGROUND_WIDTH = 1270.0f;
    private static final float BACKGROUND_HEIGHT = 999.0f;

    private static final float TRACK_WIDTH = 460.0f;
    private static final float TRACK_HEIGHT = 4.0f;
    private static final float TRACK_Y = 824.0f;
    private static final float CANCEL_WIDTH = 160.0f;
    private static final float CANCEL_HEIGHT = 44.0f;
    private static final float CANCEL_Y = 932.0f;

    private LinkenLoadingScreen() {
    }

    public static float scaleFor(int screenHeight) {
        return LinkenMenuLayout.scaleFor(screenHeight);
    }

    public static float virtualWidth(int screenWidth, int screenHeight) {
        return LinkenMenuLayout.virtualWidth(screenWidth, screenHeight);
    }

    /**
     * Draws the branded loading scene.
     *
     * @param progress   0..1, or a negative value for an indeterminate sweep
     * @param alpha      global opacity 0..1
     * @param exit       0..1 slide-out amount
     */
    public static void render(DrawContext context, int screenWidth, int screenHeight,
                              float progress, float alpha, float exit,
                              String title, String subtitle) {
        if (screenWidth <= 0 || screenHeight <= 0) {
            return;
        }
        if (!dev.linkenvisuals.render.Pipelines.resourcesReady()) {
            return;
        }
        float fade = SplashProgress.clamp01(alpha);
        if (fade <= 0.001f) {
            return;
        }
        float scale = scaleFor(screenHeight);
        float width = Math.max(1.0f, screenWidth / scale);
        float height = DESIGN_HEIGHT;
        boolean indeterminate = progress < 0.0f;

        context.getMatrices().pushMatrix();
        context.getMatrices().scale(scale, scale);
        float slide = SplashProgress.smoothstep(exit) * 30.0f;
        if (slide > 0.0f) {
            context.getMatrices().translate(0.0f, -slide);
        }

        drawBackdrop(context, width, height, fade);
        drawFrame(context, width, height, fade, scale);
        drawWordmark(context, width, fade);
        drawCaption(context, width, title, subtitle, fade);
        drawProgress(context, width, progress, fade, scale, indeterminate);
        drawFooter(context, width, height, fade);

        context.getMatrices().popMatrix();
    }

    private static void drawBackdrop(DrawContext context, float width, float height, float fade) {
        float cover = Math.max(width / BACKGROUND_WIDTH, height / BACKGROUND_HEIGHT) * 1.06f;
        float drawWidth = BACKGROUND_WIDTH * cover;
        float drawHeight = BACKGROUND_HEIGHT * cover;
        float x = (width - drawWidth) * 0.5f;
        float y = (height - drawHeight) * 0.5f;
        context.drawTexture(RenderPipelines.GUI_TEXTURED, BACKGROUND,
            Math.round(x), Math.round(y), 0.0f, 0.0f,
            Math.round(drawWidth), Math.round(drawHeight),
            Math.round(BACKGROUND_WIDTH), Math.round(BACKGROUND_HEIGHT),
            Math.round(BACKGROUND_WIDTH), Math.round(BACKGROUND_HEIGHT),
            LinkenMenuChrome.rgba(0xFFFFFF, 150, fade));

        // The photograph is only a texture under the brand scene; the heavy
        // scrim keeps the loading typography readable at every resolution.
        context.fillGradient(0, 0, Math.round(width), Math.round(height),
            LinkenMenuChrome.rgba(0x040706, 178, fade),
            LinkenMenuChrome.rgba(0x020403, 238, fade));
        context.fillGradient(0, 0, Math.round(width), 210,
            LinkenMenuChrome.rgba(0x010202, 155, fade),
            LinkenMenuChrome.rgba(0x010202, 0, fade));
        context.fillGradient(0, Math.round(height) - 230, Math.round(width), Math.round(height),
            LinkenMenuChrome.rgba(0x010202, 0, fade),
            LinkenMenuChrome.rgba(0x010202, 218, fade));

        long now = System.currentTimeMillis();
        float pulse = 0.5f + 0.5f * (float) Math.sin(now * 0.0012);
        GlowPipeline.radial(context, width * 0.5f, 465.0f, 410.0f,
            LinkenMenuChrome.rgba(LinkenMenuChrome.ACCENT, Math.round(9.0f + pulse * 5.0f), fade));
        GlowPipeline.radial(context, width * 0.50f, height * 0.86f, 300.0f,
            LinkenMenuChrome.rgba(LinkenMenuChrome.ACCENT, 5, fade));
    }

    private static void drawFrame(DrawContext context, float width, float height, float fade, float scale) {
        float inset = 24.0f;
        float length = 18.0f;
        int color = LinkenMenuChrome.rgba(LinkenMenuChrome.ACCENT, 72, fade);
        context.fill(Math.round(inset), Math.round(inset), Math.round(inset + length), Math.round(inset + 1), color);
        context.fill(Math.round(inset), Math.round(inset), Math.round(inset + 1), Math.round(inset + length), color);
        context.fill(Math.round(width - inset - length), Math.round(height - inset - 1), Math.round(width - inset),
            Math.round(height - inset), color);
        context.fill(Math.round(width - inset - 1), Math.round(height - inset - length), Math.round(width - inset),
            Math.round(height - inset), color);

        // A barely visible inner hairline gives the scene the same editorial
        // material language as the main menu without competing with the text.
        LinkenMenuChrome.roundedOutline(context, 46.0f, 46.0f, width - 92.0f, height - 92.0f, 12.0f, 1.0f,
            LinkenMenuChrome.rgba(0xFFFFFF, 8, fade), scale);
    }

    private static void drawWordmark(DrawContext context, float width, float fade) {
        GlyphAtlas font = LinkenFonts.bodyMedium();
        float size = 64.0f;
        String word = "linken";
        String dot = ".";
        float wordWidth = LinkenMenuChrome.textWidth(font, word, size);
        float dotWidth = LinkenMenuChrome.textWidth(font, dot, size);
        float left = width * 0.5f - (wordWidth + dotWidth) * 0.5f;
        float top = 382.0f;
        LinkenMenuChrome.drawText(context, font, word, left, top, size,
            LinkenMenuChrome.rgba(LinkenMenuChrome.TEXT, fade));
        LinkenMenuChrome.drawText(context, font, dot, left + wordWidth, top, size,
            LinkenMenuChrome.rgba(LinkenMenuChrome.ACCENT, fade));

        GlyphAtlas labelFont = LinkenFonts.headingLight();
        String label = "VISUALS";
        float labelSize = 9.5f;
        float tracking = 3.1f;
        float labelWidth = LinkenMenuChrome.textWidth(labelFont, label, labelSize)
            + tracking * Math.max(0, label.length() - 1);
        LinkenMenuChrome.drawSpaced(context, labelFont, label, width * 0.5f - labelWidth * 0.5f,
            top + 70.0f, labelSize, tracking, LinkenMenuChrome.rgba(LinkenMenuChrome.MUTED3, fade));
    }

    private static void drawCaption(DrawContext context, float width, String title, String subtitle, float fade) {
        String safeTitle = title == null ? "" : title;
        String safeSubtitle = subtitle == null ? "" : subtitle;
        GlyphAtlas titleFont = LinkenFonts.headingLight();
        GlyphAtlas body = LinkenFonts.body();

        float titleSize = 11.5f;
        float tracking = 2.4f;
        float titleWidth = LinkenMenuChrome.textWidth(titleFont, safeTitle, titleSize)
            + tracking * Math.max(0, safeTitle.length() - 1);
        LinkenMenuChrome.drawSpaced(context, titleFont, safeTitle, width * 0.5f - titleWidth * 0.5f,
            600.0f, titleSize, tracking, LinkenMenuChrome.rgba(LinkenMenuChrome.TEXT3, fade));

        if (!safeSubtitle.isBlank()) {
            String line = LinkenMenuChrome.ellipsize(body, safeSubtitle, 620.0f, 10.5f);
            LinkenMenuChrome.drawCentered(context, body, line, width * 0.5f, 626.0f, 10.5f,
                LinkenMenuChrome.rgba(LinkenMenuChrome.MUTED2, fade));
        }
    }

    private static void drawProgress(DrawContext context, float width, float progress, float fade,
                                     float scale, boolean indeterminate) {
        float trackX = width * 0.5f - TRACK_WIDTH * 0.5f;
        LinkenMenuChrome.roundedFill(context, trackX, TRACK_Y, TRACK_WIDTH, TRACK_HEIGHT, 2.0f,
            LinkenMenuChrome.rgba(0xFFFFFF, 24, fade), scale);
        LinkenMenuChrome.roundedOutline(context, trackX, TRACK_Y, TRACK_WIDTH, TRACK_HEIGHT, 2.0f, 1.0f,
            LinkenMenuChrome.rgba(0xFFFFFF, 18, fade), scale);

        float fill = 0.0f;
        if (indeterminate) {
            float sweep = (float) ((System.currentTimeMillis() * 0.00038) % 1.0);
            float beam = TRACK_WIDTH * 0.32f;
            float beamX = trackX + sweep * (TRACK_WIDTH - beam);
            LinkenMenuChrome.roundedFill(context, beamX, TRACK_Y, beam, TRACK_HEIGHT, 2.0f,
                LinkenMenuChrome.rgba(LinkenMenuChrome.ACCENT, fade), scale);
            fill = beam;
        } else {
            float value = SplashProgress.displayProgress(progress);
            if (value > 0.0f) {
                float fillWidth = Math.max(TRACK_HEIGHT, TRACK_WIDTH * value);
                GlowPipeline.line(context, trackX + fillWidth * 0.5f, TRACK_Y + TRACK_HEIGHT * 0.5f,
                    fillWidth, 18.0f, LinkenMenuChrome.rgba(LinkenMenuChrome.ACCENT, 42, fade));
                LinkenMenuChrome.roundedFill(context, trackX, TRACK_Y, fillWidth, TRACK_HEIGHT, 2.0f,
                    LinkenMenuChrome.rgba(LinkenMenuChrome.ACCENT, fade), scale);
                fill = fillWidth;
            }
        }

        GlyphAtlas mono = LinkenFonts.mono();
        GlyphAtlas body = LinkenFonts.body();
        String left = indeterminate ? "" : I18nFallback.stage(progress);
        String right = indeterminate ? "" : SplashProgress.percent(progress) + "%";
        if (!left.isBlank()) {
            LinkenMenuChrome.drawText(context, body, left, trackX, TRACK_Y + 14.0f, 9.5f,
                LinkenMenuChrome.rgba(LinkenMenuChrome.MUTED, fade));
        }
        if (!right.isBlank()) {
            LinkenMenuChrome.drawRightText(context, mono, right, trackX + TRACK_WIDTH, TRACK_Y + 13.0f, 9.5f,
                LinkenMenuChrome.rgba(LinkenMenuChrome.TEXT4, fade));
        }
        // Keep the glow helper referenced for both branches: an idle full line
        // would otherwise make the determinate bar look flat on HDR panels.
        if (fill <= 0.0f) {
            GlowPipeline.line(context, width * 0.5f, TRACK_Y + TRACK_HEIGHT * 0.5f, 1.0f, 1.0f,
                LinkenMenuChrome.rgba(LinkenMenuChrome.ACCENT, 0, fade));
        }
    }

    private static void drawFooter(DrawContext context, float width, float height, float fade) {
        GlyphAtlas body = LinkenFonts.body();
        String footer = I18nFallback.footer();
        float size = 9.5f;
        String line = LinkenMenuChrome.ellipsize(body, footer, width - 140.0f, size);
        LinkenMenuChrome.drawCentered(context, body, line, width * 0.5f, height - 72.0f, size,
            LinkenMenuChrome.rgba(LinkenMenuChrome.MUTED4, fade));

        String disclaimer = I18nFallback.disclaimer();
        LinkenMenuChrome.drawCentered(context, body, disclaimer, width * 0.5f, height - 50.0f, 8.5f,
            LinkenMenuChrome.rgba(0x35413B, fade));
    }

    // ------------------------------------------------------------------ cancel button

    public static boolean cancelHovered(int screenWidth, int screenHeight, double mouseX, double mouseY) {
        float scale = scaleFor(screenHeight);
        float width = virtualWidth(screenWidth, screenHeight);
        return cancelRect(width).contains((float) (mouseX / scale), (float) (mouseY / scale));
    }

    public static void renderCancelButton(DrawContext context, int screenWidth, int screenHeight,
                                          boolean hovered, String label, float alpha) {
        float fade = SplashProgress.clamp01(alpha);
        if (fade <= 0.001f) {
            return;
        }
        float scale = scaleFor(screenHeight);
        float width = virtualWidth(screenWidth, screenHeight);
        LinkenMenuLayout.Rect rect = cancelRect(width);

        context.getMatrices().pushMatrix();
        context.getMatrices().scale(scale, scale);
        LinkenMenuChrome.drawSecondary(context, rect.x(), rect.y(), rect.width(), rect.height(), 8.0f,
            hovered ? 1.0f : 0.0f, false, fade, scale);
        LinkenMenuChrome.drawCentered(context, LinkenFonts.headingLight(),
            label == null ? "" : label, rect.x() + rect.width() * 0.5f,
            rect.y() + rect.height() * 0.5f, 10.5f,
            LinkenMenuChrome.rgba(hovered ? LinkenMenuChrome.ACCENT : LinkenMenuChrome.TEXT3, fade));
        context.getMatrices().popMatrix();
    }

    private static LinkenMenuLayout.Rect cancelRect(float virtualWidth) {
        return new LinkenMenuLayout.Rect(virtualWidth * 0.5f - CANCEL_WIDTH * 0.5f, CANCEL_Y,
            CANCEL_WIDTH, CANCEL_HEIGHT);
    }

    // ------------------------------------------------------------------ i18n fallback

    /**
     * Kept in a tiny nested helper so the renderer itself has no hidden
     * Minecraft client lookups and can be exercised in the splash phase.
     */
    private static final class I18nFallback {
        private I18nFallback() {
        }

        static String stage(float progress) {
            return dev.linkenvisuals.i18n.I18n.get(SplashProgress.stageKey(progress));
        }

        static String footer() {
            return dev.linkenvisuals.i18n.I18n.get("linkenvisuals.linkmenu.disclaimer2",
                SharedConstants.getGameVersion().name(), LinkenMenuChrome.version());
        }

        static String disclaimer() {
            return dev.linkenvisuals.i18n.I18n.get("loading.splash.disclaimer");
        }
    }
}
