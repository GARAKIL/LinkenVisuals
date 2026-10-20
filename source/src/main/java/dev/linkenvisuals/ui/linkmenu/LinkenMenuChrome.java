package dev.linkenvisuals.ui.linkmenu;

import dev.linkenvisuals.LinkenVisuals;
import dev.linkenvisuals.i18n.I18n;
import dev.linkenvisuals.media.MediaWatcher;
import dev.linkenvisuals.render.Fonts;
import dev.linkenvisuals.render.GlowPipeline;
import dev.linkenvisuals.render.GlyphAtlas;
import dev.linkenvisuals.render.IconPipeline;
import dev.linkenvisuals.render.MsdfText;
import dev.linkenvisuals.render.RoundedRectPipeline;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Visual chrome shared by the LINKEN main menu (and later by M4 screens):
 * palette tokens, background art, wordmark, clock, button material, socials,
 * media card and footer.
 *
 * <p>All coordinates and sizes are design units of the 1920x1080 canvas.
 */
public final class LinkenMenuChrome {
    public static final int TEXT = 0xEEF2EF;
    public static final int TEXT2 = 0xDFE5E1;
    public static final int TEXT3 = 0xC3CCC7;
    public static final int TEXT4 = 0xA8B1AC;
    public static final int MUTED = 0x8B968F;
    public static final int MUTED2 = 0x78837D;
    public static final int MUTED3 = 0x5D6862;
    public static final int MUTED4 = 0x4C5651;
    public static final int INK = 0x0B120E;
    public static final int ACCENT = 0x7EDFB4;
    public static final int DANGER = 0xE08C8C;

    public static final int ICON_PERSON = 'P';
    public static final int ICON_PEOPLE = 'N';
    public static final int ICON_GEAR = 'O';
    public static final int ICON_POWER = 'Q';
    public static final int ICON_DISCORD = 0xF144;
    public static final int ICON_TELEGRAM = 0xF1B6;

    private static final Identifier BACKGROUND = LinkenVisuals.id("textures/linkmenu/bg-tower.png");
    private static final float BACKGROUND_WIDTH = 1270.0f;
    private static final float BACKGROUND_HEIGHT = 999.0f;

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("EEEE, d MMMM");

    private static long clockSecond = -1L;
    private static String clockTime = "";
    private static String clockDate = "";
    private static String modVersion;

    private LinkenMenuChrome() {
    }

    public static int rgba(int rgb, int alpha, float fade) {
        return (Math.max(0, Math.min(255, Math.round(alpha * fade))) << 24) | (rgb & 0xFFFFFF);
    }

    public static int rgba(int rgb, float fade) {
        return rgba(rgb, 255, fade);
    }

    // ------------------------------------------------------------------ background

    public static void drawBackground(DrawContext context, LinkenMenuLayout.Metrics layout,
                                      float parallaxX, float parallaxY, float fade) {
        float cover = Math.max(layout.width() / BACKGROUND_WIDTH, layout.height() / BACKGROUND_HEIGHT) * 1.07f;
        float drawWidth = BACKGROUND_WIDTH * cover;
        float drawHeight = BACKGROUND_HEIGHT * cover;
        float offsetX = -parallaxX * Math.min((drawWidth - layout.width()) * 0.5f, 42.0f);
        float offsetY = -parallaxY * Math.min((drawHeight - layout.height()) * 0.5f, 26.0f);
        float x = (layout.width() - drawWidth) * 0.5f + offsetX;
        float y = (layout.height() - drawHeight) * 0.5f + offsetY;

        context.drawTexture(RenderPipelines.GUI_TEXTURED, BACKGROUND,
            Math.round(x), Math.round(y), 0.0f, 0.0f,
            Math.round(drawWidth), Math.round(drawHeight),
            Math.round(BACKGROUND_WIDTH), Math.round(BACKGROUND_HEIGHT),
            Math.round(BACKGROUND_WIDTH), Math.round(BACKGROUND_HEIGHT), 0xFFFFFFFF);

        // Horizontal scrim built from narrow slices: DrawContext only exposes a
        // vertical gradient, and 32 steps are visually indistinguishable at 8-bit alpha.
        int strips = 32;
        int baseAlpha = Math.round(128.0f * fade);
        for (int i = 0; i < strips; i++) {
            float t = (i + 0.5f) / strips;
            int alpha = Math.round(baseAlpha * (1.0f - t));
            if (alpha <= 0) {
                continue;
            }
            int x0 = Math.round(layout.width() * i / strips);
            int x1 = Math.round(layout.width() * (i + 1) / strips);
            context.fill(x0, 0, x1, Math.round(layout.height()), alpha << 24 | 0x060908);
        }

        context.fillGradient(0, 0, Math.round(layout.width()), Math.round(layout.height()),
            rgba(0x040706, 34, fade), rgba(0x040706, 150, fade));

        GlowPipeline.radial(context, 260.0f, 560.0f, 540.0f, rgba(ACCENT, 10, fade));
        GlowPipeline.radial(context, layout.width() * 0.72f, layout.height() * 0.30f, 460.0f,
            rgba(ACCENT, 8, fade));
    }

