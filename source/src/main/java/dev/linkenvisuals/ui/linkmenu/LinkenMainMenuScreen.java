package dev.linkenvisuals.ui.linkmenu;

import dev.linkenvisuals.LinkenVisuals;
import dev.linkenvisuals.i18n.I18n;
import dev.linkenvisuals.media.MediaWatcher;
import dev.linkenvisuals.render.Pipelines;
import dev.linkenvisuals.ui.Spring;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.entity.player.SkinTextures;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import java.time.LocalTime;
import java.util.function.Supplier;

/**
 * LINKEN main menu (M1): fixed 1920x1080 composition anchored to the screen
 * edges, with a parallax background, editorial left column and live media card.
 *
 * <p>This screen is independent from the customizable Beta menu. It never
 * touches {@code MainMenuConfig}; switching styles therefore preserves the
 * Beta/Vanilla layouts.
 */
public final class LinkenMainMenuScreen extends Screen {
    private static final float OPEN_MS = 460.0f;
    private static final float CLOSE_MS = 220.0f;
    private static final long QUIT_CONFIRM_MS = 3000L;

    private static final String DISCORD_URL = "https://discord.gg/linkenvisuals";
    private static final String TELEGRAM_URL = "https://t.me/linkenvisuals";

    private final Spring singleplayerHover = new Spring(0.0, 280.0, 20.0);
    private final Spring multiplayerHover = new Spring(0.0, 280.0, 20.0);
    private final Spring settingsHover = new Spring(0.0, 280.0, 20.0);
    private final Spring altManagerHover = new Spring(0.0, 280.0, 20.0);
    private final Spring quitHover = new Spring(0.0, 280.0, 20.0);
    private final Spring telegramHover = new Spring(0.0, 280.0, 20.0);
    private final Spring discordHover = new Spring(0.0, 280.0, 20.0);
    private final Spring accountHover = new Spring(0.0, 280.0, 20.0);

    private LinkenMenuLayout.Metrics layout;
    private int layoutWidth = -1;
    private int layoutHeight = -1;

    private long openedAt;
    private long lastFrame = System.nanoTime();
    private long closingAt;
    private long quitArmedUntil;
    private boolean closing;
    private boolean openSoundPlayed;
    private float parallaxX;
    private float parallaxY;
    private Runnable pendingAction;

    private Identifier skin;
    private Supplier<SkinTextures> skinLookup;

    public LinkenMainMenuScreen() {
        super(Text.empty());
    }

    @Override
    protected void init() {
        Pipelines.warmUp();
        LinkenFonts.body();
        MediaWatcher.ensureRunning();
        openedAt = Util.getMeasuringTimeMs();
        lastFrame = System.nanoTime();
        closing = false;
        closingAt = 0L;
        quitArmedUntil = 0L;
        pendingAction = null;
        openSoundPlayed = false;
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
        float visibility = visibility(millis);

        if (closing && millis - closingAt >= CLOSE_MS) {
            Runnable action = pendingAction;
            pendingAction = null;
            if (action != null) {
                action.run();
            }
            return;
        }

        float virtualMouseX = (float) mouseX / metrics.scale();
        float virtualMouseY = (float) mouseY / metrics.scale();
        updateParallax(virtualMouseX, virtualMouseY, metrics);

        // Background geometry is expressed on the virtual 1920x1080 canvas, so
        // it has to share the same design-unit transform as the rest of the UI.
        context.getMatrices().pushMatrix();
        context.getMatrices().scale(metrics.scale(), metrics.scale());
        LinkenMenuChrome.drawBackground(context, metrics, parallaxX, parallaxY, 1.0f);
        context.getMatrices().popMatrix();

        if (!openSoundPlayed && client != null) {
            client.getSoundManager().play(PositionedSoundInstance.ui(LinkenVisuals.OPEN_SOUND, 0.9f, 1.0f));
            openSoundPlayed = true;
        }

        context.getMatrices().pushMatrix();
        context.getMatrices().scale(metrics.scale(), metrics.scale());
        float fade = Math.max(0.0f, Math.min(1.0f, visibility));
        context.getMatrices().translate(0.0f, (1.0f - fade) * 18.0f);

        LinkenMenuChrome.drawWordmark(context, fade);
        LinkenMenuChrome.drawClock(context, metrics, fade);
        renderWelcome(context, metrics, fade);
        renderAccount(context, metrics, virtualMouseX, virtualMouseY, deltaSeconds, fade);

        boolean interactive = !closing;
        float singleHover = step(singleplayerHover, interactive && metrics.singleplayer().contains(virtualMouseX, virtualMouseY), deltaSeconds);
        float multiHover = step(multiplayerHover, interactive && metrics.multiplayer().contains(virtualMouseX, virtualMouseY), deltaSeconds);
        float settingsHoverValue = step(settingsHover, interactive && metrics.settings().contains(virtualMouseX, virtualMouseY), deltaSeconds);
        float altHover = step(altManagerHover, interactive && metrics.altManager().contains(virtualMouseX, virtualMouseY), deltaSeconds);
        float quitHoverValue = step(quitHover, interactive && metrics.quit().contains(virtualMouseX, virtualMouseY), deltaSeconds);

        LinkenMenuChrome.drawButton(context, metrics.singleplayer(), true, LinkenMenuChrome.ICON_PERSON,
            I18n.get("linkenvisuals.menu.singleplayer"), singleHover, false, false, fade, metrics.scale());
        LinkenMenuChrome.drawButton(context, metrics.multiplayer(), false, LinkenMenuChrome.ICON_PEOPLE,
            I18n.get("linkenvisuals.menu.multiplayer"), multiHover, false, false, fade, metrics.scale());
        LinkenMenuChrome.drawButton(context, metrics.settings(), false, LinkenMenuChrome.ICON_GEAR,
            I18n.get("linkenvisuals.menu.settings"), settingsHoverValue, false, false, fade, metrics.scale());
        LinkenMenuChrome.drawButton(context, metrics.altManager(), false, LinkenMenuChrome.ICON_PERSON,
            I18n.get("linkenvisuals.menu.alt_manager"), altHover, false, false, fade, metrics.scale());

        boolean quitArmed = millis < quitArmedUntil;
        LinkenMenuChrome.drawButton(context, metrics.quit(), false, LinkenMenuChrome.ICON_POWER,
            I18n.get(quitArmed ? "linkenvisuals.linkmenu.quit_confirm" : "linkenvisuals.linkmenu.quit_short"),
            quitHoverValue, quitArmed, true, fade, metrics.scale());

        renderSocials(context, metrics, virtualMouseX, virtualMouseY, deltaSeconds, interactive, fade);
        LinkenMenuChrome.drawMusic(context, metrics.music(), MediaWatcher.current(), fade, metrics.scale());
        LinkenMenuChrome.drawFooter(context, metrics, fade);

        context.getMatrices().popMatrix();
    }

