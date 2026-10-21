package dev.linkenvisuals.ui.linkmenu;

import dev.linkenvisuals.audio.LinkenSounds;

import dev.linkenvisuals.LinkenVisuals;
import dev.linkenvisuals.i18n.I18n;
import dev.linkenvisuals.render.CirclePipeline;
import dev.linkenvisuals.render.GlowPipeline;
import dev.linkenvisuals.render.IconPipeline;
import dev.linkenvisuals.ui.Spring;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.network.MultiplayerServerListPinger;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.option.ServerList;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.network.NetworkingBackend;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import org.lwjgl.glfw.GLFW;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * LINKEN server browser. It owns its data model (vanilla {@link ServerList}),
 * pinger and a small custom text-input modal so the whole screen can share the
 * 1920x1080 LINKEN chrome instead of the Beta browser.
 */
public final class LinkenMultiplayerScreen extends Screen {
    private static final int COLUMNS = 3;
    private static final int ROWS = 3;
    private static final int PER_PAGE = COLUMNS * ROWS;

    private enum Modal {
        NONE, ADD, EDIT, DIRECT, DELETE
    }

    private final Screen parent;
    private final Spring openSpring = new Spring(0.0, 900.0, 42.0);
    private final Spring backSpring = new Spring(0.0, 900.0, 50.0);
    private final Spring[] cardHover = new Spring[PER_PAGE];
    private final Spring[] toolbarHover = new Spring[4];
    private final Spring prevSpring = new Spring(0.0, 900.0, 50.0);
    private final Spring nextSpring = new Spring(0.0, 900.0, 50.0);
    private final Spring modalCancelSpring = new Spring(0.0, 900.0, 50.0);
    private final Spring modalConfirmSpring = new Spring(0.0, 900.0, 50.0);
    private final Spring modalCloseSpring = new Spring(0.0, 900.0, 50.0);
    private ServerList serverList;
    private final MultiplayerServerListPinger pinger = new MultiplayerServerListPinger();
    private final List<ServerInfo> servers = new ArrayList<>();
    private int selected = -1;
    private int page;
    private int lastClickedIndex = -1;
    private long lastClickTime;
    private long lastFrame = System.nanoTime();
    private boolean openSoundPlayed;
    private Modal modal = Modal.NONE;
    private String nameInput = "";
    private String addressInput = "";
    private int focusedField = -1;
    private long caretTick;
    private String modalError;

    private LinkenMenuLayout.Rect backRect;
    private LinkenMenuLayout.Rect addRect;
    private LinkenMenuLayout.Rect directRect;
    private LinkenMenuLayout.Rect refreshRect;
    private final LinkenMenuLayout.Rect[] cardRects = new LinkenMenuLayout.Rect[PER_PAGE];
    private LinkenMenuLayout.Rect joinRect;
    private LinkenMenuLayout.Rect editRect;
    private LinkenMenuLayout.Rect deleteRect;
    private LinkenMenuLayout.Rect prevRect;
    private LinkenMenuLayout.Rect nextRect;
    private LinkenMenuLayout.Rect nameFieldRect;
    private LinkenMenuLayout.Rect addressFieldRect;
    private LinkenMenuLayout.Rect modalCancelRect;
    private LinkenMenuLayout.Rect modalConfirmRect;
    private LinkenMenuLayout.Rect modalCloseRect;
    private LinkenMenuLayout.Rect modalBounds;

    public LinkenMultiplayerScreen(Screen parent) {
        super(Text.literal("Multiplayer"));
        this.parent = parent;
        for (int i = 0; i < cardHover.length; i++) {
            cardHover[i] = new Spring(0.0, 900.0, 50.0);
        }
        for (int i = 0; i < toolbarHover.length; i++) {
            toolbarHover[i] = new Spring(0.0, 900.0, 50.0);
        }
    }

    @Override
    protected void init() {
        if (client != null && client.getSoundManager() != null) {
            client.getSoundManager().play(PositionedSoundInstance.ui(LinkenVisuals.OPEN_SOUND, 0.9f, 1.0f));
            openSoundPlayed = true;
        }
        serverList = new ServerList(client);
        serverList.loadFile();
        refreshServers();
        openSpring.reset(0.0);
        lastFrame = System.nanoTime();
    }

    @Override
    public void tick() {
        pinger.tick();
    }

    private void refreshServers() {
        pinger.cancel();
        servers.clear();
        if (serverList == null) {
            return;
        }
        for (int i = 0; i < serverList.size(); i++) {
            ServerInfo info = serverList.get(i);
            servers.add(info);
            info.setStatus(ServerInfo.Status.INITIAL);
            Util.getIoWorkerExecutor().execute(() -> ping(info));
        }
        int pages = pageCount();
        if (page >= pages) {
            page = Math.max(0, pages - 1);
        }
        if (selected >= servers.size()) {
            selected = servers.isEmpty() ? -1 : servers.size() - 1;
        }
        ensureSelectedVisible();
    }

