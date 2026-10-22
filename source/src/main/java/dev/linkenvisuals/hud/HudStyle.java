package dev.linkenvisuals.hud;

import dev.linkenvisuals.clickgui.ModuleRegistry;
import dev.linkenvisuals.render.GlassPanelPipeline;
import dev.linkenvisuals.render.NeonEdge;
import dev.linkenvisuals.render.RoundedRectPipeline;
import dev.linkenvisuals.ui.Theme;
import dev.linkenvisuals.util.ColorMath;
import net.minecraft.client.gui.DrawContext;

/**
 * Shared HUD material/shape resolver. The donor exposes HUD Style and HUD
 * Shape in its Interface group; keeping the values central here means every
 * HUD surface follows the same choice instead of hard-coding one look.
 */
public final class HudStyle {
    public enum Variant {
        CARD, MINIMAL, NEON, AURORA, GLASS, SOLID, GRADIENT, OUTLINE
    }

    public enum Shape {
        ROUNDED, PILL, SHARP
    }

    private static final int BODY = 0x0E0F14;

    private HudStyle() {
    }

    public static Variant variant() {
        String value = ModuleRegistry.hudStyle() == null ? "Card" : ModuleRegistry.hudStyle().value();
        return switch (value == null ? "" : value.toLowerCase(java.util.Locale.ROOT)) {
            case "minimal" -> Variant.MINIMAL;
            case "neon" -> Variant.NEON;
            case "aurora" -> Variant.AURORA;
            case "glass" -> Variant.GLASS;
            case "solid" -> Variant.SOLID;
            case "gradient" -> Variant.GRADIENT;
            case "outline" -> Variant.OUTLINE;
            default -> Variant.CARD;
        };
    }

    public static Shape shape() {
        String value = ModuleRegistry.hudShape() == null ? "Rounded" : ModuleRegistry.hudShape().value();
        return switch (value == null ? "" : value.toLowerCase(java.util.Locale.ROOT)) {
            case "pill" -> Shape.PILL;
            case "sharp" -> Shape.SHARP;
            default -> Shape.ROUNDED;
        };
    }

    public static float radius(float width, float height) {
        return radius(shape(), width, height);
    }

    /** Pure shape rule; kept static so the geometry can be tested without Minecraft. */
    public static float radius(Shape shape, float width, float height) {
        float cap = Math.max(0.0f, Math.min(width, height) * 0.5f);
        return switch (shape == null ? Shape.ROUNDED : shape) {
            case PILL -> cap;
            case SHARP -> Math.min(2.0f, cap);
            default -> Math.min(5.0f, cap);
        };
    }

