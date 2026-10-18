package dev.linkenvisuals.render;

import dev.linkenvisuals.LinkenVisuals;
import net.minecraft.util.Identifier;

/**
 * Process-wide, lazily loaded MSDF fonts (SF Bold and SF Regular).
 *
 * <p>Previously {@code LoadingOverlay}, {@code WidgetStylePipeline} and several
 * screens each carried their own copies of the font identifiers, fields and load
 * blocks. {@link GlyphAtlas#load} already caches by identifier, so the atlases
 * were shared; this class removes the orchestration duplicates and the
 * repeated drawing-pipeline warm-up calls.
 *
 * <p>Loading is attempted exactly once per process; on failure the accessors
 * keep returning {@code null} and callers fall back to vanilla text (logged
 * once instead of being retried on every call).
 */
public final class Fonts {
    private static final Identifier TITLE_FONT_DATA = LinkenVisuals.id("fonts/msdf/sf_bold.json");
    private static final Identifier TITLE_FONT_ATLAS = LinkenVisuals.id("fonts/msdf/sf_bold.png");
    private static final Identifier TEXT_FONT_DATA = LinkenVisuals.id("fonts/msdf/sf_regular.json");
    private static final Identifier TEXT_FONT_ATLAS = LinkenVisuals.id("fonts/msdf/sf_regular.png");

    private static boolean attempted;
    private static GlyphAtlas titleFont;
    private static GlyphAtlas textFont;

    private Fonts() {
    }

    /** Bold display font for titles/headings; {@code null} when unavailable. */
    public static GlyphAtlas titleFont() {
        ensure();
        return titleFont;
    }

    /** Regular text font for body labels; {@code null} when unavailable. */
    public static GlyphAtlas textFont() {
        ensure();
        return textFont;
    }

    private static void ensure() {
        if (attempted) {
            return;
        }
        attempted = true;
        try {
            Pipelines.warmUp();
            titleFont = GlyphAtlas.load(TITLE_FONT_DATA, TITLE_FONT_ATLAS);
            textFont = GlyphAtlas.load(TEXT_FONT_DATA, TEXT_FONT_ATLAS);
        } catch (Exception e) {
            LinkenVisuals.LOGGER.warn("Failed to load MSDF fonts: {}", e.getMessage());
        }
    }
}
