package dev.linkenvisuals.util;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DragPathTest {
    @Test
    void degenerateSegmentYieldsSinglePoint() {
        List<double[]> points = DragPath.sample(10.0, 10.0, 10.0, 10.0, 8);
        assertEquals(1, points.size());
        assertEquals(10.0, points.get(0)[0], 1.0e-9);
    }

    @Test
    void includesBothEndpoints() {
        List<double[]> points = DragPath.sample(0.0, 0.0, 100.0, 0.0, 5);
        assertEquals(0.0, points.get(0)[0], 1.0e-9);
        assertEquals(100.0, points.get(points.size() - 1)[0], 1.0e-9);
    }

    @Test
    void neverExceedsBudget() {
        for (int budget = 1; budget <= 12; budget++) {
            List<double[]> points = DragPath.sample(0.0, 0.0, 500.0, 300.0, budget);
            assertTrue(points.size() <= budget, "budget " + budget + " exceeded: " + points.size());
        }
    }

    @Test
    void followsTheSegment() {
        List<double[]> points = DragPath.sample(0.0, 0.0, 100.0, 100.0, 6);
        for (double[] point : points) {
            assertEquals(point[0], point[1], 1.0e-6, "samples must stay on the diagonal");
        }
    }

    @Test
    void nonPositiveBudgetYieldsNothing() {
        assertTrue(DragPath.sample(0.0, 0.0, 10.0, 10.0, 0).isEmpty());
    }

    @Test
    void mapsHotbarIndicesToHandlerSlots() {
        assertEquals(36, DragPath.inventoryIndexToSlot(0));
        assertEquals(44, DragPath.inventoryIndexToSlot(8));
        assertEquals(9, DragPath.inventoryIndexToSlot(9));
        assertEquals(35, DragPath.inventoryIndexToSlot(35));
    }
}
