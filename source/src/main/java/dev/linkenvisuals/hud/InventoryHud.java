package dev.linkenvisuals.hud;

import dev.linkenvisuals.clickgui.ModuleRegistry;
import dev.linkenvisuals.clickgui.link.LinkGuiTheme;
import dev.linkenvisuals.render.RoundedRectPipeline;
import dev.linkenvisuals.util.ColorMath;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.item.ItemStack;
import org.joml.Matrix3x2fStack;

/**
 * Donor inventory card: 3 rows of main inventory (27 slots), optional hotbar
 * row separated by a 1 px line.
 */
public final class InventoryHud extends HudElement {
    private static final float SLOT = 18.0f;
    private static final float ICON = 16.0f;
    private static final float ITEM_SCALE = 0.9f;
    private static final float GAP = 2.0f;
    private static final float PADDING = 7.0f;
    private static final float HOTBAR_GAP = 7.0f;
    private static final float SEPARATOR = 1.0f;
    private static final int COLUMNS = 9;

    private final HudTween fade = new HudTween(true);
    private static boolean lastRenderVisible;

    public InventoryHud() {
        super("inventoryhud", 0.82f, 0.55f);
    }

    public static boolean lastRenderVisible() {
        return lastRenderVisible;
    }

    @Override
    public float width() {
        return PADDING * 2.0f + COLUMNS * SLOT + (COLUMNS - 1) * GAP;
    }

    @Override
    public float height() {
        int rows = showHotbar() ? 4 : 3;
        return PADDING * 2.0f + rows * SLOT + (rows - 1) * GAP
            + (showHotbar() ? HOTBAR_GAP : 0.0f);
    }

    @Override
    public void render(DrawContext context, float x, float y) {
        lastRenderVisible = false;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null) {
            return;
        }
        boolean editing = HudManager.isEditing() || client.currentScreen instanceof ChatScreen;
        if (!isEnabled() && !editing) {
            return;
        }
        float alpha = fade.value(1.0f, 200L);
        if (alpha <= 0.001f) {
            return;
        }
        boolean hotbar = showHotbar();
        int rows = hotbar ? 4 : 3;
        float width = width();
        float height = height();
        float fa = Math.max(0.0f, Math.min(1.0f, alpha));
        HudSurface.panel(context, id(), x, y, width, height, fa);

        if (hotbar) {
            float separatorY = y + PADDING + 3.0f * SLOT + 2.0f * GAP + HOTBAR_GAP * 0.5f;
            RoundedRectPipeline.draw(context, x + PADDING + 3.0f, separatorY,
                width - PADDING * 2.0f - 6.0f, SEPARATOR, SEPARATOR * 0.5f,
                ColorMath.withAlpha(LinkGuiTheme.isLight() ? 0x000000 : 0xFFFFFF,
                    Math.round((editing ? 85.0f : 60.0f) * fa)));
        }

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < COLUMNS; col++) {
                int inventoryIndex = inventoryIndex(row, col);
                float slotX = Math.round(x + PADDING + col * (SLOT + GAP));
                float slotY = Math.round(y + PADDING + row * (SLOT + GAP)
                    + (hotbar && row == 3 ? HOTBAR_GAP : 0.0f));
                RoundedRectPipeline.draw(context, slotX, slotY, SLOT, SLOT, 5.0f,
                    HudStyle.inset(Math.round(230.0f * fa)));
                ItemStack stack = client.player.getInventory().getStack(inventoryIndex);
                if (!stack.isEmpty()) {
                    drawItem(context, client, stack, slotX, slotY);
                }
            }
        }
        lastRenderVisible = true;
    }

    private static void drawItem(DrawContext context, MinecraftClient client, ItemStack stack,
                                 float slotX, float slotY) {
        float itemSize = ICON * ITEM_SCALE;
        float ix = slotX + (SLOT - itemSize) * 0.5f;
        float iy = slotY + (SLOT - itemSize) * 0.5f;
        Matrix3x2fStack matrices = context.getMatrices();
        matrices.pushMatrix();
        matrices.translate(ix, iy);
        matrices.scale(ITEM_SCALE, ITEM_SCALE);
        context.drawItem(stack, 0, 0);
        context.drawStackOverlay(client.textRenderer, stack, 0, 0);
        matrices.popMatrix();
    }

    static int inventoryIndex(int row, int col) {
        if (row < 3) {
            return 9 + row * COLUMNS + col;
        }
        return col;
    }

    static int slotRows(boolean hotbar) {
        return hotbar ? 4 : 3;
    }

    private boolean isEnabled() {
        return ModuleRegistry.inventoryHud() == null || ModuleRegistry.inventoryHud().value();
    }

    private static boolean showHotbar() {
        return ModuleRegistry.inventoryHotbar() != null && ModuleRegistry.inventoryHotbar().value();
    }
}