    // ------------------------------------------------------------------ header

    public static void drawWordmark(DrawContext context, float fade) {
        GlyphAtlas font = LinkenFonts.bodyMedium();
        float size = 27.0f;
        float x = LinkenMenuLayout.MARGIN;
        float y = 36.0f;
        String word = "linken";
        drawText(context, font, word, x, y, size, rgba(TEXT, fade));
        float wordWidth = textWidth(font, word, size);
        drawText(context, font, ".", x + wordWidth, y, size, rgba(ACCENT, fade));
        drawSpaced(context, LinkenFonts.headingLight(), I18n.get("linkenvisuals.linkmenu.visuals"),
            x, y + 29.0f, 9.0f, 2.1f, rgba(MUTED3, fade));
    }

    public static void drawClock(DrawContext context, LinkenMenuLayout.Metrics layout, float fade) {
        updateClock();
        float right = layout.clock().right();
        drawRightText(context, LinkenFonts.mono(), clockTime, right, 36.0f, 44.0f, rgba(TEXT, fade));
        drawRightText(context, LinkenFonts.body(), clockDate, right, 84.0f, 12.5f, rgba(MUTED2, fade));
    }

    private static void updateClock() {
        long second = System.currentTimeMillis() / 1000L;
        if (second == clockSecond) {
            return;
        }
        clockSecond = second;
        Locale locale = currentLocale();
        clockTime = LocalTime.now().format(TIME_FORMAT);
        String date = LocalDate.now().format(DATE_FORMAT.withLocale(locale));
        clockDate = date.isEmpty() ? date : date.substring(0, 1).toUpperCase(locale) + date.substring(1);
    }

    private static Locale currentLocale() {
        String language = I18n.getEffectiveLanguage();
        if (language.startsWith("ru")) {
            return Locale.forLanguageTag("ru");
        }
        if (language.startsWith("uk")) {
            return Locale.forLanguageTag("uk");
        }
        return Locale.ENGLISH;
    }

    // ------------------------------------------------------------------ buttons