    private void renderWelcome(DrawContext context, LinkenMenuLayout.Metrics metrics, float fade) {
        String greeting = I18n.get(LinkenMenuChrome.greetingKey(LocalTime.now().getHour()));
        LinkenMenuChrome.drawSpaced(context, LinkenFonts.headingLight(), greeting,
            metrics.eyebrow().x(), metrics.eyebrow().y(), 10.0f, 2.2f,
            LinkenMenuChrome.rgba(LinkenMenuChrome.ACCENT, fade));

        String welcome = I18n.get("linkenvisuals.linkmenu.welcome");
        float welcomeSize = LinkenMenuChrome.fitSize(LinkenFonts.bodyMedium(), welcome,
            metrics.headline().width(), LinkenMenuLayout.HEADLINE_SIZE, 26.0f);
        LinkenMenuChrome.drawText(context, LinkenFonts.bodyMedium(), welcome,
            metrics.headline().x(), metrics.headline().y(), welcomeSize,
            LinkenMenuChrome.rgba(LinkenMenuChrome.TEXT, fade));
        String name = LinkenMenuChrome.ellipsize(LinkenFonts.bodyMedium(), LinkenMenuChrome.username(),
            metrics.headline().width(), LinkenMenuLayout.HEADLINE_SIZE);
        LinkenMenuChrome.drawText(context, LinkenFonts.bodyMedium(), name,
            metrics.headline().x(), metrics.headline().y() + LinkenMenuLayout.HEADLINE_LINE,
            LinkenMenuLayout.HEADLINE_SIZE, LinkenMenuChrome.rgba(LinkenMenuChrome.TEXT, fade));

        String subtitle = LinkenMenuChrome.ellipsize(LinkenFonts.body(),
            I18n.get("linkenvisuals.linkmenu.subtitle"), metrics.subtitle().width(), 14.0f);
        LinkenMenuChrome.drawText(context, LinkenFonts.body(), subtitle,
            metrics.subtitle().x(), metrics.subtitle().y(), 14.0f,
            LinkenMenuChrome.rgba(LinkenMenuChrome.MUTED2, fade));
    }

