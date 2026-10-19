package dev.linkenvisuals.module.impl;

import dev.linkenvisuals.clickgui.Category;
import dev.linkenvisuals.clickgui.setting.BooleanSetting;
import dev.linkenvisuals.clickgui.setting.Setting;
import dev.linkenvisuals.clickgui.setting.SliderSetting;
import dev.linkenvisuals.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.List;

/**
 * MineHelper (Phase 5.7): prevents tool breaking by canceling block break
 * when durability is low.
 * Donor's {@code MineHelper} (49 lines) ported.
 */
public final class MineHelperModule extends Module {
    public final BooleanSetting saveTools;
    public final SliderSetting percent;

    private long lastWarnMs;

    public MineHelperModule() {
        this(new BooleanSetting("Save Tools", true),
             new SliderSetting("Percent", 1.0f, 100.0f, 1.0f, 5.0f, "%"));
    }

    private MineHelperModule(BooleanSetting saveTools, SliderSetting percent) {
        super("minehelper", "Mine Helper", "prevent tool breaking",
            Category.PLAYER,
            List.of(),
            List.<Setting>of(saveTools, percent));
        this.saveTools = saveTools;
        this.percent = percent;
    }

    /**
     * Returns true if block breaking should be canceled (tool almost broken).
     */
    public boolean shouldCancelBreak() {
        if (!enabled() || !saveTools.value()) {
            return false;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return false;
        }
        ItemStack stack = client.player.getMainHandStack();
        if (!isMiningTool(stack)) {
            return false;
        }
        double durability = getDurabilityPercent(stack);
        if (durability >= percent.value()) {
            return false;
        }
        long now = System.currentTimeMillis();
        if (now - lastWarnMs > 1500L) {
            client.player.sendMessage(
                Text.literal("MineHelper: tool almost broken (" + Math.round(durability) + "%)"), true);
            lastWarnMs = now;
        }
        return true;
    }

    private boolean isMiningTool(ItemStack stack) {
        return stack != null && stack.isDamageable()
            && stack.contains(DataComponentTypes.TOOL);
    }

    private double getDurabilityPercent(ItemStack stack) {
        int max = stack.getMaxDamage();
        if (max <= 0) {
            return 100.0;
        }
        return (max - stack.getDamage()) * 100.0 / max;
    }
}
