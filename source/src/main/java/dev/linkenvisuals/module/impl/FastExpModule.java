package dev.linkenvisuals.module.impl;

import dev.linkenvisuals.clickgui.Category;
import dev.linkenvisuals.clickgui.setting.Setting;
import dev.linkenvisuals.clickgui.setting.SliderSetting;
import dev.linkenvisuals.mixin.MinecraftClientAccessor;
import dev.linkenvisuals.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Items;

import java.util.List;

/**
 * FastExp (Phase 5.7): reduces XP bottle throw cooldown.
 * Donor's {@code FastExp} (48 lines) ported.
 */
public final class FastExpModule extends Module {
    public final SliderSetting delay;

    public FastExpModule() {
        this(new SliderSetting("Delay", 0.0f, 4.0f, 1.0f, 0.0f, "ticks"));
    }

    private FastExpModule(SliderSetting delay) {
        super("fastexp", "Fast Exp", "faster XP bottle throws",
            Category.PLAYER,
            List.of(),
            List.<Setting>of(delay));
        this.delay = delay;
    }

    /** Called each tick to reduce cooldown. */
    public static void tick() {
        FastExpModule module = dev.linkenvisuals.module.Modules.fastExp();
        if (module == null || !module.enabled()) {
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return;
        }
        boolean holding = client.player.getMainHandStack().isOf(Items.EXPERIENCE_BOTTLE)
            || client.player.getOffHandStack().isOf(Items.EXPERIENCE_BOTTLE);
        if (!holding) {
            return;
        }
        MinecraftClientAccessor accessor = (MinecraftClientAccessor) client;
        int target = Math.round(module.delay.value());
        if (accessor.linkenvisuals$getItemUseCooldown() > target) {
            accessor.linkenvisuals$setItemUseCooldown(target);
        }
    }
}
