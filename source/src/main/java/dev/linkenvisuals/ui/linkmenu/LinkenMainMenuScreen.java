package dev.linkenvisuals.ui.linkmenu;

import dev.linkenvisuals.audio.LinkenMenuMusic;
import dev.linkenvisuals.i18n.I18n;
import dev.linkenvisuals.render.Pipelines;
import dev.linkenvisuals.ui.Spring;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import dev.linkenvisuals.ui.linkmenu.LinkenSelectWorldScreen;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

/**
 * LINKEN main menu, ported 1:1 from the donor {@code CustomMainMenuScreen}.
 *
 * <p>The composition is anchored to the donor 1920x1080 canvas. It is
 * independent from the customizable Beta menu and never touches
 * {@code MainMenuConfig}; switching styles therefore preserves the Beta and
 * Vanilla layouts.
 */
public final class LinkenMainMenuScreen extends Screen {
    private static final float INTRO_MS = 450.0f;
    private static final long QUIT_CONFIRM_MS = 3000L;

    private static final String DISCORD_URL = "https://dsc.gg/linkenvisuals";
    private static final String TELEGRAM_URL = "https://t.me/linkenvisuals";
    private static final String SITE_URL = "https://linkenvisuals.ru";
    private static final String DEV_URL = "https://t.me/linkenvisualsdev";

    private final Spring multiplayerHover = new Spring(0.0, 280.0, 20.0);
    private final Spring singleplayerHover = new Spring(0.0, 280.0, 20.0);
    private final Spring settingsHover = new Spring(0.0, 280.0, 20.0);
    private final Spring quitHover = new Spring(0.0, 280.0, 20.0);
    private final Spring accountHover = new Spring(0.0, 280.0, 20.0);
    private final Spring[] socialHover = {
        new Spring(0.0, 280.0, 20.0), new Spring(0.0, 280.0, 20.0),
        new Spring(0.0, 280.0, 20.0), new Spring(0.0, 280.0, 20.0)
    };

    private LinkenMenuLayout.Metrics layout;
    private int layoutWidth = -1;
    private int layoutHeight = -1;

    private long openedAt;
    private long lastFrame = System.nanoTime();
    private long quitArmedUntil;
    private float parallaxX;
    private float parallaxY;

    public LinkenMainMenuScreen() {
        super(Text.empty());
    }

