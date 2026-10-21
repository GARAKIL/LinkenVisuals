package dev.linkenvisuals.clickgui.link;

import dev.linkenvisuals.LinkenVisuals;
import dev.linkenvisuals.clickgui.Category;
import dev.linkenvisuals.clickgui.ConfigManager;
import dev.linkenvisuals.clickgui.KeyNames;
import dev.linkenvisuals.clickgui.ModuleInfo;
import dev.linkenvisuals.clickgui.ModuleRegistry;
import dev.linkenvisuals.clickgui.link.LinkGuiLayout.Metrics;
import dev.linkenvisuals.clickgui.link.LinkGuiLayout.Rect;
import dev.linkenvisuals.clickgui.setting.BooleanSetting;
import dev.linkenvisuals.clickgui.setting.ColorSetting;
import dev.linkenvisuals.clickgui.setting.KeybindSetting;
import dev.linkenvisuals.clickgui.setting.ModeSetting;
import dev.linkenvisuals.clickgui.setting.Setting;
import dev.linkenvisuals.clickgui.setting.SliderSetting;
import dev.linkenvisuals.clickgui.setting.StringSetting;
import dev.linkenvisuals.i18n.I18n;
import dev.linkenvisuals.media.MediaWatcher;
import dev.linkenvisuals.render.CirclePipeline;
import dev.linkenvisuals.render.ColorFieldPipeline;
import dev.linkenvisuals.render.GlowPipeline;
import dev.linkenvisuals.render.GlyphAtlas;
import dev.linkenvisuals.render.IconPipeline;
import dev.linkenvisuals.render.MsdfText;
import dev.linkenvisuals.render.PanelPipeline;
import dev.linkenvisuals.render.RoundedRectPipeline;
import dev.linkenvisuals.ui.Spring;
import dev.linkenvisuals.ui.linkmenu.LinkenFonts;
import dev.linkenvisuals.ui.linkmenu.LinkenMenuChrome;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.entity.player.SkinTextures;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.joml.Matrix3x2fStack;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

/**
 * LINKEN ClickGUI shell (M3/M4): category rail, two-column module grid, settings
 * inspector and config profiles. It consumes the same ModuleRegistry/Setting
 * model as the Classic and Beta shells, so modules and configs stay in sync.
 */
public final class LinkGuiScreen extends Screen {
    private static final Identifier BACKGROUND = LinkenVisuals.id("textures/linkmenu/bg-tower.png");
    private static final float BACKGROUND_WIDTH = 1270.0f;
    private static final float BACKGROUND_HEIGHT = 999.0f;
    private final Screen returnScreen;

    private static Category selected = Category.VISUAL;
    private static ModuleInfo focused;
    private static final StringBuilder search = new StringBuilder();
    private static final StringBuilder configName = new StringBuilder();
    private static float scrollTarget;
    private static float panelScrollTarget;
    private static float configScrollTarget;
    private static boolean configInputFocused;

    private final Spring openSpring = new Spring(0.0, 900.0, 42.0);
    private final Spring scrollSpring = new Spring(0.0, 1100.0, 60.0);
    private final Spring panelScrollSpring = new Spring(0.0, 1100.0, 60.0);
    private final Spring configScrollSpring = new Spring(0.0, 1100.0, 60.0);
    private final Map<Category, Spring> navHover = new EnumMap<>(Category.class);
    private final Map<ModuleInfo, Spring> cardHover = new HashMap<>();
    private final Spring closeHover = new Spring(0.0, 900.0, 50.0);
    private final LinkGuiCursorTrail cursorTrail = new LinkGuiCursorTrail();

    private long lastFrame = System.nanoTime();
    private boolean closing;
    private String status;
    private long statusUntil;
    private int statusColor = LinkGuiTheme.ACCENT;
    private SkinTextures skin;

    private float contentHeight;
    private float contentView;
    private float panelContentHeight;
    private float panelView;
    private float configContentHeight;
    private float configView;
    private float searchCaret;
    private boolean searchFocused;
    private long lastTyped;

    private final List<CardHit> cardHits = new ArrayList<>();
    private final List<SettingHit> interfaceHits = new ArrayList<>();
    private final List<SettingHit> settingHits = new ArrayList<>();
    private final List<NavHit> navHits = new ArrayList<>();
    private final List<ConfigHit> configHits = new ArrayList<>();
    private Rect closeHit;
    private final List<ThemeHit> themeHits = new ArrayList<>();
    private Rect searchHit;
    private Rect headerToggleHit;
    private Rect moduleBindHit;
    private boolean settingsOpen;
    private boolean settingsDragging;
    private float settingsX = Float.NaN;
    private float settingsY = Float.NaN;
    private float settingsDragX;
    private float settingsDragY;
    private Rect settingsCloseHit;
    private Rect settingsHeaderHit;
    private Rect configInputHit;
    private Rect configCreateHit;
    private Rect configFolderHit;
    private Rect configRefreshHit;
    private Rect configAutoHit;

    private SliderSetting draggingSlider;
    private float dragTrackX;
    private float dragTrackWidth;
    private ModeSetting openMode;
    private Rect openModeBox;
    private final List<OptionHit> optionHits = new ArrayList<>();
    private ColorSetting openColor;
    private boolean draggingColor;
    private boolean draggingHue;
    private float pickerX;
    private float pickerY;
    private float pickerFieldSize;

    public LinkGuiScreen() {
        this(null);
    }

    public LinkGuiScreen(Screen returnScreen) {
        super(Text.empty());
        this.returnScreen = returnScreen;
        ensureState();
    }

    private static void ensureState() {
        if (focused != null) {
            return;
        }
        List<ModuleInfo> modules = ModuleRegistry.byCategory(selected);
        focused = modules.isEmpty() ? null : modules.get(0);
    }

    @Override
    protected void init() {
        ModuleRegistry.syncFromMenuConfig();
        LinkGuiTheme.apply(LinkGuiConfig.theme());
        for (Category category : Category.values()) {
            navHover.computeIfAbsent(category, key -> new Spring(0.0, 900.0, 48.0));
        }
        ensureState();
        skin = loadSkin();
        cursorTrail.clear();
        closing = false;
        lastFrame = System.nanoTime();
        if (client != null) {
            client.getSoundManager().play(PositionedSoundInstance.ui(LinkenVisuals.OPEN_SOUND, 1.0f, 0.86f));
        }
    }

