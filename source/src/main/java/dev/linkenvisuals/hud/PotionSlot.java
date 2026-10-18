package dev.linkenvisuals.hud;

import dev.linkenvisuals.render.Fonts;
import dev.linkenvisuals.render.GlyphAtlas;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

import java.util.Map;

public final class PotionSlot extends HudElement {

    final int index;
    private GlyphAtlas bold;

    public PotionSlot(int index) {
        super("potion_slot_" + index, 0.045f, 0.32f + index * 0.09f);
        this.index = index;
    }

    private void ensureLoaded() {
        if (bold == null) {
            bold = Fonts.titleFont();
        }
    }

    static boolean isEditMode() {
        return MinecraftClient.getInstance().currentScreen instanceof ChatScreen;
    }

    private boolean isActiveMode() {
        return PotionLayoutMode.get() == PotionLayoutMode.Mode.SEPARATE;
    }

    private Map.Entry<RegistryEntry<StatusEffect>, PotionEffectTracker.EffectState> entry() {
        return isActiveMode() ? PotionEffectTracker.entryAt(index) : null;
    }

    @Override
    public float width() {
        return isActiveMode() && (entry() != null || isEditMode()) ? PotionCapsuleRenderer.CAPSULE_W : 0.0f;
    }

    @Override
    public float height() {
        return isActiveMode() && (entry() != null || isEditMode()) ? PotionCapsuleRenderer.CAPSULE_H : 0.0f;
    }

    @Override
    public void render(DrawContext context, float x, float y) {
        if (!isActiveMode()) {
            return;
        }
        Map.Entry<RegistryEntry<StatusEffect>, PotionEffectTracker.EffectState> entry = entry();
        ensureLoaded();
        if (entry == null) {
            if (isEditMode()) {
                PotionCapsuleRenderer.drawGhost(context, bold, x, y, index % 2 == 1);
            }
            return;
        }
        PotionCapsuleRenderer.draw(context, bold, x, y, entry.getKey(), entry.getValue());
    }
}
