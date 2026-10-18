package dev.linkenvisuals.hud;

import dev.linkenvisuals.util.ColorMath;
import dev.linkenvisuals.render.GlassPanelPipeline;
import dev.linkenvisuals.render.NeonEdge;
import dev.linkenvisuals.render.RoundedRectPipeline;
import dev.linkenvisuals.ui.Theme;
import net.minecraft.client.gui.DrawContext;

public final class HudSurface {
    private static final int BODY = 0x0E0F14;

    private HudSurface() {
    }

    public static void panel(DrawContext context, String element, float x, float y,
                             float width, float height, float alpha) {
        if (width <= 0.0f || height <= 0.0f || alpha <= 0.01f) {
            return;
        }
        GlassPanelPipeline.draw(context, x, y, width, height,
            ColorMath.withAlpha(Theme.hudTint(element), Math.round(225 * alpha)));
        RoundedRectPipeline.draw(context, x, y, width, height, ColorMath.withAlpha(BODY, Math.round(206 * alpha)));
        NeonEdge.frame(context, x, y, width, height, 0, alpha);
    }

    public static void plate(DrawContext context, String element, float x, float y,
                             float width, float height, float alpha) {
        if (width <= 0.0f || height <= 0.0f || alpha <= 0.01f) {
            return;
        }
        GlassPanelPipeline.draw(context, x, y, width, height,
            ColorMath.withAlpha(Theme.hudTint(element), Math.round(215 * alpha)));
        RoundedRectPipeline.draw(context, x, y, width, height, ColorMath.withAlpha(BODY, Math.round(200 * alpha)));
        RoundedRectPipeline.drawOutline(context, x, y, width, height,
            ColorMath.withAlpha(0xFFFFFF, Math.round(20 * alpha)));
    }

}