    public static void drawButton(DrawContext context, LinkenMenuLayout.Rect rect, boolean primary,
                                  int icon, String label, float hover, boolean danger, boolean centered,
                                  float fade, float uiScale) {
        float x = rect.x();
        float y = rect.y() - hover;
        float width = rect.width();
        float height = rect.height();
        float radius = 8.0f;

        if (primary) {
            roundedFill(context, x + 4.0f, y + height - 6.0f, width - 8.0f, 12.0f, radius,
                rgba(ACCENT, Math.round(60.0f + 40.0f * hover), fade), uiScale);
            int fill = mix(ACCENT, 0xFFFFFF, hover * 0.07f);
            roundedFill(context, x, y, width, height, radius, rgba(fill, fade), uiScale);
            context.fill(Math.round(x + radius), Math.round(y + 1.0f), Math.round(x + width - radius),
                Math.round(y + 2.0f), rgba(0xFFFFFF, 90, fade));
        } else {
            drawSecondary(context, x, y, width, height, radius, hover, danger, fade, uiScale);
        }

        float iconSize = 15.0f;
        float centerY = y + height * 0.5f;
        float labelSize = 12.5f;
        GlyphAtlas labelFont = primary ? LinkenFonts.heading() : LinkenFonts.headingLight();
        float labelWidth = textWidth(labelFont, label, labelSize);
        float contentWidth = iconSize + 9.0f + labelWidth;
        float iconX;
        float labelX;
        if (centered) {
            iconX = x + (width - contentWidth) * 0.5f;
            labelX = iconX + iconSize + 9.0f;
        } else {
            iconX = x + 16.0f;
            labelX = iconX + iconSize + 12.0f;
        }

        int contentColor = primary ? INK : (danger ? DANGER : mix(MUTED, ACCENT, hover));
        GlyphAtlas iconAtlas = LinkenFonts.icons();
        if (iconAtlas != null) {
            IconPipeline.draw(context, iconAtlas, icon, iconX + iconSize * 0.5f, centerY,
                iconSize, rgba(contentColor, fade));
        }
        drawOn(context, labelFont, label, labelX, centerY, labelSize, rgba(contentColor, fade));
    }

    public static void drawSecondary(DrawContext context, float x, float y, float width, float height,
                                     float radius, float hover, boolean danger, float fade, float uiScale) {
        roundedFill(context, x, y, width, height, radius, rgba(0x181E1B, 217, fade), uiScale);
        context.fill(Math.round(x + radius), Math.round(y + 1.0f), Math.round(x + width - radius),
            Math.round(y + 2.0f), rgba(0xFFFFFF, 13, fade));
        int border = danger ? mix(0xFFFFFF, DANGER, hover * 0.8f) : 0xFFFFFF;
        int borderAlpha = danger ? Math.round(23.0f + 117.0f * hover) : Math.round(23.0f + 38.0f * hover);
        roundedOutline(context, x, y, width, height, radius, 1.0f,
            rgba(border, borderAlpha, fade), uiScale);
    }

    // ------------------------------------------------------------------ socials

    public static void drawSocialTile(DrawContext context, LinkenMenuLayout.Rect rect, int icon,
                                      float hover, float fade, float uiScale) {
        float y = rect.y() - hover * 2.0f;
        drawSecondary(context, rect.x(), y, rect.width(), rect.height(), 8.0f, hover, false, fade, uiScale);
        GlyphAtlas brandAtlas = LinkenFonts.brands();
        if (brandAtlas != null) {
            IconPipeline.draw(context, brandAtlas, icon,
                rect.x() + rect.width() * 0.5f, y + rect.height() * 0.5f, 17.0f,
                rgba(mix(MUTED, ACCENT, hover), fade));
        }
    }

    // ------------------------------------------------------------------ media

