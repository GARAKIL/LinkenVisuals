package dev.linkenvisuals.util;

import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;

/**
 * Locale-independent identification of texts and widgets.
 *
 * <p>Widgets are always identified by their translation key (e.g. {@code menu.singleplayer}):
 * keys are stable, while the rendered text changes with the game language, so matching on
 * localized strings is never used.</p>
 */
public final class TextKeys {
    private TextKeys() {
    }

    /**
     * @return the translation key of the given text, or {@code null} if the text is not
     * translatable (literal texts are locale-dependent and must not be used for identification)
     */
    public static String keyOf(Text text) {
        if (text != null && text.getContent() instanceof TranslatableTextContent transparent) {
            return transparent.getKey();
        }
        return null;
    }
}