    private void ping(ServerInfo info) {
        try {
            NetworkingBackend backend = client != null && client.options != null
                ? NetworkingBackend.remote(client.options.shouldUseNativeTransport())
                : NetworkingBackend.remote(false);
            pinger.add(info, () -> {
                if (client != null) {
                    client.execute(() -> info.setStatus(
                        info.protocolVersion == SharedConstants.getGameVersion().protocolVersion()
                            ? ServerInfo.Status.SUCCESSFUL
                            : ServerInfo.Status.INCOMPATIBLE));
                }
            }, () -> {
                if (client != null) {
                    client.execute(() -> {
                        // Legacy or otherwise incompatible servers still return
                        // their MOTD before the pinger rejects the protocol.
                        info.setStatus(info.label != null && info.protocolVersion > 0
                            ? ServerInfo.Status.INCOMPATIBLE
                            : ServerInfo.Status.UNREACHABLE);
                    });
                }
            }, backend);
        } catch (UnknownHostException failure) {
            info.setStatus(ServerInfo.Status.UNREACHABLE);
        } catch (Exception failure) {
            info.setStatus(ServerInfo.Status.UNREACHABLE);
        }
    }

    private int pageCount() {
        return Math.max(1, (servers.size() + PER_PAGE - 1) / PER_PAGE);
    }

    private int startIndex() {
        return page * PER_PAGE;
    }