    public static void drawMusic(DrawContext context, LinkenMenuLayout.Rect card, MediaWatcher.Track track,
                                 float fade, float uiScale) {
        drawSecondary(context, card.x(), card.y(), card.width(), card.height(), 8.0f, 0.0f, false, fade, uiScale);
        boolean present = track != null && track.present();

        drawSpaced(context, LinkenFonts.body(), I18n.get(present
                ? "linkenvisuals.linkmenu.media.now_playing"
                : "linkenvisuals.linkmenu.media.none"),
            card.x() + 14.0f, card.y() + 9.0f, 9.0f, 1.5f, rgba(MUTED2, fade));

        float coverX = card.x() + 14.0f;
        float coverY = card.y() + 26.0f;
        float coverSize = 40.0f;
        roundedFill(context, coverX, coverY, coverSize, coverSize, 8.0f, rgba(0x201A17, 255, fade), uiScale);
        roundedOutline(context, coverX, coverY, coverSize, coverSize, 8.0f, 1.0f,
            rgba(0xFFFFFF, 18, fade), uiScale);

        long now = System.currentTimeMillis();
        float barWidth = 3.0f;
        for (int i = 0; i < 3; i++) {
            float pulse = present && track.playing()
                ? 0.5f + 0.5f * (float) Math.sin(now / 180.0 + i * 1.7)
                : 0.25f;
            float barHeight = 8.0f + pulse * 14.0f;
            float barX = coverX + coverSize * 0.5f + (i - 1) * (barWidth + 4.0f) - barWidth * 0.5f;
            roundedFill(context, barX, coverY + coverSize * 0.5f - barHeight * 0.5f, barWidth, barHeight,
                1.5f, rgba(mix(MUTED, ACCENT, pulse), fade), uiScale);
        }

        float textX = coverX + coverSize + 12.0f;
        float textWidth = card.width() - (textX - card.x()) - 14.0f;
        if (present) {
            String title = ellipsize(LinkenFonts.heading(), track.title(), textWidth, 12.5f);
            drawText(context, LinkenFonts.heading(), title, textX, card.y() + 26.0f, 12.5f, rgba(TEXT, fade));

            String artist = track.artist().isBlank() ? track.app() : track.artist();
            artist = ellipsize(LinkenFonts.body(), artist, textWidth, 10.5f);
            drawText(context, LinkenFonts.body(), artist, textX, card.y() + 44.0f, 10.5f, rgba(MUTED2, fade));

            if (track.length() > 0L) {
                float elapsed = Math.max(0.0f, Math.min(track.position(), track.length()));
                float progress = Math.max(0.0f, Math.min(1.0f, elapsed / track.length()));
                String left = fmtTime((long) elapsed);
                String right = "-" + fmtTime(track.length() - (long) elapsed);
                float rowY = card.y() + 70.0f;
                float leftWidth = textWidth(LinkenFonts.mono(), left, 9.0f);
                float rightWidth = textWidth(LinkenFonts.mono(), right, 9.0f);
                drawText(context, LinkenFonts.mono(), left, card.x() + 14.0f, rowY, 9.0f, rgba(MUTED, fade));
                drawRightText(context, LinkenFonts.mono(), right, card.right() - 14.0f, rowY, 9.0f, rgba(MUTED, fade));

                float barX = card.x() + 14.0f + leftWidth + 8.0f;
                float barW = card.width() - 28.0f - leftWidth - rightWidth - 16.0f;
                if (barW > 4.0f) {
                    roundedFill(context, barX, rowY + 4.0f, barW, 2.0f, 1.0f, rgba(0xFFFFFF, 41, fade), uiScale);
                    if (progress > 0.001f) {
                        roundedFill(context, barX, rowY + 4.0f, barW * progress, 2.0f, 1.0f,
                            rgba(ACCENT, fade), uiScale);
                    }
                }
            }
        } else {
            String hint = ellipsize(LinkenFonts.body(), I18n.get("linkenvisuals.linkmenu.media.hint"),
                textWidth, 10.5f);
            drawText(context, LinkenFonts.body(), hint, textX, card.y() + 32.0f, 10.5f, rgba(MUTED2, fade));
        }
    }

    // ------------------------------------------------------------------ footer

    public static void drawFooter(DrawContext context, LinkenMenuLayout.Metrics layout, float fade) {
        float x = layout.footer().x();
        float y = layout.footer().y();
        float width = layout.footer().width();
        String first = ellipsize(LinkenFonts.body(),
            I18n.get("linkenvisuals.linkmenu.disclaimer1"), width, 10.0f);
        String second = ellipsize(LinkenFonts.body(),
            I18n.get("linkenvisuals.linkmenu.disclaimer2", SharedConstants.getGameVersion().name(), version()),
            width, 10.0f);
        drawText(context, LinkenFonts.body(), first, x, y, 10.0f, rgba(MUTED4, fade));
        drawText(context, LinkenFonts.body(), second, x, y + LinkenMenuLayout.FOOTER_LINE_HEIGHT,
            10.0f, rgba(MUTED4, fade));
    }

    // ------------------------------------------------------------------ text helpers

