package dev.linkenvisuals.module.impl;

import dev.linkenvisuals.clickgui.Category;
import dev.linkenvisuals.module.Module;
import dev.linkenvisuals.util.PopDedupe;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * TotemTracker (Phase 5.7): announces every totem pop in chat and keeps a
 * per-player pop count. Ported from the donor's {@code TotemTracker}
 * (88 lines).
 *
 * <p>{@link #tick} remembers whether each player currently holds an enchanted
 * totem (needed because the pop packet can arrive after the totem left the
 * hands), and {@code ClientPlayNetworkHandlerMixin} feeds the pop packets.
 * Duplicate statuses inside one pop window are dropped by {@link PopDedupe}.
 */
public final class TotemTrackerModule extends Module {
    private final Map<UUID, Integer> popCounts = new HashMap<>();
    private final Map<UUID, Boolean> glintState = new HashMap<>();
    private final PopDedupe dedupe = new PopDedupe();

    public TotemTrackerModule() {
        super("totemtracker", "Totem Tracker", "announce totem pops",
            Category.PLAYER, List.of(), List.of());
    }

    @Override
    protected void onDisable() {
        popCounts.clear();
        glintState.clear();
        dedupe.clear();
    }

    /** Called each client tick: caches the last known totem enchant state. */
    public void tick(MinecraftClient client) {
        if (!enabled() || client.world == null) {
            return;
        }
        for (PlayerEntity player : client.world.getPlayers()) {
            ItemStack totem = findTotem(player);
            if (totem != null) {
                glintState.put(player.getUuid(), totem.hasGlint());
            }
        }
    }

    /** Called from the mixin on a totem-use status packet. */
    public void onTotemPop(PlayerEntity player, MinecraftClient client) {
        if (!enabled()) {
            return;
        }
        if (!dedupe.accept(player.getUuid(), System.currentTimeMillis())) {
            return;
        }
        recordPop(player.getUuid());
        if (client.player == null) {
            return;
        }
        ItemStack totem = findTotem(player);
        boolean enchanted = totem != null
            ? totem.hasGlint()
            : Boolean.TRUE.equals(glintState.get(player.getUuid()));
        MutableText message = Text.literal("")
            .append(Text.literal(player.getName().getString()).formatted(Formatting.GOLD))
            .append(Text.literal(" popped a totem ").formatted(Formatting.WHITE))
            .append(enchanted
                ? Text.literal("\u2714").formatted(Formatting.GREEN)
                : Text.literal("\u2718").formatted(Formatting.RED));
        client.player.sendMessage(message, false);
    }

    public void recordPop(UUID playerId) {
        popCounts.merge(playerId, 1, Integer::sum);
    }

    public int getPops(UUID playerId) {
        return popCounts.getOrDefault(playerId, 0);
    }

    public void clearPlayer(UUID playerId) {
        popCounts.remove(playerId);
        glintState.remove(playerId);
        dedupe.remove(playerId);
    }

    private static ItemStack findTotem(PlayerEntity player) {
        ItemStack offHand = player.getOffHandStack();
        if (offHand.isOf(Items.TOTEM_OF_UNDYING)) {
            return offHand;
        }
        ItemStack mainHand = player.getMainHandStack();
        if (mainHand.isOf(Items.TOTEM_OF_UNDYING)) {
            return mainHand;
        }
        return null;
    }
}
