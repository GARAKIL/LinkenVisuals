package dev.linkenvisuals.clickgui.link;

import dev.linkenvisuals.clickgui.ModuleRegistry;
import dev.linkenvisuals.ui.linkmenu.LinkenMenuChrome;
import dev.linkenvisuals.util.ColorMath;

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

    private static int targetAccent = ACCENT;
    private static int targetAccentDim = ACCENT_DIM;
    private static int targetText = TEXT;
    private static int targetText2 = TEXT2;
    private static int targetText3 = TEXT3;
    private static int targetMuted = MUTED;
    private static int targetMuted2 = MUTED2;
    private static int targetMuted3 = MUTED3;
    private static int targetInk = INK;
    private static int targetOnDark = ON_DARK;
    private static int targetDanger = DANGER;
    private static int targetPanel = PANEL;
    private static int targetPanelLight = PANEL_LIGHT;
    private static int targetRow = ROW;
    private static int targetHover = HOVER;
    private static int targetActive = ACTIVE;
    private static int targetField = FIELD;
    private static int targetTrack = TRACK;
    private static int targetBackdropTop = BACKDROP_TOP;
    private static int targetBackdropBottom = BACKDROP_BOTTOM;
    private static boolean initialized;

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
        refreshCustom();
    }

    /**
     * Recomputes the target palette and (when custom colours are enabled)
     * overrides it. The displayed tokens are moved toward these targets by
     * {@link #tick(float)} so theme switches cross-fade instead of snapping.
     */
    public static void refreshCustom() {
        if (light) {
            targetAccent = preset.accent();
            targetAccentDim = preset.accentDim();
            targetText = 0x1C1F26;
            targetText2 = 0x2B3039;
            targetText3 = 0x3C424D;
            targetMuted = 0x626A75;
            targetMuted2 = 0x7A828D;
            targetMuted3 = 0xA0A7B1;
            targetInk = 0xFFFFFF;
            targetOnDark = 0xEEF2EF;
            targetPanel = 0xEEF1F6;
            targetPanelLight = 0xF7F9FC;
            targetRow = 0xF2F4F8;
            targetHover = 0xE7ECF3;
            targetActive = 0xE1EAF5;
            targetField = 0xFFFFFF;
            targetTrack = 0xD3DAE4;
            targetBackdropTop = 0xF5F7FA;
            targetBackdropBottom = 0xE3E8F0;
        } else {
            targetAccent = preset.accent();
            targetAccentDim = preset.accentDim();
            targetText = LinkenMenuChrome.TEXT;
            targetText2 = LinkenMenuChrome.TEXT3;
            targetText3 = LinkenMenuChrome.TEXT3;
            targetMuted = LinkenMenuChrome.MUTED;
            targetMuted2 = LinkenMenuChrome.MUTED2;
            targetMuted3 = LinkenMenuChrome.MUTED4;
            targetInk = LinkenMenuChrome.INK;
            targetOnDark = LinkenMenuChrome.TEXT;
            targetPanel = 0x151B17;
            targetPanelLight = 0x1B221D;
            targetRow = 0x181F1A;
            targetHover = 0x202922;
            targetActive = 0x1A231D;
            targetField = 0x0C100E;
            targetTrack = 0x2B342E;
            targetBackdropTop = 0x050706;
            targetBackdropBottom = 0x020403;
        }
        targetDanger = LinkenMenuChrome.DANGER;
        if (ModuleRegistry.customColors()
            && ModuleRegistry.uiBackground() != null && ModuleRegistry.uiText() != null) {
            int bg = ModuleRegistry.uiBackground().rgb() & 0xFFFFFF;
            int text = ModuleRegistry.uiText().rgb() & 0xFFFFFF;
            targetPanel = bg;
            targetPanelLight = ColorMath.mix(bg, 0xFFFFFF, light ? 0.08f : 0.06f);
            targetRow = ColorMath.mix(bg, 0xFFFFFF, light ? 0.04f : 0.03f);
            targetHover = ColorMath.mix(bg, light ? 0x000000 : 0xFFFFFF, light ? 0.05f : 0.08f);
            targetActive = ColorMath.mix(bg, light ? 0x000000 : 0xFFFFFF, light ? 0.09f : 0.12f);
            targetField = ColorMath.mix(bg, light ? 0xFFFFFF : 0x000000, light ? 0.25f : 0.30f);
            targetTrack = ColorMath.mix(bg, light ? 0x000000 : 0xFFFFFF, 0.18f);
            targetBackdropTop = ColorMath.mix(bg, 0x000000, 0.45f);
            targetBackdropBottom = ColorMath.mix(bg, 0x000000, 0.62f);
            targetText = text;
            targetText2 = ColorMath.mix(text, bg, 0.22f);
            targetText3 = ColorMath.mix(text, bg, 0.38f);
            targetOnDark = text;
        }
        if (!initialized) {
            snapToTargets();
            initialized = true;
        }
    }

    /** Smooth theme cross-fade, ~300 ms at 60 fps. */
    public static void tick(float deltaSeconds) {
        float k = 1.0f - (float) Math.exp(-12.0f * Math.max(0.0f, deltaSeconds));
        ACCENT = lerp(ACCENT, targetAccent, k);
        ACCENT_DIM = lerp(ACCENT_DIM, targetAccentDim, k);
        TEXT = lerp(TEXT, targetText, k);
        TEXT2 = lerp(TEXT2, targetText2, k);
        TEXT3 = lerp(TEXT3, targetText3, k);
        MUTED = lerp(MUTED, targetMuted, k);
        MUTED2 = lerp(MUTED2, targetMuted2, k);
        MUTED3 = lerp(MUTED3, targetMuted3, k);
        INK = lerp(INK, targetInk, k);
        ON_DARK = lerp(ON_DARK, targetOnDark, k);
        DANGER = lerp(DANGER, targetDanger, k);
        PANEL = lerp(PANEL, targetPanel, k);
        PANEL_LIGHT = lerp(PANEL_LIGHT, targetPanelLight, k);
        ROW = lerp(ROW, targetRow, k);
        HOVER = lerp(HOVER, targetHover, k);
        ACTIVE = lerp(ACTIVE, targetActive, k);
        FIELD = lerp(FIELD, targetField, k);
        TRACK = lerp(TRACK, targetTrack, k);
        BACKDROP_TOP = lerp(BACKDROP_TOP, targetBackdropTop, k);
        BACKDROP_BOTTOM = lerp(BACKDROP_BOTTOM, targetBackdropBottom, k);
    }

    private static void snapToTargets() {
        ACCENT = targetAccent;
        ACCENT_DIM = targetAccentDim;
        TEXT = targetText;
        TEXT2 = targetText2;
        TEXT3 = targetText3;
        MUTED = targetMuted;
        MUTED2 = targetMuted2;
        MUTED3 = targetMuted3;
        INK = targetInk;
        ON_DARK = targetOnDark;
        DANGER = targetDanger;
        PANEL = targetPanel;
        PANEL_LIGHT = targetPanelLight;
        ROW = targetRow;
        HOVER = targetHover;
        ACTIVE = targetActive;
        FIELD = targetField;
        TRACK = targetTrack;
        BACKDROP_TOP = targetBackdropTop;
        BACKDROP_BOTTOM = targetBackdropBottom;
    }

    private static int lerp(int from, int to, float t) {
        int fr = (from >> 16) & 0xFF;
        int fg = (from >> 8) & 0xFF;
        int fb = from & 0xFF;
        int tr = (to >> 16) & 0xFF;
        int tg = (to >> 8) & 0xFF;
        int tb = to & 0xFF;
        int r = Math.round(fr + (tr - fr) * t);
        int g = Math.round(fg + (tg - fg) * t);
        int b = Math.round(fb + (tb - fb) * t);
        return (r << 16) | (g << 8) | b;
    }

    /** Base colour used by HUD panels; custom background overrides the preset. */
    public static int hudBase() {
        if (ModuleRegistry.customColors() && ModuleRegistry.uiBackground() != null) {
            return ModuleRegistry.uiBackground().rgb() & 0xFFFFFF;
        }
        return preset.background();
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
