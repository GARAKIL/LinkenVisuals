package dev.linkenvisuals.module.impl;

import dev.linkenvisuals.clickgui.Category;
import dev.linkenvisuals.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * UseTracker (Phase 5.7): tracks active item usage.
 * Donor's {@code UseTracker} (159 lines) simplified.
 */
public final class UseTrackerModule extends Module {
    private final Map<Hand, ItemStack> usingItems = new EnumMap<>(Hand.class);
    private final Map<Hand, Integer> useTimers = new EnumMap<>(Hand.class);

    public UseTrackerModule() {
        super("usetracker", "Use Tracker", "track item usage",
            Category.PLAYER, List.of(), List.of());
    }

    @Override
    protected void onDisable() {
        usingItems.clear();
        useTimers.clear();
    }

    /** Called each tick to update usage tracking. */
    public static void tick() {
        UseTrackerModule module = dev.linkenvisuals.module.Modules.useTracker();
        if (module == null || !module.enabled()) {
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return;
        }
        for (Hand hand : Hand.values()) {
            ItemStack using = client.player.getStackInHand(hand);
            boolean isUsing = client.player.isUsingItem()
                && client.player.getActiveHand() == hand;
            if (isUsing) {
                module.usingItems.put(hand, using.copy());
                module.useTimers.merge(hand, 1, Integer::sum);
            } else {
                module.usingItems.remove(hand);
                module.useTimers.remove(hand);
            }
        }
    }

    public ItemStack getUsingItem(Hand hand) {
        return usingItems.get(hand);
    }

    public int getUseTime(Hand hand) {
        return useTimers.getOrDefault(hand, 0);
    }
}
