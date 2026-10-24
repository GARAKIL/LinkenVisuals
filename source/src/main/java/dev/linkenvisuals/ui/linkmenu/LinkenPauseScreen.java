package dev.linkenvisuals.ui.linkmenu;

import dev.linkenvisuals.i18n.I18n;
import dev.linkenvisuals.render.IconPipeline;
import dev.linkenvisuals.ui.Spring;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.OpenToLanScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.StatsScreen;
import net.minecraft.client.gui.screen.advancement.AdvancementsScreen;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * LINKEN pause menu (donor 11a geometry): a compact 440x~351 design-unit
 * panel with a 56 px topbar, one primary resume row, a 2-column action grid,
 * a divider and the save/quit row. It deliberately replaces the previous
 * wide hub panel so the ESC screen matches the donor instead of the settings
 * hub.
 */
public final class LinkenPauseScreen extends Screen {
    private static final float WIN_W = 440.0f;
    private static final float PAD = 22.0f;
    private static final float TOPBAR = 56.0f;
    private static final float BODY_TOP = 18.0f;
    private static final float BODY_BOTTOM = 20.0f;
    private static final float ROW = 44.0f;
    private static final float GAP = 8.0f;
    private static final float DIVIDER_PAD = 6.0f;

    private record Item(String label, String detail, int icon, boolean danger, Runnable action) {
    }

    private final List<Item> grid = new ArrayList<>();
    private final List<Item> footer = new ArrayList<>();
    private final Map<Integer, Spring> hover = new HashMap<>();
    private final Spring open = new Spring(0.0, 900.0, 42.0);
    private final Spring closeHover = new Spring(0.0, 900.0, 50.0);
    private final Spring backHover = new Spring(0.0, 900.0, 50.0);
    private long lastFrame = System.nanoTime();
    private float openValue;

    private LinkenMenuLayout.Rect backRect;
    private final List<LinkenMenuLayout.Rect> gridRects = new ArrayList<>();
    private final List<LinkenMenuLayout.Rect> footerRects = new ArrayList<>();
    private LinkenMenuLayout.Rect closeRect;

    public LinkenPauseScreen() {
        super(Text.empty());
        buildEntries();
    }

    private void buildEntries() {
        MinecraftClient mc = MinecraftClient.getInstance();
        grid.clear();
        footer.clear();
        if (mc == null) {
            return;
        }

        grid.add(new Item(I18n.get("pause.settings"), "VIDEO, CONTROLS, LANGUAGE",
            LinkenMenuChrome.MENU_ICON_GEAR, false, () -> mc.setScreen(new LinkenSettingsScreen(this))));

        if (mc.getNetworkHandler() != null) {
            grid.add(new Item(I18n.get("pause.advancements"), "WORLD PROGRESS",
                LinkenMenuChrome.MENU_ICON_PEOPLE, false, () -> mc.setScreen(
                    new AdvancementsScreen(mc.getNetworkHandler().getAdvancementHandler(), this))));
        }
        if (mc.player != null) {
            grid.add(new Item(I18n.get("pause.stats"), "YOUR WORLD NUMBERS",
                LinkenMenuChrome.MENU_ICON_PEOPLE, false, () -> mc.setScreen(
                    new StatsScreen(this, mc.player.getStatHandler()))));
        }
        if (mc.isInSingleplayer()) {
            grid.add(new Item(I18n.get("pause.lan"), "SHARE THIS WORLD",
                LinkenMenuChrome.MENU_ICON_GLOBE, false, () -> mc.setScreen(new OpenToLanScreen(this))));
        }

        footer.add(new Item(mc.isInSingleplayer() ? I18n.get("pause.quit_world") : I18n.get("pause.disconnect"),
            mc.isInSingleplayer() ? "SAVE THE WORLD AND LEAVE" : "LEAVE THE SERVER",
            LinkenMenuChrome.MENU_ICON_POWER, true,
            () -> mc.disconnect(ClientWorld.QUITTING_MULTIPLAYER_TEXT)));
        footer.add(new Item(I18n.get("linkenvisuals.menu.quit"), "CLOSE MINECRAFT",
            LinkenMenuChrome.MENU_ICON_POWER, true, mc::scheduleStop));
    }

    private int gridRows() {
        return Math.max(1, (grid.size() + 1) / 2);
    }

    private float panelHeight() {
        float gridH = gridRows() * ROW + Math.max(0, gridRows() - 1) * GAP;
        return TOPBAR + BODY_TOP + ROW + GAP + gridH + DIVIDER_PAD * 2.0f + 1.0f
            + GAP + ROW + BODY_BOTTOM;
    }

