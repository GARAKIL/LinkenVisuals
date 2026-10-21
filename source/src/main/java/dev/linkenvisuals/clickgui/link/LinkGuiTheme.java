package dev.linkenvisuals.clickgui.link;

import dev.linkenvisuals.ui.linkmenu.LinkenMenuChrome;

import java.util.Locale;

/**
 * Palette of the LINKEN ClickGUI. Dark presets keep the original tower palette;
 * LIGHT and FROST switch the whole shell (backdrop, panels, rows, text) to the
 * donor's light material pass.
 */
public final class LinkGuiTheme {
    public enum Preset {
        MIDNIGHT("Midnight", 0x7EDFB4, 0x3F8E6B, 0x0F1217, false),
        GRAPHITE("Graphite", 0x9AA4AD, 0x5C656D, 0x121317, false),
        AMETHYST("Amethyst", 0xB18AE0, 0x6A4F8A, 0x14101C, false),
        OCEAN("Ocean", 0x4DA3E8, 0x2D628F, 0x0C121C, false),
        ROSE("Rose", 0xE05C74, 0x8C3446, 0x1A0E16, false),
        EMERALD("Emerald", 0x3ECF7A, 0x237A48, 0x0C1612, false),
        LIGHT("Light", 0x4DA3E8, 0x2D628F, 0xECEFF3, true),
        FROST("Frost", 0x8A63E8, 0x5A3F9A, 0xF4F6FA, true);

        private final String label;
        private final int accent;
        private final int accentDim;
        private final int background;
        private final boolean light;

        Preset(String label, int accent, int accentDim, int background, boolean light) {
            this.label = label;
            this.accent = accent;
            this.accentDim = accentDim;
            this.background = background;
            this.light = light;
        }

        public String label() {
            return label;
        }

        public int accent() {
            return accent;
        }

        public int accentDim() {
            return accentDim;
        }

        public int background() {
            return background;
        }

        public boolean light() {
            return light;
        }

        public static Preset byName(String name) {
            if (name == null || name.isBlank()) {
                return MIDNIGHT;
            }
            for (Preset preset : values()) {
                if (preset.name().equalsIgnoreCase(name.trim())) {
                    return preset;
                }
            }
            return MIDNIGHT;
        }
    }

    public static int ACCENT = Preset.MIDNIGHT.accent();
    public static int ACCENT_DIM = Preset.MIDNIGHT.accentDim();
    public static int TEXT = LinkenMenuChrome.TEXT;
    public static int TEXT2 = LinkenMenuChrome.TEXT3;
    public static int TEXT3 = LinkenMenuChrome.TEXT3;
    public static int MUTED = LinkenMenuChrome.MUTED;
    public static int MUTED2 = LinkenMenuChrome.MUTED2;
    public static int MUTED3 = LinkenMenuChrome.MUTED4;
    public static int INK = LinkenMenuChrome.INK;
    public static int ON_DARK = LinkenMenuChrome.TEXT;
    public static int DANGER = LinkenMenuChrome.DANGER;
    public static int PANEL = 0x151B17;
    public static int PANEL_LIGHT = 0x1B221D;
    public static int ROW = 0x181F1A;
    public static int HOVER = 0x202922;
    public static int ACTIVE = 0x1A231D;
    public static int FIELD = 0x0C100E;
    public static int TRACK = 0x2B342E;
    public static int BACKDROP_TOP = 0x050706;
    public static int BACKDROP_BOTTOM = 0x020403;

    private static Preset preset = Preset.MIDNIGHT;
    private static boolean light;

    private LinkGuiTheme() {
    }

    public static Preset preset() {
        return preset;
    }

    public static boolean isLight() {
        return light;
    }

    public static void apply(String name) {
        preset = Preset.byName(name);
        light = preset.light();
        ACCENT = preset.accent();
        ACCENT_DIM = preset.accentDim();
        if (light) {
            TEXT = 0x1C1F26;
            TEXT2 = 0x2B3039;
            TEXT3 = 0x3C424D;
            MUTED = 0x626A75;
            MUTED2 = 0x7A828D;
            MUTED3 = 0xA0A7B1;
            INK = 0xFFFFFF;
            ON_DARK = 0xEEF2EF;
            PANEL = 0xEEF1F6;
            PANEL_LIGHT = 0xF7F9FC;
            ROW = 0xF2F4F8;
            HOVER = 0xE7ECF3;
            ACTIVE = 0xE1EAF5;
            FIELD = 0xFFFFFF;
            TRACK = 0xD3DAE4;
            BACKDROP_TOP = 0xF5F7FA;
            BACKDROP_BOTTOM = 0xE3E8F0;
        } else {
            TEXT = LinkenMenuChrome.TEXT;
            TEXT2 = LinkenMenuChrome.TEXT3;
            TEXT3 = LinkenMenuChrome.TEXT3;
            MUTED = LinkenMenuChrome.MUTED;
            MUTED2 = LinkenMenuChrome.MUTED2;
            MUTED3 = LinkenMenuChrome.MUTED4;
            INK = LinkenMenuChrome.INK;
            ON_DARK = LinkenMenuChrome.TEXT;
            PANEL = 0x151B17;
            PANEL_LIGHT = 0x1B221D;
            ROW = 0x181F1A;
            HOVER = 0x202922;
            ACTIVE = 0x1A231D;
            FIELD = 0x0C100E;
            TRACK = 0x2B342E;
            BACKDROP_TOP = 0x050706;
            BACKDROP_BOTTOM = 0x020403;
        }
    }

    public static String cycle() {
        Preset[] values = Preset.values();
        preset = values[(preset.ordinal() + 1) % values.length];
        apply(preset.name());
        return preset.name();
    }

    public static String themeLabel() {
        return preset.label().toUpperCase(Locale.ROOT);
    }

    public static int rgba(int rgb, float alpha) {
        return LinkenMenuChrome.rgba(rgb, alpha);
    }

    public static int rgba(int rgb, int alpha) {
        return (Math.max(0, Math.min(255, alpha)) << 24) | (rgb & 0xFFFFFF);
    }
}
