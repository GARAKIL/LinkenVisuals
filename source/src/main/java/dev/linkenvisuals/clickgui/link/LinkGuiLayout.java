package dev.linkenvisuals.clickgui.link;

import dev.linkenvisuals.ui.linkmenu.LinkenMenuLayout;

/**
 * Pure geometry for the LINKEN ClickGUI shell. Values are design units on the
 * same 1080-high virtual canvas as the main menu, which keeps the rail, list and
 * settings proportions stable at every window size.
 */
public final class LinkGuiLayout {
    public static final float MARGIN = 56.0f;
    public static final float GAP = 24.0f;
    public static final float TOP = 150.0f;
    public static final float BOTTOM_MARGIN = 72.0f;
    public static final int NAV_ITEMS = 7;
    public static final float NAV_ITEM_HEIGHT = 64.0f;
    public static final float NAV_ITEM_GAP = 8.0f;
    public static final float CARD_HEIGHT = 94.0f;
    public static final float CARD_GAP = 14.0f;
    public static final int CARD_COLUMNS = 2;

    private LinkGuiLayout() {
    }

    public record Rect(float x, float y, float width, float height) {
        public float right() {
            return x + width;
        }

        public float bottom() {
            return y + height;
        }

        public boolean contains(float px, float py) {
            return px >= x && px <= right() && py >= y && py <= bottom();
        }

        public boolean intersects(Rect other) {
            return x < other.right() && right() > other.x
                && y < other.bottom() && bottom() > other.y;
        }
    }

    public record Metrics(
        float scale,
        float width,
        float height,
        Rect header,
        Rect close,
        Rect nav,
        Rect center,
        Rect settings,
        Rect config,
        Rect search,
        Rect cards,
        Rect settingsContent
    ) {
    }

    public static float scaleFor(int screenWidth, int screenHeight) {
        if (screenWidth <= 0 || screenHeight <= 0) {
            return 0.01f;
        }
        // Width-limited only on very narrow windows; normally height decides.
        return Math.max(0.01f, Math.min(screenHeight / 1080.0f, screenWidth / 1280.0f));
    }

    public static float virtualWidth(int screenWidth, int screenHeight) {
        return Math.max(1.0f, screenWidth / scaleFor(screenWidth, screenHeight));
    }

    public static Metrics compute(int screenWidth, int screenHeight) {
        float scale = scaleFor(screenWidth, screenHeight);
        float width = virtualWidth(screenWidth, screenHeight);
        float height = Math.max(1080.0f, screenHeight / scale);

        float navWidth = Math.max(240.0f, Math.min(300.0f, width * 0.15f));
        float settingsWidth = Math.max(520.0f, Math.min(720.0f, width * 0.36f));
        float navX = MARGIN;
        float centerX = navX + navWidth + GAP;
        float settingsX = width - MARGIN - settingsWidth;
        float centerWidth = Math.max(300.0f, settingsX - GAP - centerX);
        float top = TOP;
        float bottom = height - BOTTOM_MARGIN;
        float contentHeight = bottom - top;

        Rect header = new Rect(MARGIN, 40.0f, width - MARGIN * 2.0f, 58.0f);
        Rect close = new Rect(width - MARGIN - 48.0f, 42.0f, 48.0f, 48.0f);
        Rect nav = new Rect(navX, top, navWidth, contentHeight);
        Rect center = new Rect(centerX, top, centerWidth, contentHeight);
        Rect settings = new Rect(settingsX, top, settingsWidth, contentHeight);
        Rect config = new Rect(centerX, top, settings.right() - centerX, contentHeight);
        Rect search = new Rect(center.right() - 28.0f - 318.0f, center.y + 24.0f, 318.0f, 46.0f);
        Rect cards = new Rect(center.x + 28.0f, center.y + 104.0f,
            center.width - 56.0f, center.height - 132.0f);
        Rect settingsContent = new Rect(settings.x + 28.0f, settings.y + 112.0f,
            settings.width - 56.0f, settings.height - 140.0f);

        return new Metrics(scale, width, height, header, close, nav, center, settings,
            config, search, cards, settingsContent);
    }

    public static Rect navItem(Metrics metrics, int index) {
        return new Rect(metrics.nav().x() + 12.0f,
            metrics.nav().y() + 68.0f + index * (NAV_ITEM_HEIGHT + NAV_ITEM_GAP),
            metrics.nav().width() - 24.0f, NAV_ITEM_HEIGHT);
    }

    public static Rect card(Metrics metrics, int index) {
        float cardWidth = (metrics.cards().width() - CARD_GAP) / CARD_COLUMNS;
        int column = index % CARD_COLUMNS;
        int row = index / CARD_COLUMNS;
        return new Rect(metrics.cards().x() + column * (cardWidth + CARD_GAP),
            metrics.cards().y() + row * (CARD_HEIGHT + CARD_GAP), cardWidth, CARD_HEIGHT);
    }

    public static int columns(int moduleCount) {
        return Math.max(1, Math.min(CARD_COLUMNS, moduleCount));
    }
}
