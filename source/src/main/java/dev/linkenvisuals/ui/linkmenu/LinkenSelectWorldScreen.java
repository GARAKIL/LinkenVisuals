package dev.linkenvisuals.ui.linkmenu;

import dev.linkenvisuals.audio.LinkenSounds;
import dev.linkenvisuals.i18n.I18n;
import dev.linkenvisuals.mixin.SelectWorldScreenAccessor;
import dev.linkenvisuals.render.Pipelines;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.gui.screen.world.WorldListWidget;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.world.level.storage.LevelSummary;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/**
 * LINKEN singleplayer world browser. The vanilla {@link SelectWorldScreen} is
 * used as the functional backend (world list, play/edit/delete/recreate), but
 * its chrome is replaced with the same editorial LINKEN shell as the title,
 * multiplayer and settings screens.
 */
public final class LinkenSelectWorldScreen extends SelectWorldScreen {
    private static final float PANEL_WIDTH = 1000.0f;
    private static final float PANEL_TOP = 150.0f;
    private static final float PANEL_HEIGHT = 720.0f;
    private static final float ROW_HEIGHT = 66.0f;
    private static final float ROW_GAP = 8.0f;
    private static final float LIST_TOP = 252.0f;
    private static final float LIST_HEIGHT = 390.0f;

    private final Screen parent;
    private WorldListWidget levelList;
    private final dev.linkenvisuals.util.input.TextInput search =
        new dev.linkenvisuals.util.input.TextInput().maxLength(64);
    private boolean searchFocused;
    private long lastTyped;
    private float scroll;
    private float scrollTarget;
    private final List<RowHit> rowHits = new ArrayList<>();
    private final List<ButtonHit> buttonHits = new ArrayList<>();
    private LinkenMenuLayout.Rect searchHit;
    private LinkenMenuLayout.Rect panelHit;

