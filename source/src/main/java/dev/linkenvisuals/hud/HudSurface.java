package dev.linkenvisuals.hud;

import net.minecraft.client.gui.DrawContext;

/**
 * HUD panel entry point. Material and radius are resolved by {@link HudStyle}
 * so the donor Interface settings affect every surface consistently.
 */
public final class HudSurface {
    private HudSurface() {
    }

    public static void panel(DrawContext context, String element, float x, float y,
                             float width, float height, float alpha) {
        HudStyle.panel(context, element, x, y, width, height, alpha);
    }

    public static void plate(DrawContext context, String element, float x, float y,
                             float width, float height, float alpha) {
        HudStyle.plate(context, element, x, y, width, height, alpha);
    }
}
