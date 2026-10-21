package dev.linkenvisuals.clickgui.link;

import dev.linkenvisuals.render.CirclePipeline;
import dev.linkenvisuals.render.GlowPipeline;
import dev.linkenvisuals.ui.linkmenu.LinkenMenuChrome;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.List;

/** Soft cursor trail used by the LINKEN ClickGUI. */
public final class LinkGuiCursorTrail {
    private static final long LIFETIME_MS = 780L;
    private static final int MAX_POINTS = 64;
    private static final float MIN_STEP = 2.4f;

    private record Point(float x, float y, long time) {
    }

    private final List<Point> points = new ArrayList<>(MAX_POINTS);
    private float lastX = Float.NaN;
    private float lastY = Float.NaN;

    public void clear() {
        points.clear();
        lastX = Float.NaN;
        lastY = Float.NaN;
    }

    public void update(float x, float y) {
        if (!Float.isFinite(x) || !Float.isFinite(y) || Math.abs(x) > 100000.0f || Math.abs(y) > 100000.0f) {
            lastX = Float.NaN;
            lastY = Float.NaN;
            return;
        }
        long now = System.currentTimeMillis();
        expire(now);
        if (Float.isNaN(lastX)) {
            points.add(new Point(x, y, now));
            lastX = x;
            lastY = y;
            trim();
            return;
        }
        float dx = x - lastX;
        float dy = y - lastY;
        float distance = (float) Math.hypot(dx, dy);
        if (distance < 0.25f) {
            return;
        }
        int steps = Math.max(1, Math.min(MAX_POINTS, (int) Math.ceil(distance / MIN_STEP)));
        for (int i = 1; i <= steps; i++) {
            float t = i / (float) steps;
            points.add(new Point(lastX + dx * t, lastY + dy * t, now));
        }
        lastX = x;
        lastY = y;
        trim();
    }

    public void render(DrawContext context) {
        long now = System.currentTimeMillis();
        expire(now);
        if (points.isEmpty()) {
            return;
        }
        for (int i = 0; i < points.size(); i++) {
            Point point = points.get(i);
            float progress = points.size() <= 1 ? 1.0f : i / (float) (points.size() - 1);
            float age = Math.max(0.0f, Math.min(1.0f, (now - point.time()) / (float) LIFETIME_MS));
            float fade = (1.0f - age) * (1.0f - age);
            if (fade <= 0.02f) {
                continue;
            }
            float size = 2.2f + progress * 5.3f;
            float alpha = fade * (0.18f + progress * 0.74f);
            GlowPipeline.radial(context, point.x(), point.y(), size * 2.6f,
                LinkenMenuChrome.rgba(LinkGuiTheme.ACCENT, Math.round(70 * alpha)));
            CirclePipeline.draw(context, point.x(), point.y(), size * 0.5f,
                LinkenMenuChrome.rgba(LinkGuiTheme.ACCENT, Math.round(220 * alpha)));
        }
        Point head = points.get(points.size() - 1);
        float age = Math.max(0.0f, Math.min(1.0f, (now - head.time()) / (float) LIFETIME_MS));
        float fade = 1.0f - age;
        GlowPipeline.radial(context, head.x(), head.y(), 22.0f,
            LinkenMenuChrome.rgba(LinkGuiTheme.ACCENT, Math.round(45 * fade)));
        CirclePipeline.draw(context, head.x(), head.y(), 2.8f,
            LinkenMenuChrome.rgba(0xFFFFFF, Math.round(220 * fade)));
    }

    private void expire(long now) {
        points.removeIf(point -> now - point.time() > LIFETIME_MS);
    }

    private void trim() {
        while (points.size() > MAX_POINTS) {
            points.remove(0);
        }
    }
}
