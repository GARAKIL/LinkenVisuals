package dev.linkenvisuals.clickgui.link;

/**
 * Geometry for the LINKEN ClickGUI, matching the donor {@code LinkGuiScreen}
 * on its fixed 1920x1080 canvas: a 1090x830 main window at 410/90, a 340x920
 * theme/interface panel at 1540/90, a 330x358 profile card at 40/90 and a
 * bottom dock of 52 px tiles.
 */
public final class LinkGuiLayout {
    public static final float CANVAS_WIDTH = 1920.0f;
    public static final float CANVAS_HEIGHT = 1080.0f;

    public static final float MAIN_WIDTH = 1090.0f;
    public static final float MAIN_HEIGHT = 830.0f;
    public static final float MAIN_X = 410.0f;
    public static final float MAIN_Y = 90.0f;
    public static final float HEADER_HEIGHT = 62.0f;

    public static final float DOCK_HEIGHT = 52.0f;
    public static final float DOCK_BOTTOM = 45.0f;
    public static final float DOCK_TILE = 52.0f;
    public static final float DOCK_GAP = 10.0f;
    public static final int NAV_ITEMS = 6;

    public static final float SIDE_WIDTH = 330.0f;
    public static final float SIDE_X = 40.0f;
    public static final float SIDE_Y = 90.0f;
    public static final float PROFILE_HEIGHT = 358.0f;

    public static final float THEME_WIDTH = 340.0f;
    public static final float THEME_HEIGHT = 920.0f;
    public static final float THEME_X = 1540.0f;
    public static final float THEME_Y = 90.0f;

    public static final float CARD_HEIGHT = 102.0f;
    public static final float CARD_GAP = 12.0f;
    public static final int CARD_COLUMNS = 3;
    public static final float CARD_RADIUS = 10.0f;
    public static final float CARD_PAD = 12.0f;
    public static final float CARD_ICON_SIZE = 17.0f;
    public static final float CARD_TOGGLE_W = 32.0f;
    public static final float CARD_TOGGLE_H = 18.0f;

