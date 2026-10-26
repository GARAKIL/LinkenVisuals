package dev.linkenvisuals.ui.linkmenu;

import dev.linkenvisuals.render.GlyphAtlas;
import net.minecraft.client.gui.DrawContext;

public final class ClickGuiIcons {
    private ClickGuiIcons() {
    }

    private static void fa(DrawContext context, char codepoint, float cx, float cy, float size, int color) {
        GlyphAtlas atlas = LinkenFonts.iconsFa();
        LinkenMenuChrome.drawCentered(context, atlas, String.valueOf(codepoint), cx, cy, size, color);
    }

    public static void user(DrawContext context, float cx, float cy, float size, int color) {
        fa(context, '\uF007', cx, cy, size, color);
    }

    public static void musicNote(DrawContext context, float cx, float cy, float size, int color) {
        fa(context, '\uF001', cx, cy, size, color);
    }

    public static void monitor(DrawContext context, float cx, float cy, float size, int color) {
        fa(context, '\uF108', cx, cy, size, color);
    }

    public static void keyboard(DrawContext context, float cx, float cy, float size, int color) {
        fa(context, '\uF11C', cx, cy, size, color);
    }

    public static void globe(DrawContext context, float cx, float cy, float size, int color) {
        fa(context, '\uF0AC', cx, cy, size, color);
    }

    public static void chat(DrawContext context, float cx, float cy, float size, int color) {
        fa(context, '\uF075', cx, cy, size, color);
    }

    public static void layers(DrawContext context, float cx, float cy, float size, int color) {
        fa(context, '\uF5FD', cx, cy, size, color);
    }

    public static void accessibility(DrawContext context, float cx, float cy, float size, int color) {
        fa(context, '\uF29A', cx, cy, size, color);
    }

    public static void database(DrawContext context, float cx, float cy, float size, int color) {
        fa(context, '\uF1C0', cx, cy, size, color);
    }

    public static void info(DrawContext context, float cx, float cy, float size, int color) {
        fa(context, '\uF05A', cx, cy, size, color);
    }

    public static void check(DrawContext context, float cx, float cy, float size, int color) {
        fa(context, '\uF00C', cx, cy, size * 0.92f, color);
    }

    public static void gear(DrawContext context, float cx, float cy, float size, int color) {
        fa(context, '\uF013', cx, cy, size, color);
    }

    public static void chartBar(DrawContext context, float cx, float cy, float size, int color) {
        fa(context, '\uF080', cx, cy, size, color);
    }

    public static void trophy(DrawContext context, float cx, float cy, float size, int color) {
        fa(context, '\uF091', cx, cy, size, color);
    }

    public static void play(DrawContext context, float cx, float cy, float size, int color) {
        fa(context, '\uF04B', cx, cy, size * 0.9f, color);
    }

    public static void pause(DrawContext context, float cx, float cy, float size, int color) {
        fa(context, '\uF04C', cx, cy, size * 0.9f, color);
    }

    public static void backward(DrawContext context, float cx, float cy, float size, int color) {
        fa(context, '\uF048', cx, cy, size * 0.88f, color);
    }

    public static void forward(DrawContext context, float cx, float cy, float size, int color) {
        fa(context, '\uF051', cx, cy, size * 0.88f, color);
    }

    public static void volumeMute(DrawContext context, float cx, float cy, float size, int color) {
        fa(context, '\uF6A9', cx, cy, size * 0.9f, color);
    }

    public static void volumeUp(DrawContext context, float cx, float cy, float size, int color) {
        fa(context, '\uF028', cx, cy, size * 0.9f, color);
    }

    public static void volumeDown(DrawContext context, float cx, float cy, float size, int color) {
        fa(context, '\uF027', cx, cy, size * 0.9f, color);
    }

    public static void palette(DrawContext context, float cx, float cy, float size, int color) {
        fa(context, '\uF53F', cx, cy, size, color);
    }

    public static void sliders(DrawContext context, float cx, float cy, float size, int color) {
        fa(context, '\uF1DE', cx, cy, size, color);
    }

    public static void xmark(DrawContext context, float cx, float cy, float size, int color) {
        fa(context, '\uF00D', cx, cy, size, color);
    }

    public static void signOut(DrawContext context, float cx, float cy, float size, int color) {
        fa(context, '\uF08B', cx, cy, size * 0.95f, color);
    }

    public static void comment(DrawContext context, float cx, float cy, float size, int color) {
        fa(context, '\uF086', cx, cy, size, color);
    }
}
