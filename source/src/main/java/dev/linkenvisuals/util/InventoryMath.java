package dev.linkenvisuals.util;

/**
 * Pure inventory-index helpers, Minecraft-free so they are unit-testable.
 */
public final class InventoryMath {
    public static final int HOTBAR_FROM = 0;
    public static final int HOTBAR_TO = 8;
    public static final int MAIN_INVENTORY_FROM = 9;
    public static final int MAIN_INVENTORY_TO = 35;

    private InventoryMath() {
    }

    /** Player inventory index (0..35) to a player-screen-handler slot id. */
    public static int indexToSlot(int index) {
        if (index >= HOTBAR_FROM && index <= HOTBAR_TO) {
            return 36 + index;
        }
        return index;
    }

    /**
     * Index of the highest rank in {@code [from, to]}; ties go to the lowest
     * index, {@code -1} when nothing ranks. Used by ElytraSwap to pick the
     * chestplate: the donor's {@code findBestChestplate} returned chainmail
     * before netherite (inconsistent priority, see MERGE_DECISIONS D35), this
     * helper keeps strict best-first semantics.
     */
    public static int pickBest(int[] ranks, int from, int to) {
        int best = -1;
        int bestRank = -1;
        int last = Math.min(to, ranks.length - 1);
        for (int i = Math.max(0, from); i <= last; i++) {
            if (ranks[i] > bestRank) {
                bestRank = ranks[i];
                best = i;
            }
        }
        return best;
    }
}
