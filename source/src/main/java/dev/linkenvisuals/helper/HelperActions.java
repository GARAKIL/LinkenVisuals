package dev.linkenvisuals.helper;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.Locale;

/** Shared donor helper-item selection logic for Ft/Hw/Rw. */
public final class HelperActions {
    private HelperActions() {
    }

    public static boolean select(MinecraftClient client, Item item, String label, String... nameTokens) {
        if (client == null || client.player == null) {
            return false;
        }
        int slot = findHotbar(client, item, nameTokens);
        if (slot < 0) {
            client.inGameHud.getChatHud().addMessage(Text.literal(label + ": item not found"));
            return false;
        }
        if (client.player.getInventory().getSelectedSlot() == slot) {
            client.inGameHud.getChatHud().addMessage(Text.literal(label + ": already selected"));
            return false;
        }
        client.player.getInventory().setSelectedSlot(slot);
        client.inGameHud.getChatHud().addMessage(Text.literal(label + ": selected"));
        return true;
    }

    public static int findHotbar(MinecraftClient client, Item item, String... nameTokens) {
        if (client == null || client.player == null) {
            return -1;
        }
        for (int slot = 0; slot < 9; slot++) {
            ItemStack stack = client.player.getInventory().getStack(slot);
            if (stack.isEmpty() || !stack.isOf(item)) {
                continue;
            }
            if (nameTokens.length == 0 || matchesName(stack, nameTokens)) {
                return slot;
            }
        }
        return -1;
    }

    private static boolean matchesName(ItemStack stack, String[] tokens) {
        String name = stack.getName().getString().toLowerCase(Locale.ROOT);
        for (String token : tokens) {
            if (!name.contains(token.toLowerCase(Locale.ROOT))) {
                return false;
            }
        }
        return true;
    }
}
