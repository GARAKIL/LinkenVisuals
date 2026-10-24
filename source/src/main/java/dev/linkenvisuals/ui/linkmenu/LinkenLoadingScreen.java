package dev.linkenvisuals.ui.linkmenu;

import dev.linkenvisuals.render.GlowPipeline;
import dev.linkenvisuals.render.GlyphAtlas;
import net.minecraft.SharedConstants;
import net.minecraft.client.gl.RenderPipelines;
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
        float slide = SplashProgress.smoothstep(exit) * screenHeight;
        context.getMatrices().pushMatrix();
        if (slide > 0.0f) {
            context.getMatrices().translate(0.0f, -slide);
        }
        try {
            drawBootstrap(context, screenWidth, screenHeight, progress, fade, a, subtitle);
        } finally {
            context.getMatrices().popMatrix();
        }
    }

    /**
     * First-frame scene. The custom shaders and MSDF pipelines are not loaded
     * yet, so every glyph is rasterised on the CPU by {@link SplashFont}. This
     * is the same composition as the full scene: without it the first frames
     * were a nearly empty black screen with only the corner brackets.
     */
    private static void drawBootstrap(DrawContext context, int sw, int sh,
                                      float progress, float fade, int a, String subtitle) {
        int top = (a << 24) | 0x0A0E0C;
        int mid = (a << 24) | 0x060908;
        int bottom = (a << 24) | 0x050706;
        int split = Math.round(sh * 0.55f);
        context.fillGradient(0, 0, sw, split, top, mid);
        context.fillGradient(0, split, sw, sh, mid, bottom);

        double gui = net.minecraft.client.MinecraftClient.getInstance().getWindow().getScaleFactor();
        if (gui <= 0.0) {
            gui = 1.0;
        }
        int alignTo = Math.max(1, (int) Math.round(gui));
        float fw = (float) (sw * gui);
        float fh = (float) (sh * gui);
        float k = Math.min(fw / DESIGN_WIDTH, fh / DESIGN_HEIGHT);
        if (k <= 0.0f) {
            return;
        }
        int tint = (a << 24) | 0xFFFFFF;

        SplashFont.Run glow = SplashFont.glow(0x7EDFB4, 0.06f);
        if (glow != null) {
            int width = Math.round(sw * 0.92f);
            int height = Math.round(sh * 0.76f);
            int x = (sw - width) / 2;
            int y = Math.round(sh * 0.42f) - height / 2;
            context.drawTexture(RenderPipelines.GUI_TEXTURED, glow.id(), x, y, 0.0f, 0.0f,
                width, height, glow.width(), glow.height(), glow.width(), glow.height(), tint);
        }

        int inset = Math.max(2, logical(24.0f * k, gui));
        int length = Math.max(2, logical(18.0f * k, gui));
        int thick = Math.max(1, logical(k, gui));
        int corner = (Math.round(a * 0.35f) << 24) | 0x7EDFB4;
        context.fill(inset, inset, inset + length, inset + thick, corner);
        context.fill(inset, inset, inset + thick, inset + length, corner);
        context.fill(sw - inset - length, sh - inset - thick, sw - inset, sh - inset, corner);
        context.fill(sw - inset - thick, sh - inset - length, sw - inset, sh - inset, corner);

        drawBootstrapWordmark(context, fw, fh, k, gui, alignTo, tint);
        drawBootstrapProgress(context, fw, fh, k, gui, alignTo, tint, fade, progress, subtitle);
        drawBootstrapFooter(context, fw, fh, k, gui, alignTo, tint);
    }

    private static void drawBootstrapWordmark(DrawContext context, float fw, float fh, float k,
                                              double gui, int alignTo, int tint) {
        int wordPx = Math.max(8, Math.round(72.0f * k));
        int visualsPx = Math.max(5, Math.round(12.0f * k));
        SplashFont.Run word = SplashFont.text(SplashFont.Face.GOLOS_SEMI, "linken", wordPx,
            0xEEF2EF, -0.04f, alignTo);
        SplashFont.Run dot = SplashFont.text(SplashFont.Face.GOLOS_SEMI, ".", wordPx,
            0x7EDFB4, 0.0f, alignTo);
        SplashFont.Run visuals = SplashFont.text(SplashFont.Face.UNBOUNDED, "VISUALS", visualsPx,
            0x5D6862, 0.42f, alignTo);
        if (word == null) {
            return;
        }
        float wordBox = word.height() - word.pad() * 2.0f;
        float visualsBox = visuals == null ? 0.0f : visuals.height() - visuals.pad() * 2.0f;
        float gap = visuals == null ? 0.0f : 10.0f * k;
        float blockHeight = wordBox + gap + visualsBox;
        float blockTop = fh * 0.44f - blockHeight * 0.5f;
        float lineAdvance = word.advance() + (dot == null ? 0.0f : dot.advance());
        float pen = fw * 0.5f - lineAdvance * 0.5f;
        float baseline = blockTop + (word.baseline() - word.pad());
        drawRun(context, word, pen, baseline, gui, tint);
        if (dot != null) {
            drawRun(context, dot, pen + word.advance(), baseline, gui, tint);
        }
        if (visuals != null) {
            float visualsTop = blockTop + wordBox + gap;
            drawRun(context, visuals, fw * 0.5f - visuals.advance() * 0.5f,
                visualsTop + (visuals.baseline() - visuals.pad()), gui, tint);
        }
    }

    private static void drawBootstrapProgress(DrawContext context, float fw, float fh, float k,
                                               double gui, int alignTo, int tint, float fade,
                                               float progress, String subtitle) {
        int stagePx = Math.max(5, Math.round(12.5f * k));
        int percentPx = Math.max(5, Math.round(11.5f * k));
        String stageText = subtitle == null ? "" : subtitle;
        SplashFont.Run stage = SplashFont.text(SplashFont.Face.GOLOS, stageText, stagePx,
            0x78837D, 0.0f, alignTo);
        SplashFont.Run percent = SplashFont.text(SplashFont.Face.PLEXMONO,
            Math.round(progress * 100.0f) + "%", percentPx, 0xC3CCC7, 0.0f, alignTo);

        float trackWidth = 460.0f * k;
        float trackLeft = fw * 0.5f - trackWidth * 0.5f;
        float rowBottom = fh - 200.0f * k;
        float baseline = rowBottom;
        float rowTop = rowBottom;
        if (stage != null) {
            float descender = stage.height() - stage.pad() - stage.baseline();
            baseline = rowBottom - descender;
            rowTop = baseline - (stage.baseline() - stage.pad());
        }
        float trackHeight = Math.max(1.0f, 3.0f * k);
        float trackBottom = rowTop - 12.0f * k;
        float trackTop = trackBottom - trackHeight;

        int x1 = logical(trackLeft, gui);
        int x2 = logical(trackLeft + trackWidth, gui);
        int y1 = logical(trackTop, gui);
        int y2 = y1 + Math.max(1, (int) Math.round(trackHeight / gui));
        fillRounded(context, x1, y1, x2, y2, (Math.round(fade * 255.0f * 0.08f) << 24) | 0xFFFFFF);

        int filled = logical(trackLeft + trackWidth * progress, gui);
        if (filled > x1) {
            int spread = Math.max(1, logical(12.0f * k, gui));
            int halo = Math.round(fade * 255.0f * 0.42f);
            if (halo > 2) {
                context.fillGradient(x1, y1 - spread, filled, y1,
                    (0 << 24) | 0x7EDFB4, (halo << 24) | 0x7EDFB4);
                context.fillGradient(x1, y2, filled, y2 + spread,
                    (halo << 24) | 0x7EDFB4, (0 << 24) | 0x7EDFB4);
            }
            fillRounded(context, x1, y1, filled, y2, (Math.round(fade * 255.0f) << 24) | 0x7EDFB4);
        }
        if (stage != null) {
            drawRun(context, stage, trackLeft, baseline, gui, tint);
        }
        if (percent != null) {
            drawRun(context, percent, trackLeft + trackWidth - percent.advance(), baseline, gui, tint);
        }
    }

    private static void drawBootstrapFooter(DrawContext context, float fw, float fh, float k,
                                             double gui, int alignTo, int tint) {
        int footerPx = Math.max(5, Math.round(9.5f * k));
        int disclaimerPx = Math.max(5, Math.round(10.5f * k));
        SplashFont.Run footer = SplashFont.text(SplashFont.Face.PLEXMONO,
            I18nFallback.footer(), footerPx, 0x4C5651, 0.0f, alignTo);
        SplashFont.Run disclaimer = SplashFont.text(SplashFont.Face.GOLOS,
            I18nFallback.disclaimer(), disclaimerPx, 0x3F4A44, 0.0f, alignTo);
        float bottom = fh - 44.0f * k;
        if (disclaimer != null) {
            float box = disclaimer.height() - disclaimer.pad() * 2.0f;
            float top = bottom - box;
            drawRun(context, disclaimer, fw * 0.5f - disclaimer.advance() * 0.5f,
                top + (disclaimer.baseline() - disclaimer.pad()), gui, tint);
            bottom = top - 6.0f * k;
        }
        if (footer != null) {
            float box = footer.height() - footer.pad() * 2.0f;
            float top = bottom - box;
            drawRun(context, footer, fw * 0.5f - footer.advance() * 0.5f,
                top + (footer.baseline() - footer.pad()), gui, tint);
        }
    }

    private static void drawRun(DrawContext context, SplashFont.Run run, float penX, float baselineY,
                                double gui, int tint) {
        int x = logical(penX - run.pad(), gui);
        int y = logical(baselineY - run.baseline(), gui);
        int width = Math.max(1, (int) Math.round(run.width() / gui));
        int height = Math.max(1, (int) Math.round(run.height() / gui));
        context.drawTexture(RenderPipelines.GUI_TEXTURED, run.id(), x, y, 0.0f, 0.0f,
            width, height, run.width(), run.height(), run.width(), run.height(), tint);
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

    /** Releases CPU-rasterised bootstrap textures once the splash is gone. */
    public static void dispose() {
        SplashFont.dispose();
    }

    private static int logical(float physical, double gui) {
        return (int) Math.round(physical / gui);
    }

    /**
     * A rounded bar made from plain fills: the rounded-rect shader is not
     * available during the first resource reload. At 2-4 px tall a 1 px corner
     * cut plus a half-alpha corner pixel reads as a round cap.
     */
    private static void fillRounded(DrawContext context, int x1, int y1, int x2, int y2, int color) {
        int w = x2 - x1;
        int h = y2 - y1;
        if (w <= 2 || h <= 2) {
            context.fill(x1, y1, x2, y2, color);
            return;
        }
        int soft = (color & 0xFFFFFF) | (Math.round(((color >>> 24) & 0xFF) * 0.45f) << 24);
        context.fill(x1 + 1, y1, x2 - 1, y2, color);
        context.fill(x1, y1 + 1, x1 + 1, y2 - 1, color);
        context.fill(x2 - 1, y1 + 1, x2, y2 - 1, color);
        context.fill(x1, y1, x1 + 1, y1 + 1, soft);
        context.fill(x1, y2 - 1, x1 + 1, y2, soft);
        context.fill(x2 - 1, y1, x2, y1 + 1, soft);
        context.fill(x2 - 1, y2 - 1, x2, y2, soft);
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