    private static final float FIT_PAD = 12.0f;

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
        float offsetX,
        float offsetY,
        float width,
        float height,
        Rect header,
        Rect close,
        Rect nav,
        Rect center,
        Rect settings,
        Rect profile,
        Rect config,
        Rect all,
        Rect search,
        Rect cards,
        Rect settingsContent,
        Rect profileContent,
        Rect theme
    ) {
    }

    public static float scaleFor(int screenWidth, int screenHeight) {
        return scaleFor(screenWidth, screenHeight, 1.0f);
    }

    public static float scaleFor(int screenWidth, int screenHeight, float uiScale) {
        if (screenWidth <= 0 || screenHeight <= 0) {
            return 0.01f;
        }
        float fitRaw = Math.min(screenWidth / CANVAS_WIDTH, screenHeight / CANVAS_HEIGHT);
        float fitPad = Math.min((screenWidth - FIT_PAD * 2.0f) / CANVAS_WIDTH,
            (screenHeight - FIT_PAD * 2.0f) / CANVAS_HEIGHT);
        return Math.max(0.01f, Math.min(fitPad * Math.max(0.5f, uiScale), fitRaw));
    }

    public static float virtualWidth(int screenWidth, int screenHeight) {
        return CANVAS_WIDTH;
    }

    public static Metrics compute(int screenWidth, int screenHeight) {
        return compute(screenWidth, screenHeight, 1.0f);
    }

    public static Metrics compute(int screenWidth, int screenHeight, float uiScale) {
        float fitRaw = Math.min(screenWidth / CANVAS_WIDTH, screenHeight / CANVAS_HEIGHT);
        float fitPad = Math.min((screenWidth - FIT_PAD * 2.0f) / CANVAS_WIDTH,
            (screenHeight - FIT_PAD * 2.0f) / CANVAS_HEIGHT);
        float scale = Math.max(0.01f, Math.min(fitPad * Math.max(0.5f, uiScale), fitRaw));
        float offsetX = (screenWidth - CANVAS_WIDTH * scale) * 0.5f;
        float offsetY = (screenHeight - CANVAS_HEIGHT * scale) * 0.5f;
        float width = CANVAS_WIDTH;
        float height = CANVAS_HEIGHT;

        Rect center = new Rect(MAIN_X, MAIN_Y, MAIN_WIDTH, MAIN_HEIGHT);
        Rect header = new Rect(MAIN_X, MAIN_Y, MAIN_WIDTH, HEADER_HEIGHT);
        Rect close = new Rect(MAIN_X + MAIN_WIDTH - 54.0f, MAIN_Y + 14.0f, 34.0f, 34.0f);
        Rect all = new Rect(MAIN_X + 178.0f, MAIN_Y + 14.0f, 52.0f, 34.0f);
        Rect search = new Rect(MAIN_X + 242.0f, MAIN_Y + 14.0f,
            close.x() - 8.0f - 34.0f - 8.0f - (MAIN_X + 242.0f), 34.0f);

        Rect nav = new Rect(MAIN_X, height - DOCK_BOTTOM - DOCK_HEIGHT, MAIN_WIDTH, DOCK_HEIGHT);
        Rect profile = new Rect(SIDE_X, SIDE_Y, SIDE_WIDTH, PROFILE_HEIGHT);
        Rect theme = new Rect(THEME_X, THEME_Y, THEME_WIDTH, THEME_HEIGHT);
        Rect cards = new Rect(MAIN_X + 20.0f, MAIN_Y + HEADER_HEIGHT + 20.0f,
            MAIN_WIDTH - 40.0f, MAIN_HEIGHT - HEADER_HEIGHT - 20.0f - 92.0f - 8.0f);
        Rect profileContent = new Rect(profile.x() + 16.0f, profile.y() + 16.0f,
            profile.width() - 32.0f, profile.height() - 32.0f);

        return new Metrics(scale, offsetX, offsetY, width, height, header, close, nav, center, profile, profile,
            center, all, search, cards, profileContent, profileContent, theme);
    }

    public static Rect navItem(Metrics metrics, int index) {
        float itemWidth = (metrics.nav().width() - DOCK_GAP * (NAV_ITEMS - 1)) / NAV_ITEMS;
        return new Rect(metrics.nav().x() + index * (itemWidth + DOCK_GAP),
            metrics.nav().y(), itemWidth, metrics.nav().height());
    }

    public static Rect card(Metrics metrics, int index) {
        float cardWidth = (metrics.cards().width() - CARD_GAP * (CARD_COLUMNS - 1)) / CARD_COLUMNS;
        int column = index % CARD_COLUMNS;
        int row = index / CARD_COLUMNS;
        return new Rect(metrics.cards().x() + column * (cardWidth + CARD_GAP),
            metrics.cards().y() + row * (CARD_HEIGHT + CARD_GAP), cardWidth, CARD_HEIGHT);
    }

    public static int columns(int moduleCount) {
        return Math.max(1, Math.min(CARD_COLUMNS, moduleCount));
    }

    public record CardMetrics(
        Rect icon,
        Rect title,
        Rect description,
        Rect bind,
        Rect toggle,
        Rect more
    ) {
    }

    /** Internal card geometry, matching the donor 3-column module tile. */
    public static CardMetrics cardMetrics(Rect card) {
        float pad = CARD_PAD;
        float icon = CARD_ICON_SIZE;
        return new CardMetrics(
            new Rect(card.x() + pad, card.y() + 11.0f, icon, icon),
            new Rect(card.x() + pad + icon + 9.0f, card.y() + 11.0f,
                Math.max(20.0f, card.width() - pad * 2.0f - icon - 9.0f - CARD_TOGGLE_W - 10.0f), 18.0f),
            new Rect(card.x() + pad, card.y() + 36.0f,
                Math.max(20.0f, card.width() - pad * 2.0f), 28.0f),
            new Rect(card.x() + pad, card.bottom() - 27.0f, 110.0f, 18.0f),
            new Rect(card.right() - pad - CARD_TOGGLE_W, card.y() + 11.0f,
                CARD_TOGGLE_W, CARD_TOGGLE_H),
            new Rect(card.right() - pad - 24.0f, card.bottom() - 29.0f, 24.0f, 20.0f)
        );
    }
}
