package dev.linkenvisuals.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Pure geometry for {@code ItemScroller}: the drag path is sampled so that a
 * fast mouse movement cannot skip slots. Minecraft-free so it is testable.
 */
public final class DragPath {
    private DragPath() {
    }

    /**
     * Returns at most {@code maxSamples} points along the segment from
     * {@code (startX,startY)} to {@code (endX,endY)}, inclusive of both ends.
     * Degenerate segments yield a single point.
     */
    public static List<double[]> sample(double startX, double startY, double endX, double endY,
                                        int maxSamples) {
        List<double[]> points = new ArrayList<>();
        if (maxSamples <= 0) {
            return points;
        }
        int steps = Math.max(Math.abs((int) Math.round(endX) - (int) Math.round(startX)),
            Math.abs((int) Math.round(endY) - (int) Math.round(startY)));
        if (steps <= 0) {
            points.add(new double[]{endX, endY});
            return points;
        }
        if (maxSamples == 1) {
            points.add(new double[]{endX, endY});
            return points;
        }
        int count = Math.min(maxSamples, steps + 1);
        for (int i = 0; i < count; i++) {
            if (i == count - 1) {
                points.add(new double[]{endX, endY});
                break;
            }
            double t = i / (double) (count - 1);
            points.add(new double[]{startX + (endX - startX) * t, startY + (endY - startY) * t});
        }
        return points;
    }
}
