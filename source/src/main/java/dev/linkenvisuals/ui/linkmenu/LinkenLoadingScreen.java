package dev.linkenvisuals.ui.linkmenu;

import dev.linkenvisuals.render.GlowPipeline;
import dev.linkenvisuals.render.GlyphAtlas;
import net.minecraft.SharedConstants;
import net.minecraft.client.gui.DrawContext;

/**
 * Full-screen LINKEN loading scene shared by the splash overlay and the four
 * vanilla loading screens.
 *
 * <p>The composition mirrors the donor 9d loading screen on its 1920x1080
 * canvas: a two-stop vertical gradient, an elliptical accent glow behind the
 * wordmark, corner brackets, the {@code linken.} + {@code VISUALS} block
 * centred at 44% height, a 460 px progress rail pinned 200 px above the bottom
 * edge and the stacked footer signature. No photography or badge logo is used.
 */
public final class LinkenLoadingScreen {
    private static final float DESIGN_WIDTH = 1920.0f;
    private static final float DESIGN_HEIGHT = 1080.0f;

    private static final float TRACK_WIDTH = 460.0f;
    private static final float TRACK_HEIGHT = 3.0f;
    private static final float CANCEL_WIDTH = 160.0f;
    private static final float CANCEL_HEIGHT = 44.0f;
    private static final float CANCEL_Y = 950.0f;

    private LinkenLoadingScreen() {
    }

    /**
     * First-frame bootstrap scene. This runs before the resource reload has
     * produced our shaders/font atlases, so it deliberately uses only vanilla
     * {@code fill}/{@code fillGradient} and the vanilla text renderer. Without
     * it the initial Mojang splash would stay visible until resources load.
     */
    public static void renderBootstrap(DrawContext context, int screenWidth, int screenHeight,
                                       float progress, float alpha, float exit,
                                       String title, String subtitle) {
        if (screenWidth <= 0 || screenHeight <= 0) {
            return;
        }
        float fade = SplashProgress.clamp01(alpha);
        if (fade <= 0.001f) {
            return;
        }
        int a = Math.round(fade * 255.0f);
        int top = (a << 24) | 0x0A0E0C;
        int mid = (a << 24) | 0x060908;
        int bottom = (a << 24) | 0x050706;
        float slide = SplashProgress.smoothstep(exit) * screenHeight;
        context.getMatrices().pushMatrix();
        if (slide > 0.0f) {
            context.getMatrices().translate(0.0f, -slide);
        }

        int split = Math.round(screenHeight * 0.55f);
        context.fillGradient(0, 0, screenWidth, split, top, mid);
        context.fillGradient(0, split, screenWidth, screenHeight, mid, bottom);

        float k = Math.min(screenWidth / DESIGN_WIDTH, screenHeight / DESIGN_HEIGHT);
        float trackWidth = Math.min(460.0f * k, screenWidth * 0.70f);
        float trackX = screenWidth * 0.5f - trackWidth * 0.5f;
        float trackY = screenHeight * 0.76f;
        float trackHeight = Math.max(2.0f, 3.0f * k);
        context.fill(Math.round(trackX), Math.round(trackY),
            Math.round(trackX + trackWidth), Math.round(trackY + trackHeight),
            (Math.round(a * 0.08f) << 24) | 0xFFFFFF);
        float fill = SplashProgress.displayProgress(progress);
        if (fill > 0.0f) {
            context.fill(Math.round(trackX), Math.round(trackY),
                Math.round(trackX + trackWidth * fill), Math.round(trackY + trackHeight),
                (a << 24) | 0x7EDFB4);
        }

        int inset = Math.max(12, Math.round(24.0f * k));
        int length = Math.max(10, Math.round(18.0f * k));
        int corner = (Math.round(a * 0.35f) << 24) | 0x7EDFB4;
        context.fill(inset, inset, inset + length, inset + 1, corner);
        context.fill(inset, inset, inset + 1, inset + length, corner);
        context.fill(screenWidth - inset - length, screenHeight - inset - 1,
            screenWidth - inset, screenHeight - inset, corner);
        context.fill(screenWidth - inset - 1, screenHeight - inset - length,
            screenWidth - inset, screenHeight - inset, corner);

        context.getMatrices().popMatrix();
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
        float slide = SplashProgress.smoothstep(exit) * height;
        if (slide > 0.0f) {
            context.getMatrices().translate(0.0f, -slide);
        }

        drawBackdrop(context, width, height, fade);
        drawGlow(context, width, height, fade);
        drawFrame(context, width, height, fade);
        drawWordmark(context, width, height, fade);
        drawProgress(context, width, height, progress, fade, scale, indeterminate);
        drawFooter(context, width, height, fade);

        context.getMatrices().popMatrix();
    }