    private void renderAccount(DrawContext context, LinkenMenuLayout.Metrics metrics, float mouseX, float mouseY,
                               double deltaSeconds, float fade) {
        LinkenMenuLayout.Rect rect = metrics.account();
        float hover = step(accountHover, !closing && rect.contains(mouseX, mouseY), deltaSeconds);

        LinkenMenuChrome.roundedFill(context, rect.x(), rect.y(), rect.width(), rect.height(), 8.0f,
            LinkenMenuChrome.rgba(0x181E1B, 217, fade), metrics.scale());
        context.fill(Math.round(rect.x() + 8.0f), Math.round(rect.y() + 1.0f),
            Math.round(rect.right() - 8.0f), Math.round(rect.y() + 2.0f),
            LinkenMenuChrome.rgba(0xFFFFFF, 13, fade));
        LinkenMenuChrome.roundedOutline(context, rect.x(), rect.y(), rect.width(), rect.height(), 8.0f, 1.0f,
            LinkenMenuChrome.rgba(0xFFFFFF, Math.round(23.0f + 38.0f * hover), fade), metrics.scale());

        float avatarX = rect.x() + 8.0f;
        float avatarY = rect.y() + 8.0f;
        float avatarSize = 40.0f;
        LinkenMenuChrome.roundedFill(context, avatarX, avatarY, avatarSize, avatarSize, 6.0f,
            LinkenMenuChrome.rgba(0x101613, 255, fade), metrics.scale());
        Identifier texture = skinTexture();
        if (texture != null) {
            int color = LinkenMenuChrome.rgba(0xFFFFFF, fade);
            context.drawTexture(RenderPipelines.GUI_TEXTURED, texture,
                Math.round(avatarX + 4.0f), Math.round(avatarY + 4.0f), 8.0f, 8.0f,
                32, 32, 8, 8, 64, 64, color);
            context.drawTexture(RenderPipelines.GUI_TEXTURED, texture,
                Math.round(avatarX + 4.0f), Math.round(avatarY + 4.0f), 40.0f, 8.0f,
                32, 32, 8, 8, 64, 64, color);
        }

        float textX = avatarX + avatarSize + 10.0f;
        String switchLabel = I18n.get("linkenvisuals.linkmenu.account.switch");
        float switchWidth = LinkenMenuChrome.textWidth(LinkenFonts.body(), switchLabel, 10.0f);
        float switchX = rect.right() - 14.0f - switchWidth;
        float nameWidth = Math.max(20.0f, switchX - textX - 8.0f);

        String meta = I18n.get("linkenvisuals.linkmenu.account.meta", LinkenMenuChrome.licenseWord());
        LinkenMenuChrome.drawSpaced(context, LinkenFonts.body(), meta, textX, rect.y() + 11.0f,
            8.5f, 1.2f, LinkenMenuChrome.rgba(LinkenMenuChrome.MUTED2, fade));
        String name = LinkenMenuChrome.ellipsize(LinkenFonts.heading(), LinkenMenuChrome.username(),
            nameWidth, 13.0f);
        LinkenMenuChrome.drawText(context, LinkenFonts.heading(), name,
            textX, rect.y() + 27.0f, 13.0f, LinkenMenuChrome.rgba(LinkenMenuChrome.TEXT, fade));

        LinkenMenuChrome.drawText(context, LinkenFonts.body(), switchLabel, switchX, rect.y() + 31.0f,
            10.0f, LinkenMenuChrome.rgba(LinkenMenuChrome.mix(LinkenMenuChrome.MUTED,
                LinkenMenuChrome.TEXT, hover), fade));
        if (hover > 0.02f) {
            context.fill(Math.round(switchX), Math.round(rect.y() + 43.0f),
                Math.round(switchX + switchWidth), Math.round(rect.y() + 44.0f),
                LinkenMenuChrome.rgba(LinkenMenuChrome.ACCENT, Math.round(180.0f * hover), fade));
        }
    }

