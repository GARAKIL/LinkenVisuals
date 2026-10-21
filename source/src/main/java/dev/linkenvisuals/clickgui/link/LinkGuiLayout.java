package dev.linkenvisuals.clickgui.link;

/**
 * Geometry for the donor-inspired LINKEN ClickGUI: a central 1080x720 module
 * window, a horizontal category dock, a narrow left inspector and a persistent
 * theme window on the right. Values are design units on the 1920x1080 canvas
 * (the donor's 1280x720 composition scaled by 1.5).
 */
public final class LinkGuiLayout {
    public static final float MARGIN = 48.0f;
    public static final float MAIN_WIDTH = 1080.0f;
    public static final float MAIN_HEIGHT = 720.0f;
    public static final float MAIN_TOP = 150.0f;
    public static final float MAIN_GAP = 18.0f;
    public static final float DOCK_WIDTH = 540.0f;
    public static final float DOCK_HEIGHT = 72.0f;
    public static final float SIDE_WIDTH = 330.0f;
    public static final float SIDE_HEIGHT = 600.0f;
    public static final float THEME_WIDTH = 360.0f;
    public static final float THEME_HEIGHT = 855.0f;
    public static final int NAV_ITEMS = 7;
    public static final float NAV_ITEM_HEIGHT = 52.0f;
    public static final float NAV_ITEM_GAP = 5.0f;
    public static final float CARD_HEIGHT = 94.0f;
    public static final float CARD_GAP = 12.0f;
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

        public boolean inside(float screenWidth, float screenHeight) {
            return x >= 0.0f && y >= 0.0f && width > 0.0f && height > 0.0f
                && right() <= screenWidth && bottom() <= screenHeight;
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
        Rect settingsContent,
        Rect theme
    ) {
    }

    public static float scaleFor(int screenWidth, int screenHeight) {
        if (screenWidth <= 0 || screenHeight <= 0) {
            return 0.01f;
        }
        return Math.max(0.01f, Math.min(screenHeight / 1080.0f, screenWidth / 1280.0f));
    }

    public static float virtualWidth(int screenWidth, int screenHeight) {
        return Math.max(1.0f, screenWidth / scaleFor(screenWidth, screenHeight));
    }

    public static Metrics compute(int screenWidth, int screenHeight) {
        float scale = scaleFor(screenWidth, screenHeight);
        float width = virtualWidth(screenWidth, screenHeight);
        float height = Math.max(1080.0f, screenHeight / scale);

        float available = width - SIDE_WIDTH - THEME_WIDTH - MARGIN * 2.0f - MAIN_GAP * 2.0f;
        float mainWidth = Math.min(MAIN_WIDTH, Math.max(600.0f, available));
        float mainX = (width - mainWidth) * 0.5f;
        float mainY = MAIN_TOP;
        Rect center = new Rect(mainX, mainY, mainWidth, MAIN_HEIGHT);
        Rect header = new Rect(center.x() + 24.0f, center.y() + 18.0f,
            center.width() - 48.0f, 64.0f);
        Rect close = new Rect(center.right() - 60.0f, center.y() + 18.0f, 44.0f, 44.0f);
        Rect search = new Rect(center.right() - 72.0f - 380.0f, center.y() + 28.0f, 380.0f, 44.0f);

        Rect nav = new Rect((width - DOCK_WIDTH) * 0.5f, center.bottom() + MAIN_GAP,
            DOCK_WIDTH, DOCK_HEIGHT);
        Rect settings = new Rect(MARGIN, mainY, SIDE_WIDTH, SIDE_HEIGHT);
        Rect theme = new Rect(width - MARGIN - THEME_WIDTH,
            mainY + (MAIN_HEIGHT - THEME_HEIGHT) * 0.5f, THEME_WIDTH, THEME_HEIGHT);
        Rect cards = new Rect(center.x() + 24.0f, center.y() + 104.0f,
            center.width() - 48.0f, center.height() - 104.0f - 100.0f);
        Rect settingsContent = new Rect(settings.x() + 18.0f, settings.y() + 96.0f,
            settings.width() - 36.0f, settings.height() - 116.0f);

        return new Metrics(scale, width, height, header, close, nav, center, settings,
            center, search, cards, settingsContent, theme);
    }

    public static Rect navItem(Metrics metrics, int index) {
        float inner = metrics.nav().width() - 20.0f;
        float itemWidth = (inner - (NAV_ITEMS - 1) * NAV_ITEM_GAP) / NAV_ITEMS;
        return new Rect(metrics.nav().x() + 10.0f + index * (itemWidth + NAV_ITEM_GAP),
            metrics.nav().y() + 10.0f, itemWidth, NAV_ITEM_HEIGHT);
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
