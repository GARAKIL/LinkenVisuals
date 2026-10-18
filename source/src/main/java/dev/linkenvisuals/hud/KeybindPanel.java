package dev.linkenvisuals.hud;

import dev.linkenvisuals.render.Fonts;
import dev.linkenvisuals.util.ColorMath;
import dev.linkenvisuals.LinkenVisuals;
import dev.linkenvisuals.clickgui.ModuleInfo;
import dev.linkenvisuals.clickgui.ModuleRegistry;
import dev.linkenvisuals.render.GlassWindowPipeline;
import dev.linkenvisuals.render.GlowPipeline;
import dev.linkenvisuals.render.GlyphAtlas;
import dev.linkenvisuals.render.MsdfText;
import dev.linkenvisuals.render.NeonEdge;
import dev.linkenvisuals.render.RoundedRectPipeline;
import dev.linkenvisuals.ui.Theme;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class KeybindPanel extends HudElement {
    private static final Identifier ICON = LinkenVisuals.id("textures/gui/link_icon.png");

    private static final float PADDING = 7.0f;
    private static final float HEADER_H = 17.0f;
    private static final float ROW_H = 13.0f;
    private static final float ROW_PAD = 4.0f;

    private static final float ICON_W = 9.0f;
    private static final float ICON_H = 8.0f;
    private static final float MIN_WIDTH = 80.0f;
    private static final float ROW_ICON = 8.0f;

    private GlyphAtlas text;
    private GlyphAtlas bold;
    private final List<Entry> entries = new ArrayList<>();
    private float cachedWidth = MIN_WIDTH;
    private float cachedHeight;
    private int cachedRevision = -1;

    public KeybindPanel() {
        super("keybinds", 0.9f, 0.16f);
    }

    private void ensureLoaded() {
        if (text == null) {
            text = Fonts.textFont();
        }
        if (bold == null) {
            bold = Fonts.titleFont();
        }
    }

    private void refresh() {
        int revision = ModuleInfo.revision();
        if (revision == cachedRevision) {
            return;
        }
        cachedRevision = revision;

        entries.clear();
        for (ModuleInfo module : ModuleRegistry.all()) {
            if (module.hasKeybind() && !module.binding()) {
                entries.add(new Entry(module.name(), module.keybind().toUpperCase(Locale.ROOT), module.category().icon()));
            }
        }

        if (entries.isEmpty()) {
            cachedWidth = 0.0f;
            cachedHeight = 0.0f;
            return;
        }

        ensureLoaded();
        float widest = MsdfText.width(bold, "Keybinds", 6.8f) + ICON_W + 8.0f;
        for (Entry entry : entries) {
            float rowWidth = ROW_ICON + 6.0f + MsdfText.width(bold, entry.name, 6.6f) + 12.0f
                + MsdfText.width(bold, entry.key, 5.9f);
            widest = Math.max(widest, rowWidth);
        }
        cachedWidth = Math.max(MIN_WIDTH, widest + PADDING * 2.0f);
        cachedHeight = HEADER_H + ROW_PAD + entries.size() * ROW_H + ROW_PAD;
    }

    @Override
    public float width() {
        refresh();
        return cachedWidth;
    }

    @Override
    public float height() {
        return cachedHeight;
    }

    @Override
    public void render(DrawContext context, float x, float y) {
        if (entries.isEmpty()) {
            return;
        }
        HudSurface.panel(context, "keybinds", x, y, cachedWidth, cachedHeight, 1.0f);

        float iconY = y + (HEADER_H - ICON_H) * 0.5f;
        context.drawTexture(RenderPipelines.GUI_TEXTURED, ICON, Math.round(x + PADDING), Math.round(iconY), 0.0f, 0.0f,
            Math.round(ICON_W), Math.round(ICON_H), 128, 114, 128, 114, ColorMath.withAlpha(0xFFFFFF, 220));

        float dividerX = x + PADDING + ICON_W + 5.0f;
        context.fill(Math.round(dividerX), Math.round(y + 5.0f), Math.round(dividerX) + 1, Math.round(y + HEADER_H - 5.0f),
            ColorMath.withAlpha(0xFFFFFF, 26));

        MsdfText.drawOn(context, bold, "Keybinds", x + PADDING + ICON_W + 8.0f,
            y + HEADER_H * 0.5f, 6.8f, ColorMath.withAlpha(0xFFFFFF, 235));

        context.fill(Math.round(x + PADDING), Math.round(y + HEADER_H), Math.round(x + cachedWidth - PADDING),
            Math.round(y + HEADER_H) + 1, ColorMath.withAlpha(0xFFFFFF, 26));

        float rowY = y + HEADER_H + ROW_PAD;
        for (Entry entry : entries) {
            float centerY = rowY + ROW_H * 0.5f;

            context.drawTexture(RenderPipelines.GUI_TEXTURED, entry.mark,
                Math.round(x + PADDING), Math.round(centerY - ROW_ICON * 0.5f), 0.0f, 0.0f,
                Math.round(ROW_ICON), Math.round(ROW_ICON), 128, 128, 128, 128,
                ColorMath.withAlpha(NeonEdge.cool(), 190));

            MsdfText.drawOn(context, bold, entry.name, x + PADDING + ROW_ICON + 6.0f, centerY, 6.6f,
                ColorMath.withAlpha(0xFFFFFF, 230));

            MsdfText.drawRightOn(context, bold, entry.key, x + cachedWidth - PADDING, centerY, 5.9f,
                ColorMath.withAlpha(0xFFFFFF, 140));

            rowY += ROW_H;
        }
    }

    private record Entry(String name, String key, Identifier mark) {
    }
}