    private static void drawBackdrop(DrawContext context, float width, float height, float fade) {
        int w = Math.max(1, Math.round(width));
        int h = Math.round(height);
        int split = Math.round(height * 0.55f);
        context.fillGradient(0, 0, w, split,
            LinkenMenuChrome.rgba(0x0A0E0C, fade), LinkenMenuChrome.rgba(0x060908, fade));
        context.fillGradient(0, split, w, h,
            LinkenMenuChrome.rgba(0x060908, fade), LinkenMenuChrome.rgba(0x050706, fade));
    }

    private static void drawGlow(DrawContext context, float width, float height, float fade) {
        // Donor glow is an ellipse at 50% / 42% of the frame, 92% x 76% large.
        // The radial shader is a unit quad, so stretching the matrix gives the
        // same soft elliptical falloff without a second pipeline.
        float centerX = width * 0.5f;
        float centerY = height * 0.42f;
        float radius = height * 0.38f;
        float stretch = Math.max(1.0f, width * 0.46f / radius);
        context.getMatrices().pushMatrix();
        context.getMatrices().translate(centerX, centerY);
        context.getMatrices().scale(stretch, 1.0f);
        GlowPipeline.radial(context, 0.0f, 0.0f, radius,
            LinkenMenuChrome.rgba(LinkenMenuChrome.ACCENT, 10, fade));
        context.getMatrices().popMatrix();
    }

    private static void drawFrame(DrawContext context, float width, float height, float fade) {
        int inset = 24;
        int length = 18;
        int color = LinkenMenuChrome.rgba(LinkenMenuChrome.ACCENT, 90, fade);
        context.fill(inset, inset, inset + length, inset + 1, color);
        context.fill(inset, inset, inset + 1, inset + length, color);
        context.fill(Math.round(width) - inset - length, Math.round(height) - inset - 1,
            Math.round(width) - inset, Math.round(height) - inset, color);
        context.fill(Math.round(width) - inset - 1, Math.round(height) - inset - length,
            Math.round(width) - inset, Math.round(height) - inset, color);
    }

    private static void drawWordmark(DrawContext context, float width, float height, float fade) {
        GlyphAtlas font = LinkenFonts.bodyMedium();
        float size = 72.0f;
        String word = "linken";
        String dot = ".";
        float tracking = -0.04f * size;
        float wordWidth = LinkenMenuChrome.textWidth(font, word, size)
            + tracking * Math.max(0, word.length() - 1);
        float dotWidth = LinkenMenuChrome.textWidth(font, dot, size);
        float lineWidth = wordWidth + dotWidth;
        float x = width * 0.5f - lineWidth * 0.5f;

        GlyphAtlas labelFont = LinkenFonts.headingLight();
        String label = "VISUALS";
        float labelSize = 12.0f;
        float labelTracking = 0.42f * labelSize;
        float labelWidth = LinkenMenuChrome.textWidth(labelFont, label, labelSize)
            + labelTracking * Math.max(0, label.length() - 1);

        // Donor centers the word + VISUALS block on 44% of the canvas. MSDF
        // drawing is top-anchored, so convert the visual cap boxes to tops.
        float wordBox = 0.70f * size;
        float labelBox = 0.70f * labelSize;
        float gap = 10.0f;
        float blockHeight = wordBox + gap + labelBox;
        float blockTop = height * 0.44f - blockHeight * 0.5f;
        float wordCenter = blockTop + wordBox * 0.5f;
        float labelCenter = blockTop + wordBox + gap + labelBox * 0.5f;
        float topOffset = 0.952f - 0.35f;

        LinkenMenuChrome.drawSpaced(context, font, word, x, wordCenter - topOffset * size,
            size, tracking, LinkenMenuChrome.rgba(LinkenMenuChrome.TEXT, fade));
        LinkenMenuChrome.drawOn(context, font, dot, x + wordWidth, wordCenter, size,
            LinkenMenuChrome.rgba(LinkenMenuChrome.ACCENT, fade));
        LinkenMenuChrome.drawSpaced(context, labelFont, label,
            width * 0.5f - labelWidth * 0.5f, labelCenter - topOffset * labelSize,
            labelSize, labelTracking, LinkenMenuChrome.rgba(LinkenMenuChrome.MUTED3, fade));
    }

