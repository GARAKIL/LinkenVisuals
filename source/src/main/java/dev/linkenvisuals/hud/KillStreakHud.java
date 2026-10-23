package dev.linkenvisuals.hud;

import dev.linkenvisuals.clickgui.ModuleRegistry;
import dev.linkenvisuals.i18n.I18n;
import dev.linkenvisuals.module.impl.KillStreakModule;
import dev.linkenvisuals.render.Fonts;
import dev.linkenvisuals.render.GlyphAtlas;
import dev.linkenvisuals.render.MsdfText;
import dev.linkenvisuals.render.RoundedRectPipeline;
import dev.linkenvisuals.ui.Theme;
import dev.linkenvisuals.util.ColorMath;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

/**
 * Donor-style KillStreak card: tile, overline, tier title, dots, ×N counter and
 * combo timer. The card uses the shared HudSurface/HudStyle material, so all
 * HUD styles and shapes apply; no raw GL is used.
 */
public final class KillStreakHud extends HudElement {
    private static final float HEIGHT = 32.0f;
    private static final float MIN_WIDTH = 132.0f;
    private static final long DISPLAY_MS = 3000L;
    private static final long FADE_MS = 350L;
    private static final float PAD = 7.0f;
    private static final float TILE = 20.0f;
    private static final float GAP = 6.0f;
    private static final float OVERLINE_SIZE = 5.5f;
    private static final float TITLE_SIZE = 8.5f;
    private static final float COUNTER_SIZE = 9.5f;

    private static boolean lastRenderVisible;
    private float cachedWidth = MIN_WIDTH;

    public KillStreakHud() {
        super("killstreak", 0.5f, 0.07f);
    }

    public static boolean lastRenderVisible() {
        return lastRenderVisible;
    }

    @Override
    public float width() {
        refreshWidth();
        return cachedWidth;
    }

    @Override
    public float height() {
        return HEIGHT;
    }

    private void refreshWidth() {
        GlyphAtlas bold = Fonts.titleFont();
        if (bold == null) {
            cachedWidth = MIN_WIDTH;
            return;
        }
        int streak = previewStreak();
        String overline = I18n.get("linkenvisuals.hud.killstreak");
        String title = titleFor(previewTier());
        String counter = "x" + Math.max(2, streak);
        float textWidth = Math.max(bold.width(overline, OVERLINE_SIZE), bold.width(title, TITLE_SIZE));
        float counterWidth = bold.width(counter, COUNTER_SIZE) + 6.0f;
        cachedWidth = Math.max(MIN_WIDTH, PAD + TILE + GAP + textWidth + 10.0f
            + counterWidth + PAD);
    }

