package dev.linkenvisuals.clickgui.link;

import dev.linkenvisuals.ui.linkmenu.LinkenMenuChrome;

import java.util.Locale;

/**
 * Palette of the LINKEN ClickGUI. Themes are deliberately dark-only: the
 * donor's light presets need a separate full material pass and are left for a
 * later polish milestone. The selected preset is persisted by
 * {@link LinkGuiConfig}.
 */
public final class LinkGuiTheme {
    public enum Preset {
        MIDNIGHT("Midnight", 0x7EDFB4, 0x3F8E6B),
        GRAPHITE("Graphite", 0x9AA4AD, 0x5C656D),
        AMETHYST("Amethyst", 0xB18AE0, 0x6A4F8A),
        OCEAN("Ocean", 0x4DA3E8, 0x2D628F),
        ROSE("Rose", 0xE05C74, 0x8C3446),
        EMERALD("Emerald", 0x3ECF7A, 0x237A48);

        private final String label;
        private final int accent;
        private final int accentDim;

        Preset(String label, int accent, int accentDim) {
            this.label = label;
            this.accent = accent;
            this.accentDim = accentDim;
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
    public static final int TEXT = LinkenMenuChrome.TEXT;
    public static final int TEXT2 = LinkenMenuChrome.TEXT3;
    public static final int TEXT3 = LinkenMenuChrome.TEXT3;
    public static final int MUTED = LinkenMenuChrome.MUTED;
    public static final int MUTED2 = LinkenMenuChrome.MUTED2;
    public static final int MUTED3 = LinkenMenuChrome.MUTED4;
    public static final int INK = LinkenMenuChrome.INK;
    public static final int DANGER = LinkenMenuChrome.DANGER;
    public static final int PANEL = 0x151B17;
    public static final int PANEL_LIGHT = 0x1B221D;
    public static final int ROW = 0x181F1A;
    public static final int TRACK = 0x2B342E;

    private static Preset preset = Preset.MIDNIGHT;

    private LinkGuiTheme() {
    }

    public static Preset preset() {
        return preset;
    }

    public static void apply(String name) {
        preset = Preset.byName(name);
        ACCENT = preset.accent();
        ACCENT_DIM = preset.accentDim();
    }

    public static String cycle() {
        Preset[] values = Preset.values();
        preset = values[(preset.ordinal() + 1) % values.length];
        ACCENT = preset.accent();
        ACCENT_DIM = preset.accentDim();
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
