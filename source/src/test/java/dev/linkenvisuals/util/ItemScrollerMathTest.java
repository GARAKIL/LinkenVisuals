package dev.linkenvisuals.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ItemScrollerMathTest {
    @Test
    void nonLeftButtonNeverDrags() {
        assertEquals(ItemScrollerMath.DragMode.NONE,
            ItemScrollerMath.resolveDragMode(1, true, true, true));
        assertEquals(ItemScrollerMath.DragMode.NONE,
            ItemScrollerMath.resolveDragMode(2, true, true, true));
    }

    @Test
    void shiftMovesWholeStacks() {
        assertEquals(ItemScrollerMath.DragMode.MOVE_STACKS,
            ItemScrollerMath.resolveDragMode(0, true, false, false));
        assertEquals(ItemScrollerMath.DragMode.MOVE_STACKS,
            ItemScrollerMath.resolveDragMode(0, true, false, true));
    }

    @Test
    void altMovesMatchingAndCtrlMovesOne() {
        assertEquals(ItemScrollerMath.DragMode.MOVE_MATCHING,
            ItemScrollerMath.resolveDragMode(0, false, false, true));
        assertEquals(ItemScrollerMath.DragMode.MOVE_ONE,
            ItemScrollerMath.resolveDragMode(0, false, true, false));
    }

    @Test
    void altOutranksCtrl() {
        assertEquals(ItemScrollerMath.DragMode.MOVE_MATCHING,
            ItemScrollerMath.resolveDragMode(0, false, true, true));
    }

    @Test
    void plainLeftButtonStaysVanilla() {
        assertEquals(ItemScrollerMath.DragMode.NONE,
            ItemScrollerMath.resolveDragMode(0, false, false, false));
    }

    @Test
    void delayFloorHolds() {
        assertEquals(ItemScrollerMath.MIN_DELAY_MS, ItemScrollerMath.effectiveDelayMs(0L));
        assertEquals(ItemScrollerMath.MIN_DELAY_MS, ItemScrollerMath.effectiveDelayMs(49L));
        assertEquals(250L, ItemScrollerMath.effectiveDelayMs(250L));
    }

    @Test
    void budgetCapsAtMaxMovesPerCall() {
        assertEquals(0, ItemScrollerMath.clampBudget(0));
        assertEquals(1, ItemScrollerMath.clampBudget(1));
        assertEquals(ItemScrollerMath.MAX_MOVES_PER_CALL, ItemScrollerMath.clampBudget(9));
        assertEquals(0, ItemScrollerMath.clampBudget(-4));
    }

    @Test
    void budgetRespectsSchedule() {
        ItemScrollerMath.MoveBudget budget = new ItemScrollerMath.MoveBudget();
        budget.schedule(1_000L, 50L);
        assertEquals(0, budget.rawBudget(1_000L, 50L));
        assertEquals(0, budget.rawBudget(1_049L, 50L));
        assertEquals(1, budget.rawBudget(1_050L, 50L));
        assertEquals(2, budget.rawBudget(1_100L, 50L));
    }

    @Test
    void scheduleStaysMonotonicWhenBehind() {
        ItemScrollerMath.MoveBudget budget = new ItemScrollerMath.MoveBudget();
        budget.schedule(1_000L, 50L);
        budget.schedule(1_010L, 50L);
        assertEquals(0, budget.rawBudget(1_049L, 50L));
        assertEquals(0, budget.rawBudget(1_099L, 50L));
        assertEquals(1, budget.rawBudget(1_100L, 50L));
        assertEquals(2, budget.rawBudget(1_150L, 50L));
    }

    @Test
    void zeroDelayFallsBackToFloor() {
        ItemScrollerMath.MoveBudget budget = new ItemScrollerMath.MoveBudget();
        budget.schedule(1_000L, 0L);
        assertEquals(0, budget.rawBudget(1_010L, 0L));
        assertEquals(1, budget.rawBudget(1_050L, 0L));
    }

    @Test
    void resetClearsThrottle() {
        ItemScrollerMath.MoveBudget budget = new ItemScrollerMath.MoveBudget();
        budget.schedule(1_000L, 50L);
        budget.reset();
        assertTrue(budget.rawBudget(1_010L, 50L) >= 1);
    }
}
