package dev.linkenvisuals.hud;

import dev.linkenvisuals.module.impl.TotemCounterModule;
import dev.linkenvisuals.module.Modules;
import dev.linkenvisuals.render.Fonts;
import dev.linkenvisuals.render.GlyphAtlas;
import dev.linkenvisuals.render.MsdfText;
import dev.linkenvisuals.util.ColorMath;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

/**
 * HUD element for {@code TotemCounter}: totem icon plus the inventory count,
 * placed next to the crosshair by default like the donor's counter.
 */
public final class TotemCounterHud extends HudElement {
    private static final float HEIGHT = 20.0f;
    private static final float PADDING = 6.0f;
    private static final float ICON_SIZE = 16.0f;
    private static final float GAP = 6.0f;
    private static final float COUNT_SIZE = 9.0f;
    private static final float DEFAULT_WIDTH = 36.0f;

    private GlyphAtlas bold;
    private ItemStack icon;
    private float lastWidth = DEFAULT_WIDTH;

    public TotemCounterHud() {
        super("totemcounter", 0.5f, 0.585f);
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
        TotemCounterModule module = Modules.totemCounter();
        boolean editing = HudManager.isEditing();
        if (module == null || (!module.enabled() && !editing)) {
            return;
        }
        if (bold == null) {
            bold = Fonts.titleFont();
        }
        if (icon == null) {
            icon = new ItemStack(Items.TOTEM_OF_UNDYING);
        }

        String count = Integer.toString(module.getCount());
        lastWidth = PADDING + ICON_SIZE + GAP + MsdfText.width(bold, count, COUNT_SIZE) + PADDING;
        HudSurface.panel(context, id(), x, y, lastWidth, HEIGHT, 1.0f);
        context.drawItem(icon, Math.round(x + PADDING), Math.round(y + (HEIGHT - ICON_SIZE) * 0.5f));
        MsdfText.draw(context, bold, count, x + PADDING + ICON_SIZE + GAP,
            MsdfText.topFor(y + HEIGHT * 0.5f, COUNT_SIZE), COUNT_SIZE,
            ColorMath.withAlpha(0xFFFFFF, 235));
    }
}