    public static void panel(DrawContext context, String element, float x, float y,
                             float width, float height, float alpha) {
        if (width <= 0.0f || height <= 0.0f || alpha <= 0.01f) {
            return;
        }
        float radius = radius(width, height);
        int tint = Theme.hudTint(element);
        int bodyAlpha = Math.round(206 * alpha);
        switch (variant()) {
            case CARD -> {
                GlassPanelPipeline.draw(context, x, y, width, height,
                    ColorMath.withAlpha(tint, Math.round(225 * alpha)));
                RoundedRectPipeline.draw(context, x, y, width, height, radius,
                    ColorMath.withAlpha(BODY, bodyAlpha));
                NeonEdge.frame(context, x, y, width, height, 0, alpha);
            }
            case MINIMAL -> {
                // Donor Minimal: a low-alpha tinted body and a thin vertical
                // accent gradient rail, with a 4 px radius cap and no border.
                float minimalRadius = Math.min(radius, 4.0f);
                float barWidth = 2.2f;
                float barInset = Math.min(height * 0.18f, radius + 1.0f);
                RoundedRectPipeline.draw(context, x, y, width, height, minimalRadius,
                    ColorMath.withAlpha(mixAccentIntoBody(0.02f), Math.round(120 * alpha)));
                context.fillGradient(Math.round(x + 2.0f), Math.round(y + barInset),
                    Math.round(x + 2.0f + barWidth), Math.round(y + height - barInset),
                    ColorMath.withAlpha(Theme.accent(), Math.round(235 * alpha)),
                    ColorMath.withAlpha(Theme.secondary(), Math.round(235 * alpha)));
            }
            case NEON -> {
                RoundedRectPipeline.draw(context, x, y, width, height, radius,
                    ColorMath.withAlpha(BODY, Math.round(210 * alpha)));
                RoundedRectPipeline.drawOutline(context, x, y, width, height, radius, 1.2f,
                    ColorMath.withAlpha(Theme.accent(), Math.round(145 * alpha)));
                NeonEdge.frame(context, x, y, width, height, 0, alpha * 0.8f);
            }
            case AURORA -> {
                RoundedRectPipeline.draw(context, x, y, width, height, radius,
                    ColorMath.withAlpha(BODY, Math.round(218 * alpha)));
                RoundedRectPipeline.draw(context, x + 1.0f, y + 1.0f, width - 2.0f,
                    Math.max(2.0f, height * 0.45f), Math.max(1.0f, radius - 1.0f),
                    ColorMath.withAlpha(Theme.accent(), Math.round(26 * alpha)));
                RoundedRectPipeline.drawOutline(context, x, y, width, height, radius,
                    ColorMath.withAlpha(Theme.accent(), Math.round(55 * alpha)));
            }
            case GLASS -> {
                GlassPanelPipeline.draw(context, x, y, width, height,
                    ColorMath.withAlpha(tint, Math.round(255 * alpha)));
                RoundedRectPipeline.drawOutline(context, x, y, width, height, radius,
                    ColorMath.withAlpha(0xFFFFFF, Math.round(28 * alpha)));
            }
            case SOLID -> {
                RoundedRectPipeline.draw(context, x, y, width, height, radius,
                    ColorMath.withAlpha(BODY, Math.round(235 * alpha)));
                RoundedRectPipeline.drawOutline(context, x, y, width, height, radius,
                    ColorMath.withAlpha(0xFFFFFF, Math.round(30 * alpha)));
            }
            case GRADIENT -> {
                RoundedRectPipeline.draw(context, x, y, width, height, radius,
                    ColorMath.withAlpha(Theme.accent(), Math.round(22 * alpha)));
                RoundedRectPipeline.draw(context, x + 1.0f, y + 1.0f, width - 2.0f,
                    height - 2.0f, Math.max(1.0f, radius - 1.0f),
                    ColorMath.withAlpha(BODY, Math.round(215 * alpha)));
                RoundedRectPipeline.drawOutline(context, x, y, width, height, radius,
                    ColorMath.withAlpha(Theme.accent(), Math.round(75 * alpha)));
            }
            case OUTLINE -> {
                RoundedRectPipeline.draw(context, x, y, width, height, radius,
                    ColorMath.withAlpha(BODY, Math.round(72 * alpha)));
                RoundedRectPipeline.drawOutline(context, x, y, width, height, radius, 1.2f,
                    ColorMath.withAlpha(Theme.accent(), Math.round(150 * alpha)));
            }
        }
    }

    public static void plate(DrawContext context, String element, float x, float y,
                             float width, float height, float alpha) {
        if (width <= 0.0f || height <= 0.0f || alpha <= 0.01f) {
            return;
        }
        float radius = radius(width, height);
        GlassPanelPipeline.draw(context, x, y, width, height,
            ColorMath.withAlpha(Theme.hudTint(element), Math.round(215 * alpha)));
        RoundedRectPipeline.draw(context, x, y, width, height, radius,
            ColorMath.withAlpha(BODY, Math.round(200 * alpha)));
        RoundedRectPipeline.drawOutline(context, x, y, width, height, radius,
            ColorMath.withAlpha(0xFFFFFF, Math.round(20 * alpha)));
    }

    private static int mixAccentIntoBody(float amount) {
        return ColorMath.mix(BODY, Theme.accent(), amount);
    }
}