    public LinkenSelectWorldScreen(Screen parent) {
        super(parent);
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        Pipelines.warmUp();
        levelList = ((SelectWorldScreenAccessor) (Object) this).linkenvisuals$levelList();
        if (levelList != null) {
            levelList.visible = false;
            levelList.load();
            levelList.setSearch(search.value());
        }
        // Keep the functional widgets alive for keyboard/double-click handling,
        // but never render their vanilla chrome.
        for (var child : children()) {
            if (child instanceof net.minecraft.client.gui.widget.ClickableWidget widget) {
                widget.visible = false;
            }
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        LinkenMenuLayout.Metrics metrics = LinkenMenuLayout.compute(width, height);
        float mx = (float) mouseX / metrics.scale();
        float my = (float) mouseY / metrics.scale();

        context.getMatrices().pushMatrix();
        context.getMatrices().scale(metrics.scale(), metrics.scale());
        LinkenMenuChrome.drawBackground(context, metrics, 0.0f, 0.0f, 1.0f);
        LinkenMenuChrome.drawWordmark(context, metrics, 1.0f);
        LinkenMenuChrome.drawClock(context, metrics, 1.0f);

        float x = (metrics.width() - PANEL_WIDTH) * 0.5f;
        float y = PANEL_TOP;
        panelHit = new LinkenMenuLayout.Rect(x, y, PANEL_WIDTH, PANEL_HEIGHT);
        LinkenMenuChrome.roundedFill(context, x, y, PANEL_WIDTH, PANEL_HEIGHT, 14.0f,
            LinkenMenuChrome.rgba(0x0E1411, 244, 1.0f), metrics.scale());
        LinkenMenuChrome.roundedOutline(context, x, y, PANEL_WIDTH, PANEL_HEIGHT, 14.0f, 1.0f,
            LinkenMenuChrome.rgba(0xFFFFFF, 24, 1.0f), metrics.scale());

        LinkenMenuChrome.drawText(context, LinkenFonts.heading(), I18n.get("linkmenu.select.title"),
            x + 30.0f, y + 26.0f, 28.0f, LinkenMenuChrome.rgba(LinkenMenuChrome.TEXT, 1.0f));
        LinkenMenuChrome.drawSpaced(context, LinkenFonts.body(),
            I18n.get("linkmenu.select.subtitle"), x + 32.0f, y + 66.0f, 9.0f, 1.4f,
            LinkenMenuChrome.rgba(LinkenMenuChrome.MUTED2, 1.0f));

        searchHit = new LinkenMenuLayout.Rect(x + 30.0f, y + 98.0f, 420.0f, 40.0f);
        boolean searchHover = searchHit.contains(mx, my);
        LinkenMenuChrome.roundedFill(context, searchHit.x(), searchHit.y(), searchHit.width(), searchHit.height(),
            8.0f, LinkenMenuChrome.rgba(0x0A0F0C, 220, 1.0f), metrics.scale());
        LinkenMenuChrome.roundedOutline(context, searchHit.x(), searchHit.y(), searchHit.width(), searchHit.height(),
            8.0f, 1.0f, LinkenMenuChrome.rgba(searchFocused ? LinkenMenuChrome.ACCENT : 0xFFFFFF,
                searchFocused ? 130 : (searchHover ? 50 : 24), 1.0f), metrics.scale());
        String query = search.value();
        LinkenMenuChrome.drawOn(context, LinkenFonts.body(),
            query.isEmpty() ? I18n.get("linkmenu.select.search") : query,
            searchHit.x() + 14.0f, searchHit.y() + 20.0f, 11.5f,
            LinkenMenuChrome.rgba(query.isEmpty() ? LinkenMenuChrome.MUTED3 : LinkenMenuChrome.TEXT, 1.0f));
        if (searchFocused) {
            String beforeCaret = query.substring(0, Math.min(search.caret(), query.length()));
            float caret = searchHit.x() + 14.0f
                + LinkenMenuChrome.textWidth(LinkenFonts.body(), beforeCaret, 11.5f);
            context.fill(Math.round(caret), Math.round(searchHit.y() + 11.0f),
                Math.round(caret + 1.2f), Math.round(searchHit.bottom() - 11.0f),
                LinkenMenuChrome.rgba(LinkenMenuChrome.ACCENT, 220, 1.0f));
        }

        drawList(context, metrics, x, mx, my);
        drawButtons(context, metrics, x, y, mx, my);
        context.getMatrices().popMatrix();
    }

    private void drawList(DrawContext context, LinkenMenuLayout.Metrics metrics, float panelX,
                          float mx, float my) {
        rowHits.clear();
        if (levelList != null) {
            // WorldListWidget populates its children from the async future in
            // renderWidget(); run it off-screen so our custom list can read
            // the filtered entries without showing vanilla chrome.
            context.getMatrices().pushMatrix();
            context.getMatrices().translate(-10000.0f, -10000.0f);
            levelList.renderWidget(context, 0, 0, 0.0f);
            context.getMatrices().popMatrix();
        }
        List<WorldListWidget.WorldEntry> entries = visibleEntries();
        float listX = panelX + 30.0f;
        float listWidth = PANEL_WIDTH - 60.0f;
        float contentHeight = entries.size() * (ROW_HEIGHT + ROW_GAP) - ROW_GAP;
        scrollTarget = Math.max(0.0f, Math.min(scrollTarget, Math.max(0.0f, contentHeight - LIST_HEIGHT)));
        scroll += (scrollTarget - scroll) * 0.35f;
        if (Math.abs(scrollTarget - scroll) < 0.3f) {
            scroll = scrollTarget;
        }

        context.enableScissor(Math.round(listX - 4.0f), Math.round(PANEL_TOP + LIST_TOP - 4.0f),
            Math.round(listX + listWidth + 4.0f), Math.round(PANEL_TOP + LIST_TOP + LIST_HEIGHT + 4.0f));
        if (entries.isEmpty()) {
            LinkenMenuChrome.drawCentered(context, LinkenFonts.body(), I18n.get("linkmenu.select.empty"),
                listX + listWidth * 0.5f, PANEL_TOP + LIST_TOP + 100.0f, 12.0f,
                LinkenMenuChrome.rgba(LinkenMenuChrome.MUTED3, 1.0f));
        }
        WorldListWidget.WorldEntry selected = selectedEntry();
        for (int i = 0; i < entries.size(); i++) {
            WorldListWidget.WorldEntry entry = entries.get(i);
            float rowY = PANEL_TOP + LIST_TOP + i * (ROW_HEIGHT + ROW_GAP) - scroll;
            if (rowY + ROW_HEIGHT < PANEL_TOP + LIST_TOP - 8.0f
                || rowY > PANEL_TOP + LIST_TOP + LIST_HEIGHT + 8.0f) {
                continue;
            }
            Rect row = new Rect(listX, rowY, listWidth, ROW_HEIGHT);
            boolean hovered = row.contains(mx, my);
            boolean active = entry == selected;
            LinkenMenuChrome.roundedFill(context, row.x(), row.y(), row.width(), row.height(), 8.0f,
                LinkenMenuChrome.rgba(active ? 0x173024 : (hovered ? 0x18201B : 0x121814),
                    active ? 238 : 220, 1.0f), metrics.scale());
            LinkenMenuChrome.roundedOutline(context, row.x(), row.y(), row.width(), row.height(), 8.0f, 1.0f,
                LinkenMenuChrome.rgba(active ? LinkenMenuChrome.ACCENT : 0xFFFFFF,
                    active ? 120 : (hovered ? 44 : 18), 1.0f), metrics.scale());

            LevelSummary level = entry.getLevel();
            float iconSize = 42.0f;
            LinkenMenuChrome.roundedFill(context, row.x() + 12.0f, row.y() + 12.0f, iconSize, iconSize, 7.0f,
                LinkenMenuChrome.rgba(0x22332A, 255, 1.0f), metrics.scale());
            String initial = level.getDisplayName().isEmpty() ? "?"
                : level.getDisplayName().substring(0, 1).toUpperCase();
            LinkenMenuChrome.drawCentered(context, LinkenFonts.heading(), initial,
                row.x() + 12.0f + iconSize * 0.5f, row.y() + 12.0f + iconSize * 0.5f, 16.0f,
                LinkenMenuChrome.rgba(LinkenMenuChrome.ACCENT, 1.0f));

            LinkenMenuChrome.drawOn(context, LinkenFonts.heading(), entry.getLevelDisplayName(),
                row.x() + 68.0f, row.y() + 23.0f, 13.0f,
                LinkenMenuChrome.rgba(LinkenMenuChrome.TEXT, 1.0f));
            String details = level.getDetails().getString();
            LinkenMenuChrome.drawOn(context, LinkenFonts.body(), details,
                row.x() + 68.0f, row.y() + 43.0f, 9.5f,
                LinkenMenuChrome.rgba(LinkenMenuChrome.MUTED2, 1.0f));
            LinkenMenuChrome.drawRightText(context, LinkenFonts.mono(),
                level.getVersion().getString(), row.right() - 14.0f, row.y() + 23.0f, 8.5f,
                LinkenMenuChrome.rgba(LinkenMenuChrome.MUTED3, 1.0f));
            if (active) {
                LinkenMenuChrome.roundedFill(context, row.x(), row.y() + 12.0f, 2.0f, row.height() - 24.0f,
                    1.0f, LinkenMenuChrome.rgba(LinkenMenuChrome.ACCENT, 1.0f), metrics.scale());
            }
            rowHits.add(new RowHit(entry, row));
        }
        context.disableScissor();
    }

    private void drawButtons(DrawContext context, LinkenMenuLayout.Metrics metrics, float panelX,
                             float panelY, float mx, float my) {
        buttonHits.clear();
        float y = panelY + PANEL_HEIGHT - 70.0f;
        float x = panelX + 30.0f;
        float gap = 10.0f;
        WorldListWidget.WorldEntry selected = selectedEntry();
        boolean hasSelection = selected != null;
        boolean editable = hasSelection && selected.getLevel().isEditable();
        boolean deletable = hasSelection && selected.getLevel().isDeletable();
        boolean recreatable = hasSelection && selected.getLevel().isRecreatable();

        float playW = 180.0f;
        Rect play = new Rect(x, y, playW, 44.0f);
        drawButton(context, metrics, play, I18n.get("linkmenu.select.play"), true, hasSelection, false, mx, my);
        buttonHits.add(new ButtonHit(play, hasSelection, () -> {
            WorldListWidget.WorldEntry entry = selectedEntry();
            if (entry != null) {
                entry.play();
            }
        }));
        x += playW + gap;

        float createW = 180.0f;
        Rect create = new Rect(x, y, createW, 44.0f);
        drawButton(context, metrics, create, I18n.get("linkmenu.select.create"), false, true, false, mx, my);
        buttonHits.add(new ButtonHit(create, true,
            () -> CreateWorldScreen.show(client, () -> client.setScreen(this))));
        x += createW + gap;

        Rect edit = new Rect(x, y, 120.0f, 44.0f);
        drawButton(context, metrics, edit, I18n.get("linkmenu.select.edit"), false, editable, false, mx, my);
        buttonHits.add(new ButtonHit(edit, editable, () -> {
            WorldListWidget.WorldEntry entry = selectedEntry();
            if (entry != null) {
                entry.edit();
            }
        }));
        x += 130.0f;

        Rect delete = new Rect(x, y, 120.0f, 44.0f);
        drawButton(context, metrics, delete, I18n.get("linkmenu.select.delete"), false, deletable, true, mx, my);
        buttonHits.add(new ButtonHit(delete, deletable, () -> {
            WorldListWidget.WorldEntry entry = selectedEntry();
            if (entry != null) {
                entry.deleteIfConfirmed();
            }
        }));
        x += 130.0f;

        Rect recreate = new Rect(x, y, 140.0f, 44.0f);
        drawButton(context, metrics, recreate, I18n.get("linkmenu.select.recreate"), false, recreatable, false, mx, my);
        buttonHits.add(new ButtonHit(recreate, recreatable, () -> {
            WorldListWidget.WorldEntry entry = selectedEntry();
            if (entry != null) {
                entry.recreate();
            }
        }));
        x += 150.0f;

        Rect back = new Rect(panelX + PANEL_WIDTH - 150.0f, y, 120.0f, 44.0f);
        drawButton(context, metrics, back, I18n.get("linkmenu.select.back"), false, true, false, mx, my);
        buttonHits.add(new ButtonHit(back, true, this::close));
    }

    private void drawButton(DrawContext context, LinkenMenuLayout.Metrics metrics,
                            Rect rect, String label, boolean primary, boolean enabled,
                            boolean danger, float mx, float my) {
        boolean hovered = enabled && rect.contains(mx, my);
        if (primary && enabled) {
            LinkenMenuChrome.roundedFill(context, rect.x(), rect.y(), rect.width(), rect.height(), 8.0f,
                LinkenMenuChrome.rgba(LinkenMenuChrome.mix(LinkenMenuChrome.ACCENT, 0xFFFFFF, hovered ? 0.08f : 0.0f),
                    255, 1.0f), metrics.scale());
        } else if (danger) {
            LinkenMenuChrome.roundedFill(context, rect.x(), rect.y(), rect.width(), rect.height(), 8.0f,
                LinkenMenuChrome.rgba(enabled ? 0x2A1717 : 0x181313, enabled ? 225 : 170, 1.0f), metrics.scale());
            LinkenMenuChrome.roundedOutline(context, rect.x(), rect.y(), rect.width(), rect.height(), 8.0f,
                1.0f, LinkenMenuChrome.rgba(LinkenMenuChrome.DANGER, enabled ? 110 : 30, 1.0f), metrics.scale());
        } else if (primary) {
            LinkenMenuChrome.roundedFill(context, rect.x(), rect.y(), rect.width(), rect.height(), 8.0f,
                LinkenMenuChrome.rgba(0x1A231D, 190, 1.0f), metrics.scale());
        } else {
            LinkenMenuChrome.drawSecondary(context, rect.x(), rect.y(), rect.width(), rect.height(),
                8.0f, hovered ? 1.0f : 0.0f, false, 1.0f, metrics.scale());
        }
        int labelColor = primary && enabled ? LinkenMenuChrome.INK
            : (danger && enabled ? LinkenMenuChrome.DANGER
                : (enabled ? LinkenMenuChrome.TEXT2 : LinkenMenuChrome.MUTED3));
        LinkenMenuChrome.drawCentered(context, LinkenFonts.heading(),
            label, rect.x() + rect.width() * 0.5f, rect.y() + rect.height() * 0.5f, 10.5f,
            LinkenMenuChrome.rgba(labelColor, 1.0f));
    }

    private WorldListWidget.WorldEntry selectedEntry() {
        if (levelList == null) {
            return null;
        }
        WorldListWidget.Entry selected = levelList.getSelectedOrNull();
        return selected instanceof WorldListWidget.WorldEntry entry ? entry : null;
    }

    private List<WorldListWidget.WorldEntry> visibleEntries() {
        List<WorldListWidget.WorldEntry> entries = new ArrayList<>();
        if (levelList == null) {
            return entries;
        }
        for (WorldListWidget.Entry entry : levelList.children()) {
            if (entry instanceof WorldListWidget.WorldEntry world) {
                entries.add(world);
            }
        }
        return entries;
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubleClick) {
        LinkenMenuLayout.Metrics metrics = LinkenMenuLayout.compute(width, height);
        float mx = (float) click.x() / metrics.scale();
        float my = (float) click.y() / metrics.scale();
        if (click.button() != 0) {
            return super.mouseClicked(click, doubleClick);
        }
        if (searchHit != null && searchHit.contains(mx, my)) {
            searchFocused = true;
            lastTyped = Util.getMeasuringTimeMs();
            return true;
        }
        for (ButtonHit hit : buttonHits) {
            if (hit.enabled() && hit.bounds().contains(mx, my)) {
                LinkenSounds.playUiClick();
                hit.action().run();
                return true;
            }
        }
        for (RowHit hit : rowHits) {
            if (hit.bounds().contains(mx, my)) {
                LinkenSounds.playUiClick();
                if (levelList != null) {
                    levelList.setSelected(hit.entry());
                }
                if (doubleClick) {
                    hit.entry().play();
                }
                return true;
            }
        }
        if (panelHit != null && panelHit.contains(mx, my)) {
            searchFocused = false;
            return true;
        }
        return super.mouseClicked(click, doubleClick);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        LinkenMenuLayout.Metrics metrics = LinkenMenuLayout.compute(width, height);
        float mx = (float) mouseX / metrics.scale();
        float my = (float) mouseY / metrics.scale();
        if (panelHit != null && panelHit.contains(mx, my)) {
            scrollTarget -= (float) verticalAmount * 40.0f;
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean charTyped(CharInput input) {
        if (searchFocused && search.charTyped(input)) {
            lastTyped = Util.getMeasuringTimeMs();
            if (levelList != null) {
                levelList.setSearch(search.value());
            }
            return true;
        }
        return super.charTyped(input);
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (input.key() == GLFW.GLFW_KEY_ESCAPE) {
            close();
            return true;
        }
        if (searchFocused && search.keyPressed(input)) {
            if (levelList != null) {
                levelList.setSearch(search.value());
            }
            return true;
        }
        if (searchFocused && input.key() == GLFW.GLFW_KEY_ENTER) {
            WorldListWidget.WorldEntry entry = selectedEntry();
            if (entry != null) {
                entry.play();
            }
            return true;
        }
        return super.keyPressed(input);
    }

    @Override
    public void close() {
        if (client != null) {
            Screen target = parent;
            if (target == null || target == this) {
                target = new LinkenMainMenuScreen();
            }
            client.setScreen(target);
        }
    }

    private record RowHit(WorldListWidget.WorldEntry entry, Rect bounds) {
    }

    private record ButtonHit(Rect bounds, boolean enabled, Runnable action) {
    }

    private record Rect(float x, float y, float width, float height) {
        float right() {
            return x + width;
        }

        float bottom() {
            return y + height;
        }

        boolean contains(float px, float py) {
            return px >= x && px <= right() && py >= y && py <= bottom();
        }
    }
}