    @Override
    public void render(DrawContext context, float x, float y) {
        lastRenderVisible = false;
        if (ModuleRegistry.killStreakHud() == null || !ModuleRegistry.killStreakHud().value()) {
            return;
        }
        boolean editing = HudManager.isEditing();
        int streak = previewStreak();
        long now = System.currentTimeMillis();
        long age = KillStreakModule.lastKillMs() == 0L ? Long.MAX_VALUE : now - KillStreakModule.lastKillMs();
        if (!editing && (streak < 2 || age > DISPLAY_MS + FADE_MS)) {
            return;
        }
        float alpha = editing || age <= DISPLAY_MS ? 1.0f
            : 1.0f - Math.min(1.0f, (age - DISPLAY_MS) / (float) FADE_MS);
        if (alpha <= 0.01f) {
            return;
        }
        refreshWidth();
        GlyphAtlas bold = Fonts.titleFont();
        if (bold == null) {
            return;
        }
        float drawY = y;
        int accent = (KillStreakModule.themeAccentEnabled()
            ? Theme.accent() : Theme.DEFAULT_ACCENT) & 0xFFFFFF;
        int panelAlpha = Math.round(255.0f * alpha);
        int text = ColorMath.withAlpha(0xF2FBF5, panelAlpha);
        int muted = ColorMath.withAlpha(0xB6C9BE, Math.round(panelAlpha * 0.8f));

        HudSurface.panel(context, id(), x, drawY, cachedWidth, HEIGHT, alpha);
        RoundedRectPipeline.draw(context, x + PAD, drawY + (HEIGHT - TILE) * 0.5f,
            TILE, TILE, 6.0f, HudStyle.inset(Math.round(230.0f * alpha)));
        RoundedRectPipeline.drawBorder(context, x + PAD, drawY + (HEIGHT - TILE) * 0.5f,
            TILE, TILE, 6.0f, 0.0f, 1.0f, 1.4f,
            ColorMath.withAlpha(accent, Math.round(180.0f * alpha)));
        drawSword(context, x + PAD + TILE * 0.5f, drawY + HEIGHT * 0.5f,
            Math.round(190.0f * alpha), accent);

        float textX = x + PAD + TILE + GAP;
        MsdfText.drawOn(context, bold, I18n.get("linkenvisuals.hud.killstreak"),
            textX, drawY + 9.0f, OVERLINE_SIZE, muted);
        MsdfText.drawOn(context, bold, titleFor(previewTier()),
            textX, drawY + 20.5f, TITLE_SIZE, text);
        float dotsX = textX + bold.width(I18n.get("linkenvisuals.hud.killstreak"), OVERLINE_SIZE) + 4.0f;
        for (int i = 0; i < 5; i++) {
            boolean active = i <= previewTier();
            RoundedRectPipeline.draw(context, dotsX + i * 4.0f, drawY + 8.0f,
                active ? 3.0f : 2.0f, 2.0f, 1.0f,
                ColorMath.withAlpha(active ? accent : 0xFFFFFF,
                    active ? Math.round(220.0f * alpha) : Math.round(45.0f * alpha)));
        }

        String counter = "x" + Math.max(2, streak);
        float counterX = x + cachedWidth - PAD - bold.width(counter, COUNTER_SIZE);
        MsdfText.drawOn(context, bold, counter, counterX, drawY + HEIGHT * 0.5f,
            COUNTER_SIZE, ColorMath.withAlpha(accent, panelAlpha));

        if (KillStreakModule.comboTimerEnabled()) {
            long remainingAge = editing ? 0L : Math.min(age, DISPLAY_MS);
            float remain = 1.0f - Math.min(1.0f, remainingAge / (float) DISPLAY_MS);
            float barY = drawY + HEIGHT - 3.0f;
            RoundedRectPipeline.draw(context, x + PAD, barY, cachedWidth - PAD * 2.0f, 2.0f,
                1.0f, ColorMath.withAlpha(0xFFFFFF, Math.round(24.0f * alpha)));
            if (remain > 0.01f) {
                RoundedRectPipeline.draw(context, x + PAD, barY,
                    (cachedWidth - PAD * 2.0f) * remain, 2.0f, 1.0f,
                    ColorMath.withAlpha(accent, Math.round(200.0f * alpha)));
            }
        }
        lastRenderVisible = true;
    }

    private static int previewStreak() {
        return HudManager.isEditing() ? 3 : KillStreakModule.streak();
    }

    private static int previewTier() {
        return HudManager.isEditing() ? 1 : KillStreakModule.tier();
    }

    private static String titleFor(int tier) {
        return switch (Math.max(0, Math.min(4, tier))) {
            case 0 -> I18n.get("linkenvisuals.hud.killstreak.double");
            case 1 -> I18n.get("linkenvisuals.hud.killstreak.triple");
            case 2 -> I18n.get("linkenvisuals.hud.killstreak.mega");
            case 3 -> I18n.get("linkenvisuals.hud.killstreak.monster");
            default -> I18n.get("linkenvisuals.hud.killstreak.unstop");
        };
    }

    private static void drawSword(DrawContext context, float cx, float cy, int alpha, int accent) {
        int colour = ColorMath.withAlpha(accent, alpha);
        RoundedRectPipeline.draw(context, cx - 1.0f, cy - 7.0f, 2.0f, 10.0f, 1.0f, colour);
        RoundedRectPipeline.draw(context, cx - 5.0f, cy + 2.0f, 10.0f, 1.6f, 0.8f, colour);
        RoundedRectPipeline.draw(context, cx - 1.5f, cy + 3.0f, 3.0f, 4.0f, 1.0f, colour);
    }
}
