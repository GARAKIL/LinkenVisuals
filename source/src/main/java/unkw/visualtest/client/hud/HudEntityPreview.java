package unkw.visualtest.client.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.LivingEntity;

public final class HudEntityPreview {
    private static Framebuffer framebufferOverride;

    private HudEntityPreview() {
    }

    public static Framebuffer getFramebufferOverride() {
        return framebufferOverride;
    }

    public static void render(DrawContext context, MinecraftClient client, LivingEntity entity, int x, int y, int width, int height) {
        if (entity == null || entity.isRemoved() || width < 4 || height < 4) {
            return;
        }

        int x1 = x;
        int y1 = y;
        int x2 = x + width;
        int y2 = y + height;
        int size = Math.round(Math.min(width, height) * 0.42f);

        context.enableScissor(x1, y1, x2, y2);
        try {
            InventoryScreen.drawEntity(
                context,
                x1,
                y1,
                x2,
                y2,
                size,
                0.0625f,
                (float) (x1 + x2) * 0.5f,
                (float) (y1 + y2) * 0.5f,
                entity
            );
        } catch (Throwable ignored) {
        } finally {
            context.disableScissor();
        }
    }
}
