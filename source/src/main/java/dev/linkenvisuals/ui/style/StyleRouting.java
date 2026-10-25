package dev.linkenvisuals.ui.style;

/**
 * Pure style-routing rules shared by mixins, the style manager and tests.
 *
 * <p>Keeping the rules here avoids binary "Beta or not" checks: LINKEN must
 * never accidentally inherit the Beta branch of an unported screen. Until a
 * screen is ported to the LINKEN layer it deliberately uses the vanilla
 * chrome, which is why {@link #usesVanillaChrome(MenuStyle)} is true for both
 * VANILLA and LINKEN.
 */
public final class StyleRouting {
    private StyleRouting() {
    }

    /** Video Settings cycle: cycles styles allowed by active StyleFlavor. */
    public static MenuStyle nextStyle(MenuStyle current) {
        return StyleFlavor.nextMenuStyle(current);
    }

    /** LINKEN has a fixed composition and must not overwrite the editable Beta/Vanilla config. */
    public static boolean shouldApplyPreset(MenuStyle style) {
        return style != MenuStyle.LINKEN;
    }

    /** Unported screens (multiplayer, settings, loading, pause) use vanilla chrome under LINKEN. */
    public static boolean usesVanillaChrome(MenuStyle style) {
        return style != MenuStyle.BETA;
    }
}