    private void ensureSelectedVisible() {
        if (selected >= 0 && (selected < startIndex() || selected >= startIndex() + PER_PAGE)) {
            page = selected / PER_PAGE;
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        long now = System.nanoTime();
        float seconds = Math.min((now - lastFrame) / 1_000_000_000.0f, 0.05f);
        lastFrame = now;
        openSpring.step(modal == Modal.NONE ? 1.0 : 0.86, seconds);
        float fade = (float) openSpring.get();

        LinkenMenuLayout.Metrics metrics = LinkenMenuLayout.compute(width, height);
        float mx = mouseX / metrics.scale();
        float my = mouseY / metrics.scale();

        context.getMatrices().pushMatrix();
        context.getMatrices().scale(metrics.scale(), metrics.scale());
        LinkenMenuChrome.drawBackground(context, metrics, 0.0f, 0.0f, 1.0f);
        GlowPipeline.radial(context, metrics.width() * 0.55f, 470.0f, 720.0f,
            LinkenMenuChrome.rgba(LinkenMenuChrome.ACCENT, 10, fade));

        context.getMatrices().translate(0.0f, (1.0f - fade) * 18.0f);
        LinkenMenuChrome.drawWordmark(context, fade);
        LinkenMenuChrome.drawClock(context, metrics, fade);

        backRect = new LinkenMenuLayout.Rect(metrics.width() - LinkenMenuLayout.MARGIN - 132.0f,
            104.0f, 132.0f, 44.0f);
        drawChip(context, metrics, backRect, "BACK", LinkenMenuChrome.ICON_POWER, mx, my, fade, backSpring, seconds);

        LinkenMenuChrome.drawSpaced(context, LinkenFonts.headingLight(), "SERVER BROWSER",
            LinkenMenuLayout.MARGIN, 146.0f, 8.5f, 1.6f,
            LinkenMenuChrome.rgba(LinkenMenuChrome.ACCENT, fade));
        LinkenMenuChrome.drawText(context, LinkenFonts.headingLight(), "MULTIPLAYER",
            LinkenMenuLayout.MARGIN, 162.0f, 40.0f, LinkenMenuChrome.rgba(LinkenMenuChrome.TEXT, fade));
        String savedLabel = servers.size() == 1 ? "1 SAVED SERVER" : servers.size() + " SAVED SERVERS";
        LinkenMenuChrome.drawText(context, LinkenFonts.body(),
            servers.isEmpty() ? "NO SERVERS SAVED YET" : savedLabel + "  /  DOUBLE CLICK TO JOIN",
            LinkenMenuLayout.MARGIN, 218.0f, 11.0f, LinkenMenuChrome.rgba(LinkenMenuChrome.MUTED2, fade));

        float actionY = 160.0f;
        addRect = new LinkenMenuLayout.Rect(metrics.width() - LinkenMenuLayout.MARGIN - 164.0f, actionY, 164.0f, 50.0f);
        directRect = new LinkenMenuLayout.Rect(addRect.x() - 12.0f - 210.0f, actionY, 210.0f, 50.0f);
        drawButton(context, metrics, addRect, "ADD SERVER", true, mx, my, fade, 0.3f, seconds);
        drawButton(context, metrics, directRect, "DIRECT CONNECT", false, mx, my, fade, 1.3f, seconds);

        float gridTop = 256.0f;
        float gapX = 16.0f;
        float gapY = 16.0f;
        float gridWidth = metrics.width() - LinkenMenuLayout.MARGIN * 2.0f;
        float cardWidth = (gridWidth - gapX * (COLUMNS - 1)) / COLUMNS;
        float cardHeight = 198.0f;

        for (int i = 0; i < PER_PAGE; i++) {
            int index = startIndex() + i;
            int column = i % COLUMNS;
            int row = i / COLUMNS;
            LinkenMenuLayout.Rect rect = new LinkenMenuLayout.Rect(
                LinkenMenuLayout.MARGIN + column * (cardWidth + gapX),
                gridTop + row * (cardHeight + gapY), cardWidth, cardHeight);
            cardRects[i] = rect;
            if (index >= servers.size()) {
                cardHover[i].step(0.0, seconds);
                continue;
            }
            drawServerCard(context, metrics, servers.get(index), index, rect, mx, my, fade, seconds, i);
        }

        if (servers.isEmpty()) {
            drawEmptyState(context, metrics, gridTop, cardWidth, cardHeight, fade);
        }

        float toolbarY = gridTop + ROWS * cardHeight + (ROWS - 1) * gapY + 22.0f;
        drawToolbar(context, metrics, mx, my, fade, seconds, toolbarY);
        context.getMatrices().popMatrix();

        if (modal != Modal.NONE) {
            context.fill(0, 0, width, height, 0xA8050806);
            context.getMatrices().pushMatrix();
            context.getMatrices().scale(metrics.scale(), metrics.scale());
            drawModal(context, metrics, mx, my, fade, seconds);
            context.getMatrices().popMatrix();
        }
    }

    private void drawChip(DrawContext context, LinkenMenuLayout.Metrics metrics, LinkenMenuLayout.Rect rect,
                          String label, int icon, float mx, float my, float fade, Spring spring, float seconds) {
        boolean hovered = rect.contains(mx, my);
        spring.step(hovered ? 1.0 : 0.0, seconds);
        float hover = (float) spring.get();
        LinkenMenuChrome.drawSecondary(context, rect.x(), rect.y(), rect.width(), rect.height(),
            8.0f, hover, false, fade, metrics.scale());
        float iconX = rect.x() + 16.0f;
        if (LinkenFonts.icons() != null) {
            IconPipeline.draw(context, LinkenFonts.icons(), icon, iconX + 7.0f,
                rect.y() + rect.height() * 0.5f, 14.0f,
                LinkenMenuChrome.rgba(LinkenMenuChrome.mix(LinkenMenuChrome.MUTED2, LinkenMenuChrome.ACCENT, hover), fade));
        }
        LinkenMenuChrome.drawText(context, LinkenFonts.headingLight(), label, iconX + 24.0f,
            rect.y() + 15.0f, 10.5f,
            LinkenMenuChrome.rgba(LinkenMenuChrome.mix(LinkenMenuChrome.TEXT3, LinkenMenuChrome.TEXT, hover), fade));
    }

    private void drawButton(DrawContext context, LinkenMenuLayout.Metrics metrics, LinkenMenuLayout.Rect rect,
                            String label, boolean primary, float mx, float my, float fade, float index, float seconds) {
        int slot = (int) index;
        Spring spring = toolbarHover[slot % toolbarHover.length];
        boolean hovered = rect.contains(mx, my);
        spring.step(hovered ? 1.0 : 0.0, seconds);
        float hover = (float) spring.get();
        if (primary) {
            LinkenMenuChrome.roundedFill(context, rect.x() + 4.0f, rect.y() + rect.height() - 6.0f,
                rect.width() - 8.0f, 12.0f, 8.0f,
                LinkenMenuChrome.rgba(LinkenMenuChrome.ACCENT, Math.round(55 + 35 * hover), fade), metrics.scale());
            LinkenMenuChrome.roundedFill(context, rect.x(), rect.y() - hover, rect.width(), rect.height(), 8.0f,
                LinkenMenuChrome.rgba(LinkenMenuChrome.mix(LinkenMenuChrome.ACCENT, 0xFFFFFF, hover * 0.07f), fade),
                metrics.scale());
        } else {
            LinkenMenuChrome.drawSecondary(context, rect.x(), rect.y() - hover, rect.width(), rect.height(),
                8.0f, hover, false, fade, metrics.scale());
        }
        LinkenMenuChrome.drawCentered(context, LinkenFonts.headingLight(), label,
            rect.x() + rect.width() * 0.5f, rect.y() + 15.0f - hover, 10.5f,
            LinkenMenuChrome.rgba(primary ? LinkenMenuChrome.INK : LinkenMenuChrome.TEXT3, fade));
    }

    private void drawServerCard(DrawContext context, LinkenMenuLayout.Metrics metrics, ServerInfo info, int index,
                                LinkenMenuLayout.Rect rect, float mx, float my, float fade, float seconds, int slot) {
        boolean hovered = rect.contains(mx, my);
        cardHover[slot].step(hovered ? 1.0 : 0.0, seconds);
        float hover = (float) cardHover[slot].get();
        boolean active = index == selected;

        int fill = LinkenMenuChrome.mix(0x111713, 0x18211B, hover);
        LinkenMenuChrome.roundedFill(context, rect.x(), rect.y() - hover * 2.0f, rect.width(), rect.height(),
            12.0f, LinkenMenuChrome.rgba(fill, 238, fade), metrics.scale());
        int border = active ? LinkenMenuChrome.ACCENT : 0xFFFFFF;
        int alpha = active ? 170 : Math.round(20 + hover * 50);
        LinkenMenuChrome.roundedOutline(context, rect.x(), rect.y() - hover * 2.0f, rect.width(), rect.height(),
            12.0f, active ? 1.4f : 1.0f, LinkenMenuChrome.rgba(border, alpha, fade), metrics.scale());

        float iconSize = 48.0f;
        float iconX = rect.x() + 18.0f;
        float iconY = rect.y() + 18.0f - hover * 2.0f;
        LinkenMenuChrome.roundedFill(context, iconX, iconY, iconSize, iconSize, 9.0f,
            LinkenMenuChrome.rgba(active ? LinkenMenuChrome.mix(LinkenMenuChrome.ACCENT, 0x0A0F0C, 0.35f) : 0x253029, 220, fade), metrics.scale());
        String initial = info.name == null || info.name.isBlank()
            ? "?" : info.name.substring(0, 1).toUpperCase(Locale.ROOT);
        LinkenMenuChrome.drawCentered(context, LinkenFonts.heading(), initial,
            iconX + iconSize * 0.5f, iconY + 10.0f, 23.0f,
            LinkenMenuChrome.rgba(active ? LinkenMenuChrome.INK : LinkenMenuChrome.TEXT2, fade));

        float textX = iconX + iconSize + 14.0f;
        float textRight = rect.right() - 18.0f;
        String name = LinkenMenuChrome.ellipsize(LinkenFonts.headingLight(),
            info.name == null ? "SERVER" : info.name, textRight - textX, 15.0f);
        LinkenMenuChrome.drawText(context, LinkenFonts.headingLight(), name,
            textX, rect.y() + 21.0f - hover * 2.0f, 15.0f,
            LinkenMenuChrome.rgba(LinkenMenuChrome.TEXT, fade));
        String address = LinkenMenuChrome.ellipsize(LinkenFonts.mono(), info.address,
            textRight - textX, 9.5f);
        LinkenMenuChrome.drawText(context, LinkenFonts.mono(), address,
            textX, rect.y() + 44.0f - hover * 2.0f, 9.5f,
            LinkenMenuChrome.rgba(LinkenMenuChrome.MUTED3, fade));

        String motd = info.label == null ? "" : info.label.getString().replace('\n', ' ');
        motd = LinkenMenuChrome.ellipsize(LinkenFonts.body(), motd, rect.width() - 36.0f, 10.0f);
        if (motd.isBlank()) {
            motd = "NO DESCRIPTION";
        }
        LinkenMenuChrome.drawText(context, LinkenFonts.body(), motd,
            rect.x() + 18.0f, rect.y() + 86.0f - hover * 2.0f, 10.0f,
            LinkenMenuChrome.rgba(LinkenMenuChrome.TEXT3, fade));

        String status = statusLabel(info);
        int statusColor = statusColor(info);
        CirclePipeline.draw(context, rect.x() + 22.0f, rect.bottom() - 27.0f - hover * 2.0f, 4.0f,
            LinkenMenuChrome.rgba(statusColor, 220, fade));
        LinkenMenuChrome.drawText(context, LinkenFonts.body(), status,
            rect.x() + 32.0f, rect.bottom() - 35.0f - hover * 2.0f, 9.0f,
            LinkenMenuChrome.rgba(statusColor, fade));
        String ping = info.getStatus() == ServerInfo.Status.SUCCESSFUL && info.ping > 0
            ? info.ping + " ms" : "";
        if (!ping.isBlank()) {
            LinkenMenuChrome.drawRightText(context, LinkenFonts.mono(), ping,
                rect.right() - 18.0f, rect.bottom() - 35.0f - hover * 2.0f, 9.0f,
                LinkenMenuChrome.rgba(LinkenMenuChrome.MUTED3, fade));
        }
        if (active) {
            LinkenMenuChrome.drawSpaced(context, LinkenFonts.headingLight(), "SELECTED",
                rect.right() - 74.0f, rect.y() + 66.0f - hover * 2.0f, 7.0f, 1.1f,
                LinkenMenuChrome.rgba(LinkenMenuChrome.ACCENT, fade));
        }
    }

    private void drawEmptyState(DrawContext context, LinkenMenuLayout.Metrics metrics, float gridTop,
                                float cardWidth, float cardHeight, float fade) {
        float cx = metrics.width() * 0.5f;
        LinkenMenuChrome.drawCentered(context, LinkenFonts.headingLight(), "NOTHING HERE YET",
            cx, gridTop + 70.0f, 22.0f, LinkenMenuChrome.rgba(LinkenMenuChrome.TEXT2, fade));
        LinkenMenuChrome.drawCentered(context, LinkenFonts.body(), "ADD A SERVER OR USE DIRECT CONNECT",
            cx, gridTop + 106.0f, 10.0f, LinkenMenuChrome.rgba(LinkenMenuChrome.MUTED2, fade));
    }

    private void drawToolbar(DrawContext context, LinkenMenuLayout.Metrics metrics, float mx, float my,
                             float fade, float seconds, float y) {
        joinRect = new LinkenMenuLayout.Rect(LinkenMenuLayout.MARGIN, y, 206.0f, 46.0f);
        editRect = new LinkenMenuLayout.Rect(joinRect.right() + 12.0f, y, 96.0f, 46.0f);
        deleteRect = new LinkenMenuLayout.Rect(editRect.right() + 8.0f, y, 96.0f, 46.0f);
        refreshRect = new LinkenMenuLayout.Rect(deleteRect.right() + 8.0f, y, 112.0f, 46.0f);

        drawToolbarButton(context, metrics, joinRect, selected >= 0 ? "JOIN SERVER" : "SELECT A SERVER",
            selected >= 0, mx, my, fade, seconds, 0);
        drawToolbarButton(context, metrics, editRect, "EDIT", selected >= 0, mx, my, fade, seconds, 1);
        drawToolbarButton(context, metrics, deleteRect, "DELETE", selected >= 0, mx, my, fade, seconds, 2);
        drawToolbarButton(context, metrics, refreshRect, "REFRESH", true, mx, my, fade, seconds, 3);

        if (selected >= 0 && selected < servers.size()) {
            String name = LinkenMenuChrome.ellipsize(LinkenFonts.body(), servers.get(selected).name,
                280.0f, 10.0f);
            LinkenMenuChrome.drawText(context, LinkenFonts.body(), "SELECTED  /  " + name,
                refreshRect.right() + 18.0f, y + 17.0f, 9.5f,
                LinkenMenuChrome.rgba(LinkenMenuChrome.MUTED2, fade));
        }

        float navX = metrics.width() - LinkenMenuLayout.MARGIN - 160.0f;
        prevRect = new LinkenMenuLayout.Rect(navX, y, 44.0f, 46.0f);
        nextRect = new LinkenMenuLayout.Rect(navX + 116.0f, y, 44.0f, 46.0f);
        boolean canPrev = page > 0;
        boolean canNext = page + 1 < pageCount();
        drawToolbarButton(context, metrics, prevRect, "<", canPrev, mx, my, fade, seconds, 0);
        drawToolbarButton(context, metrics, nextRect, ">", canNext, mx, my, fade, seconds, 0);
        LinkenMenuChrome.drawCentered(context, LinkenFonts.mono(), (page + 1) + " / " + pageCount(),
            navX + 80.0f, y + 16.0f, 10.0f, LinkenMenuChrome.rgba(LinkenMenuChrome.MUTED2, fade));
    }

    private void drawToolbarButton(DrawContext context, LinkenMenuLayout.Metrics metrics, LinkenMenuLayout.Rect rect,
                                   String label, boolean enabled, float mx, float my, float fade, float seconds, int slot) {
        boolean hovered = enabled && rect.contains(mx, my);
        Spring spring = toolbarHover[slot % toolbarHover.length];
        spring.step(hovered ? 1.0 : 0.0, seconds);
        float hover = (float) spring.get();
        if (slot == 0) {
            if (enabled) {
                LinkenMenuChrome.roundedFill(context, rect.x() + 4.0f, rect.y() + rect.height() - 5.0f,
                    rect.width() - 8.0f, 10.0f, 7.0f,
                    LinkenMenuChrome.rgba(LinkenMenuChrome.ACCENT, Math.round(45 + 35 * hover), fade), metrics.scale());
            }
            LinkenMenuChrome.roundedFill(context, rect.x(), rect.y() - hover, rect.width(), rect.height(), 7.0f,
                LinkenMenuChrome.rgba(enabled
                    ? LinkenMenuChrome.mix(LinkenMenuChrome.ACCENT, 0xFFFFFF, hover * 0.07f)
                    : 0x202622, fade), metrics.scale());
        } else {
            LinkenMenuChrome.drawSecondary(context, rect.x(), rect.y() - hover, rect.width(), rect.height(),
                7.0f, enabled ? hover : 0.0f, false, fade, metrics.scale());
        }
        int color = enabled ? (slot == 0 ? LinkenMenuChrome.INK : LinkenMenuChrome.TEXT3) : LinkenMenuChrome.MUTED4;
        LinkenMenuChrome.drawCentered(context, LinkenFonts.headingLight(), label,
            rect.x() + rect.width() * 0.5f, rect.y() + 14.0f - hover, 9.5f,
            LinkenMenuChrome.rgba(color, fade));
    }

    private void drawModal(DrawContext context, LinkenMenuLayout.Metrics metrics, float mx, float my,
                           float fade, float seconds) {
        float width = modal == Modal.DELETE ? 540.0f : 620.0f;
        float height = modal == Modal.DELETE ? 270.0f : (modal == Modal.DIRECT ? 320.0f : 388.0f);
        float x = (metrics.width() - width) * 0.5f;
        float y = (metrics.height() - height) * 0.5f;
        modalBounds = new LinkenMenuLayout.Rect(x, y, width, height);
        LinkenMenuChrome.roundedFill(context, x, y, width, height, 14.0f,
            LinkenMenuChrome.rgba(0x0E1411, 250, fade), metrics.scale());
        LinkenMenuChrome.roundedOutline(context, x, y, width, height, 14.0f, 1.0f,
            LinkenMenuChrome.rgba(0xFFFFFF, 32, fade), metrics.scale());

        String title = switch (modal) {
            case ADD -> "ADD SERVER";
            case EDIT -> "EDIT SERVER";
            case DIRECT -> "DIRECT CONNECT";
            case DELETE -> "DELETE SERVER";
            case NONE -> "";
        };
        LinkenMenuChrome.drawText(context, LinkenFonts.headingLight(), title, x + 30.0f, y + 26.0f, 20.0f,
            LinkenMenuChrome.rgba(LinkenMenuChrome.TEXT, fade));
        LinkenMenuChrome.drawText(context, LinkenFonts.body(),
            modal == Modal.DELETE ? "THIS CANNOT BE UNDONE." : "FILL IN THE DETAILS BELOW.",
            x + 30.0f, y + 56.0f, 9.0f, LinkenMenuChrome.rgba(LinkenMenuChrome.MUTED2, fade));

        modalCloseRect = new LinkenMenuLayout.Rect(x + width - 52.0f, y + 22.0f, 30.0f, 30.0f);
        modalCloseSpring.step(modalCloseRect.contains(mx, my) ? 1.0 : 0.0, seconds);
        LinkenMenuChrome.drawSecondary(context, modalCloseRect.x(), modalCloseRect.y(), 30.0f, 30.0f,
            7.0f, (float) modalCloseSpring.get(), false, fade, metrics.scale());
        LinkenMenuChrome.drawCentered(context, LinkenFonts.body(), "x", modalCloseRect.x() + 15.0f,
            modalCloseRect.y() + 9.0f, 10.0f, LinkenMenuChrome.rgba(LinkenMenuChrome.MUTED2, fade));

        nameFieldRect = null;
        addressFieldRect = null;
        if (modal == Modal.ADD || modal == Modal.EDIT || modal == Modal.DIRECT) {
            float fieldY = y + 102.0f;
            if (modal != Modal.DIRECT) {
                nameFieldRect = new LinkenMenuLayout.Rect(x + 30.0f, fieldY, width - 60.0f, 54.0f);
                drawField(context, metrics, nameFieldRect, "SERVER NAME", nameInput, focusedField == 0, fade);
                fieldY += 92.0f;
            }
            addressFieldRect = new LinkenMenuLayout.Rect(x + 30.0f, fieldY, width - 60.0f, 54.0f);
            drawField(context, metrics, addressFieldRect, "SERVER ADDRESS", addressInput, focusedField == 1, fade);
        } else if (modal == Modal.DELETE) {
            String target = selected >= 0 && selected < servers.size() ? servers.get(selected).name : "";
            LinkenMenuChrome.drawCentered(context, LinkenFonts.headingLight(), target,
                x + width * 0.5f, y + 118.0f, 18.0f, LinkenMenuChrome.rgba(LinkenMenuChrome.TEXT2, fade));
        }

        if (modalError != null) {
            LinkenMenuChrome.drawText(context, LinkenFonts.body(), modalError, x + 30.0f,
                y + height - 96.0f, 9.0f, LinkenMenuChrome.rgba(LinkenMenuChrome.DANGER, fade));
        }

        modalCancelRect = new LinkenMenuLayout.Rect(x + width - 30.0f - 110.0f - 10.0f - 130.0f,
            y + height - 64.0f, 110.0f, 42.0f);
        modalConfirmRect = new LinkenMenuLayout.Rect(x + width - 30.0f - 130.0f, y + height - 64.0f, 130.0f, 42.0f);
        drawModalButton(context, metrics, modalCancelRect, "CANCEL", false, mx, my, fade, seconds, modalCancelSpring);
        drawModalButton(context, metrics, modalConfirmRect, modal == Modal.DELETE ? "DELETE" : "CONFIRM",
            true, mx, my, fade, seconds, modalConfirmSpring);
    }

    private void drawField(DrawContext context, LinkenMenuLayout.Metrics metrics, LinkenMenuLayout.Rect rect,
                           String label, String value, boolean focused, float fade) {
        LinkenMenuChrome.drawSpaced(context, LinkenFonts.headingLight(), label, rect.x(), rect.y() - 20.0f,
            7.5f, 1.4f, LinkenMenuChrome.rgba(focused ? LinkenMenuChrome.ACCENT : LinkenMenuChrome.MUTED3, fade));
        LinkenMenuChrome.roundedFill(context, rect.x(), rect.y(), rect.width(), rect.height(), 7.0f,
            LinkenMenuChrome.rgba(focused ? 0x212C24 : 0x181F1A, 240, fade), metrics.scale());
        LinkenMenuChrome.roundedOutline(context, rect.x(), rect.y(), rect.width(), rect.height(), 7.0f,
            focused ? 1.3f : 1.0f,
            LinkenMenuChrome.rgba(focused ? LinkenMenuChrome.ACCENT : 0xFFFFFF,
                focused ? 170 : 28, fade), metrics.scale());
        String shown = value.isBlank() ? (focused ? "" : "TYPE HERE...") : value;
        LinkenMenuChrome.drawText(context, LinkenFonts.mono(), shown, rect.x() + 16.0f,
            rect.y() + 18.0f, 10.0f,
            LinkenMenuChrome.rgba(value.isBlank() && !focused ? LinkenMenuChrome.MUTED4 : LinkenMenuChrome.TEXT2, fade));
        if (focused && (System.currentTimeMillis() / 500L) % 2L == 0L) {
            float caretX = rect.x() + 16.0f + LinkenMenuChrome.textWidth(LinkenFonts.mono(), value, 10.0f);
            context.fill(Math.round(caretX), Math.round(rect.y() + 17.0f),
                Math.round(caretX + 1.0f), Math.round(rect.y() + 37.0f),
                LinkenMenuChrome.rgba(LinkenMenuChrome.ACCENT, fade));
        }
    }

    private void drawModalButton(DrawContext context, LinkenMenuLayout.Metrics metrics, LinkenMenuLayout.Rect rect,
                                 String label, boolean primary, float mx, float my, float fade, float seconds,
                                 Spring spring) {
        boolean hovered = rect.contains(mx, my);
        spring.step(hovered ? 1.0 : 0.0, seconds);
        float hover = (float) spring.get();
        if (primary) {
            LinkenMenuChrome.roundedFill(context, rect.x(), rect.y() - hover, rect.width(), rect.height(), 7.0f,
                LinkenMenuChrome.rgba(LinkenMenuChrome.mix(LinkenMenuChrome.ACCENT, 0xFFFFFF, hover * 0.07f), fade),
                metrics.scale());
        } else {
            LinkenMenuChrome.drawSecondary(context, rect.x(), rect.y() - hover, rect.width(), rect.height(),
                7.0f, hover, false, fade, metrics.scale());
        }
        LinkenMenuChrome.drawCentered(context, LinkenFonts.headingLight(), label,
            rect.x() + rect.width() * 0.5f, rect.y() + 13.0f - hover, 9.5f,
            LinkenMenuChrome.rgba(primary ? LinkenMenuChrome.INK : LinkenMenuChrome.TEXT3, fade));
    }

    private String statusLabel(ServerInfo info) {
        if (info.getStatus() == ServerInfo.Status.SUCCESSFUL) {
            if (info.playerCountLabel != null && !info.playerCountLabel.getString().isBlank()) {
                return info.playerCountLabel.getString();
            }
            return "ONLINE";
        }
        return switch (info.getStatus()) {
            case PINGING -> "PINGING...";
            case UNREACHABLE -> "OFFLINE";
            case INCOMPATIBLE -> "INCOMPATIBLE";
            default -> "UNKNOWN";
        };
    }

    private int statusColor(ServerInfo info) {
        return switch (info.getStatus()) {
            case SUCCESSFUL -> LinkenMenuChrome.ACCENT;
            case PINGING -> LinkenMenuChrome.MUTED2;
            case UNREACHABLE, INCOMPATIBLE -> LinkenMenuChrome.DANGER;
            default -> LinkenMenuChrome.MUTED3;
        };
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubleClick) {
        LinkenMenuLayout.Metrics metrics = LinkenMenuLayout.compute(width, height);
        float mx = (float) click.x() / metrics.scale();
        float my = (float) click.y() / metrics.scale();

        if (modal != Modal.NONE) {
            handleModalClick(mx, my);
            return true;
        }
        if (backRect != null && backRect.contains(mx, my)) {
            playClick();
            close();
            return true;
        }
        if (addRect != null && addRect.contains(mx, my)) {
            playClick();
            openModal(Modal.ADD);
            return true;
        }
        if (directRect != null && directRect.contains(mx, my)) {
            playClick();
            openModal(Modal.DIRECT);
            return true;
        }
        for (int i = 0; i < cardRects.length; i++) {
            LinkenMenuLayout.Rect rect = cardRects[i];
            int index = startIndex() + i;
            if (rect != null && index < servers.size() && rect.contains(mx, my)) {
                long now = Util.getMeasuringTimeMs();
                if (lastClickedIndex == index && now - lastClickTime < 280L) {
                    selected = index;
                    joinSelected();
                } else {
                    selected = index;
                    lastClickedIndex = index;
                    lastClickTime = now;
                    playClick();
                }
                return true;
            }
        }
        if (joinRect != null && joinRect.contains(mx, my)) {
            if (selected >= 0) {
                playClick();
                joinSelected();
            }
            return true;
        }
        if (editRect != null && editRect.contains(mx, my)) {
            if (selected >= 0) {
                playClick();
                openModal(Modal.EDIT);
            }
            return true;
        }
        if (deleteRect != null && deleteRect.contains(mx, my)) {
            if (selected >= 0) {
                playClick();
                openModal(Modal.DELETE);
            }
            return true;
        }
        if (refreshRect != null && refreshRect.contains(mx, my)) {
            playClick();
            refreshServers();
            return true;
        }
        if (prevRect != null && prevRect.contains(mx, my) && page > 0) {
            playClick();
            page--;
            return true;
        }
        if (nextRect != null && nextRect.contains(mx, my) && page + 1 < pageCount()) {
            playClick();
            page++;
            return true;
        }
        return super.mouseClicked(click, doubleClick);
    }

    private void handleModalClick(float mx, float my) {
        if (modalCloseRect != null && modalCloseRect.contains(mx, my)
            || modalCancelRect != null && modalCancelRect.contains(mx, my)) {
            playClick();
            closeModal();
            return;
        }
        if (modalConfirmRect != null && modalConfirmRect.contains(mx, my)) {
            playClick();
            confirmModal();
            return;
        }
        if (nameFieldRect != null && nameFieldRect.contains(mx, my)) {
            focusedField = 0;
            return;
        }
        if (addressFieldRect != null && addressFieldRect.contains(mx, my)) {
            focusedField = 1;
            return;
        }
        if (modalBounds == null || !modalBounds.contains(mx, my)) {
            closeModal();
        }
    }

    @Override
    public boolean charTyped(CharInput input) {
        if (modal == Modal.NONE || focusedField < 0) {
            return super.charTyped(input);
        }
        if (!input.isValidChar() || Character.isISOControl(input.codepoint())) {
            return false;
        }
        String value = input.asString();
        if (focusedField == 0) {
            if (nameInput.length() < 40) {
                nameInput += value;
            }
        } else if (addressInput.length() < 128) {
            addressInput += value;
        }
        return true;
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        int key = input.key();
        if (modal != Modal.NONE) {
            if (key == GLFW.GLFW_KEY_ESCAPE) {
                playClick();
                closeModal();
                return true;
            }
            if (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER) {
                playClick();
                confirmModal();
                return true;
            }
            if (key == GLFW.GLFW_KEY_TAB) {
                if (nameFieldRect != null) {
                    focusedField = focusedField == 0 ? 1 : 0;
                }
                return true;
            }
            if (key == GLFW.GLFW_KEY_BACKSPACE && focusedField >= 0) {
                if (focusedField == 0 && !nameInput.isEmpty()) {
                    nameInput = nameInput.substring(0, nameInput.offsetByCodePoints(nameInput.length(), -1));
                } else if (focusedField == 1 && !addressInput.isEmpty()) {
                    addressInput = addressInput.substring(0, addressInput.offsetByCodePoints(addressInput.length(), -1));
                }
                return true;
            }
            return true;
        }
        if (key == GLFW.GLFW_KEY_ESCAPE) {
            close();
            return true;
        }
        if (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER) {
            if (selected >= 0) {
                joinSelected();
            }
            return true;
        }
        if (key == GLFW.GLFW_KEY_LEFT) {
            if (page > 0) {
                page--;
            }
            return true;
        }
        if (key == GLFW.GLFW_KEY_RIGHT) {
            if (page + 1 < pageCount()) {
                page++;
            }
            return true;
        }
        if (key == GLFW.GLFW_KEY_DOWN) {
            if (!servers.isEmpty()) {
                selected = Math.min(servers.size() - 1, selected + COLUMNS);
                ensureSelectedVisible();
            }
            return true;
        }
        if (key == GLFW.GLFW_KEY_UP) {
            if (!servers.isEmpty()) {
                selected = Math.max(0, selected - COLUMNS);
                ensureSelectedVisible();
            }
            return true;
        }
        if (key == GLFW.GLFW_KEY_F5) {
            refreshServers();
            return true;
        }
        return super.keyPressed(input);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (modal != Modal.NONE) {
            return true;
        }
        if (verticalAmount > 0 && page > 0) {
            page--;
            return true;
        }
        if (verticalAmount < 0 && page + 1 < pageCount()) {
            page++;
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    private void openModal(Modal type) {
        modal = type;
        modalError = null;
        focusedField = type == Modal.DIRECT ? 1 : 0;
        if (type == Modal.ADD) {
            nameInput = "";
            addressInput = "";
        } else if (type == Modal.EDIT && selected >= 0 && selected < servers.size()) {
            ServerInfo info = servers.get(selected);
            nameInput = info.name == null ? "" : info.name;
            addressInput = info.address == null ? "" : info.address;
        } else if (type == Modal.DIRECT) {
            addressInput = "";
            nameInput = "";
        }
    }

    private void closeModal() {
        modal = Modal.NONE;
        focusedField = -1;
        modalError = null;
    }

    private void confirmModal() {
        if (modal == Modal.DELETE) {
            if (selected >= 0 && selected < servers.size() && serverList != null) {
                serverList.remove(servers.get(selected));
                serverList.saveFile();
                selected = -1;
                closeModal();
                refreshServers();
            }
            return;
        }
        String address = addressInput.trim();
        if (address.isEmpty() || !ServerAddress.isValid(address)) {
            modalError = "ENTER A VALID SERVER ADDRESS.";
            focusedField = 1;
            return;
        }
        if (modal == Modal.DIRECT) {
            closeModal();
            ServerInfo info = new ServerInfo(address, address, ServerInfo.ServerType.OTHER);
            ConnectScreen.connect(this, client, ServerAddress.parse(address), info, false, null);
            return;
        }
        String name = nameInput.trim().isEmpty() ? address : nameInput.trim();
        if (modal == Modal.ADD && serverList != null) {
            serverList.add(new ServerInfo(name, address, ServerInfo.ServerType.OTHER), false);
            serverList.saveFile();
            closeModal();
            refreshServers();
            return;
        }
        if (modal == Modal.EDIT && serverList != null && selected >= 0 && selected < servers.size()) {
            ServerInfo info = servers.get(selected);
            info.name = name;
            info.address = address;
            serverList.saveFile();
            closeModal();
            refreshServers();
        }
    }

    private void joinSelected() {
        if (selected < 0 || selected >= servers.size() || client == null) {
            return;
        }
        ServerInfo info = servers.get(selected);
        if (info.address == null || !ServerAddress.isValid(info.address)) {
            return;
        }
        ConnectScreen.connect(this, client, ServerAddress.parse(info.address), info, false, null);
    }

    @Override
    public void close() {
        pinger.cancel();
        if (client != null) {
            client.setScreen(parent);
        }
    }

    private void playClick() {
        if (client != null) {
            client.getSoundManager().play(PositionedSoundInstance.ui(LinkenSounds.UI_CLICK, 0.8f, 1.0f));
        }
    }
}