    private void renderSocials(DrawContext context, LinkenMenuLayout.Metrics metrics, float mouseX, float mouseY,
                               double deltaSeconds, boolean interactive, float fade) {
        LinkenMenuLayout.Rect socials = metrics.socials();
        LinkenMenuLayout.Rect telegram = new LinkenMenuLayout.Rect(
            socials.x(), socials.y(), LinkenMenuLayout.SOCIAL_SIZE, LinkenMenuLayout.SOCIAL_SIZE);
        LinkenMenuLayout.Rect discord = new LinkenMenuLayout.Rect(
            socials.x() + LinkenMenuLayout.SOCIAL_SIZE + LinkenMenuLayout.SOCIAL_GAP,
            socials.y(), LinkenMenuLayout.SOCIAL_SIZE, LinkenMenuLayout.SOCIAL_SIZE);

        float telegramValue = step(telegramHover, interactive && telegram.contains(mouseX, mouseY), deltaSeconds);
        float discordValue = step(discordHover, interactive && discord.contains(mouseX, mouseY), deltaSeconds);

        LinkenMenuChrome.drawSocialTile(context, telegram, LinkenMenuChrome.ICON_TELEGRAM,
            telegramValue, fade, metrics.scale());
        LinkenMenuChrome.drawSocialTile(context, discord, LinkenMenuChrome.ICON_DISCORD,
            discordValue, fade, metrics.scale());
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

    private float visibility(long millis) {
        if (closing) {
            float progress = Math.min((millis - closingAt) / CLOSE_MS, 1.0f);
            return 1.0f - progress;
        }
        float progress = Math.min((millis - openedAt) / OPEN_MS, 1.0f);
        float inverse = 1.0f - progress;
        return 1.0f - inverse * inverse * inverse;
    }

    private void navigate(Runnable action) {
        if (closing) {
            return;
        }
        closing = true;
        closingAt = Util.getMeasuringTimeMs();
        pendingAction = action;
    }

    private LinkenMenuLayout.Metrics layout() {
        if (layout == null || layoutWidth != width || layoutHeight != height) {
            layout = LinkenMenuLayout.compute(width, height);
            layoutWidth = width;
            layoutHeight = height;
        }
        return layout;
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubleClick) {
        if (closing || click.button() != 0) {
            return super.mouseClicked(click, doubleClick);
        }
        LinkenMenuLayout.Metrics metrics = layout();
        float mouseX = (float) click.x() / metrics.scale();
        float mouseY = (float) click.y() / metrics.scale();
        long now = Util.getMeasuringTimeMs();

        if (metrics.quit().contains(mouseX, mouseY)) {
            if (now < quitArmedUntil) {
                playClick();
                if (client != null) {
                    client.scheduleStop();
                }
            } else {
                playClick();
                quitArmedUntil = now + QUIT_CONFIRM_MS;
            }
            return true;
        }
        if (metrics.singleplayer().contains(mouseX, mouseY)) {
            playClick();
            navigate(() -> {
                if (client != null) {
                    client.setScreen(new SelectWorldScreen(this));
                }
            });
            return true;
        }
        if (metrics.multiplayer().contains(mouseX, mouseY)) {
            playClick();
            navigate(() -> {
                if (client != null) {
                    // M1 deviation: the LINKEN multiplayer screen arrives in M4.
                    client.setScreen(new MultiplayerScreen(this));
                }
            });
            return true;
        }
        if (metrics.settings().contains(mouseX, mouseY)) {
            playClick();
            navigate(() -> {
                if (client != null) {
                    client.setScreen(new OptionsScreen(this, client.options));
                }
            });
            return true;
        }
        if (metrics.altManager().contains(mouseX, mouseY) || metrics.account().contains(mouseX, mouseY)) {
            playClick();
            navigate(() -> {
                if (client != null) {
                    client.setScreen(new LinkenAltManagerScreen(this));
                }
            });
            return true;
        }

        LinkenMenuLayout.Rect socials = metrics.socials();
        LinkenMenuLayout.Rect telegram = new LinkenMenuLayout.Rect(
            socials.x(), socials.y(), LinkenMenuLayout.SOCIAL_SIZE, LinkenMenuLayout.SOCIAL_SIZE);
        LinkenMenuLayout.Rect discord = new LinkenMenuLayout.Rect(
            socials.x() + LinkenMenuLayout.SOCIAL_SIZE + LinkenMenuLayout.SOCIAL_GAP,
            socials.y(), LinkenMenuLayout.SOCIAL_SIZE, LinkenMenuLayout.SOCIAL_SIZE);
        if (telegram.contains(mouseX, mouseY)) {
            playClick();
            LinkenMenuChrome.openUrl(TELEGRAM_URL);
            return true;
        }
        if (discord.contains(mouseX, mouseY)) {
            playClick();
            LinkenMenuChrome.openUrl(DISCORD_URL);
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

    private void playClick() {
        if (client != null) {
            client.getSoundManager().play(PositionedSoundInstance.ui(SoundEvents.UI_BUTTON_CLICK.value(), 1.0f, 1.0f));
        }
    }

    private Identifier skinTexture() {
        if (skin != null) {
            return skin;
        }
        if (client == null || client.getGameProfile() == null) {
            return null;
        }
        SkinTextures textures = null;
        try {
            if (skinLookup == null) {
                skinLookup = client.getSkinProvider().supplySkinTextures(client.getGameProfile(), false);
            }
            textures = skinLookup.get();
        } catch (RuntimeException unavailable) {
            LinkenVisuals.LOGGER.debug("LINKEN skin lookup not yet available: {}", unavailable.getMessage());
            skinLookup = null;
        }
        if (textures == null) {
            try {
                skin = DefaultSkinHelper.getSkinTextures(client.getGameProfile()).body().texturePath();
            } catch (RuntimeException unavailable) {
                LinkenVisuals.LOGGER.debug("LINKEN default skin unavailable: {}", unavailable.getMessage());
            }
        } else {
            skin = textures.body().texturePath();
        }
        return skin;
    }
}