    public static void drawText(DrawContext context, GlyphAtlas font, String text, float x, float top,
                                float size, int color) {
        GlyphAtlas resolved = resolve(font, text);
        if (resolved != null) {
            MsdfText.draw(context, resolved, text, x, top, size, color);
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null) {
            context.drawTextWithShadow(client.textRenderer, text, Math.round(x), Math.round(top + size * 0.2f), color);
        }
    }

    public static void drawOn(DrawContext context, GlyphAtlas font, String text, float x, float centerY,
                              float size, int color) {
        GlyphAtlas resolved = resolve(font, text);
        if (resolved != null) {
            MsdfText.drawOn(context, resolved, text, x, centerY, size, color);
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null) {
            context.drawTextWithShadow(client.textRenderer, text, Math.round(x),
                Math.round(centerY - client.textRenderer.fontHeight * 0.5f), color);
        }
    }

    public static void drawCentered(DrawContext context, GlyphAtlas font, String text, float centerX,
                                    float centerY, float size, int color) {
        GlyphAtlas resolved = resolve(font, text);
        if (resolved != null) {
            MsdfText.drawCenteredOn(context, resolved, text, centerX, centerY, size, color);
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null) {
            context.drawCenteredTextWithShadow(client.textRenderer, text, Math.round(centerX),
                Math.round(centerY - client.textRenderer.fontHeight * 0.5f), color);
        }
    }

    public static void drawRightText(DrawContext context, GlyphAtlas font, String text, float rightX,
                                     float top, float size, int color) {
        GlyphAtlas resolved = resolve(font, text);
        if (resolved != null) {
            MsdfText.drawRight(context, resolved, text, rightX, top, size, color);
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null) {
            context.drawTextWithShadow(client.textRenderer, text,
                Math.round(rightX - client.textRenderer.getWidth(text)), Math.round(top + size * 0.2f), color);
        }
    }

    public static void drawSpaced(DrawContext context, GlyphAtlas font, String text, float x, float top,
                                  float size, float tracking, int color) {
        GlyphAtlas resolved = resolve(font, text);
        if (resolved == null) {
            drawText(context, font, text, x, top, size, color);
            return;
        }
        float cursor = x;
        for (int offset = 0; offset < text.length(); ) {
            int codepoint = text.codePointAt(offset);
            String glyph = new String(Character.toChars(codepoint));
            MsdfText.draw(context, resolved, glyph, cursor, top, size, color);
            cursor += resolved.width(glyph, size) + tracking;
            offset += Character.charCount(codepoint);
        }
    }

    public static float textWidth(GlyphAtlas font, String text, float size) {
        GlyphAtlas resolved = resolve(font, text);
        if (resolved != null) {
            return resolved.width(text, size);
        }
        MinecraftClient client = MinecraftClient.getInstance();
        return client != null ? client.textRenderer.getWidth(text) : text.length() * size * 0.5f;
    }

    /** Shrinks a headline just enough to fit, without dropping any words. */
    public static float fitSize(GlyphAtlas font, String text, float maxWidth, float preferred, float minimum) {
        if (text == null || text.isEmpty() || maxWidth <= 0.0f) {
            return preferred;
        }
        float width = textWidth(font, text, preferred);
        if (width <= maxWidth) {
            return preferred;
        }
        return Math.max(minimum, preferred * maxWidth / width);
    }

    public static String ellipsize(GlyphAtlas font, String text, float maxWidth, float size) {
        if (text == null || text.isEmpty() || maxWidth <= 0.0f) {
            return text == null ? "" : text;
        }
        if (textWidth(font, text, size) <= maxWidth) {
            return text;
        }
        String suffix = textWidth(font, "...", size) <= maxWidth ? "..." : "";
        int end = text.length();
        while (end > 0) {
            end = text.offsetByCodePoints(end, -1);
            String candidate = text.substring(0, end) + suffix;
            if (textWidth(font, candidate, size) <= maxWidth) {
                return candidate;
            }
        }
        return suffix;
    }

