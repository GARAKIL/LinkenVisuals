package dev.linkenvisuals.hud;

import dev.linkenvisuals.module.Modules;
import dev.linkenvisuals.module.impl.UseTrackerModule;
import dev.linkenvisuals.render.Fonts;
import dev.linkenvisuals.render.GlyphAtlas;
import dev.linkenvisuals.render.MsdfText;
import dev.linkenvisuals.render.NeonEdge;
import dev.linkenvisuals.util.ColorMath;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

/**
 * HUD element for {@code UseTracker}: the item currently being used plus the
 * remaining use time and a progress bar. Hidden while nothing is in use.
 */
public final class UseTrackerHud extends HudElement {
    private static final float HEIGHT = 22.0f;
    private static final float PADDING = 7.0f;
    private static final float GAP = 8.0f;
    private static final float NAME_SIZE = 8.0f;
    private static final float TIME_SIZE = 7.0f;
    private static final float BAR_HEIGHT = 2.0f;
    private static final float MIN_WIDTH = 84.0f;

    private GlyphAtlas text;
    private GlyphAtlas bold;
    private float lastWidth = MIN_WIDTH;

    public UseTrackerHud() {
        super("usetracker", 0.5f, 0.36f);
    }

    @Override
    public float width() {
        return lastWidth;
    }

    @Override
    public float height() {
        return HEIGHT;
    }

    @Override
    public void render(DrawContext context, float x, float y) {
        UseTrackerModule module = Modules.useTracker();
        if (module == null || (!module.enabled() && !HudManager.isEditing())) {
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        PlayerEntity player = client.player;
        if (player == null || !player.isUsingItem()) {
            return;
        }
        ItemStack stack = player.getActiveItem();
        if (stack.isEmpty()) {
            return;
        }
        if (text == null) {
            text = Fonts.textFont();
        }
        if (bold == null) {
            bold = Fonts.titleFont();
        }

        String name = stack.getCount() > 1
            ? stack.getCount() + "x " + stack.getName().getString()
            : stack.getName().getString();
        int maxUse = Math.max(1, stack.getMaxUseTime(player));
        int left = Math.max(0, player.getItemUseTimeLeft());
        float progress = Math.min(1.0f, (maxUse - left) / (float) maxUse);
        String seconds = (left / 20) + "." + ((left % 20) / 2) + "s";

        float nameWidth = MsdfText.width(bold, name, NAME_SIZE);
        float timeWidth = MsdfText.width(text, seconds, TIME_SIZE);
        lastWidth = Math.max(MIN_WIDTH, PADDING + nameWidth + GAP + timeWidth + PADDING);
        HudSurface.panel(context, id(), x, y, lastWidth, HEIGHT, 1.0f);

        float centerY = y + HEIGHT * 0.5f;
        MsdfText.draw(context, bold, name, x + PADDING, MsdfText.topFor(centerY - 2.0f, NAME_SIZE),
            NAME_SIZE, ColorMath.withAlpha(0xFFFFFF, 235));
        MsdfText.draw(context, text, seconds, x + lastWidth - PADDING - timeWidth,
            MsdfText.topFor(centerY - 2.0f, TIME_SIZE), TIME_SIZE,
            ColorMath.withAlpha(0xFFFFFF, 170));

        float barX = x + PADDING;
        float barY = y + HEIGHT - PADDING + 1.0f;
        float barWidth = lastWidth - PADDING * 2.0f;
        context.fill(Math.round(barX), Math.round(barY), Math.round(barX + barWidth),
            Math.round(barY + BAR_HEIGHT), ColorMath.withAlpha(0xFFFFFF, 36));
        context.fill(Math.round(barX), Math.round(barY),
            Math.round(barX + barWidth * progress), Math.round(barY + BAR_HEIGHT),
            ColorMath.withAlpha(NeonEdge.cool(), 220));
    }
}