    @Override
    protected void init() {
        LinkenFonts.body();
        LinkenFonts.heading();
        LinkenFonts.headingLight();
        LinkenFonts.mono();
        LinkenFonts.icons2();
        lastFrame = System.nanoTime();
    }

    @Override
    public boolean shouldPause() {
        return true;
    }

    @Override
    public void close() {
        if (client != null) {
            client.setScreen(null);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        long now = System.nanoTime();
        float deltaSeconds = Math.min((now - lastFrame) / 1_000_000_000.0f, 0.05f);
        lastFrame = now;
        open.step(1.0, deltaSeconds);
        openValue = (float) open.get();

        LinkenMenuLayout.Metrics metrics = LinkenMenuLayout.compute(width, height);
        float mx = mouseX / metrics.scale();
        float my = mouseY / metrics.scale();

        if (client != null && client.world != null) {
            context.fill(0, 0, width, height, 0x66050807);
        } else {
            context.getMatrices().pushMatrix();
            context.getMatrices().scale(metrics.scale(), metrics.scale());
            LinkenMenuChrome.drawBackground(context, metrics, 0.0f, 0.0f, 1.0f);
            context.getMatrices().popMatrix();
        }

        float w = WIN_W;
        float h = panelHeight();
        float x = (metrics.width() - w) * 0.5f;
        float y = (metrics.height() - h) * 0.5f;
        float slide = (1.0f - openValue) * 18.0f;

        context.getMatrices().pushMatrix();
        context.getMatrices().scale(metrics.scale(), metrics.scale());
        context.getMatrices().translate(0.0f, slide);

        LinkenMenuChrome.roundedFill(context, x, y, w, h, 14.0f,
            LinkenMenuChrome.rgba(0x0E1411, Math.round(244.0f * openValue)), metrics.scale());
        LinkenMenuChrome.roundedOutline(context, x, y, w, h, 14.0f, 1.0f,
            LinkenMenuChrome.rgba(0xFFFFFF, Math.round(26.0f * openValue)), metrics.scale());
        LinkenMenuChrome.roundedFill(context, x + 24.0f, y + 1.0f, w - 48.0f, 1.0f, 0.5f,
            LinkenMenuChrome.rgba(0xFFFFFF, Math.round(38.0f * openValue)), metrics.scale());
        context.fill(Math.round(x + PAD), Math.round(y + TOPBAR - 1.0f),
            Math.round(x + w - PAD), Math.round(y + TOPBAR),
            LinkenMenuChrome.rgba(0xFFFFFF, Math.round(20.0f * openValue)));

        // Topbar: title, world/server line, close.
        LinkenMenuChrome.drawText(context, LinkenFonts.bodyMedium(), I18n.get("pause.title"),
            x + PAD, y + 14.0f, 17.0f, LinkenMenuChrome.rgba(LinkenMenuChrome.TEXT, openValue));
        LinkenMenuChrome.drawText(context, LinkenFonts.body(), worldLine(),
            x + PAD, y + 34.0f, 9.0f, LinkenMenuChrome.rgba(LinkenMenuChrome.MUTED2, openValue));

        closeRect = new LinkenMenuLayout.Rect(x + w - PAD - 28.0f, y + 14.0f, 28.0f, 28.0f);
        boolean closeOver = closeRect.contains(mx, my);
        closeHover.step(closeOver ? 1.0 : 0.0, deltaSeconds);
        drawClose(context, closeRect, (float) closeHover.get());

        float cy = y + TOPBAR + BODY_TOP;
        backRect = new LinkenMenuLayout.Rect(x + PAD, cy, w - PAD * 2.0f, ROW);
        boolean backOver = backRect.contains(mx, my);
        backHover.step(backOver ? 1.0 : 0.0, deltaSeconds);
        float backH = (float) backHover.get();
        LinkenMenuChrome.roundedFill(context, backRect.x() + 4.0f, backRect.y() + backRect.height() - 6.0f,
            backRect.width() - 8.0f, 12.0f, 8.0f,
            LinkenMenuChrome.rgba(LinkenMenuChrome.ACCENT, Math.round((55.0f + 35.0f * backH) * openValue)),
            metrics.scale());
        LinkenMenuChrome.roundedFill(context, backRect.x(), backRect.y() - backH, backRect.width(), ROW,
            8.0f, LinkenMenuChrome.rgba(LinkenMenuChrome.mix(LinkenMenuChrome.ACCENT, 0xFFFFFF,
                backH * 0.08f), openValue), metrics.scale());
        drawIconAndLabel(context, backRect, I18n.get("pause.resume"), LinkenMenuChrome.MENU_ICON_POWER,
            LinkenMenuChrome.INK, backH, openValue, 13.5f);
        cy += ROW + GAP;

        // 2-column grid.
        gridRects.clear();
        float tileW = (w - PAD * 2.0f - GAP) * 0.5f;
        for (int i = 0; i < grid.size(); i++) {
            int column = i % 2;
            int row = i / 2;
            LinkenMenuLayout.Rect rect = new LinkenMenuLayout.Rect(
                x + PAD + column * (tileW + GAP), cy + row * (ROW + GAP), tileW, ROW);
            gridRects.add(rect);
            drawTile(context, metrics, rect, grid.get(i), i, mx, my, deltaSeconds);
        }
        cy += gridRows() * ROW + Math.max(0, gridRows() - 1) * GAP;

        // Divider.
        cy += DIVIDER_PAD;
        context.fill(Math.round(x + PAD), Math.round(cy), Math.round(x + w - PAD), Math.round(cy + 1.0f),
            LinkenMenuChrome.rgba(0xFFFFFF, Math.round(20.0f * openValue)));
        cy += 1.0f + DIVIDER_PAD + GAP - DIVIDER_PAD;

        // Footer actions.
        footerRects.clear();
        for (int i = 0; i < footer.size(); i++) {
            LinkenMenuLayout.Rect rect = new LinkenMenuLayout.Rect(
                x + PAD + i * (tileW + GAP), cy, tileW, ROW);
            footerRects.add(rect);
            int index = 100 + i;
            boolean over = rect.contains(mx, my);
            Spring spring = hover.computeIfAbsent(index, key -> new Spring(0.0, 900.0, 50.0));
            spring.step(over ? 1.0 : 0.0, deltaSeconds);
            drawDanger(context, metrics, rect, footer.get(i), (float) spring.get());
        }

        LinkenMenuChrome.drawSpaced(context, LinkenFonts.headingLight(), "ESC  /  RESUME",
            x + PAD, y + h - 14.0f, 7.0f, 1.3f,
            LinkenMenuChrome.rgba(LinkenMenuChrome.MUTED4, openValue));
        LinkenMenuChrome.drawRightText(context, LinkenFonts.mono(), "LINKEN",
            x + w - PAD, y + h - 14.0f, 8.0f,
            LinkenMenuChrome.rgba(LinkenMenuChrome.ACCENT, Math.round(140.0f * openValue)));

        context.getMatrices().popMatrix();
    }

    private void drawTile(DrawContext context, LinkenMenuLayout.Metrics metrics, LinkenMenuLayout.Rect rect,
                          Item item, int index, float mx, float my, float deltaSeconds) {
        boolean over = rect.contains(mx, my);
        Spring spring = hover.computeIfAbsent(index, key -> new Spring(0.0, 900.0, 50.0));
        spring.step(over ? 1.0 : 0.0, deltaSeconds);
        float hoverValue = (float) spring.get();
        LinkenMenuChrome.roundedFill(context, rect.x(), rect.y() - hoverValue, rect.width(), ROW, 8.0f,
            LinkenMenuChrome.rgba(LinkenMenuChrome.mix(0x181E1B, 0x202A23, hoverValue),
                Math.round(220.0f * openValue)), metrics.scale());
        LinkenMenuChrome.roundedOutline(context, rect.x(), rect.y() - hoverValue, rect.width(), ROW, 8.0f,
            1.0f, LinkenMenuChrome.rgba(0xFFFFFF, Math.round((11.0f + 44.0f * hoverValue) * openValue)),
            metrics.scale());
        drawIconAndLabel(context, rect, item.label(), item.icon(),
            LinkenMenuChrome.mix(LinkenMenuChrome.TEXT3, LinkenMenuChrome.ACCENT, hoverValue),
            hoverValue, openValue, 10.0f);
    }

    private void drawDanger(DrawContext context, LinkenMenuLayout.Metrics metrics,
                            LinkenMenuLayout.Rect rect, Item item, float hoverValue) {
        LinkenMenuChrome.roundedFill(context, rect.x(), rect.y() - hoverValue, rect.width(), ROW, 8.0f,
            LinkenMenuChrome.rgba(0x1A1212, Math.round(220.0f * openValue)), metrics.scale());
        LinkenMenuChrome.roundedOutline(context, rect.x(), rect.y() - hoverValue, rect.width(), ROW, 8.0f,
            1.0f, LinkenMenuChrome.rgba(LinkenMenuChrome.DANGER,
                Math.round((20.0f + 70.0f * hoverValue) * openValue)), metrics.scale());
        drawIconAndLabel(context, rect, item.label(), item.icon(),
            LinkenMenuChrome.DANGER, hoverValue, openValue, 10.0f);
    }

    private void drawIconAndLabel(DrawContext context, LinkenMenuLayout.Rect rect, String label,
                                  int icon, int color, float hover, float alpha, float size) {
        if (LinkenFonts.icons2() != null) {
            IconPipeline.draw(context, LinkenFonts.icons2(), icon, rect.x() + 17.0f,
                rect.y() + ROW * 0.5f - hover, 13.0f,
                LinkenMenuChrome.rgba(color, Math.round(255.0f * alpha)));
        }
        float labelX = rect.x() + 31.0f;
        String fitted = LinkenMenuChrome.ellipsize(LinkenFonts.headingLight(), label,
            rect.right() - labelX - 12.0f, size);
        LinkenMenuChrome.drawText(context, LinkenFonts.headingLight(), fitted,
            labelX, rect.y() + (ROW - size) * 0.5f - hover, size,
            LinkenMenuChrome.rgba(color, Math.round(255.0f * alpha)));
    }

    private void drawClose(DrawContext context, LinkenMenuLayout.Rect rect, float hover) {
        LinkenMenuChrome.roundedFill(context, rect.x(), rect.y(), rect.width(), rect.height(), 7.0f,
            LinkenMenuChrome.rgba(LinkenMenuChrome.mix(0x151C18, 0x202A23, hover),
                Math.round(230.0f * openValue)), 1.0f);
        int color = LinkenMenuChrome.rgba(LinkenMenuChrome.mix(LinkenMenuChrome.MUTED,
            LinkenMenuChrome.TEXT, hover), openValue);
        context.getMatrices().pushMatrix();
        context.getMatrices().translate(rect.x() + rect.width() * 0.5f, rect.y() + rect.height() * 0.5f);
        context.getMatrices().rotate((float) Math.toRadians(45.0));
        context.fill(-6, 0, 6, 1, color);
        context.getMatrices().rotate((float) Math.toRadians(90.0));
        context.fill(-6, 0, 6, 1, color);
        context.getMatrices().popMatrix();
    }

    private static String worldLine() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null) {
            return I18n.get("pause.singleplayer");
        }
        if (mc.getCurrentServerEntry() != null && mc.getCurrentServerEntry().name != null) {
            return I18n.get("pause.multiplayer") + "  /  " + mc.getCurrentServerEntry().name;
        }
        if (mc.getServer() != null && mc.getServer().getSaveProperties() != null) {
            return I18n.get("pause.singleplayer") + "  /  " + mc.getServer().getSaveProperties().getLevelName();
        }
        return I18n.get("pause.title");
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubleClick) {
        LinkenMenuLayout.Metrics metrics = LinkenMenuLayout.compute(width, height);
        float mx = (float) click.x() / metrics.scale();
        float my = (float) click.y() / metrics.scale();
        if (closeRect != null && closeRect.contains(mx, my)) {
            playClick();
            close();
            return true;
        }
        if (backRect != null && backRect.contains(mx, my)) {
            playClick();
            close();
            return true;
        }
        for (int i = 0; i < gridRects.size() && i < grid.size(); i++) {
            if (gridRects.get(i).contains(mx, my)) {
                playClick();
                grid.get(i).action().run();
                return true;
            }
        }
        for (int i = 0; i < footerRects.size() && i < footer.size(); i++) {
            if (footerRects.get(i).contains(mx, my)) {
                playClick();
                footer.get(i).action().run();
                return true;
            }
        }
        return super.mouseClicked(click, doubleClick);
    }

    @Override
    public boolean keyPressed(net.minecraft.client.input.KeyInput input) {
        if (input.key() == GLFW.GLFW_KEY_ESCAPE) {
            close();
            return true;
        }
        return super.keyPressed(input);
    }

    private void playClick() {
        if (client != null) {
            client.getSoundManager().play(net.minecraft.client.sound.PositionedSoundInstance.ui(
                dev.linkenvisuals.LinkenVisuals.OPEN_SOUND, 0.8f, 1.0f));
        }
    }
}
