package dev.linkenvisuals.ui.linkmenu;

/**
 * Pure geometry for the LINKEN main menu.
 *
 * <p>All values are design units on a virtual 1920x1080 canvas. The screen
 * multiplies them with {@link #scaleFor(int)} so the composition keeps its
 * proportions at 720p, 1440p and 4K. The class deliberately has no Minecraft
 * imports so the layout rules can be unit-tested.
 */
public final class LinkenMenuLayout {
    public static final float DESIGN_HEIGHT = 1080.0f;
    public static final float MARGIN = 56.0f;

    public static final float COLUMN_X = 56.0f;
    public static final float COLUMN_TOP = 300.0f;
    public static final float COLUMN_WIDTH = 340.0f;

    public static final float HEADLINE_GAP = 22.0f;
    public static final float HEADLINE_SIZE = 38.0f;
    public static final float HEADLINE_LINE = 46.0f;

    public static final float BUTTON_HEIGHT = 50.0f;
    public static final float BUTTON_GAP = 9.0f;
    public static final float QUIT_WIDTH = 108.0f;

    public static final float SOCIAL_SIZE = 38.0f;
    public static final float SOCIAL_GAP = 9.0f;

    public static final float MUSIC_WIDTH = 320.0f;
    public static final float MUSIC_HEIGHT = 86.0f;

    public static final float FOOTER_LINE_HEIGHT = 15.0f;
    public static final float BOTTOM_MARGIN = 44.0f;

    private LinkenMenuLayout() {
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

        public boolean inside(float screenWidth, float screenHeight) {
            return x >= 0.0f && y >= 0.0f && width > 0.0f && height > 0.0f
                && right() <= screenWidth && bottom() <= screenHeight;
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
        Rect column,
        Rect eyebrow,
        Rect headline,
        Rect subtitle,
        Rect account,
        Rect singleplayer,
        Rect multiplayer,
        Rect settings,
        Rect altManager,
        Rect quit,
        Rect socials,
        Rect music,
        Rect footer,
        Rect clock
    ) {
    }

    public static float scaleFor(int screenHeight) {
        if (screenHeight <= 0) {
            return 0.01f;
        }
        return Math.max(0.01f, screenHeight / DESIGN_HEIGHT);
    }

    public static float virtualWidth(int screenWidth, int screenHeight) {
        return Math.max(1.0f, screenWidth / scaleFor(screenHeight));
    }

    public static Metrics compute(int screenWidth, int screenHeight) {
        float scale = scaleFor(screenHeight);
        float width = Math.max(1.0f, screenWidth / scale);
        float height = DESIGN_HEIGHT;

        float x = MARGIN;
        float top = COLUMN_TOP;

        Rect eyebrow = new Rect(x, top, COLUMN_WIDTH, 14.0f);
        Rect headline = new Rect(x, top + HEADLINE_GAP, COLUMN_WIDTH, HEADLINE_LINE * 2.0f);
        Rect subtitle = new Rect(x, headline.bottom() + 4.0f, COLUMN_WIDTH, 18.0f);
        Rect account = new Rect(x, subtitle.bottom() + 16.0f, COLUMN_WIDTH, 56.0f);

        float buttonsTop = account.bottom() + 18.0f;
        Rect singleplayer = new Rect(x, buttonsTop, COLUMN_WIDTH, BUTTON_HEIGHT);
        Rect multiplayer = new Rect(x, singleplayer.bottom() + BUTTON_GAP, COLUMN_WIDTH, BUTTON_HEIGHT);
        Rect settings = new Rect(x, multiplayer.bottom() + BUTTON_GAP, COLUMN_WIDTH, BUTTON_HEIGHT);

        float secondaryTop = settings.bottom() + BUTTON_GAP;
        float altWidth = COLUMN_WIDTH - QUIT_WIDTH - BUTTON_GAP;
        Rect altManager = new Rect(x, secondaryTop, altWidth, BUTTON_HEIGHT);
        Rect quit = new Rect(altManager.right() + BUTTON_GAP, secondaryTop, QUIT_WIDTH, BUTTON_HEIGHT);

        float footerTop = height - BOTTOM_MARGIN - FOOTER_LINE_HEIGHT * 2.0f;
        Rect music = new Rect(
            Math.max(MARGIN, width - MARGIN - MUSIC_WIDTH),
            height - BOTTOM_MARGIN - MUSIC_HEIGHT,
            MUSIC_WIDTH,
            MUSIC_HEIGHT
        );

        float footerWidth = Math.max(120.0f, Math.min(560.0f, music.x() - MARGIN - 16.0f));
        Rect footer = new Rect(x, footerTop, footerWidth, FOOTER_LINE_HEIGHT * 2.0f);

        float socialsTop = footerTop - 16.0f - SOCIAL_SIZE;
        Rect socials = new Rect(x, socialsTop, SOCIAL_SIZE * 2.0f + SOCIAL_GAP, SOCIAL_SIZE);

        Rect clock = new Rect(Math.max(MARGIN, width - MARGIN - 300.0f), 38.0f, 300.0f, 70.0f);
        Rect column = new Rect(x, top, COLUMN_WIDTH, quit.bottom() - top);

        return new Metrics(scale, width, height, column, eyebrow, headline, subtitle, account,
            singleplayer, multiplayer, settings, altManager, quit, socials, music, footer, clock);
    }
}