    private SkinTextures loadSkin() {
        if (client == null || client.getGameProfile() == null) {
            return null;
        }
        try {
            Supplier<SkinTextures> lookup = client.getSkinProvider()
                .supplySkinTextures(client.getGameProfile(), false);
            SkinTextures textures = lookup.get();
            if (textures != null) {
                return textures;
            }
        } catch (RuntimeException unavailable) {
            LinkenVisuals.LOGGER.debug("LINKEN ClickGUI skin lookup failed: {}", unavailable.getMessage());
        }
        try {
            return DefaultSkinHelper.getSkinTextures(client.getGameProfile());
        } catch (RuntimeException unavailable) {
            LinkenVisuals.LOGGER.debug("LINKEN ClickGUI default skin failed: {}", unavailable.getMessage());
            return null;
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void close() {
        if (closing) {
            return;
        }
        if (ModuleRegistry.configAutoSave() != null && ModuleRegistry.configAutoSave().value()) {
            ConfigManager.save();
        }
        if (client != null) {
            client.getSoundManager().play(PositionedSoundInstance.ui(LinkenVisuals.BACK_SOUND, 1.0f, 0.9f));
        }
        closing = true;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        long now = System.nanoTime();
        float deltaSeconds = Math.min((now - lastFrame) / 1_000_000_000.0f, 0.05f);
        lastFrame = now;

        Metrics metrics = LinkGuiLayout.compute(width, height);
        openSpring.step(closing ? 0.0 : 1.0, deltaSeconds);
        float openT = (float) openSpring.get();
        if (closing && openT <= 0.01f) {
            if (client != null) {
                client.setScreen(returnScreen);
            }
            return;
        }

        float mx = (float) mouseX / metrics.scale();
        float my = (float) mouseY / metrics.scale();

        context.fill(0, 0, width, height, 0xFF050706);
        context.getMatrices().pushMatrix();
        context.getMatrices().scale(metrics.scale(), metrics.scale());
        context.getMatrices().translate(0.0f, (1.0f - openT) * 18.0f);

        drawBackdrop(context, metrics, openT);
        drawHeader(context, metrics, mx, my, openT, deltaSeconds);
        drawNav(context, metrics, mx, my, openT, deltaSeconds);
        if (selected == Category.CONFIG) {
            drawConfigPanel(context, metrics, mx, my, openT, deltaSeconds);
        } else {
            drawModulePanel(context, metrics, mx, my, openT, deltaSeconds);
            drawSettingsPanel(context, metrics, mx, my, openT, deltaSeconds);
        }
        drawPopups(context, metrics, mx, my, openT);
        drawFooter(context, metrics, openT);
        drawInterfacePanel(context, metrics, mx, my, openT, deltaSeconds);
        drawProfilePanel(context, metrics, openT);
        if (settingsOpen && focused != null && focused != clickGuiModule()) {
            drawSettingsPanel(context, metrics, mx, my, openT, deltaSeconds);
        }
        drawThemePanel(context, metrics, mx, my, openT);
        cursorTrail.update(mx, my);
        cursorTrail.render(context);
        context.getMatrices().popMatrix();
    }

    // ------------------------------------------------------------------ backdrop

    private void drawBackdrop(DrawContext context, Metrics metrics, float openT) {
        if (LinkGuiTheme.isLight()) {
            context.fillGradient(0, 0, Math.round(metrics.width()), Math.round(metrics.height()),
                LinkenMenuChrome.rgba(LinkGuiTheme.BACKDROP_TOP, openT),
                LinkenMenuChrome.rgba(LinkGuiTheme.BACKDROP_BOTTOM, openT));
            GlowPipeline.radial(context, metrics.width() * 0.50f, metrics.height() * 0.42f,
                560.0f, LinkenMenuChrome.rgba(LinkGuiTheme.ACCENT, 8, openT));
            return;
        }
        float cover = Math.max(metrics.width() / BACKGROUND_WIDTH, metrics.height() / BACKGROUND_HEIGHT) * 1.05f;
        float drawWidth = BACKGROUND_WIDTH * cover;
        float drawHeight = BACKGROUND_HEIGHT * cover;
        float x = (metrics.width() - drawWidth) * 0.5f;
        float y = (metrics.height() - drawHeight) * 0.5f;
        context.drawTexture(RenderPipelines.GUI_TEXTURED, BACKGROUND,
            Math.round(x), Math.round(y), 0.0f, 0.0f,
            Math.round(drawWidth), Math.round(drawHeight),
            Math.round(BACKGROUND_WIDTH), Math.round(BACKGROUND_HEIGHT),
            Math.round(BACKGROUND_WIDTH), Math.round(BACKGROUND_HEIGHT),
            LinkenMenuChrome.rgba(0xFFFFFF, 58, openT));
        context.fillGradient(0, 0, Math.round(metrics.width()), Math.round(metrics.height()),
            LinkenMenuChrome.rgba(0x030504, 232, openT), LinkenMenuChrome.rgba(0x020403, 247, openT));
        GlowPipeline.radial(context, metrics.width() * 0.50f, metrics.height() * 0.42f,
            560.0f, LinkenMenuChrome.rgba(LinkGuiTheme.ACCENT, 9, openT));
        GlowPipeline.radial(context, metrics.width() * 0.16f, metrics.height() * 0.75f,
            340.0f, LinkenMenuChrome.rgba(LinkGuiTheme.ACCENT, 5, openT));
    }

    // ------------------------------------------------------------------ header

    private void drawHeader(DrawContext context, Metrics metrics, float mx, float my,
                            float openT, float deltaSeconds) {
        // Donor-style brand in the top-left corner, outside the floating main
        // window. The module window owns its own player/search header.
        GlyphAtlas wordFont = LinkenFonts.bodyMedium();
        String word = "linken";
        float wordSize = 31.0f;
        float wordWidth = LinkenMenuChrome.textWidth(wordFont, word, wordSize);
        LinkenMenuChrome.drawText(context, wordFont, word, LinkGuiLayout.MARGIN, 38.0f, wordSize,
            LinkenMenuChrome.rgba(LinkGuiTheme.TEXT, openT));
        LinkenMenuChrome.drawText(context, wordFont, ".", LinkGuiLayout.MARGIN + wordWidth, 38.0f, wordSize,
            LinkenMenuChrome.rgba(LinkGuiTheme.ACCENT, openT));
        LinkenMenuChrome.drawSpaced(context, LinkenFonts.headingLight(), "CLICKGUI",
            LinkGuiLayout.MARGIN, 78.0f, 9.5f, 2.7f, LinkenMenuChrome.rgba(LinkGuiTheme.MUTED, openT));

        Rect main = metrics.center();
        String user = client != null && client.getSession() != null ? client.getSession().getUsername() : "player";
        float headSize = 38.0f;
        float headX = main.x() + 22.0f;
        float headY = main.y() + 14.0f;
        LinkenMenuChrome.roundedFill(context, headX, headY, headSize, headSize, 8.0f,
            LinkenMenuChrome.rgba(LinkGuiTheme.ROW, 230, openT), metrics.scale());
        LinkenMenuChrome.roundedOutline(context, headX, headY, headSize, headSize, 8.0f, 1.0f,
            LinkenMenuChrome.rgba(LinkGuiTheme.ACCENT, 70, openT), metrics.scale());
        if (skin != null) {
            PlayerSkinDrawer.draw(context, skin, Math.round(headX + 3.0f), Math.round(headY + 3.0f), 32);
        }
        LinkenMenuChrome.drawText(context, LinkenFonts.bodyMedium(),
            LinkenMenuChrome.ellipsize(LinkenFonts.bodyMedium(), user, 220.0f, 17.0f),
            main.x() + 70.0f, main.y() + 18.0f, 17.0f,
            LinkenMenuChrome.rgba(LinkGuiTheme.TEXT, openT));
        int count = selected == Category.CONFIG ? ConfigManager.getProfiles().size()
            : ModuleRegistry.byCategory(selected).size();
        LinkenMenuChrome.drawSpaced(context, LinkenFonts.mono(),
            selected.label().toUpperCase(Locale.ROOT) + "  /  " + String.format(Locale.ROOT, "%02d", count)
                + (count == 1 ? " MODULE" : " MODULES"),
            main.x() + 70.0f, main.y() + 40.0f, 7.5f, 1.4f,
            LinkenMenuChrome.rgba(LinkGuiTheme.MUTED3, openT));

        Rect close = metrics.close();
        boolean closeOver = close.contains(mx, my);
        closeHover.step(closeOver ? 1.0 : 0.0, deltaSeconds);
        float hover = (float) closeHover.get();
        LinkenMenuChrome.drawSecondary(context, close.x(), close.y(), close.width(), close.height(),
            9.0f, hover, false, openT, metrics.scale());
        float cx = close.x() + close.width() * 0.5f;
        float cy = close.y() + close.height() * 0.5f;
        int color = LinkenMenuChrome.rgba(LinkenMenuChrome.mix(LinkGuiTheme.ON_DARK, 0xFFFFFF, hover), openT);
        Matrix3x2fStack matrices = context.getMatrices();
        matrices.pushMatrix();
        matrices.translate(cx, cy);
        matrices.rotate((float) Math.toRadians(45.0));
        PanelPipeline.draw(context, -7.0f, -0.7f, 14.0f, 1.4f, color);
        matrices.rotate((float) Math.toRadians(90.0));
        PanelPipeline.draw(context, -7.0f, -0.7f, 14.0f, 1.4f, color);
        matrices.popMatrix();
        closeHit = close;
    }

    // ------------------------------------------------------------------ navigation rail

    private void drawNav(DrawContext context, Metrics metrics, float mx, float my,
                         float openT, float deltaSeconds) {
        Rect nav = metrics.nav();
        LinkenMenuChrome.roundedFill(context, nav.x(), nav.y(), nav.width(), nav.height(), 12.0f,
            LinkenMenuChrome.rgba(LinkGuiTheme.PANEL, 232, openT), metrics.scale());
        LinkenMenuChrome.roundedOutline(context, nav.x(), nav.y(), nav.width(), nav.height(), 12.0f, 1.0f,
            LinkenMenuChrome.rgba(0xFFFFFF, 17, openT), metrics.scale());

        navHits.clear();
        Category[] categories = Category.values();
        for (int i = 0; i < categories.length; i++) {
            Category category = categories[i];
            Rect item = LinkGuiLayout.navItem(metrics, i);
            boolean hovered = item.contains(mx, my);
            Spring spring = navHover.get(category);
            spring.step(hovered ? 1.0 : 0.0, deltaSeconds);
            float hover = (float) spring.get();
            boolean active = category == selected;

            if (active) {
                LinkenMenuChrome.roundedFill(context, item.x(), item.y(), item.width(), item.height(), 9.0f,
                    LinkenMenuChrome.rgba(LinkGuiTheme.ACCENT, 26, openT), metrics.scale());
                LinkenMenuChrome.roundedOutline(context, item.x(), item.y(), item.width(), item.height(), 9.0f, 1.0f,
                    LinkenMenuChrome.rgba(LinkGuiTheme.ACCENT, 92, openT), metrics.scale());
            } else if (hover > 0.02f) {
                LinkenMenuChrome.roundedFill(context, item.x(), item.y(), item.width(), item.height(), 9.0f,
                    LinkenMenuChrome.rgba(0xFFFFFF, Math.round(8 * hover * openT)),
                    metrics.scale());
            }

            float iconSize = 18.0f;
            float iconX = item.x() + (item.width() - iconSize) * 0.5f;
            float iconY = item.y() + 8.0f;
            context.drawTexture(RenderPipelines.GUI_TEXTURED, category.icon(),
                Math.round(iconX), Math.round(iconY), 0.0f, 0.0f, 18, 18, 128, 128, 128, 128,
                LinkenMenuChrome.rgba(LinkenMenuChrome.mix(LinkGuiTheme.MUTED,
                    active ? LinkGuiTheme.ACCENT : LinkGuiTheme.TEXT3, active ? 1.0f : hover * 0.65f), openT));

            String label = I18n.get("gui.category." + category.name().toLowerCase(Locale.ROOT));
            float labelSize = 7.5f;
            LinkenMenuChrome.drawCentered(context, LinkenFonts.headingLight(), label,
                item.x() + item.width() * 0.5f, item.y() + 31.0f, labelSize,
                LinkenMenuChrome.rgba(active ? LinkGuiTheme.TEXT : LinkGuiTheme.TEXT3, openT));
            navHits.add(new NavHit(category, item));
        }
    }

    // ------------------------------------------------------------------ module grid

    private void drawModulePanel(DrawContext context, Metrics metrics, float mx, float my,
                                 float openT, float deltaSeconds) {
        Rect panel = metrics.center();
        LinkenMenuChrome.roundedFill(context, panel.x(), panel.y(), panel.width(), panel.height(), 12.0f,
            LinkenMenuChrome.rgba(LinkGuiTheme.PANEL, 224, openT), metrics.scale());
        LinkenMenuChrome.roundedOutline(context, panel.x(), panel.y(), panel.width(), panel.height(), 12.0f, 1.0f,
            LinkenMenuChrome.rgba(0xFFFFFF, 17, openT), metrics.scale());

        List<ModuleInfo> modules = visibleModules();

        Rect searchRect = metrics.search();
        boolean searchOver = searchRect.contains(mx, my);
        int border = searchFocused ? LinkGuiTheme.ACCENT : 0xFFFFFF;
        int borderAlpha = searchFocused ? 120 : (searchOver ? 52 : 24);
        LinkenMenuChrome.roundedFill(context, searchRect.x(), searchRect.y(), searchRect.width(), searchRect.height(),
            8.0f, LinkenMenuChrome.rgba(LinkGuiTheme.FIELD, 205, openT), metrics.scale());
        LinkenMenuChrome.roundedOutline(context, searchRect.x(), searchRect.y(), searchRect.width(), searchRect.height(),
            8.0f, 1.0f, LinkenMenuChrome.rgba(border, borderAlpha, openT), metrics.scale());
        String query = search.toString();
        String placeholder = I18n.get("gui.search.placeholder");
        float textY = searchRect.y() + 15.0f;
        if (query.isEmpty() && !searchFocused) {
            LinkenMenuChrome.drawText(context, LinkenFonts.body(), placeholder,
                searchRect.x() + 17.0f, textY, 12.0f, LinkenMenuChrome.rgba(LinkGuiTheme.MUTED2, openT));
        } else {
            LinkenMenuChrome.drawText(context, LinkenFonts.body(), query,
                searchRect.x() + 17.0f, textY, 12.0f, LinkenMenuChrome.rgba(LinkGuiTheme.TEXT, openT));
            searchCaret = searchRect.x() + 17.0f + LinkenMenuChrome.textWidth(LinkenFonts.body(), query, 12.0f);
            float blink = (System.currentTimeMillis() - lastTyped) < 700L
                ? 1.0f : 0.35f + 0.65f * (float) Math.abs(Math.sin(System.currentTimeMillis() / 1000.0 * 3.1));
            context.fill(Math.round(searchCaret), Math.round(searchRect.y() + 13.0f),
                Math.round(searchCaret + 1.2f), Math.round(searchRect.bottom() - 13.0f),
                LinkenMenuChrome.rgba(LinkGuiTheme.ACCENT, Math.round(220 * blink * openT)));
        }
        LinkenMenuChrome.drawSpaced(context, LinkenFonts.headingLight(), "CTRL + L/R",
            searchRect.x() + searchRect.width() - 74.0f, searchRect.y() + 19.0f, 7.5f, 1.4f,
            LinkenMenuChrome.rgba(LinkGuiTheme.MUTED3, openT));
        searchHit = searchRect;

        Rect area = metrics.cards();
        float contentHeight = Math.max(0.0f, (float) Math.ceil(modules.size() / (float) LinkGuiLayout.CARD_COLUMNS)
            * (LinkGuiLayout.CARD_HEIGHT + LinkGuiLayout.CARD_GAP) - LinkGuiLayout.CARD_GAP);
        this.contentHeight = contentHeight;
        contentView = area.height();
        scrollTarget = Math.max(0.0f, Math.min(scrollTarget, Math.max(0.0f, contentHeight - area.height())));
        scrollSpring.step(scrollTarget, deltaSeconds);
        float scroll = (float) scrollSpring.get();

        // DrawContext#enableScissor transforms by the current matrix, and the
        // whole screen is already scaled to design units. Passing design-space
        // coordinates here avoids scaling them twice.
        int scissorL = Math.round(area.x());
        int scissorT = Math.round(area.y());
        int scissorR = Math.round(area.right());
        int scissorB = Math.round(area.bottom());
        context.enableScissor(scissorL, scissorT, scissorR, scissorB);
        cardHits.clear();
        if (modules.isEmpty()) {
            LinkenMenuChrome.drawCentered(context, LinkenFonts.body(), I18n.get("gui.empty.category_title"),
                area.x() + area.width() * 0.5f, area.y() + area.height() * 0.42f, 16.0f,
                LinkenMenuChrome.rgba(LinkGuiTheme.MUTED2, openT));
            LinkenMenuChrome.drawCentered(context, LinkenFonts.body(), I18n.get("gui.empty.category_subtitle"),
                area.x() + area.width() * 0.5f, area.y() + area.height() * 0.42f + 28.0f, 11.0f,
                LinkenMenuChrome.rgba(LinkGuiTheme.MUTED3, openT));
        }
        for (int i = 0; i < modules.size(); i++) {
            ModuleInfo module = modules.get(i);
            Rect base = LinkGuiLayout.card(metrics, i);
            Rect card = new Rect(base.x(), base.y() - scroll, base.width(), base.height());
            if (card.bottom() < area.y() || card.y() > area.bottom()) {
                cardHits.add(new CardHit(module, card, false, null));
                continue;
            }
            boolean hovered = card.contains(mx, my);
            Spring spring = cardHover.computeIfAbsent(module, key -> new Spring(0.0, 900.0, 55.0));
            spring.step(hovered ? 1.0 : 0.0, deltaSeconds);
            drawModuleCard(context, metrics, module, card, (float) spring.get(), openT);
            Rect pin = new Rect(card.right() - 34.0f, card.bottom() - 29.0f, 20.0f, 20.0f);
            cardHits.add(new CardHit(module, card, true, pin));
        }
        context.disableScissor();
        scrollbar(context, metrics, area.right() - 4.0f, area.y(), area.height(), contentHeight, scroll, openT);
        drawMediaStrip(context, metrics, panel, openT);
    }

    private void drawMediaStrip(DrawContext context, Metrics metrics, Rect panel, float openT) {
        MediaWatcher.Track track = MediaWatcher.current();
        boolean present = track != null && track.present();
        Rect strip = new Rect(panel.x() + 24.0f, panel.bottom() - 88.0f,
            panel.width() - 48.0f, 70.0f);
        LinkenMenuChrome.roundedFill(context, strip.x(), strip.y(), strip.width(), strip.height(), 9.0f,
            LinkenMenuChrome.rgba(LinkGuiTheme.PANEL_LIGHT, 220, openT), metrics.scale());
        LinkenMenuChrome.roundedOutline(context, strip.x(), strip.y(), strip.width(), strip.height(), 9.0f,
            1.0f, LinkenMenuChrome.rgba(0xFFFFFF, 18, openT), metrics.scale());

        LinkenMenuChrome.drawSpaced(context, LinkenFonts.body(),
            present ? "NOW PLAYING" : "MEDIA",
            strip.x() + 14.0f, strip.y() + 9.0f, 8.0f, 1.4f,
            LinkenMenuChrome.rgba(LinkGuiTheme.MUTED3, openT));
        float barX = strip.x() + 14.0f;
        float barY = strip.y() + 30.0f;
        long now = System.currentTimeMillis();
        float barW = 3.0f;
        for (int i = 0; i < 3; i++) {
            float pulse = present && track.playing()
                ? 0.5f + 0.5f * (float) Math.sin(now / 180.0 + i * 1.7)
                : 0.22f;
            float barH = 8.0f + pulse * 14.0f;
            LinkenMenuChrome.roundedFill(context, barX + i * (barW + 4.0f),
                barY + 8.0f - barH * 0.5f, barW, barH, 1.5f,
                LinkenMenuChrome.rgba(LinkenMenuChrome.mix(LinkGuiTheme.MUTED, LinkGuiTheme.ACCENT, pulse), openT),
                metrics.scale());
        }

        float textX = strip.x() + 48.0f;
        float textW = strip.width() - 62.0f;
        if (present) {
            String title = LinkenMenuChrome.ellipsize(LinkenFonts.heading(), track.title(), textW, 11.0f);
            LinkenMenuChrome.drawText(context, LinkenFonts.heading(), title,
                textX, strip.y() + 14.0f, 11.0f, LinkenMenuChrome.rgba(LinkGuiTheme.TEXT, openT));
            String artist = track.artist().isBlank() ? track.app() : track.artist();
            LinkenMenuChrome.drawText(context, LinkenFonts.body(),
                LinkenMenuChrome.ellipsize(LinkenFonts.body(), artist, textW, 9.0f),
                textX, strip.y() + 31.0f, 9.0f, LinkenMenuChrome.rgba(LinkGuiTheme.MUTED2, openT));
            if (track.length() > 0L) {
                float progress = Math.max(0.0f, Math.min(1.0f,
                    (float) track.position() / (float) track.length()));
                float progressY = strip.bottom() - 13.0f;
                LinkenMenuChrome.roundedFill(context, textX, progressY, textW, 2.0f, 1.0f,
                    LinkenMenuChrome.rgba(0xFFFFFF, 34, openT), metrics.scale());
                if (progress > 0.001f) {
                    LinkenMenuChrome.roundedFill(context, textX, progressY, textW * progress, 2.0f, 1.0f,
                        LinkenMenuChrome.rgba(LinkGuiTheme.ACCENT, openT), metrics.scale());
                }
            }
        } else {
            LinkenMenuChrome.drawText(context, LinkenFonts.body(), "Nothing is playing right now",
                textX, strip.y() + 26.0f, 9.0f, LinkenMenuChrome.rgba(LinkGuiTheme.MUTED2, openT));
        }
    }

    private void drawModuleCard(DrawContext context, Metrics metrics, ModuleInfo module, Rect card,
                                float hover, float openT) {
        boolean active = module == focused;
        module.enableGlow.step(module.enabled() ? 1.0 : 0.0, 0.05);
        float on = (float) module.enableGlow.get();

        int fill = active ? LinkGuiTheme.ACTIVE : LinkGuiTheme.ROW;
        float fillAlpha = 208 + hover * 30;
        LinkenMenuChrome.roundedFill(context, card.x(), card.y(), card.width(), card.height(), 9.0f,
            LinkenMenuChrome.rgba(fill, Math.round(fillAlpha * openT)), metrics.scale());
        if (on > 0.02f) {
            LinkenMenuChrome.roundedFill(context, card.x(), card.y() + 13.0f, 2.5f, card.height() - 26.0f, 1.2f,
                LinkenMenuChrome.rgba(LinkGuiTheme.ACCENT, Math.round(220 * on * openT)), metrics.scale());
        }
        int outline = active ? LinkGuiTheme.ACCENT : 0xFFFFFF;
        int outlineAlpha = active ? 104 : Math.round(17 + hover * 26);
        LinkenMenuChrome.roundedOutline(context, card.x(), card.y(), card.width(), card.height(), 9.0f, 1.0f,
            LinkenMenuChrome.rgba(outline, Math.round(outlineAlpha * openT)), metrics.scale());

        LinkenMenuChrome.roundedFill(context, card.x() + 14.0f, card.y() + 12.0f, 28.0f, 28.0f, 7.0f,
            LinkenMenuChrome.rgba(active ? LinkGuiTheme.ACTIVE : LinkGuiTheme.PANEL_LIGHT, 220, openT), metrics.scale());
        context.drawTexture(RenderPipelines.GUI_TEXTURED, module.category().icon(),
            Math.round(card.x() + 20.0f), Math.round(card.y() + 18.0f), 0.0f, 0.0f,
            16, 16, 128, 128, 128, 128,
            LinkenMenuChrome.rgba(active ? LinkGuiTheme.ACCENT : LinkGuiTheme.MUTED2, openT));

        LinkenMenuChrome.drawText(context, LinkenFonts.headingLight(),
            LinkenMenuChrome.ellipsize(LinkenFonts.headingLight(), module.name(), card.width() - 116.0f, 17.0f),
            card.x() + 50.0f, card.y() + 15.0f, 17.0f,
            LinkenMenuChrome.rgba(active ? LinkGuiTheme.TEXT : LinkGuiTheme.TEXT3, openT));
        String description = LinkenMenuChrome.ellipsize(LinkenFonts.body(), module.description(),
            card.width() - 100.0f, 10.0f);
        LinkenMenuChrome.drawText(context, LinkenFonts.body(), description,
            card.x() + 50.0f, card.y() + 40.0f, 10.0f,
            LinkenMenuChrome.rgba(LinkGuiTheme.MUTED2, openT));

        String key = module.binding() ? "..." : (module.hasKeybind() ? module.keybind() : "UNBOUND");
        float chipW = Math.max(54.0f, LinkenMenuChrome.textWidth(LinkenFonts.mono(), key, 8.0f) + 18.0f);
        LinkenMenuChrome.roundedFill(context, card.x() + 16.0f, card.bottom() - 27.0f, chipW, 16.0f, 4.0f,
            LinkenMenuChrome.rgba(0xFFFFFF, 18, openT), metrics.scale());
        LinkenMenuChrome.drawCentered(context, LinkenFonts.mono(), key,
            card.x() + 16.0f + chipW * 0.5f, card.bottom() - 19.0f, 8.0f,
            LinkenMenuChrome.rgba(module.binding() ? LinkGuiTheme.ACCENT : LinkGuiTheme.MUTED, openT));

        float switchW = 38.0f;
        float switchH = 20.0f;
        float switchX = card.right() - 18.0f - switchW;
        float switchY = card.y() + 16.0f;
        drawSwitch(context, switchX, switchY, switchW, switchH, on, openT, metrics.scale());

        boolean pinned = LinkGuiConfig.pinned(module.name());
        float pinX = card.right() - 24.0f;
        float pinY = card.bottom() - 19.0f;
        CirclePipeline.draw(context, pinX, pinY, 5.2f,
            LinkenMenuChrome.rgba(pinned ? LinkGuiTheme.ACCENT : 0xFFFFFF,
                pinned ? 210 : Math.round(45 + hover * 40), openT));
        CirclePipeline.draw(context, pinX, pinY, 2.6f,
            LinkenMenuChrome.rgba(pinned ? LinkGuiTheme.INK : 0x0A0F0C,
                pinned ? 240 : 110, openT));
    }

    // ------------------------------------------------------------------ settings

    private ModuleInfo clickGuiModule() {
        for (ModuleInfo module : ModuleRegistry.byCategory(Category.VISUAL)) {
            if ("ClickGUI".equalsIgnoreCase(module.name())) {
                return module;
            }
        }
        return null;
    }

    private Rect settingsWindowRect(Metrics metrics) {
        float w = 330.0f;
        float h = 440.0f;
        if (Float.isNaN(settingsX) || Float.isNaN(settingsY)) {
            settingsX = metrics.profile().x() + 40.0f;
            settingsY = metrics.profile().y() - 40.0f;
        }
        settingsX = Math.max(20.0f, Math.min(settingsX, metrics.width() - w - 20.0f));
        settingsY = Math.max(20.0f, Math.min(settingsY, metrics.height() - h - 20.0f));
        return new Rect(settingsX, settingsY, w, h);
    }

    private void openSettingsWindow(ModuleInfo module, Rect card) {
        focused = module;
        settingsOpen = true;
        settingsX = card.x() + 40.0f;
        settingsY = card.y() + 20.0f;
        panelScrollTarget = 0.0f;
    }

    private void drawInterfacePanel(DrawContext context, Metrics metrics, float mx, float my,
                                    float openT, float deltaSeconds) {
        Rect panel = metrics.settings();
        LinkenMenuChrome.roundedFill(context, panel.x(), panel.y(), panel.width(), panel.height(), 12.0f,
            LinkenMenuChrome.rgba(LinkGuiTheme.PANEL, 232, openT), metrics.scale());
        LinkenMenuChrome.roundedOutline(context, panel.x(), panel.y(), panel.width(), panel.height(), 12.0f, 1.0f,
            LinkenMenuChrome.rgba(0xFFFFFF, 17, openT), metrics.scale());
        LinkenMenuChrome.drawSpaced(context, LinkenFonts.headingLight(), "INTERFACE",
            panel.x() + 18.0f, panel.y() + 16.0f, 8.5f, 2.0f,
            LinkenMenuChrome.rgba(LinkGuiTheme.MUTED, openT));
        LinkenMenuChrome.drawText(context, LinkenFonts.body(), "CLICKGUI / UI",
            panel.x() + 18.0f, panel.y() + 34.0f, 11.0f,
            LinkenMenuChrome.rgba(LinkGuiTheme.TEXT2, openT));

        interfaceHits.clear();
        ModuleInfo ui = clickGuiModule();
        if (ui == null) {
            return;
        }
        List<Setting> settings = new ArrayList<>(ui.leftSettings());
        settings.addAll(ui.rightSettings());
        if (ModuleRegistry.isGuiStyleLocked()) {
            settings.removeIf(setting -> "GUI Style".equalsIgnoreCase(setting.name()));
        }
        Rect area = metrics.settingsContent();
        float y = area.y() - 4.0f;
        for (Setting setting : settings) {
            float h = rowHeight(setting);
            if (y + h > area.bottom()) {
                break;
            }
            Rect row = new Rect(area.x(), y, area.width(), h);
            drawSetting(context, metrics, setting, row, mx, my, openT, deltaSeconds);
            interfaceHits.add(new SettingHit(setting, row));
            y += h + 6.0f;
        }
    }

    private void drawProfilePanel(DrawContext context, Metrics metrics, float openT) {
        Rect panel = metrics.profile();
        LinkenMenuChrome.roundedFill(context, panel.x(), panel.y(), panel.width(), panel.height(), 12.0f,
            LinkenMenuChrome.rgba(LinkGuiTheme.PANEL, 232, openT), metrics.scale());
        LinkenMenuChrome.roundedOutline(context, panel.x(), panel.y(), panel.width(), panel.height(), 12.0f, 1.0f,
            LinkenMenuChrome.rgba(0xFFFFFF, 17, openT), metrics.scale());
        LinkenMenuChrome.drawSpaced(context, LinkenFonts.headingLight(), "PROFILE",
            panel.x() + 18.0f, panel.y() + 16.0f, 8.5f, 2.0f,
            LinkenMenuChrome.rgba(LinkGuiTheme.MUTED, openT));
        LinkenMenuChrome.drawText(context, LinkenFonts.body(), ConfigManager.getActiveProfile(),
            panel.x() + 18.0f, panel.y() + 34.0f, 12.0f,
            LinkenMenuChrome.rgba(LinkGuiTheme.ACCENT, openT));
        List<String> profiles = ConfigManager.getProfiles();
        float y = panel.y() + 62.0f;
        int shown = 0;
        for (String profile : profiles) {
            if (shown >= 5) {
                break;
            }
            boolean active = profile.equalsIgnoreCase(ConfigManager.getActiveProfile());
            Rect row = new Rect(panel.x() + 18.0f, y, panel.width() - 36.0f, 36.0f);
            LinkenMenuChrome.roundedFill(context, row.x(), row.y(), row.width(), row.height(), 7.0f,
                LinkenMenuChrome.rgba(active ? LinkGuiTheme.ACTIVE : LinkGuiTheme.ROW, 220, openT),
                metrics.scale());
            LinkenMenuChrome.drawText(context, LinkenFonts.body(), profile,
                row.x() + 12.0f, row.y() + 10.0f, 10.0f,
                LinkenMenuChrome.rgba(active ? LinkGuiTheme.ACCENT : LinkGuiTheme.TEXT3, openT));
            y += 42.0f;
            shown++;
        }
        if (shown == 0) {
            LinkenMenuChrome.drawText(context, LinkenFonts.body(), "NO PROFILES",
                panel.x() + 18.0f, panel.y() + 72.0f, 10.0f,
                LinkenMenuChrome.rgba(LinkGuiTheme.MUTED3, openT));
        }
    }

    private void drawSettingsPanel(DrawContext context, Metrics metrics, float mx, float my,
                                   float openT, float deltaSeconds) {
        if (focused == null || focused == clickGuiModule()) {
            return;
        }
        Rect panel = settingsWindowRect(metrics);
        LinkenMenuChrome.roundedFill(context, panel.x(), panel.y(), panel.width(), panel.height(), 12.0f,
            LinkenMenuChrome.rgba(LinkGuiTheme.PANEL, 240, openT), metrics.scale());
        LinkenMenuChrome.roundedOutline(context, panel.x(), panel.y(), panel.width(), panel.height(), 12.0f, 1.0f,
            LinkenMenuChrome.rgba(LinkGuiTheme.ACCENT, 80, openT), metrics.scale());
        settingsHeaderHit = new Rect(panel.x(), panel.y(), panel.width() - 40.0f, 56.0f);
        settingsCloseHit = new Rect(panel.right() - 36.0f, panel.y() + 10.0f, 26.0f, 26.0f);
        boolean closeHover = settingsCloseHit.contains(mx, my);
        LinkenMenuChrome.roundedFill(context, settingsCloseHit.x(), settingsCloseHit.y(),
            settingsCloseHit.width(), settingsCloseHit.height(), 7.0f,
            LinkenMenuChrome.rgba(closeHover ? 0x2A1A1A : LinkGuiTheme.FIELD, 230, openT), metrics.scale());
        LinkenMenuChrome.drawCentered(context, LinkenFonts.headingLight(), "X",
            settingsCloseHit.x() + settingsCloseHit.width() * 0.5f,
            settingsCloseHit.y() + 8.0f, 9.0f,
            LinkenMenuChrome.rgba(closeHover ? LinkGuiTheme.DANGER : LinkGuiTheme.MUTED2, openT));
        settingHits.clear();
        headerToggleHit = null;
        moduleBindHit = null;
        if (focused == null) {
            LinkenMenuChrome.drawCentered(context, LinkenFonts.body(), "SELECT A MODULE",
                panel.x() + panel.width() * 0.5f, panel.y() + panel.height() * 0.46f, 14.0f,
                LinkenMenuChrome.rgba(LinkGuiTheme.MUTED2, openT));
            return;
        }

        LinkenMenuChrome.drawText(context, LinkenFonts.bodyMedium(),
            LinkenMenuChrome.ellipsize(LinkenFonts.bodyMedium(), focused.name(), panel.width() - 140.0f, 16.0f),
            panel.x() + 18.0f, panel.y() + 20.0f, 16.0f, LinkenMenuChrome.rgba(LinkGuiTheme.TEXT, openT));
        String description = LinkenMenuChrome.ellipsize(LinkenFonts.body(), focused.description(),
            panel.width() - 36.0f, 9.5f);
        LinkenMenuChrome.drawText(context, LinkenFonts.body(), description,
            panel.x() + 18.0f, panel.y() + 46.0f, 9.5f,
            LinkenMenuChrome.rgba(LinkGuiTheme.MUTED2, openT));

        float toggleW = 42.0f;
        float toggleH = 22.0f;
        float toggleX = panel.right() - 92.0f;
        float toggleY = panel.y() + 14.0f;
        focused.enableGlow.step(focused.enabled() ? 1.0 : 0.0, deltaSeconds);
        drawSwitch(context, toggleX, toggleY, toggleW, toggleH, (float) focused.enableGlow.get(), openT, metrics.scale());
        headerToggleHit = new Rect(toggleX - 6.0f, toggleY - 6.0f, toggleW + 12.0f, toggleH + 12.0f);

        float dividerY = panel.y() + 74.0f;
        context.fill(Math.round(panel.x() + 18.0f), Math.round(dividerY), Math.round(panel.right() - 18.0f),
            Math.round(dividerY + 1.0f), LinkenMenuChrome.rgba(0xFFFFFF, 19, openT));

        Rect bindRow = new Rect(panel.x() + 18.0f, dividerY + 10.0f, panel.width() - 36.0f, 46.0f);
        drawBindRow(context, metrics, bindRow, mx, my, openT, deltaSeconds);
        moduleBindHit = bindRow;

        Rect area = new Rect(panel.x() + 18.0f, panel.y() + 150.0f,
            panel.width() - 36.0f, panel.height() - 168.0f);

        List<Setting> settings = new ArrayList<>(focused.leftSettings());
        settings.addAll(focused.rightSettings());
        if (ModuleRegistry.isGuiStyleLocked()) {
            settings.removeIf(setting -> "GUI Style".equalsIgnoreCase(setting.name()));
        }
        float contentHeight = 0.0f;
        for (Setting setting : settings) {
            contentHeight += rowHeight(setting) + 8.0f;
        }
        contentHeight = Math.max(0.0f, contentHeight - 8.0f);
        panelContentHeight = contentHeight;
        panelView = area.height();
        panelScrollTarget = Math.max(0.0f, Math.min(panelScrollTarget,
            Math.max(0.0f, contentHeight - area.height())));
        panelScrollSpring.step(panelScrollTarget, deltaSeconds);
        float scroll = (float) panelScrollSpring.get();

        context.enableScissor(Math.round(area.x()), Math.round(area.y()),
            Math.round(area.right()), Math.round(area.bottom()));
        float y = area.y() - scroll;
        if (settings.isEmpty()) {
            LinkenMenuChrome.drawCentered(context, LinkenFonts.body(), "NO SETTINGS",
                area.x() + area.width() * 0.5f, area.y() + area.height() * 0.4f, 12.0f,
                LinkenMenuChrome.rgba(LinkGuiTheme.MUTED3, openT));
        }
        for (Setting setting : settings) {
            float h = rowHeight(setting);
            Rect row = new Rect(area.x(), y, area.width(), h);
            if (row.bottom() >= area.y() - 10.0f && row.y() <= area.bottom() + 10.0f) {
                drawSetting(context, metrics, setting, row, mx, my, openT, deltaSeconds);
                settingHits.add(new SettingHit(setting, row));
            }
            y += h + 8.0f;
        }
        context.disableScissor();
        scrollbar(context, metrics, area.right() + 4.0f, area.y(), area.height(), contentHeight, scroll, openT);
    }

    private void drawBindRow(DrawContext context, Metrics metrics, Rect row, float mx, float my,
                             float openT, float deltaSeconds) {
        boolean hovered = row.contains(mx, my);
        int fill = hovered ? LinkGuiTheme.HOVER : LinkGuiTheme.ROW;
        LinkenMenuChrome.roundedFill(context, row.x(), row.y(), row.width(), row.height(), 7.0f,
            LinkenMenuChrome.rgba(fill, Math.round(198 * openT)), metrics.scale());
        LinkenMenuChrome.roundedOutline(context, row.x(), row.y(), row.width(), row.height(), 7.0f, 1.0f,
            LinkenMenuChrome.rgba(0xFFFFFF, Math.round((16 + (hovered ? 22 : 0)) * openT)), metrics.scale());
        LinkenMenuChrome.drawText(context, LinkenFonts.body(), "Module keybind",
            row.x() + 16.0f, row.y() + 11.0f, 11.0f, LinkenMenuChrome.rgba(LinkGuiTheme.MUTED2, openT));
        String label = focused.binding() ? "PRESS A KEY..." : (focused.hasKeybind() ? focused.keybind() : "NONE");
        float chipW = Math.max(64.0f, LinkenMenuChrome.textWidth(LinkenFonts.mono(), label, 9.0f) + 20.0f);
        float chipX = row.right() - 16.0f - chipW;
        LinkenMenuChrome.roundedFill(context, chipX, row.y() + 16.0f, chipW, 21.0f, 5.0f,
            LinkenMenuChrome.rgba(0x000000, 92, openT), metrics.scale());
        LinkenMenuChrome.drawCentered(context, LinkenFonts.mono(), label,
            chipX + chipW * 0.5f, row.y() + 26.5f, 9.0f,
            LinkenMenuChrome.rgba(focused.binding() ? LinkGuiTheme.ACCENT : LinkGuiTheme.TEXT3, openT));
    }

    private void drawSetting(DrawContext context, Metrics metrics, Setting setting, Rect row,
                             float mx, float my, float openT, float deltaSeconds) {
        boolean hovered = row.contains(mx, my);
        LinkenMenuChrome.roundedFill(context, row.x(), row.y(), row.width(), row.height(), 7.0f,
            LinkenMenuChrome.rgba(hovered ? LinkGuiTheme.HOVER : LinkGuiTheme.ROW,
                Math.round((198 + (hovered ? 20 : 0)) * openT)), metrics.scale());
        LinkenMenuChrome.roundedOutline(context, row.x(), row.y(), row.width(), row.height(), 7.0f, 1.0f,
            LinkenMenuChrome.rgba(0xFFFFFF, Math.round((14 + (hovered ? 20 : 0)) * openT)), metrics.scale());

        LinkenMenuChrome.drawText(context, LinkenFonts.body(),
            LinkenMenuChrome.ellipsize(LinkenFonts.body(), setting.name(), row.width() - 90.0f, 11.5f),
            row.x() + 16.0f, row.y() + 12.0f, 11.5f,
            LinkenMenuChrome.rgba(LinkGuiTheme.TEXT3, openT));

        if (setting instanceof BooleanSetting bool) {
            bool.glow.step(bool.value() ? 1.0 : 0.0, deltaSeconds);
            float switchW = 38.0f;
            float switchH = 20.0f;
            drawSwitch(context, row.right() - 16.0f - switchW, row.y() + (row.height() - switchH) * 0.5f,
                switchW, switchH, (float) bool.glow.get(), openT, metrics.scale());
        } else if (setting instanceof SliderSetting slider) {
            String value = slider.display();
            LinkenMenuChrome.drawRightText(context, LinkenFonts.mono(), value,
                row.right() - 16.0f, row.y() + 13.0f, 10.0f,
                LinkenMenuChrome.rgba(LinkGuiTheme.ACCENT, openT));
            float trackX = row.x() + 16.0f;
            float trackW = row.width() - 32.0f;
            float trackY = row.bottom() - 23.0f;
            LinkenMenuChrome.roundedFill(context, trackX, trackY, trackW, 3.0f, 1.5f,
                LinkenMenuChrome.rgba(LinkGuiTheme.TRACK, openT), metrics.scale());
            float fill = Math.max(3.0f, trackW * Math.max(0.0f, Math.min(1.0f, slider.fraction())));
            LinkenMenuChrome.roundedFill(context, trackX, trackY, fill, 3.0f, 1.5f,
                LinkenMenuChrome.rgba(LinkGuiTheme.ACCENT, openT), metrics.scale());
            float knobX = trackX + trackW * Math.max(0.0f, Math.min(1.0f, slider.fraction()));
            GlowPipeline.radial(context, knobX, trackY + 1.5f, 10.0f,
                LinkenMenuChrome.rgba(LinkGuiTheme.ACCENT, 42, openT));
            CirclePipeline.draw(context, knobX, trackY + 1.5f, 5.0f,
                LinkenMenuChrome.rgba(0xFFFFFF, openT));
            CirclePipeline.draw(context, knobX, trackY + 1.5f, 3.0f,
                LinkenMenuChrome.rgba(LinkGuiTheme.ACCENT, openT));
        } else if (setting instanceof ModeSetting mode) {
            String current = mode.current();
            float boxW = Math.max(112.0f, LinkenMenuChrome.textWidth(LinkenFonts.body(), current, 10.0f) + 32.0f);
            Rect box = new Rect(row.right() - 16.0f - boxW, row.y() + 10.0f, boxW, 24.0f);
            LinkenMenuChrome.roundedFill(context, box.x(), box.y(), box.width(), box.height(), 5.0f,
                LinkenMenuChrome.rgba(0x000000, 90, openT), metrics.scale());
            LinkenMenuChrome.roundedOutline(context, box.x(), box.y(), box.width(), box.height(), 5.0f, 1.0f,
                LinkenMenuChrome.rgba(LinkGuiTheme.ACCENT, mode.open() ? 100 : 34, openT), metrics.scale());
            LinkenMenuChrome.drawText(context, LinkenFonts.body(), current,
                box.x() + 11.0f, box.y() + 6.0f, 10.0f, LinkenMenuChrome.rgba(LinkGuiTheme.ACCENT, openT));
            drawChevron(context, box.right() - 13.0f, box.y() + box.height() * 0.5f, mode.open(), openT);
            if (mode.open()) {
                openMode = mode;
                openModeBox = box;
            }
            String options = mode.options().size() + " OPTIONS";
            LinkenMenuChrome.drawText(context, LinkenFonts.mono(), options,
                row.x() + 16.0f, row.bottom() - 20.0f, 8.0f,
                LinkenMenuChrome.rgba(LinkGuiTheme.MUTED3, openT));
        } else if (setting instanceof ColorSetting color) {
            float chip = 18.0f;
            float chipX = row.right() - 16.0f - chip;
            float chipY = row.y() + (row.height() - chip) * 0.5f;
            CirclePipeline.draw(context, chipX + chip * 0.5f, chipY + chip * 0.5f, chip * 0.5f,
                LinkenMenuChrome.rgba(0xFFFFFF, 40, openT));
            CirclePipeline.draw(context, chipX + chip * 0.5f, chipY + chip * 0.5f, chip * 0.5f - 1.5f,
                LinkenMenuChrome.rgba(color.rgb(), openT));
            String hex = String.format(Locale.ROOT, "#%06X", color.rgb() & 0xFFFFFF);
            LinkenMenuChrome.drawRightText(context, LinkenFonts.mono(), hex,
                chipX - 10.0f, row.y() + 13.0f, 9.0f,
                LinkenMenuChrome.rgba(LinkGuiTheme.MUTED2, openT));
        } else if (setting instanceof KeybindSetting keybind) {
            String label = keybind.label();
            float chipW = Math.max(64.0f, LinkenMenuChrome.textWidth(LinkenFonts.mono(), label, 9.0f) + 20.0f);
            float chipX = row.right() - 16.0f - chipW;
            float chipY = row.y() + (row.height() - 22.0f) * 0.5f;
            LinkenMenuChrome.roundedFill(context, chipX, chipY, chipW, 22.0f, 5.0f,
                LinkenMenuChrome.rgba(0x000000, 90, openT), metrics.scale());
            LinkenMenuChrome.drawCentered(context, LinkenFonts.mono(), label,
                chipX + chipW * 0.5f, chipY + 11.0f, 9.0f,
                LinkenMenuChrome.rgba(keybind.listening() ? LinkGuiTheme.ACCENT : LinkGuiTheme.TEXT3, openT));
        } else if (setting instanceof StringSetting string) {
            LinkenMenuChrome.drawRightText(context, LinkenFonts.body(), string.value(),
                row.right() - 16.0f, row.y() + 12.0f, 10.0f,
                LinkenMenuChrome.rgba(LinkGuiTheme.MUTED2, openT));
        }

        // Hover highlight edge.
        if (hovered) {
            LinkenMenuChrome.roundedOutline(context, row.x(), row.y(), row.width(), row.height(), 7.0f, 1.0f,
                LinkenMenuChrome.rgba(LinkGuiTheme.ACCENT, 28, openT), metrics.scale());
        }
    }

    // ------------------------------------------------------------------ config profiles

    private void drawConfigPanel(DrawContext context, Metrics metrics, float mx, float my,
                                 float openT, float deltaSeconds) {
        Rect panel = metrics.config();
        LinkenMenuChrome.roundedFill(context, panel.x(), panel.y(), panel.width(), panel.height(), 12.0f,
            LinkenMenuChrome.rgba(LinkGuiTheme.PANEL, 224, openT), metrics.scale());
        LinkenMenuChrome.roundedOutline(context, panel.x(), panel.y(), panel.width(), panel.height(), 12.0f, 1.0f,
            LinkenMenuChrome.rgba(0xFFFFFF, 17, openT), metrics.scale());
        LinkenMenuChrome.drawText(context, LinkenFonts.bodyMedium(), I18n.get("gui.config.title"),
            panel.x() + 28.0f, panel.y() + 28.0f, 27.0f, LinkenMenuChrome.rgba(LinkGuiTheme.TEXT, openT));
        LinkenMenuChrome.drawSpaced(context, LinkenFonts.headingLight(), I18n.get("gui.config.description"),
            panel.x() + 30.0f, panel.y() + 68.0f, 8.5f, 1.4f,
            LinkenMenuChrome.rgba(LinkGuiTheme.MUTED, openT));

        float controlY = panel.y() + 102.0f;
        float left = panel.x() + 28.0f;
        float right = panel.right() - 28.0f;
        Rect input = new Rect(left, controlY, 330.0f, 42.0f);
        boolean inputHover = input.contains(mx, my);
        int inputBorder = configInputFocused ? LinkGuiTheme.ACCENT : 0xFFFFFF;
        LinkenMenuChrome.roundedFill(context, input.x(), input.y(), input.width(), input.height(), 7.0f,
            LinkenMenuChrome.rgba(LinkGuiTheme.FIELD, 205, openT), metrics.scale());
        LinkenMenuChrome.roundedOutline(context, input.x(), input.y(), input.width(), input.height(), 7.0f, 1.0f,
            LinkenMenuChrome.rgba(inputBorder, configInputFocused ? 120 : (inputHover ? 50 : 25), openT),
            metrics.scale());
        String name = configName.toString();
        if (name.isEmpty() && !configInputFocused) {
            LinkenMenuChrome.drawText(context, LinkenFonts.body(), I18n.get("gui.config.input_placeholder"),
                input.x() + 15.0f, input.y() + 12.0f, 11.5f,
                LinkenMenuChrome.rgba(LinkGuiTheme.MUTED2, openT));
        } else {
            LinkenMenuChrome.drawText(context, LinkenFonts.body(), name,
                input.x() + 15.0f, input.y() + 12.0f, 11.5f,
                LinkenMenuChrome.rgba(LinkGuiTheme.TEXT, openT));
            if (configInputFocused) {
                float caret = input.x() + 15.0f + LinkenMenuChrome.textWidth(LinkenFonts.body(), name, 11.5f);
                context.fill(Math.round(caret), Math.round(input.y() + 10.0f),
                    Math.round(caret + 1.2f), Math.round(input.bottom() - 10.0f),
                    LinkenMenuChrome.rgba(LinkGuiTheme.ACCENT, 220, openT));
            }
        }
        configInputHit = input;

        Rect create = new Rect(input.right() + 12.0f, controlY, 132.0f, 42.0f);
        drawPillButton(context, metrics, create, I18n.get("gui.config.create"), mx, my, true, openT);
        configCreateHit = create;
        Rect folder = new Rect(create.right() + 10.0f, controlY, 112.0f, 42.0f);
        drawPillButton(context, metrics, folder, I18n.get("gui.config.folder"), mx, my, false, openT);
        configFolderHit = folder;
        Rect refresh = new Rect(folder.right() + 10.0f, controlY, 112.0f, 42.0f);
        drawPillButton(context, metrics, refresh, I18n.get("gui.config.refresh"), mx, my, false, openT);
        configRefreshHit = refresh;
        boolean auto = ModuleRegistry.configAutoSave() != null && ModuleRegistry.configAutoSave().value();
        float autoWidth = 190.0f;
        Rect autoRect = new Rect(right - autoWidth, controlY, autoWidth, 42.0f);
        drawPillButton(context, metrics, autoRect,
            I18n.get(auto ? "gui.config.autosave_on" : "gui.config.autosave_off"), mx, my, auto, openT);
        configAutoHit = autoRect;

        String active = I18n.get("gui.config.active_prefix") + ConfigManager.getActiveProfile();
        LinkenMenuChrome.drawText(context, LinkenFonts.body(), active, left, controlY + 60.0f, 10.5f,
            LinkenMenuChrome.rgba(LinkGuiTheme.MUTED2, openT));
        if (status != null && System.currentTimeMillis() < statusUntil) {
            LinkenMenuChrome.drawRightText(context, LinkenFonts.body(), status, right, controlY + 60.0f, 10.5f,
                LinkenMenuChrome.rgba(statusColor, openT));
        }

        Rect list = new Rect(left, controlY + 86.0f, right - left, panel.bottom() - (controlY + 86.0f) - 26.0f);
        List<String> profiles = ConfigManager.getProfiles();
        float rowH = 68.0f;
        float gap = 10.0f;
        float contentHeight = Math.max(0.0f, profiles.size() * (rowH + gap) - gap);
        this.configContentHeight = contentHeight;
        configView = list.height();
        configScrollTarget = Math.max(0.0f, Math.min(configScrollTarget,
            Math.max(0.0f, contentHeight - list.height())));
        configScrollSpring.step(configScrollTarget, deltaSeconds);
        float scroll = (float) configScrollSpring.get();

        context.enableScissor(Math.round(list.x()), Math.round(list.y()),
            Math.round(list.right()), Math.round(list.bottom()));
        configHits.clear();
        if (profiles.isEmpty()) {
            LinkenMenuChrome.drawCentered(context, LinkenFonts.body(), I18n.get("gui.config.empty"),
                list.x() + list.width() * 0.5f, list.y() + 60.0f, 14.0f,
                LinkenMenuChrome.rgba(LinkGuiTheme.MUTED2, openT));
        }
        for (int i = 0; i < profiles.size(); i++) {
            String profile = profiles.get(i);
            float y = list.y() + i * (rowH + gap) - scroll;
            Rect row = new Rect(list.x(), y, list.width(), rowH);
            if (row.bottom() < list.y() || row.y() > list.bottom()) {
                continue;
            }
            boolean activeProfile = profile.equalsIgnoreCase(ConfigManager.getActiveProfile());
            boolean hovered = row.contains(mx, my);
            LinkenMenuChrome.roundedFill(context, row.x(), row.y(), row.width(), row.height(), 8.0f,
                LinkenMenuChrome.rgba(activeProfile ? LinkGuiTheme.ACTIVE : (hovered ? LinkGuiTheme.HOVER : LinkGuiTheme.ROW),
                    Math.round(210 * openT)), metrics.scale());
            LinkenMenuChrome.roundedOutline(context, row.x(), row.y(), row.width(), row.height(), 8.0f, 1.0f,
                LinkenMenuChrome.rgba(activeProfile ? LinkGuiTheme.ACCENT : 0xFFFFFF,
                    Math.round((activeProfile ? 70 : 18) * openT)), metrics.scale());
            if (activeProfile) {
                LinkenMenuChrome.roundedFill(context, row.x(), row.y() + 12.0f, 2.5f, row.height() - 24.0f, 1.2f,
                    LinkenMenuChrome.rgba(LinkGuiTheme.ACCENT, openT), metrics.scale());
            }
            LinkenMenuChrome.drawText(context, LinkenFonts.headingLight(), profile,
                row.x() + 18.0f, row.y() + 17.0f, 16.0f,
                LinkenMenuChrome.rgba(activeProfile ? LinkGuiTheme.TEXT : LinkGuiTheme.TEXT3, openT));
            LinkenMenuChrome.drawText(context, LinkenFonts.mono(), profile + ".json",
                row.x() + 18.0f, row.y() + 43.0f, 8.5f,
                LinkenMenuChrome.rgba(LinkGuiTheme.MUTED3, openT));
            if (activeProfile) {
                LinkenMenuChrome.drawSpaced(context, LinkenFonts.headingLight(), "ACTIVE",
                    row.x() + 18.0f + LinkenMenuChrome.textWidth(LinkenFonts.headingLight(), profile, 16.0f) + 14.0f,
                    row.y() + 21.0f, 7.5f, 1.5f,
                    LinkenMenuChrome.rgba(LinkGuiTheme.ACCENT, openT));
            }

            float loadW = 82.0f;
            Rect load = new Rect(row.right() - 18.0f - 82.0f - 8.0f - 82.0f - 8.0f - 24.0f,
                row.y() + 19.0f, 82.0f, 30.0f);
            Rect save = new Rect(load.right() + 8.0f, load.y(), 82.0f, 30.0f);
            Rect delete = new Rect(save.right() + 8.0f, load.y(), 24.0f, 30.0f);
            drawPillButton(context, metrics, load, I18n.get("gui.config.load"), mx, my, false, openT);
            drawPillButton(context, metrics, save, I18n.get("gui.config.save"), mx, my, false, openT);
            drawDeleteButton(context, metrics, delete, profiles.size() > 1, mx, my, openT);
            configHits.add(new ConfigHit(profile, row, load, save, delete));
        }
        context.disableScissor();
        scrollbar(context, metrics, list.right() + 4.0f, list.y(), list.height(), contentHeight, scroll, openT);
        LinkenMenuChrome.drawText(context, LinkenFonts.body(),
            "Profiles are stored in config/linkenvisuals/configs", left, panel.bottom() - 28.0f, 9.5f,
            LinkenMenuChrome.rgba(LinkGuiTheme.MUTED3, openT));
    }

    private void drawPillButton(DrawContext context, Metrics metrics, Rect rect, String label,
                                float mx, float my, boolean active, float openT) {
        boolean hovered = rect.contains(mx, my);
        LinkenMenuChrome.drawSecondary(context, rect.x(), rect.y(), rect.width(), rect.height(), 6.0f,
            hovered ? 1.0f : (active ? 0.35f : 0.0f), false, openT, metrics.scale());
        LinkenMenuChrome.drawCentered(context, LinkenFonts.headingLight(), label,
            rect.x() + rect.width() * 0.5f, rect.y() + rect.height() * 0.5f, 9.5f,
            LinkenMenuChrome.rgba(active ? LinkGuiTheme.ACCENT : LinkGuiTheme.ON_DARK, openT));
    }

    private void drawDeleteButton(DrawContext context, Metrics metrics, Rect rect, boolean enabled,
                                  float mx, float my, float openT) {
        boolean hovered = enabled && rect.contains(mx, my);
        LinkenMenuChrome.drawSecondary(context, rect.x(), rect.y(), rect.width(), rect.height(), 6.0f,
            hovered ? 1.0f : 0.0f, true, openT, metrics.scale());
        int color = LinkenMenuChrome.rgba(enabled ? (hovered ? 0xFF8C8C : LinkGuiTheme.DANGER) : 0x6E757F, openT);
        Matrix3x2fStack matrices = context.getMatrices();
        matrices.pushMatrix();
        matrices.translate(rect.x() + rect.width() * 0.5f, rect.y() + rect.height() * 0.5f);
        matrices.rotate((float) Math.toRadians(45.0));
        PanelPipeline.draw(context, -7.0f, -0.7f, 14.0f, 1.4f, color);
        matrices.rotate((float) Math.toRadians(90.0));
        PanelPipeline.draw(context, -7.0f, -0.7f, 14.0f, 1.4f, color);
        matrices.popMatrix();
    }

    // ------------------------------------------------------------------ popups

    private void drawPopups(DrawContext context, Metrics metrics, float mx, float my, float openT) {
        optionHits.clear();
        if (openMode != null && openModeBox != null) {
            List<String> options = openMode.options();
            float rowH = 30.0f;
            float listH = options.size() * rowH + 12.0f;
            float listX = openModeBox.x();
            float listY = openModeBox.bottom() + 4.0f;
            if (listY + listH > height - 30.0f) {
                listY = openModeBox.y() - listH - 4.0f;
            }
            float listW = Math.max(openModeBox.width(), 190.0f);
            GlowPipeline.radial(context, listX + listW * 0.5f, listY + listH * 0.5f,
                Math.max(listW, listH) * 0.6f, LinkenMenuChrome.rgba(0x000000, 150, openT));
            LinkenMenuChrome.roundedFill(context, listX, listY, listW, listH, 8.0f,
                LinkenMenuChrome.rgba(LinkGuiTheme.PANEL_LIGHT, 248, openT), metrics.scale());
            LinkenMenuChrome.roundedOutline(context, listX, listY, listW, listH, 8.0f, 1.0f,
                LinkenMenuChrome.rgba(0xFFFFFF, 38, openT), metrics.scale());
            for (int i = 0; i < options.size(); i++) {
                float rowY = listY + 6.0f + i * rowH;
                boolean hovered = mx >= listX && mx <= listX + listW && my >= rowY && my <= rowY + rowH;
                boolean active = i == openMode.index();
                if (hovered || active) {
                    LinkenMenuChrome.roundedFill(context, listX + 5.0f, rowY, listW - 10.0f, rowH, 5.0f,
                        LinkenMenuChrome.rgba(active ? LinkGuiTheme.ACCENT : 0xFFFFFF,
                            active ? 24 : 10, openT), metrics.scale());
                }
                LinkenMenuChrome.drawText(context, LinkenFonts.body(), options.get(i),
                    listX + 16.0f, rowY + 8.0f, 10.0f,
                    LinkenMenuChrome.rgba(active ? LinkGuiTheme.ACCENT : LinkGuiTheme.ON_DARK, openT));
                optionHits.add(new OptionHit(openMode, i, new Rect(listX + 5.0f, rowY, listW - 10.0f, rowH)));
            }
        }

        if (openColor == null) {
            return;
        }
        float field = 220.0f;
        float hueW = 20.0f;
        float pad = 18.0f;
        float swatch = 26.0f;
        float popupW = field + hueW + pad * 3.0f;
        float popupH = field + pad * 4.0f + swatch + 28.0f;
        float px = Math.max(20.0f, Math.min(width - popupW - 20.0f, metrics.settings().x() - popupW - 18.0f));
        float py = Math.max(120.0f, Math.min(height - popupH - 20.0f, 250.0f));
        pickerX = px + pad;
        pickerY = py + pad + 24.0f;
        pickerFieldSize = field;

        GlowPipeline.radial(context, px + popupW * 0.5f, py + popupH * 0.5f,
            Math.max(popupW, popupH) * 0.58f, LinkenMenuChrome.rgba(0x000000, 160, openT));
        LinkenMenuChrome.roundedFill(context, px, py, popupW, popupH, 10.0f,
            LinkenMenuChrome.rgba(LinkGuiTheme.PANEL_LIGHT, 249, openT), metrics.scale());
        LinkenMenuChrome.roundedOutline(context, px, py, popupW, popupH, 10.0f, 1.0f,
            LinkenMenuChrome.rgba(0xFFFFFF, 40, openT), metrics.scale());
        LinkenMenuChrome.drawText(context, LinkenFonts.body(), openColor.name(),
            px + pad, py + 14.0f, 12.0f, LinkenMenuChrome.rgba(LinkGuiTheme.TEXT, openT));
        String hex = String.format(Locale.ROOT, "#%06X", openColor.rgb() & 0xFFFFFF);
        LinkenMenuChrome.drawRightText(context, LinkenFonts.mono(), hex,
            px + popupW - pad, py + 15.0f, 10.0f, LinkenMenuChrome.rgba(LinkGuiTheme.ACCENT, openT));

        ColorFieldPipeline.field(context, pickerX, pickerY, field, field,
            LinkenMenuChrome.rgba(openColor.hueColor(), openT));
        LinkenMenuChrome.roundedOutline(context, pickerX, pickerY, field, field, 1.0f, 1.0f,
            LinkenMenuChrome.rgba(0xFFFFFF, 32, openT), metrics.scale());
        float knobX = pickerX + openColor.saturation() * field;
        float knobY = pickerY + (1.0f - openColor.value()) * field;
        GlowPipeline.radial(context, knobX, knobY, 18.0f, LinkenMenuChrome.rgba(openColor.rgb(), 120, openT));
        CirclePipeline.draw(context, knobX, knobY, 7.0f, LinkenMenuChrome.rgba(0xFFFFFF, openT));
        CirclePipeline.draw(context, knobX, knobY, 4.4f, LinkenMenuChrome.rgba(openColor.rgb(), openT));

        float hueX = pickerX + field + pad;
        ColorFieldPipeline.hueVertical(context, hueX, pickerY, hueW, field, Math.round(255 * openT));
        float hueY = pickerY + openColor.hue() * field;
        GlowPipeline.radial(context, hueX + hueW * 0.5f, hueY, 18.0f,
            LinkenMenuChrome.rgba(openColor.hueColor(), 120, openT));
        CirclePipeline.draw(context, hueX + hueW * 0.5f, hueY, 7.0f, LinkenMenuChrome.rgba(0xFFFFFF, openT));
        CirclePipeline.draw(context, hueX + hueW * 0.5f, hueY, 4.4f,
            LinkenMenuChrome.rgba(openColor.hueColor(), openT));

        float swatchY = py + popupH - pad - swatch;
        for (int i = 0; i < ColorSetting.PRESETS.length; i++) {
            float sx = pickerX + i * (swatch + 11.0f);
            CirclePipeline.draw(context, sx + swatch * 0.5f, swatchY + swatch * 0.5f, swatch * 0.5f,
                LinkenMenuChrome.rgba(0xFFFFFF, 44, openT));
            CirclePipeline.draw(context, sx + swatch * 0.5f, swatchY + swatch * 0.5f, swatch * 0.5f - 2.0f,
                LinkenMenuChrome.rgba(ColorSetting.PRESETS[i], openT));
        }

        // Popup bounds are remembered through the primitive fields above.
        popupBounds = new Rect(px, py, popupW, popupH);
    }

    private Rect popupBounds;

    // ------------------------------------------------------------------ footer and helpers

    private void drawFooter(DrawContext context, Metrics metrics, float openT) {
        String left = "LINKEN / " + I18n.get("gui.category." + selected.name().toLowerCase(Locale.ROOT));
        LinkenMenuChrome.drawText(context, LinkenFonts.mono(), left,
            metrics.header().x(), metrics.height() - 43.0f, 9.0f,
            LinkenMenuChrome.rgba(LinkGuiTheme.MUTED3, openT));
        LinkenMenuChrome.drawRightText(context, LinkenFonts.mono(),
            "RMB MODULE = BIND   ESC = CLOSE", metrics.header().right(),
            metrics.height() - 43.0f, 9.0f, LinkenMenuChrome.rgba(LinkGuiTheme.MUTED3, openT));
    }

    private void drawSwitch(DrawContext context, float x, float y, float width, float height,
                            float on, float openT, float scale) {
        int fill = LinkenMenuChrome.mix(LinkGuiTheme.TRACK, LinkGuiTheme.ACCENT, on);
        LinkenMenuChrome.roundedFill(context, x, y, width, height, height * 0.5f,
            LinkenMenuChrome.rgba(fill, Math.round((150 + 80 * on) * openT)), scale);
        LinkenMenuChrome.roundedOutline(context, x, y, width, height, height * 0.5f, 1.0f,
            LinkenMenuChrome.rgba(LinkenMenuChrome.mix(0xFFFFFF, LinkGuiTheme.ACCENT, on),
                Math.round((26 + 80 * on) * openT)), scale);
        float knob = height - 6.0f;
        float knobX = x + 3.0f + on * (width - 6.0f - knob);
        CirclePipeline.draw(context, knobX + knob * 0.5f, y + height * 0.5f, knob * 0.5f,
            LinkenMenuChrome.rgba(LinkenMenuChrome.mix(0x9AA49E, 0xFFFFFF, on), openT));
    }

    private void drawChevron(DrawContext context, float cx, float cy, boolean up, float openT) {
        int color = LinkenMenuChrome.rgba(LinkGuiTheme.MUTED, openT);
        for (int i = 0; i < 3; i++) {
            float dy = up ? -i : i;
            context.fill(Math.round(cx - 4.0f + i), Math.round(cy + dy),
                Math.round(cx - 3.0f + i), Math.round(cy + dy) + 1, color);
            context.fill(Math.round(cx + 3.0f - i), Math.round(cy + dy),
                Math.round(cx + 4.0f - i), Math.round(cy + dy) + 1, color);
        }
    }

    private void scrollbar(DrawContext context, Metrics metrics, float x, float y, float viewHeight,
                           float contentSize, float scroll, float openT) {
        if (contentSize <= viewHeight + 1.0f || viewHeight <= 0.0f) {
            return;
        }
        float share = Math.min(1.0f, viewHeight / contentSize);
        float barHeight = Math.max(18.0f, viewHeight * share);
        float travel = viewHeight - barHeight;
        float at = Math.max(0.0f, Math.min(1.0f, scroll / Math.max(1.0f, contentSize - viewHeight)));
        LinkenMenuChrome.roundedFill(context, x, y, 3.0f, viewHeight, 1.5f,
            LinkenMenuChrome.rgba(0xFFFFFF, 10, openT), metrics.scale());
        LinkenMenuChrome.roundedFill(context, x, y + travel * at, 3.0f, barHeight, 1.5f,
            LinkenMenuChrome.rgba(LinkGuiTheme.ACCENT, 80, openT), metrics.scale());
    }

    private float rowHeight(Setting setting) {
        if (setting instanceof SliderSetting) {
            return 78.0f;
        }
        if (setting instanceof ModeSetting) {
            return 64.0f;
        }
        if (setting instanceof ColorSetting) {
            return 52.0f;
        }
        return 52.0f;
    }

    private List<ModuleInfo> visibleModules() {
        String query = search.toString().trim().toLowerCase(Locale.ROOT);
        List<ModuleInfo> source = query.isEmpty() ? ModuleRegistry.byCategory(selected) : ModuleRegistry.all();
        List<ModuleInfo> result = new ArrayList<>();
        // Pinned modules float to the top of their category/search result. They
        // keep registry order inside the pinned and unpinned groups.
        for (ModuleInfo module : source) {
            if (LinkGuiConfig.pinned(module.name()) && matches(module, query)) {
                result.add(module);
            }
        }
        for (ModuleInfo module : source) {
            if (!LinkGuiConfig.pinned(module.name()) && matches(module, query)) {
                result.add(module);
            }
        }
        return result;
    }

    private static boolean matches(ModuleInfo module, String query) {
        return query.isEmpty() || module.name().toLowerCase(Locale.ROOT).contains(query)
            || module.description().toLowerCase(Locale.ROOT).contains(query);
    }

    // ------------------------------------------------------------------ theme window

    private void drawThemePanel(DrawContext context, Metrics metrics, float mx, float my, float openT) {
        Rect panel = metrics.theme();
        LinkenMenuChrome.roundedFill(context, panel.x(), panel.y(), panel.width(), panel.height(), 14.0f,
            LinkenMenuChrome.rgba(LinkGuiTheme.PANEL_LIGHT, 236, openT), metrics.scale());
        LinkenMenuChrome.roundedOutline(context, panel.x(), panel.y(), panel.width(), panel.height(), 14.0f,
            1.0f, LinkenMenuChrome.rgba(0xFFFFFF, 26, openT), metrics.scale());

        LinkenMenuChrome.drawSpaced(context, LinkenFonts.headingLight(), "THEME",
            panel.x() + 18.0f, panel.y() + 20.0f, 9.0f, 1.7f,
            LinkenMenuChrome.rgba(LinkGuiTheme.ACCENT, openT));
        LinkenMenuChrome.drawText(context, LinkenFonts.headingLight(), "CLICKGUI PALETTE",
            panel.x() + 18.0f, panel.y() + 38.0f, 16.0f,
            LinkenMenuChrome.rgba(LinkGuiTheme.TEXT, openT));
        LinkenMenuChrome.drawText(context, LinkenFonts.body(), "APPLIES TO THE WHOLE SHELL",
            panel.x() + 18.0f, panel.y() + 62.0f, 9.0f,
            LinkenMenuChrome.rgba(LinkGuiTheme.MUTED2, openT));

        themeHits.clear();
        LinkGuiTheme.Preset[] presets = LinkGuiTheme.Preset.values();
        float cardX = panel.x() + 18.0f;
        float cardW = panel.width() - 36.0f;
        float cardH = 86.0f;
        float cardY = panel.y() + 88.0f;
        for (LinkGuiTheme.Preset preset : presets) {
            Rect card = new Rect(cardX, cardY, cardW, cardH);
            boolean hovered = card.contains(mx, my);
            boolean active = LinkGuiTheme.preset() == preset;
            LinkenMenuChrome.roundedFill(context, card.x(), card.y(), card.width(), card.height(), 10.0f,
                LinkenMenuChrome.rgba(hovered ? LinkGuiTheme.HOVER : LinkGuiTheme.ROW, 240, openT), metrics.scale());
            LinkenMenuChrome.roundedOutline(context, card.x(), card.y(), card.width(), card.height(), 10.0f,
                active ? 1.4f : 1.0f,
                LinkenMenuChrome.rgba(active ? preset.accent() : 0xFFFFFF,
                    active ? 190 : 22 + Math.round((hovered ? 1.0f : 0.0f) * 28.0f), openT), metrics.scale());

            LinkenMenuChrome.roundedFill(context, card.x() + 14.0f, card.y() + 14.0f, 34.0f, 34.0f,
                9.0f, LinkenMenuChrome.rgba(preset.accent(), openT), metrics.scale());
            LinkenMenuChrome.roundedFill(context, card.x() + 22.0f, card.y() + 22.0f, 18.0f, 18.0f,
                6.0f, LinkenMenuChrome.rgba(preset.accentDim(), openT), metrics.scale());
            LinkenMenuChrome.drawText(context, LinkenFonts.headingLight(), preset.label(),
                card.x() + 58.0f, card.y() + 17.0f, 12.0f,
                LinkenMenuChrome.rgba(active || hovered ? LinkGuiTheme.TEXT : LinkGuiTheme.TEXT3, openT));
            LinkenMenuChrome.drawSpaced(context, LinkenFonts.mono(),
                active ? "ACTIVE" : "CLICK TO APPLY",
                card.x() + 58.0f, card.y() + 38.0f, 7.0f, 1.0f,
                LinkenMenuChrome.rgba(active ? preset.accent() : LinkGuiTheme.MUTED3, openT));

            float barY = card.bottom() - 22.0f;
            LinkenMenuChrome.roundedFill(context, card.x() + 14.0f, barY, card.width() - 28.0f, 4.0f,
                2.0f, LinkenMenuChrome.rgba(preset.accentDim(), 150, openT), metrics.scale());
            LinkenMenuChrome.roundedFill(context, card.x() + 14.0f, barY, (card.width() - 28.0f) * 0.62f,
                4.0f, 2.0f, LinkenMenuChrome.rgba(preset.accent(), openT), metrics.scale());
            themeHits.add(new ThemeHit(preset, card));
            cardY += cardH + 8.0f;
        }
    }

    // ------------------------------------------------------------------ input

    @Override
    public boolean mouseClicked(Click click, boolean doubleClick) {
        if (closing) {
            return super.mouseClicked(click, doubleClick);
        }
        Metrics metrics = LinkGuiLayout.compute(width, height);
        float mx = (float) click.x() / metrics.scale();
        float my = (float) click.y() / metrics.scale();
        boolean rightClick = click.button() == 1;

        for (ThemeHit hit : themeHits) {
            if (hit.bounds().contains(mx, my)) {
                LinkGuiTheme.apply(hit.preset().name());
                LinkGuiConfig.setTheme(hit.preset().name());
                playClick();
                return true;
            }
        }

        if (closeHit != null && closeHit.contains(mx, my)) {
            close();
            return true;
        }
        // Popup capture comes first so a click cannot accidentally toggle a row
        // underneath an open dropdown or colour picker.
        if (openMode != null) {
            for (OptionHit hit : optionHits) {
                if (hit.bounds.contains(mx, my)) {
                    hit.mode.select(hit.index);
                    openMode = null;
                    playClick();
                    return true;
                }
            }
            openMode = null;
            return true;
        }
        if (openColor != null) {
            if (popupBounds != null && popupBounds.contains(mx, my)) {
                if (my < pickerY) {
                    return true;
                }
                if (my >= pickerY + pickerFieldSize) {
                    for (int i = 0; i < ColorSetting.PRESETS.length; i++) {
                        float sx = pickerX + i * (26.0f + 11.0f);
                        if (mx >= sx && mx <= sx + 26.0f) {
                            openColor.set(ColorSetting.PRESETS[i]);
                            return true;
                        }
                    }
                    return true;
                }
                draggingHue = mx >= pickerX + pickerFieldSize + 6.0f;
                draggingColor = true;
                applyColorDrag(mx, my);
                return true;
            }
            openColor = null;
            return true;
        }

        if (settingsOpen) {
            Rect window = settingsWindowRect(metrics);
            if (settingsCloseHit != null && settingsCloseHit.contains(mx, my)) {
                settingsOpen = false;
                return true;
            }
            if (settingsHeaderHit != null && settingsHeaderHit.contains(mx, my)) {
                settingsDragging = true;
                settingsDragX = mx - window.x();
                settingsDragY = my - window.y();
                return true;
            }
            if (window.contains(mx, my)) {
                if (headerToggleHit != null && headerToggleHit.contains(mx, my) && focused != null) {
                    focused.toggle();
                    playToggle(focused.enabled());
                    return true;
                }
                if (moduleBindHit != null && moduleBindHit.contains(mx, my) && focused != null) {
                    focused.armBinding();
                    playClick();
                    return true;
                }
                for (SettingHit hit : settingHits) {
                    if (hit.bounds.contains(mx, my)) {
                        handleSettingClick(hit.setting, hit.bounds, mx, my, rightClick);
                        return true;
                    }
                }
                return true;
            }
        }

        for (NavHit hit : navHits) {
            if (hit.bounds.contains(mx, my)) {
                selectCategory(hit.category);
                return true;
            }
        }
        if (searchHit != null && searchHit.contains(mx, my)) {
            searchFocused = true;
            configInputFocused = false;
            lastTyped = System.currentTimeMillis();
            return true;
        }
        if (selected == Category.CONFIG) {
            return clickConfig(metrics, mx, my);
        }

        if (headerToggleHit != null && headerToggleHit.contains(mx, my) && focused != null) {
            focused.toggle();
            playToggle(focused.enabled());
            return true;
        }
        if (moduleBindHit != null && moduleBindHit.contains(mx, my) && focused != null) {
            focused.armBinding();
            playClick();
            return true;
        }

        for (CardHit hit : cardHits) {
            if (!hit.visible || !hit.bounds.contains(mx, my)) {
                continue;
            }
            if (hit.pin != null && hit.pin.contains(mx, my)) {
                LinkGuiConfig.togglePin(hit.module.name());
                playClick();
                return true;
            }
            if (rightClick) {
                focused = hit.module;
                for (ModuleInfo other : ModuleRegistry.all()) {
                    other.cancelBinding();
                }
                hit.module.armBinding();
                return true;
            }
            boolean onSwitch = mx >= hit.bounds.right() - 62.0f;
            if (onSwitch) {
                focused = hit.module;
                hit.module.toggle();
                playToggle(hit.module.enabled());
            } else if (hit.module == clickGuiModule()) {
                focused = hit.module;
                settingsOpen = false;
                playClick();
            } else {
                openSettingsWindow(hit.module, hit.bounds);
                playClick();
            }
            panelScrollTarget = 0.0f;
            return true;
        }

        for (SettingHit hit : interfaceHits) {
            if (!hit.bounds.contains(mx, my)) {
                continue;
            }
            handleSettingClick(hit.setting, hit.bounds, mx, my, rightClick);
            return true;
        }
        for (SettingHit hit : settingHits) {
            if (!hit.bounds.contains(mx, my)) {
                continue;
            }
            handleSettingClick(hit.setting, hit.bounds, mx, my, rightClick);
            return true;
        }
        return super.mouseClicked(click, doubleClick);
    }

    private boolean clickConfig(Metrics metrics, float mx, float my) {
        if (configInputHit != null && configInputHit.contains(mx, my)) {
            configInputFocused = true;
            searchFocused = false;
            lastTyped = System.currentTimeMillis();
            return true;
        }
        configInputFocused = false;
        if (configCreateHit != null && configCreateHit.contains(mx, my)) {
            if (configName.isEmpty()) {
                setStatus("Enter a profile name", LinkGuiTheme.DANGER);
                return true;
            }
            boolean created = ConfigManager.createProfile(configName.toString());
            setStatus(I18n.get(created ? "gui.config.status.created" : "gui.config.status.exists",
                configName.toString()), created ? LinkGuiTheme.ACCENT : LinkGuiTheme.DANGER);
            if (created) {
                configName.setLength(0);
            }
            playClick();
            return true;
        }
        if (configFolderHit != null && configFolderHit.contains(mx, my)) {
            ConfigManager.openFolder();
            playClick();
            return true;
        }
        if (configRefreshHit != null && configRefreshHit.contains(mx, my)) {
            ConfigManager.reloadProfiles();
            setStatus(I18n.get("gui.config.status.refreshed"), LinkGuiTheme.ACCENT);
            playClick();
            return true;
        }
        if (configAutoHit != null && configAutoHit.contains(mx, my)) {
            if (ModuleRegistry.configAutoSave() != null) {
                ModuleRegistry.configAutoSave().toggle();
                playClick();
            }
            return true;
        }
        for (ConfigHit hit : configHits) {
            if (hit.load.contains(mx, my)) {
                if (ConfigManager.loadProfile(hit.profile)) {
                    setStatus(I18n.get("gui.config.status.loaded", hit.profile), LinkGuiTheme.ACCENT);
                    playClick();
                }
                return true;
            }
            if (hit.save.contains(mx, my)) {
                if (ConfigManager.saveProfile(hit.profile)) {
                    setStatus(I18n.get("gui.config.status.saved", hit.profile), LinkGuiTheme.ACCENT);
                    playClick();
                }
                return true;
            }
            if (hit.delete.contains(mx, my) && ConfigManager.getProfiles().size() > 1) {
                if (ConfigManager.deleteProfile(hit.profile)) {
                    setStatus(I18n.get("gui.config.status.deleted", hit.profile), LinkGuiTheme.ACCENT);
                    playClick();
                }
                return true;
            }
        }
        return true;
    }

    private void handleSettingClick(Setting setting, Rect row, float mx, float my, boolean rightClick) {
        if (setting instanceof BooleanSetting bool) {
            bool.toggle();
            playToggle(bool.value());
        } else if (setting instanceof SliderSetting slider) {
            draggingSlider = slider;
            dragTrackX = row.x() + 16.0f;
            dragTrackWidth = row.width() - 32.0f;
            slider.setFraction((mx - dragTrackX) / dragTrackWidth);
        } else if (setting instanceof ModeSetting mode) {
            if (rightClick) {
                mode.select((mode.index() + mode.options().size() - 1) % mode.options().size());
            } else {
                mode.toggleOpen();
                openMode = mode.open() ? mode : null;
            }
            playClick();
        } else if (setting instanceof ColorSetting color) {
            openColor = color;
            if (!color.open()) {
                color.toggleOpen();
            }
            playClick();
        } else if (setting instanceof KeybindSetting keybind) {
            keybind.arm();
            playClick();
        }
    }

    private void applyColorDrag(float mx, float my) {
        if (openColor == null) {
            return;
        }
        if (draggingHue) {
            openColor.setHue((my - pickerY) / pickerFieldSize);
        } else {
            openColor.setSaturationValue((mx - pickerX) / pickerFieldSize,
                1.0f - (my - pickerY) / pickerFieldSize);
        }
    }

    @Override
    public boolean mouseDragged(Click click, double offsetX, double offsetY) {
        Metrics metrics = LinkGuiLayout.compute(width, height);
        float mx = (float) click.x() / metrics.scale();
        float my = (float) click.y() / metrics.scale();
        if (settingsDragging) {
            settingsX = mx - settingsDragX;
            settingsY = my - settingsDragY;
            return true;
        }
        if (draggingColor) {
            applyColorDrag(mx, my);
            return true;
        }
        if (draggingSlider != null) {
            draggingSlider.setFraction((mx - dragTrackX) / dragTrackWidth);
            return true;
        }
        return super.mouseDragged(click, offsetX, offsetY);
    }

    @Override
    public boolean mouseReleased(Click click) {
        if (settingsDragging) {
            settingsDragging = false;
            return true;
        }
        draggingColor = false;
        draggingHue = false;
        if (draggingSlider != null) {
            draggingSlider = null;
            return true;
        }
        return super.mouseReleased(click);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        Metrics metrics = LinkGuiLayout.compute(width, height);
        float mx = (float) mouseX / metrics.scale();
        float my = (float) mouseY / metrics.scale();
        if (selected == Category.CONFIG) {
            configScrollTarget -= (float) verticalAmount * 34.0f;
            configScrollTarget = Math.max(0.0f, Math.min(configScrollTarget,
                Math.max(0.0f, configContentHeight - configView)));
            return true;
        }
        if (metrics.profile().contains(mx, my)) {
            panelScrollTarget -= (float) verticalAmount * 34.0f;
            panelScrollTarget = Math.max(0.0f, Math.min(panelScrollTarget,
                Math.max(0.0f, panelContentHeight - panelView)));
            return true;
        }
        if (metrics.center().contains(mx, my)) {
            scrollTarget -= (float) verticalAmount * 34.0f;
            scrollTarget = Math.max(0.0f, Math.min(scrollTarget,
                Math.max(0.0f, contentHeight - contentView)));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean charTyped(CharInput input) {
        if (configInputFocused) {
            if (input.isValidChar()) {
                configName.append(input.asString());
                lastTyped = System.currentTimeMillis();
                return true;
            }
            return false;
        }
        if (searchFocused && input.isValidChar()) {
            search.append(input.asString());
            lastTyped = System.currentTimeMillis();
            return true;
        }
        return super.charTyped(input);
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        for (ModuleInfo module : ModuleRegistry.all()) {
            if (!module.binding()) {
                continue;
            }
            if (input.key() == GLFW.GLFW_KEY_ESCAPE) {
                module.clearBinding();
            } else {
                module.resolveBinding(input.key());
            }
            return true;
        }
        KeybindSetting listening = findListeningKeybind();
        if (listening != null) {
            if (input.key() == GLFW.GLFW_KEY_ESCAPE) {
                listening.clear();
            } else {
                listening.resolve(KeyNames.of(input.key()));
            }
            return true;
        }
        if (input.key() == GLFW.GLFW_KEY_ESCAPE) {
            if (openColor != null) {
                if (openColor.open()) {
                    openColor.toggleOpen();
                }
                openColor = null;
                return true;
            }
            if (openMode != null) {
                openMode.close();
                openMode = null;
                return true;
            }
            if (configInputFocused) {
                configInputFocused = false;
                return true;
            }
            if (searchFocused) {
                searchFocused = false;
                search.setLength(0);
                return true;
            }
            close();
            return true;
        }

        if (configInputFocused && input.key() == GLFW.GLFW_KEY_BACKSPACE && configName.length() > 0) {
            configName.deleteCharAt(configName.length() - 1);
            return true;
        }
        if (searchFocused) {
            if (input.key() == GLFW.GLFW_KEY_BACKSPACE && search.length() > 0) {
                search.deleteCharAt(search.length() - 1);
                lastTyped = System.currentTimeMillis();
                return true;
            }
        }

        boolean ctrl = (input.modifiers() & GLFW.GLFW_MOD_CONTROL) != 0;
        if (ctrl && (input.key() == GLFW.GLFW_KEY_LEFT || input.key() == GLFW.GLFW_KEY_RIGHT)) {
            ModuleRegistry.cycleGuiStyle(input.key() == GLFW.GLFW_KEY_LEFT ? -1 : 1);
            return true;
        }
        return super.keyPressed(input);
    }

    private KeybindSetting findListeningKeybind() {
        if (focused == null) {
            return null;
        }
        for (Setting setting : focused.leftSettings()) {
            if (setting instanceof KeybindSetting key && key.listening()) {
                return key;
            }
        }
        for (Setting setting : focused.rightSettings()) {
            if (setting instanceof KeybindSetting key && key.listening()) {
                return key;
            }
        }
        return null;
    }

    private void selectCategory(Category category) {
        selected = category;
        focused = null;
        if (category != Category.CONFIG) {
            List<ModuleInfo> modules = ModuleRegistry.byCategory(category);
            focused = modules.isEmpty() ? null : modules.get(0);
        }
        search.setLength(0);
        searchFocused = false;
        scrollTarget = 0.0f;
        panelScrollTarget = 0.0f;
        openMode = null;
        openColor = null;
        playClick();
    }

    private void setStatus(String text, int color) {
        status = text;
        statusColor = color;
        statusUntil = System.currentTimeMillis() + 3500L;
    }

    private void playClick() {
        if (client != null) {
            client.getSoundManager().play(PositionedSoundInstance.ui(SoundEvents.UI_BUTTON_CLICK.value(), 0.8f, 1.0f));
        }
    }

    private void playToggle(boolean enabled) {
        if (client != null) {
            client.getSoundManager().play(PositionedSoundInstance.ui(
                enabled ? LinkenVisuals.TOGGLE_ON_SOUND : LinkenVisuals.TOGGLE_OFF_SOUND, 1.0f, 1.0f));
        }
    }

    private record NavHit(Category category, Rect bounds) {
    }

    private record CardHit(ModuleInfo module, Rect bounds, boolean visible, Rect pin) {
    }

    private record SettingHit(Setting setting, Rect bounds) {
    }

    private record ConfigHit(String profile, Rect row, Rect load, Rect save, Rect delete) {
    }

    private record OptionHit(ModeSetting mode, int index, Rect bounds) {
    }
    private record ThemeHit(LinkGuiTheme.Preset preset, Rect bounds) {
    }
}
