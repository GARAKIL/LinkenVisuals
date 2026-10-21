package dev.linkenvisuals.clickgui.link;

import dev.linkenvisuals.clickgui.link.LinkGuiLayout.Rect;

import java.util.ArrayList;
import java.util.List;

/**
 * Pure geometry for the donor-inspired LINKEN theme window. Three cards per
 * row, centred on the ClickGUI design canvas.
 */
public final class LinkGuiThemeWindowLayout {
    public static final float WIDTH = 568.0f;
    public static final float HEIGHT = 350.0f;
    public static final int COLUMNS = 3;
    public static final float CARD_WIDTH = 160.0f;
    public static final float CARD_HEIGHT = 110.0f;
    public static final float CARD_GAP = 16.0f;
    public static final float PAD = 28.0f;
    public static final float HEADER = 86.0f;

    public record Layout(Rect panel, Rect close, List<Rect> cards) {
    }

    private LinkGuiThemeWindowLayout() {
    }

    public static Layout compute(float canvasWidth, float canvasHeight, int count) {
        float width = Math.min(WIDTH, Math.max(320.0f, canvasWidth - 40.0f));
        int rows = Math.max(1, (count + COLUMNS - 1) / COLUMNS);
        float height = HEADER + rows * CARD_HEIGHT + Math.max(0, rows - 1) * CARD_GAP + PAD;
        float x = (canvasWidth - width) * 0.5f;
        float y = Math.max(20.0f, (canvasHeight - height) * 0.5f);
        Rect panel = new Rect(x, y, width, height);
        Rect close = new Rect(panel.right() - 52.0f, panel.y() + 16.0f, 36.0f, 36.0f);
        List<Rect> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            int column = i % COLUMNS;
            int row = i / COLUMNS;
            cards.add(new Rect(panel.x() + PAD + column * (CARD_WIDTH + CARD_GAP),
                panel.y() + HEADER + row * (CARD_HEIGHT + CARD_GAP), CARD_WIDTH, CARD_HEIGHT));
        }
        return new Layout(panel, close, cards);
    }
}