    @Override
    protected void init() {
        Pipelines.warmUp();
        LinkenFonts.body();
        LinkenMenuMusic.autoStartOnce();
        openedAt = Util.getMeasuringTimeMs();
        lastFrame = System.nanoTime();
        quitArmedUntil = 0L;
        layout = null;
        layoutWidth = -1;
        layoutHeight = -1;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        long now = System.nanoTime();
        double deltaSeconds = Math.min((now - lastFrame) / 1_000_000_000.0, 0.05);
        lastFrame = now;

        LinkenMenuLayout.Metrics metrics = layout();
        long millis = Util.getMeasuringTimeMs();
        float intro = Math.min(1.0f, (millis - openedAt) / INTRO_MS);
        intro = 1.0f - (1.0f - intro) * (1.0f - intro) * (1.0f - intro);

        float virtualMouseX = (float) mouseX / metrics.scale();
        float virtualMouseY = (float) mouseY / metrics.scale();
        updateParallax(virtualMouseX, virtualMouseY, metrics);

        context.getMatrices().pushMatrix();
        context.getMatrices().scale(metrics.scale(), metrics.scale());

        LinkenMenuChrome.drawBackground(context, metrics, parallaxX, parallaxY, 1.0f);

        context.getMatrices().translate(0.0f, (1.0f - intro) * 22.0f);
        LinkenMenuChrome.drawWordmark(context, metrics, intro);
        LinkenMenuChrome.drawClock(context, metrics, intro);
        renderWelcome(context, metrics, intro);
        renderAccount(context, metrics, virtualMouseX, virtualMouseY, deltaSeconds, intro);

        float multiHover = step(multiplayerHover, metrics.multiplayer().contains(virtualMouseX, virtualMouseY), deltaSeconds);
        float singleHover = step(singleplayerHover, metrics.singleplayer().contains(virtualMouseX, virtualMouseY), deltaSeconds);
        float settingsHoverValue = step(settingsHover, metrics.settings().contains(virtualMouseX, virtualMouseY), deltaSeconds);
        float quitHoverValue = step(quitHover, metrics.quit().contains(virtualMouseX, virtualMouseY), deltaSeconds);

        LinkenMenuChrome.drawButton(context, metrics.multiplayer(), true, LinkenMenuChrome.ICON_GLOBE,
            I18n.get("linkenvisuals.menu.multiplayer"), multiHover, false, false, intro, metrics.scale());
        LinkenMenuChrome.drawButton(context, metrics.singleplayer(), false, LinkenMenuChrome.ICON_PERSON,
            I18n.get("linkenvisuals.menu.singleplayer"), singleHover, false, false, intro, metrics.scale());
        LinkenMenuChrome.drawButton(context, metrics.settings(), false, LinkenMenuChrome.ICON_GEAR,
            I18n.get("linkenvisuals.menu.settings"), settingsHoverValue, false, false, intro, metrics.scale());

        boolean quitArmed = millis < quitArmedUntil;
        LinkenMenuChrome.drawButton(context, metrics.quit(), false, LinkenMenuChrome.ICON_POWER,
            I18n.get(quitArmed ? "linkenvisuals.linkmenu.quit_confirm" : "linkenvisuals.linkmenu.quit_short"),
            quitHoverValue, quitArmed, true, intro, metrics.scale());

        renderSocials(context, metrics, virtualMouseX, virtualMouseY, deltaSeconds, intro);
        LinkenMenuChrome.drawMusic(context, metrics.music(), LinkenMenuMusic.track(), intro, metrics.scale());
        LinkenMenuChrome.drawFooter(context, metrics, intro);
        context.getMatrices().popMatrix();

        if (intro < 0.999f) {
            context.fill(0, 0, width, height,
                LinkenMenuChrome.rgba(0x060908, Math.round(200.0f * (1.0f - intro)), 1.0f));
        }
    }

    private void renderWelcome(DrawContext context, LinkenMenuLayout.Metrics metrics, float fade) {
        LinkenMenuChrome.drawSpaced(context, LinkenFonts.headingLight(),
            I18n.get("linkenvisuals.linkmenu.eyebrow"),
            metrics.eyebrow().x(), metrics.eyebrow().y(), 10.0f, 3.0f,
            LinkenMenuChrome.rgba(LinkenMenuChrome.ACCENT, fade));

        String first = I18n.get("linkenvisuals.linkmenu.welcome_line1");
        LinkenMenuChrome.drawText(context, LinkenFonts.bodyMedium(), first,
            metrics.headline().x(), metrics.headline().y(), 38.0f,
            LinkenMenuChrome.rgba(LinkenMenuChrome.TEXT, fade));

        String second = I18n.get("linkenvisuals.linkmenu.welcome_line2", LinkenMenuChrome.username());
        second = LinkenMenuChrome.ellipsize(LinkenFonts.bodyMedium(), second,
            metrics.headline().width(), 38.0f);
        LinkenMenuChrome.drawText(context, LinkenFonts.bodyMedium(), second,
            metrics.headline().x(), metrics.headline().y() + 43.7f, 38.0f,
            LinkenMenuChrome.rgba(LinkenMenuChrome.TEXT, fade));

        String subtitle = LinkenMenuChrome.ellipsize(LinkenFonts.body(),
            I18n.get("linkenvisuals.linkmenu.subtitle"), metrics.subtitle().width(), 14.0f);
        LinkenMenuChrome.drawText(context, LinkenFonts.body(), subtitle,
            metrics.subtitle().x(), metrics.subtitle().y(), 14.0f,
            LinkenMenuChrome.rgba(LinkenMenuChrome.MUTED2, fade));
    }

