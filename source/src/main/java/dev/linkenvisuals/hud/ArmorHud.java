package dev.linkenvisuals.hud;

import dev.linkenvisuals.clickgui.ModuleRegistry;
import dev.linkenvisuals.clickgui.link.LinkGuiTheme;
import dev.linkenvisuals.render.Fonts;
import dev.linkenvisuals.render.GlyphAtlas;
import dev.linkenvisuals.render.MsdfText;
import dev.linkenvisuals.render.RoundedRectPipeline;
import dev.linkenvisuals.util.ColorMath;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.joml.Matrix3x2fStack;

/**
 * Donor V1 armour card: four 17 px inset cells, item icon and durability percent
 * below each cell. The donor's V2 hotbar-anchored mode is intentionally not
 * exposed until the HUD layer supports screen-anchored elements.
 */
public final class ArmorHud extends HudElement {
    private static final float PADDING = 6.0f;
    private static final float SLOT = 19.0f;
    private static final float CELL = 17.0f;
    private static final float GAP = 4.0f;
    private static final float PERCENT_SIZE = 5.6f;
    private static final float WIDTH = PADDING * 2.0f + SLOT * 4.0f + GAP * 3.0f;
    private static final float HEIGHT = PADDING * 2.0f + SLOT + 8.0f;

    private final HudTween fade = new HudTween(true);
    private static boolean lastRenderVisible;

    public ArmorHud() {
        super("armorhud", 0.18f, 0.8f);
    }

    public static boolean lastRenderVisible() {
        return lastRenderVisible;
    }

    @Override
    public float width() {
        return WIDTH;
    }

    @Override
    public float height() {
        return HEIGHT;
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
        PlayerEntity player = client.player;
        boolean empty = true;
        for (int slot = 0; slot < 4; slot++) {
            if (!armorStack(player, slot).isEmpty()) {
                empty = false;
                break;
            }
        }
        if (empty && !editing) {
            fade.value(0.0f, 200L);
            return;
        }
        float alpha = fade.value(1.0f, 200L);
        if (alpha <= 0.001f) {
            return;
        }
        float fa = Math.max(0.0f, Math.min(1.0f, alpha));
        int globalAlpha = Math.round(255.0f * fa);
        HudSurface.panel(context, id(), x, y, WIDTH, HEIGHT, fa);

        GlyphAtlas font = Fonts.titleFont();
        Matrix3x2fStack matrices = context.getMatrices();
        for (int i = 0; i < 4; i++) {
            float slotX = x + PADDING + i * (SLOT + GAP);
            float slotY = y + PADDING;
            RoundedRectPipeline.draw(context, slotX + 1.0f, slotY + 1.0f, CELL, CELL, 5.0f,
                HudStyle.inset(Math.round(235.0f * fa)));

            ItemStack stack = empty
                ? previewStack(i, editing)
                : armorStack(player, i);
            if (stack == null || stack.isEmpty()) {
                continue;
            }
            drawItem(context, stack, slotX + 1.0f, slotY + 1.0f, fa);
            int percent = durabilityPercent(stack);
            if (percent >= 0) {
                String label = percent + "%";
                int color = durabilityColor(percent, globalAlpha);
                MsdfText.drawCenteredOn(context, font, label, slotX + SLOT * 0.5f,
                    slotY + SLOT + 3.4f, PERCENT_SIZE, color);
            }
        }
        lastRenderVisible = true;
    }

    static ItemStack armorStack(PlayerEntity player, int index) {
        EquipmentSlot slot = switch (index) {
            case 0 -> EquipmentSlot.HEAD;
            case 1 -> EquipmentSlot.CHEST;
            case 2 -> EquipmentSlot.LEGS;
            default -> EquipmentSlot.FEET;
        };
        return player.getEquippedStack(slot);
    }

    private static ItemStack previewStack(int index, boolean editing) {        if (!editing) {
            return ItemStack.EMPTY;
        }
        return switch (index) {
            case 0 -> damaged(Items.IRON_HELMET, 12);
            case 1 -> damaged(Items.IRON_CHESTPLATE, 44);
            case 2 -> damaged(Items.IRON_LEGGINGS, 68);
            default -> damaged(Items.IRON_BOOTS, 86);
        };
    }

    private static ItemStack damaged(net.minecraft.item.Item item, int percent) {
        ItemStack stack = new ItemStack(item);
        if (stack.isDamageable() && stack.getMaxDamage() > 0) {
            stack.setDamage(Math.round(stack.getMaxDamage() * (100 - percent) / 100.0f));
        }
        return stack;
    }

    private static void drawItem(DrawContext context, ItemStack stack, float x, float y, float alpha) {
        float scale = 0.9f;
        float itemSize = 16.0f * scale;
        float ix = x + (CELL - itemSize) * 0.5f;
        float iy = y + (CELL - itemSize) * 0.5f;
        Matrix3x2fStack matrices = context.getMatrices();
        matrices.pushMatrix();
        matrices.translate(ix, iy);
        matrices.scale(scale, scale);
        context.drawItem(stack, 0, 0);
        matrices.popMatrix();
    }

    static int durabilityPercent(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return -1;
        }
        if (!stack.isDamageable()) {
            return 100;
        }
        return durabilityPercent(stack.getMaxDamage(), stack.getDamage());
    }

    static int durabilityPercent(int maxDamage, int damage) {
        if (maxDamage <= 0) {
            return -1;
        }
        int remaining = Math.max(0, maxDamage - Math.max(0, damage));
        return Math.max(0, Math.min(100, Math.round(remaining * 100.0f / maxDamage)));
    }

    static int durabilityColor(int percent, int alpha) {
        boolean light = LinkGuiTheme.isLight();
        int rgb;
        if (percent < 25) {
            rgb = light ? 0xC62828 : 0xFF6C6C;
        } else if (percent < 50) {
            rgb = light ? 0xB07400 : 0xFFD352;
        } else {
            rgb = light ? 0x228A3C : 0x6CDD7A;
        }
        return ColorMath.withAlpha(rgb, alpha);
    }

    private boolean isEnabled() {
        return ModuleRegistry.armorHud() == null || ModuleRegistry.armorHud().value();
    }
}