    private static void drawProgress(DrawContext context, float width, float height, float progress,
                                     float fade, float scale, boolean indeterminate) {
        float stageSize = 12.5f;
        float percentSize = 11.5f;
        float trackLeft = width * 0.5f - TRACK_WIDTH * 0.5f;
        // Donor pins the label row 200 design px above the bottom edge.
        float rowBottom = height - 200.0f;
        float rowTop = rowBottom - 0.70f * stageSize;
        float trackBottom = rowTop - 12.0f;
        float trackTop = trackBottom - TRACK_HEIGHT;

        LinkenMenuChrome.roundedFill(context, trackLeft, trackTop, TRACK_WIDTH, TRACK_HEIGHT,
            TRACK_HEIGHT * 0.5f, LinkenMenuChrome.rgba(0xFFFFFF, 20, fade), scale);

        float fillWidth = 0.0f;
        if (indeterminate) {
            float sweep = (float) ((System.currentTimeMillis() * 0.00038) % 1.0);
            float beam = TRACK_WIDTH * 0.32f;
            float beamX = trackLeft + sweep * (TRACK_WIDTH - beam);
            fillWidth = beam;
            LinkenMenuChrome.roundedFill(context, beamX, trackTop, beam, TRACK_HEIGHT,
                TRACK_HEIGHT * 0.5f, LinkenMenuChrome.rgba(LinkenMenuChrome.ACCENT, fade), scale);
        } else {
            float value = SplashProgress.displayProgress(progress);
            if (value > 0.0f) {
                fillWidth = Math.max(TRACK_HEIGHT, TRACK_WIDTH * value);
                int glow = Math.round(107.0f * fade);
                int x1 = Math.round(trackLeft);
                int x2 = Math.round(trackLeft + fillWidth);
                int y1 = Math.round(trackTop);
                int y2 = Math.round(trackTop + TRACK_HEIGHT);
                if (glow > 2) {
                    context.fillGradient(x1, y1 - 12, x2, y1,
                        LinkenMenuChrome.rgba(LinkenMenuChrome.ACCENT, 0, fade),
                        LinkenMenuChrome.rgba(LinkenMenuChrome.ACCENT, 107, fade));
                    context.fillGradient(x1, y2, x2, y2 + 12,
                        LinkenMenuChrome.rgba(LinkenMenuChrome.ACCENT, 107, fade),
                        LinkenMenuChrome.rgba(LinkenMenuChrome.ACCENT, 0, fade));
                }
                LinkenMenuChrome.roundedFill(context, trackLeft, trackTop, fillWidth, TRACK_HEIGHT,
                    TRACK_HEIGHT * 0.5f, LinkenMenuChrome.rgba(LinkenMenuChrome.ACCENT, fade), scale);
            }
        }

        if (!indeterminate) {
            String stage = I18nFallback.stage(progress);
            String percent = Math.round(SplashProgress.displayProgress(progress) * 100.0f) + "%";
            if (!stage.isBlank()) {
                LinkenMenuChrome.drawText(context, LinkenFonts.body(), stage, trackLeft,
                    rowBottom - 0.952f * stageSize, stageSize,
                    LinkenMenuChrome.rgba(LinkenMenuChrome.MUTED2, fade));
            }
            LinkenMenuChrome.drawRightText(context, LinkenFonts.mono(), percent,
                trackLeft + TRACK_WIDTH, rowBottom - 0.952f * percentSize, percentSize,
                LinkenMenuChrome.rgba(LinkenMenuChrome.TEXT3, fade));
        }
    }

    private static void drawFooter(DrawContext context, float width, float height, float fade) {
        float disclaimerSize = 10.5f;
        float footerSize = 9.5f;
        float bottom = height - 44.0f;
        float disclaimerTop = bottom - 0.952f * disclaimerSize;
        LinkenMenuChrome.drawCentered(context, LinkenFonts.body(), I18nFallback.disclaimer(),
            width * 0.5f, disclaimerTop + 0.952f * disclaimerSize, disclaimerSize,
            LinkenMenuChrome.rgba(0x3F4A44, fade));

        // The footer signature sits directly above the disclaimer with 6 px
        // between their cap boxes, matching the donor's stacked footer.
        float disclaimerCapTop = disclaimerTop + 0.252f * disclaimerSize;
        float footerBottom = disclaimerCapTop - 6.0f;
        float footerTop = footerBottom - 0.952f * footerSize;
        String footer = LinkenMenuChrome.ellipsize(LinkenFonts.mono(), I18nFallback.footer(),
            width - 140.0f, footerSize);
        LinkenMenuChrome.drawCentered(context, LinkenFonts.mono(), footer, width * 0.5f,
            footerTop + 0.952f * footerSize, footerSize,
            LinkenMenuChrome.rgba(LinkenMenuChrome.MUTED4, fade));
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