    private void renderAccount(DrawContext context, LinkenMenuLayout.Metrics metrics, float mouseX, float mouseY,
                               double deltaSeconds, float fade) {
        LinkenMenuLayout.Rect rect = metrics.account();
        float avatar = 38.0f;
        LinkenMenuChrome.roundedFill(context, rect.x(), rect.y(), avatar, avatar, avatar * 0.5f,
            LinkenMenuChrome.rgba(0x2A3830, 255, fade), metrics.scale());

        String nick = LinkenMenuChrome.username();
        String initial = nick.isEmpty() ? "?" : nick.substring(0, 1).toUpperCase();
        LinkenMenuChrome.drawCentered(context, LinkenFonts.heading(), initial,
            rect.x() + avatar * 0.5f, rect.y() + avatar * 0.5f, 12.0f,
            LinkenMenuChrome.rgba(LinkenMenuChrome.ACCENT, fade));

        float textX = rect.x() + avatar + 12.0f;
        String meta = I18n.get("linkenvisuals.linkmenu.account.meta", LinkenMenuChrome.licenseWord());
        LinkenMenuChrome.drawSpaced(context, LinkenFonts.body(), meta,
            textX + 10.0f, rect.y() + 1.0f, 10.5f, 0.84f,
            LinkenMenuChrome.rgba(LinkenMenuChrome.MUTED2, fade));
        if (LinkenFonts.icons() != null) {
            dev.linkenvisuals.render.IconPipeline.draw(context, LinkenFonts.icons(),
                LinkenMenuChrome.ICON_PERSON, textX + 4.5f, rect.y() + 6.5f, 9.0f,
                LinkenMenuChrome.rgba(LinkenMenuChrome.MUTED2, fade));
        }
        LinkenMenuChrome.drawText(context, LinkenFonts.heading(),
            LinkenMenuChrome.ellipsize(LinkenFonts.heading(), nick,
                metrics.column().right() - textX, 13.0f),
            textX, rect.y() + 18.0f, 13.0f,
            LinkenMenuChrome.rgba(LinkenMenuChrome.TEXT, fade));

        String switchLabel = I18n.get("linkenvisuals.linkmenu.account.switch");
        float switchWidth = LinkenMenuChrome.textWidth(LinkenFonts.body(), switchLabel, 11.0f);
        float switchX = metrics.quit().x() + (metrics.quit().width() - switchWidth) * 0.5f;
        float switchY = rect.y() + (avatar - 11.0f) * 0.5f;
        boolean switchHover = mouseX >= switchX && mouseX <= switchX + switchWidth
            && mouseY >= switchY && mouseY <= switchY + 11.0f;
        float hover = step(accountHover, switchHover, deltaSeconds);
        LinkenMenuChrome.drawText(context, LinkenFonts.body(), switchLabel,
            switchX, switchY, 11.0f,
            LinkenMenuChrome.rgba(LinkenMenuChrome.mix(LinkenMenuChrome.MUTED,
                LinkenMenuChrome.TEXT, hover), fade));
    }

    private void renderSocials(DrawContext context, LinkenMenuLayout.Metrics metrics, float mouseX, float mouseY,
                               double deltaSeconds, float fade) {
        LinkenMenuLayout.Rect socials = metrics.socials();
        int[] icons = {LinkenMenuChrome.ICON_TELEGRAM, LinkenMenuChrome.ICON_DISCORD, 0, 0};
        String[] labels = {null, null, "LV", "</>"};
        for (int i = 0; i < 4; i++) {
            float x = socials.x() + i * (LinkenMenuLayout.SOCIAL_SIZE + LinkenMenuLayout.SOCIAL_GAP);
            LinkenMenuLayout.Rect tile = new LinkenMenuLayout.Rect(
                x, socials.y(), LinkenMenuLayout.SOCIAL_SIZE, LinkenMenuLayout.SOCIAL_SIZE);
            float hover = step(socialHover[i],
                tile.contains(mouseX, mouseY), deltaSeconds);
            LinkenMenuChrome.drawSocialTile(context, tile, icons[i], labels[i], hover, fade, metrics.scale());
        }
    }

    private float step(Spring spring, boolean hovered, double deltaSeconds) {
        spring.step(hovered ? 1.0 : 0.0, deltaSeconds);
        return (float) Math.max(0.0, Math.min(1.0, spring.get()));
    }

