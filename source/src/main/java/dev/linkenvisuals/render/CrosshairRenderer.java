package dev.linkenvisuals.render;

import dev.linkenvisuals.module.impl.CrosshairModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

/**
 * Crosshair renderer (Phase 5.7): draws custom crosshair styles.
 */
public final class CrosshairRenderer {
    private CrosshairRenderer() {
    }

    public static void render(DrawContext context, CrosshairModule module) {
        MinecraftClient client = MinecraftClient.getInstance();
        int width = client.getWindow().getScaledWidth();
        int height = client.getWindow().getScaledHeight();
        int cx = width / 2;
        int cy = height / 2;
        int color = 0xFFFFFFFF;
        String style = module.style.value();

        switch (style) {
            case "Dot" -> context.fill(cx - 1, cy - 1, cx + 1, cy + 1, color);
            case "Circle" -> {
                // Simple circle via 16 segments.
                for (int i = 0; i < 16; i++) {
                    double a0 = Math.PI * 2.0 * i / 16;
                    double a1 = Math.PI * 2.0 * (i + 1) / 16;
                    int x0 = cx + (int) (Math.cos(a0) * 5);
                    int y0 = cy + (int) (Math.sin(a0) * 5);
                    int x1 = cx + (int) (Math.cos(a1) * 5);
                    int y1 = cy + (int) (Math.sin(a1) * 5);
                    context.drawLine(x0, y0, x1, y1, color);
                }
            }
            default -> { // Cross
                context.fill(cx - 5, cy - 1, cx + 5, cy + 1, color);
                context.fill(cx - 1, cy - 5, cx + 1, cy + 5, color);
            }
        }
    }
}
