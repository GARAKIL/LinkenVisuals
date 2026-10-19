package dev.linkenvisuals.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InventoryMathTest {
    @Test
    void hotbarIndicesMapToHandlerSlots() {
        assertEquals(36, InventoryMath.indexToSlot(0));
        assertEquals(44, InventoryMath.indexToSlot(8));
        assertEquals(9, InventoryMath.indexToSlot(9));
        assertEquals(35, InventoryMath.indexToSlot(35));
    }

    @Test
    void pickBestPrefersHighestRank() {
        int[] ranks = {-1, -1, 3, 5, 4};
        assertEquals(3, InventoryMath.pickBest(ranks, 0, 4));
    }

    @Test
    void pickBestKeepsLowestIndexOnTies() {
        int[] ranks = {-1, 4, -1, 4};
        assertEquals(1, InventoryMath.pickBest(ranks, 0, 3));
    }

    @Test
    void pickBestReturnsMinusOneWhenNothingRanks() {
        assertEquals(-1, InventoryMath.pickBest(new int[]{-1, -1}, 0, 1));
    }

    @Test
    void pickBestHonoursRange() {
        int[] ranks = {5, -1, 3};
        assertEquals(2, InventoryMath.pickBest(ranks, 1, 2));
        assertEquals(-1, InventoryMath.pickBest(ranks, 1, 1));
    }
}