    private void updateParallax(float mouseX, float mouseY, LinkenMenuLayout.Metrics metrics) {
        float targetX = clamp((mouseX - metrics.width() * 0.5f) / (metrics.width() * 0.5f));
        float targetY = clamp((mouseY - metrics.height() * 0.5f) / (metrics.height() * 0.5f));
        parallaxX += (targetX - parallaxX) * 0.08f;
        parallaxY += (targetY - parallaxY) * 0.08f;
    }

    private static float clamp(float value) {
        return Math.max(-1.0f, Math.min(1.0f, value));
    }

    private LinkenMenuLayout.Metrics layout() {
        if (layout == null || layoutWidth != width || layoutHeight != height) {
            layout = LinkenMenuLayout.compute(width, height);
            layoutWidth = width;
            layoutHeight = height;
        }
        return layout;
    }

    private LinkenMenuLayout.Rect musicCover(LinkenMenuLayout.Metrics metrics) {
        LinkenMenuLayout.Rect music = metrics.music();
        float coverSize = 34.0f;
        float rowY = music.bottom() - 10.5f - 12.0f - coverSize;
        return new LinkenMenuLayout.Rect(music.x(), rowY, coverSize, coverSize);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubleClick) {
        if (click.button() != 0) {
            return super.mouseClicked(click, doubleClick);
        }
        LinkenMenuLayout.Metrics metrics = layout();
        float mouseX = (float) click.x() / metrics.scale();
        float mouseY = (float) click.y() / metrics.scale();
        long now = Util.getMeasuringTimeMs();

        if (metrics.quit().contains(mouseX, mouseY)) {
            if (now < quitArmedUntil) {
                if (client != null) {
                    client.scheduleStop();
                }
            } else {
                quitArmedUntil = now + QUIT_CONFIRM_MS;
            }
            return true;
        }
        if (metrics.multiplayer().contains(mouseX, mouseY)) {
            if (client != null) {
                client.setScreen(new LinkenMultiplayerScreen(this));
            }
            return true;
        }
        if (metrics.singleplayer().contains(mouseX, mouseY)) {
            if (client != null) {
                client.setScreen(new LinkenSelectWorldScreen(this));
            }
            return true;
        }
        if (metrics.settings().contains(mouseX, mouseY)) {
            if (client != null) {
                client.setScreen(new LinkenSettingsScreen(this));
            }
            return true;
        }

        LinkenMenuLayout.Rect account = metrics.account();
        String switchLabel = I18n.get("linkenvisuals.linkmenu.account.switch");
        float switchWidth = LinkenMenuChrome.textWidth(LinkenFonts.body(), switchLabel, 11.0f);
        float switchX = metrics.quit().x() + (metrics.quit().width() - switchWidth) * 0.5f;
        float switchY = account.y() + (account.height() - 11.0f) * 0.5f;
        if (mouseX >= switchX && mouseX <= switchX + switchWidth
            && mouseY >= switchY && mouseY <= switchY + 11.0f) {
            if (client != null) {
                client.setScreen(new LinkenAltManagerScreen(this));
            }
            return true;
        }

        LinkenMenuLayout.Rect socials = metrics.socials();
        for (int i = 0; i < 4; i++) {
            float x = socials.x() + i * (LinkenMenuLayout.SOCIAL_SIZE + LinkenMenuLayout.SOCIAL_GAP);
            if (mouseX >= x && mouseX <= x + LinkenMenuLayout.SOCIAL_SIZE
                && mouseY >= socials.y() && mouseY <= socials.bottom()) {
                LinkenMenuChrome.openUrl(switch (i) {
                    case 0 -> TELEGRAM_URL;
                    case 1 -> DISCORD_URL;
                    case 2 -> SITE_URL;
                    default -> DEV_URL;
                });
                return true;
            }
        }

        if (musicCover(metrics).contains(mouseX, mouseY)) {
            LinkenMenuMusic.toggle();
            return true;
        }
        return super.mouseClicked(click, doubleClick);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        // The LINKEN art is drawn explicitly in render(); never fall back to the panorama.
    }
}
