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
    public static final float INTERFACE_HEIGHT = 282.0f;
    public static final float PROFILE_HEIGHT = 360.0f;
    public static final float THEME_WIDTH = 360.0f;
    public static final float THEME_HEIGHT = 855.0f;
    public static final int NAV_ITEMS = 7;
    public static final float NAV_PAD = 9.0f;
    public static final float NAV_TAB_HEIGHT = 54.0f;
    public static final float NAV_INDICATOR_INSET = 3.0f;
    public static final float NAV_ITEM_HEIGHT = NAV_TAB_HEIGHT;
    public static final float NAV_ITEM_GAP = 0.0f;
    public static final float HEADER_HEIGHT = 78.0f;
    public static final float CARD_HEIGHT = 129.0f;
    public static final float CARD_GAP = 15.0f;
    public static final int CARD_COLUMNS = 2;

    // Donor ModuleCard (1280x720) scaled by 1.5.
    public static final float CARD_RADIUS = 15.0f;
    public static final float CARD_BADGE_SIZE = 51.0f;
    public static final float CARD_BADGE_PAD_X = 18.0f;
    public static final float CARD_BADGE_CENTER_OFFSET = -7.5f;
    public static final float CARD_STRIPE_X = 7.5f;
    public static final float CARD_TITLE_X = 81.0f;
    public static final float CARD_TITLE_Y = 34.5f;
    public static final float CARD_TITLE_SIZE = 14.1f;
    public static final float CARD_DESC_Y = 61.5f;
    public static final float CARD_DESC_SIZE = 11.85f;
    public static final float CARD_BIND_Y = 90.0f;
    public static final float CARD_BIND_H = 21.0f;
    public static final float CARD_TOGGLE_W = 54.0f;
    public static final float CARD_TOGGLE_H = 27.0f;
    public static final float CARD_TOGGLE_RIGHT = 69.0f;
    public static final float CARD_TOGGLE_CENTER_OFFSET = -7.5f;

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
        Rect header = new Rect(center.x() + 24.0f, center.y() + 9.0f,
            center.width() - 48.0f, 60.0f);
        Rect close = new Rect(center.right() - 60.0f, center.y() + 21.0f, 36.0f, 36.0f);
        Rect all = new Rect(center.x() + 132.0f, center.y() + 18.0f, 51.0f, 42.0f);
        Rect search = new Rect(center.x() + 192.0f, center.y() + 18.0f,
            Math.max(120.0f, center.width() - 294.0f), 42.0f);

        Rect nav = new Rect((width - DOCK_WIDTH) * 0.5f, center.bottom() + MAIN_GAP,
            DOCK_WIDTH, DOCK_HEIGHT);
        Rect settings = new Rect(MARGIN, mainY, SIDE_WIDTH, INTERFACE_HEIGHT);
        Rect profile = new Rect(MARGIN, settings.bottom() + MAIN_GAP, SIDE_WIDTH, PROFILE_HEIGHT);
        Rect theme = new Rect(width - MARGIN - THEME_WIDTH,
            mainY + (MAIN_HEIGHT - THEME_HEIGHT) * 0.5f, THEME_WIDTH, THEME_HEIGHT);
        Rect cards = new Rect(center.x() + 63.0f, center.y() + HEADER_HEIGHT + 2.0f,
            center.width() - 126.0f, center.height() - HEADER_HEIGHT - 131.0f);
        Rect settingsContent = new Rect(settings.x() + 18.0f, settings.y() + 96.0f,
            settings.width() - 36.0f, settings.height() - 116.0f);
        Rect profileContent = new Rect(profile.x() + 18.0f, profile.y() + 96.0f,
            profile.width() - 36.0f, profile.height() - 116.0f);

        return new Metrics(scale, width, height, header, close, nav, center, settings, profile,
            center, all, search, cards, settingsContent, profileContent, theme);
    }

    public static Rect navItem(Metrics metrics, int index) {
        float itemWidth = (metrics.nav().width() - NAV_PAD * 2.0f) / NAV_ITEMS;
        return new Rect(metrics.nav().x() + NAV_PAD + index * itemWidth,
            metrics.nav().y() + NAV_PAD, itemWidth, NAV_TAB_HEIGHT);
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

    public record CardMetrics(
        Rect badge,
        Rect title,
        Rect description,
        Rect bind,
        Rect toggle,
        Rect stripe,
        Rect pin
    ) {
    }

    /** Internal card geometry, matching donor ModuleCard ×1.5. */
    public static CardMetrics cardMetrics(Rect card) {
        float badgeY = card.y() + (card.height() - CARD_BADGE_SIZE) * 0.5f + CARD_BADGE_CENTER_OFFSET;
        float stripeH = card.height() * 0.6f;
        return new CardMetrics(
            new Rect(card.x() + CARD_BADGE_PAD_X, badgeY, CARD_BADGE_SIZE, CARD_BADGE_SIZE),
            new Rect(card.x() + CARD_TITLE_X, card.y() + CARD_TITLE_Y,
                Math.max(10.0f, card.width() - 156.0f), 20.0f),
            new Rect(card.x() + CARD_TITLE_X, card.y() + CARD_DESC_Y,
                Math.max(10.0f, card.width() - 156.0f), 18.0f),
            new Rect(card.x() + CARD_BADGE_PAD_X, card.y() + CARD_BIND_Y,
                60.0f, CARD_BIND_H),
            new Rect(card.right() - CARD_TOGGLE_RIGHT,
                card.y() + (card.height() - CARD_TOGGLE_H) * 0.5f + CARD_TOGGLE_CENTER_OFFSET,
                CARD_TOGGLE_W, CARD_TOGGLE_H),
            new Rect(card.x() + CARD_STRIPE_X, card.y() + (card.height() - stripeH) * 0.5f,
                4.5f, stripeH),
            new Rect(card.right() - 24.0f, card.bottom() - 19.0f, 10.0f, 10.0f)
        );
    }
}
