package dev.linkenvisuals.hud;

import dev.linkenvisuals.render.Fonts;
import dev.linkenvisuals.util.ColorMath;
import dev.linkenvisuals.LinkenVisuals;
import dev.linkenvisuals.render.CirclePipeline;
import dev.linkenvisuals.render.GlassPanelPipeline;
import dev.linkenvisuals.render.GlowPipeline;
import dev.linkenvisuals.render.GlyphAtlas;
import dev.linkenvisuals.render.MsdfText;
import dev.linkenvisuals.render.NeonEdge;
import dev.linkenvisuals.module.impl.StreamerModeModule;
import dev.linkenvisuals.ui.Spring;
import dev.linkenvisuals.ui.Theme;
import dev.linkenvisuals.ui.style.StyleFlavor;
import dev.linkenvisuals.util.text.StreamerRules;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.util.Identifier;

public final class Watermark extends HudElement {
    private static final Identifier LOGO = LinkenVisuals.id("textures/gui/logo.png");

    private static final float HEIGHT = 17.0f;
    private static final float PADDING = 7.0f;
    private static final float LOGO_SIZE = 10.0f;
    private static final float NAME_SIZE = 8.4f;
    private static final float QUIET_SIZE = 5.8f;
    private static final float GAP = 8.0f;

    private GlyphAtlas text;
    private GlyphAtlas bold;
    private final Spring nameWidth = new Spring(0, 1100, 42);
    private long lastTick = System.nanoTime();
    private float totalWidth = 150.0f;
    private float fpsSlot = -1.0f;

    public Watermark() {
        super("watermark", 0.045f, 0.045f);
    }

    private void ensureLoaded() {
        if (text == null) {
            text = Fonts.textFont();
        }
        if (bold == null) {
            bold = Fonts.titleFont();
        }
        if (fpsSlot < 0.0f) {
            fpsSlot = MsdfText.width(bold, "999", QUIET_SIZE);
        }
    }

    @Override
    public float width() {
        return totalWidth;
    }

    @Override
    public float height() {
        return HEIGHT;
    }

    private static final float ASCENDER = 0.952f;
    private static final float CAP_HEIGHT = 0.70f;

    private static float baselineFor(float centerY, float size) {
        return centerY + CAP_HEIGHT * 0.5f * size;
    }

    private static float topFor(float centerY, float size) {
        return baselineFor(centerY, size) - ASCENDER * size;
    }

    @Override
    public void render(DrawContext context, float x, float y) {
        ensureLoaded();
        MinecraftClient client = MinecraftClient.getInstance();

        String username = client.getSession() != null ? client.getSession().getUsername() : "Player";
        StreamerRules streamer = StreamerModeModule.rules();
        if (streamer != null) {
            username = streamer.sanitize(username);
        }
        String fps = String.valueOf(client.getCurrentFps());
        String ping = currentPing(client) + "ms";
        String server = client.getCurrentServerEntry() != null
            ? client.getCurrentServerEntry().address : "singleplayer";

        long now = System.nanoTime();
        float deltaSeconds = (float) Math.min((now - lastTick) / 1_000_000_000.0, 0.05);
        lastTick = now;

        nameWidth.step(MsdfText.width(bold, username, NAME_SIZE), deltaSeconds);
        float nameSpan = (float) nameWidth.get();

        float unitWidth = MsdfText.width(text, "fps", QUIET_SIZE);
        float quiet = fpsSlot + 1.5f + unitWidth + GAP
            + MsdfText.width(text, ping, QUIET_SIZE) + GAP
            + MsdfText.width(text, server, QUIET_SIZE);
        totalWidth = PADDING + LOGO_SIZE + GAP + nameSpan + GAP + quiet + PADDING;

        HudSurface.panel(context, "watermark", x, y, totalWidth, HEIGHT, 1.0f);

        HudAlign.Row row = HudAlign.row(x, y, HEIGHT, PADDING, GAP);

        float logoX = row.next(LOGO_SIZE);
        float logoY = row.centerY(LOGO_SIZE);
        GlowPipeline.radial(context, logoX + LOGO_SIZE * 0.5f, row.midY(), LOGO_SIZE * 1.2f,
            ColorMath.withAlpha(NeonEdge.cool(), 75));
        HudAlign.drawIcon(context, LOGO, logoX, logoY, LOGO_SIZE, 128, ColorMath.withAlpha(0xFFFFFF, 255));

        float nameX = row.next(nameSpan);
        MsdfText.draw(context, bold, username, nameX, row.centerTextY(NAME_SIZE), NAME_SIZE,
            ColorMath.withAlpha(Theme.lighten(NeonEdge.cool(), 0.18f), 255));

        float fpsWidth = MsdfText.width(bold, fps, QUIET_SIZE);
        float fpsSlotW = fpsSlot + 1.5f;
        float fpsX = row.next(fpsSlotW + unitWidth);
        MsdfText.draw(context, bold, fps, fpsX + fpsSlotW - fpsWidth, row.centerTextY(QUIET_SIZE), QUIET_SIZE,
            ColorMath.withAlpha(0xFFFFFF, 205));
        MsdfText.draw(context, text, "fps", fpsX + fpsSlotW, row.centerTextY(QUIET_SIZE), QUIET_SIZE,
            ColorMath.withAlpha(0xFFFFFF, 90));

        drawQuiet(context, ping, row);
        drawQuiet(context, server, row);
    }

    private void drawQuiet(DrawContext context, String value, HudAlign.Row row) {
        float at = row.current();
        HudAlign.drawDivider(context, at - GAP * 0.5f, row.midY(), 4.0f, ColorMath.withAlpha(0xFFFFFF, 30));
        float textX = row.next(MsdfText.width(text, value, QUIET_SIZE));
        MsdfText.draw(context, text, value, textX, row.centerTextY(QUIET_SIZE), QUIET_SIZE,
            ColorMath.withAlpha(0xFFFFFF, 150));
    }
    private static int currentPing(MinecraftClient client) {
        if (client.player == null || client.getNetworkHandler() == null) {
            return 0;
        }
        PlayerListEntry entry = client.getNetworkHandler().getPlayerListEntry(client.player.getUuid());
        return entry != null ? entry.getLatency() : 0;
    }

}
