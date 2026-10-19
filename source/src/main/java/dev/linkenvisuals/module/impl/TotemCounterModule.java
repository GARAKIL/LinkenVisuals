package dev.linkenvisuals.module.impl;

import dev.linkenvisuals.clickgui.Category;
import dev.linkenvisuals.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Items;

import java.util.List;

/**
 * TotemCounter (Phase 5.7): counts totems in inventory.
 * Donor's {@code TotemCounter} (78 lines) ported.
 */
public final class TotemCounterModule extends Module {
    private int count;

    public TotemCounterModule() {
        super("totemcounter", "Totem Counter", "count totems",
            Category.PLAYER, List.of(), List.of());
    }

    public int getCount() {
        return count;
    }

    /** Called each tick to update count. */
    public static void tick() {
        TotemCounterModule module = dev.linkenvisuals.module.Modules.totemCounter();
        if (module == null || !module.enabled()) {
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            module.count = 0;
            return;
        }
        int c = 0;
        for (var stack : client.player.getInventory().main) {
            if (stack.isOf(Items.TOTEM_OF_UNDYING)) {
                c += stack.getCount();
            }
        }
        for (var stack : client.player.getInventory().offHand) {
            if (stack.isOf(Items.TOTEM_OF_UNDYING)) {
                c += stack.getCount();
            }
        }
        module.count = c;
    }
}