    private static GlyphAtlas resolve(GlyphAtlas preferred, String text) {
        if (preferred != null && preferred.canRender(text)) {
            return preferred;
        }
        GlyphAtlas title = Fonts.titleFont();
        if (title != null && title.canRender(text)) {
            return title;
        }
        GlyphAtlas regular = Fonts.textFont();
        if (regular != null && regular.canRender(text)) {
            return regular;
        }
        return null;
    }

    // ------------------------------------------------------------------ material helpers

    public static void roundedFill(DrawContext context, float x, float y, float width, float height,
                                   float radius, int color, float uiScale) {
        RoundedRectPipeline.draw(context, x, y, width, height, radius(radius, uiScale), color);
    }

    public static void roundedOutline(DrawContext context, float x, float y, float width, float height,
                                      float radius, float thickness, int color, float uiScale) {
        RoundedRectPipeline.drawOutline(context, x, y, width, height, radius(radius, uiScale),
            thickness(thickness, uiScale), color);
    }

    /**
     * RoundedRectPipeline takes radius/thickness in GUI units and multiplies
     * them by the window GUI scale internally. The LINKEN canvas is scaled by
     * {@code uiScale} through the matrix, so passing design units multiplied by
     * that same factor keeps the radius constant at every GUI scale.
     */
    private static float radius(float designRadius, float uiScale) {
        return designRadius * uiScale;
    }

    private static float thickness(float designThickness, float uiScale) {
        return designThickness * uiScale;
    }

    // ------------------------------------------------------------------ misc

    public static String greetingKey(int hour) {
        if (hour >= 5 && hour < 12) {
            return "linkenvisuals.linkmenu.greeting.morning";
        }
        if (hour >= 12 && hour < 18) {
            return "linkenvisuals.linkmenu.greeting.day";
        }
        if (hour >= 18 && hour < 23) {
            return "linkenvisuals.linkmenu.greeting.evening";
        }
        return "linkenvisuals.linkmenu.greeting.night";
    }

    public static String licenseWord() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.getSession() == null) {
            return I18n.get("linkenvisuals.linkmenu.account.offline");
        }
        // 1.21.11 no longer exposes a Session.AccountType; an offline/demo
        // session carries the placeholder token "0" (or no token at all).
        String token = client.getSession().getAccessToken();
        boolean licensed = token != null && !token.isBlank() && !"0".equals(token);
        return I18n.get(licensed
            ? "linkenvisuals.linkmenu.account.licensed"
            : "linkenvisuals.linkmenu.account.offline");
    }

    public static String username() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.getSession() == null || client.getSession().getUsername() == null) {
            return "player";
        }
        String name = client.getSession().getUsername();
        return name.isBlank() ? "player" : name;
    }

    public static String version() {
        if (modVersion == null) {
            modVersion = FabricLoader.getInstance()
                .getModContainer(LinkenVisuals.MOD_ID)
                .map(mod -> mod.getMetadata().getVersion().getFriendlyString())
                .orElse("dev");
        }
        return modVersion;
    }

    public static String fmtTime(long millis) {
        long seconds = Math.max(0L, millis) / 1000L;
        return seconds / 60L + ":" + (seconds % 60L < 10 ? "0" : "") + seconds % 60L;
    }

    public static int mix(int from, int to, float amount) {
        float t = Math.max(0.0f, Math.min(1.0f, amount));
        int r = Math.round(((from >> 16) & 0xFF) + ((((to >> 16) & 0xFF) - ((from >> 16) & 0xFF)) * t));
        int g = Math.round(((from >> 8) & 0xFF) + ((((to >> 8) & 0xFF) - ((from >> 8) & 0xFF)) * t));
        int b = Math.round((from & 0xFF) + (((to & 0xFF) - (from & 0xFF)) * t));
        return r << 16 | g << 8 | b;
    }

    public static void openUrl(String url) {
        try {
            Util.getOperatingSystem().open(url);
        } catch (RuntimeException failure) {
            LinkenVisuals.LOGGER.warn("Failed to open LINKEN menu URL '{}': {}", url, failure.getMessage());
        }
    }
}
