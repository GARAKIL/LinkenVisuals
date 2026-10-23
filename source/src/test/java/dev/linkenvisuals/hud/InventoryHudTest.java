package dev.linkenvisuals.hud;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class InventoryHudTest {
    @Test
    void inventoryRowsMapToPlayerInventoryIndices() {
        assertEquals(9, InventoryHud.inventoryIndex(0, 0));
        assertEquals(17, InventoryHud.inventoryIndex(0, 8));
        assertEquals(35, InventoryHud.inventoryIndex(2, 8));
        assertEquals(0, InventoryHud.inventoryIndex(3, 0));
        assertEquals(8, InventoryHud.inventoryIndex(3, 8));
    }

    @Test
    void hotbarAddsAFourthRow() {
        assertEquals(3, InventoryHud.slotRows(false));
        assertEquals(4, InventoryHud.slotRows(true));
    }
}
