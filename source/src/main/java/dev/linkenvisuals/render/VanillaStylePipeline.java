package dev.linkenvisuals.render;

import dev.linkenvisuals.LinkenVisuals;
import dev.linkenvisuals.util.TextKeys;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.RotatingCubeMapRenderer;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

/**
 * Authentic enhanced Vanilla UI pipeline for LinkenVisuals.
 * Integrates pixel-art icon assets from Icons / Icons-Menus and polished
 * textures from Better Minecraft Menu & Iconic Options.
 */
public final class VanillaStylePipeline {
    public static final Identifier WIDGETS = LinkenVisuals.id("textures/gui/vanilla/widgets.png");
    public static final Identifier SLIDER = LinkenVisuals.id("textures/gui/vanilla/slider.png");
    public static final Identifier OPTIONS_BG = LinkenVisuals.id("textures/gui/vanilla/options_background.png");
    public static final Identifier DIRT_BG = Identifier.ofVanilla("textures/block/dirt.png");
    public static final Identifier LIGHT_DIRT_BG = LinkenVisuals.id("textures/gui/vanilla/light_dirt_background.png");
    public static final Identifier HEADER_SEP = LinkenVisuals.id("textures/gui/vanilla/header_separator.png");
    /** The panorama failure warning is logged once; menus render every frame. */
    private static boolean panoramaFailureLogged;
    public static final Identifier FOOTER_SEP = LinkenVisuals.id("textures/gui/vanilla/footer_separator.png");

    public static final Identifier ICONS_MAIN = LinkenVisuals.id("textures/gui/vanilla/main_menu.png");
    public static final Identifier ICONS_OPTIONS = LinkenVisuals.id("textures/gui/vanilla/options.png");
    public static final Identifier ICONS_ESCAPE = LinkenVisuals.id("textures/gui/vanilla/escape_menu.png");
    public static final Identifier ICONS_SINGLEPLAYER = LinkenVisuals.id("textures/gui/vanilla/singleplayer.png");
    public static final Identifier ICONS_MULTIPLAYER = LinkenVisuals.id("textures/gui/vanilla/multiplayer.png");
    public static final Identifier ICONS_VIDEO = LinkenVisuals.id("textures/gui/vanilla/video_settings.png");
    public static final Identifier ICONS_MODS = LinkenVisuals.id("textures/gui/vanilla/modded.png");
    public static final Identifier ICONS_CONTROLS = LinkenVisuals.id("textures/gui/vanilla/controls.png");
    public static final Identifier ICONS_MACROS = LinkenVisuals.id("textures/gui/vanilla/macros.png");
    public static final Identifier ICONS_BACK = LinkenVisuals.id("textures/gui/vanilla/back.png");
    public static final Identifier ICONS_DIRECT = LinkenVisuals.id("textures/gui/vanilla/direct_connect.png");
    public static final Identifier ICONS_RECREATE = LinkenVisuals.id("textures/gui/vanilla/recreate.png");

    public record IconRef(Identifier texture, int u, int v, int width, int height, int sheetW, int sheetH) {
    }

    private VanillaStylePipeline() {
    }

    /**
     * Locale-independent icon lookup by translation key. Accepts both vanilla keys
     * ({@code menu.singleplayer}, {@code options.sounds}, ...) and the mod's own keys
     * ({@code linkenvisuals.menu.quit}, {@code multiplayer.join}, ...). Localized text is
     * never matched, so the result does not depend on the selected game language.
     */
    private static final Map<String, IconRef> ICON_CACHE = new HashMap<>();

    public static IconRef getIconForKey(String key) {
        if (key == null || key.isBlank()) {
            return null;
        }
        // Menus call this every frame per widget: cache the tiny records instead of
        // re-allocating one per lookup (v1.0.71 allocation audit). The key set is bounded
        // (UI translation keys).
        IconRef cached = ICON_CACHE.get(key);
        if (cached != null) {
            return cached;
        }
        IconRef resolved = resolveIcon(key);
        if (resolved != null) {
            ICON_CACHE.put(key, resolved);
        }
        return resolved;
    }

    private static IconRef resolveIcon(String key) {
        // Chat sub-options ("options.chat.visibility", "options.chat.opacity", ...) share the chat icon.
        if (key.startsWith("options.chat.")) {
            return new IconRef(ICONS_OPTIONS, 140, 0, 20, 20, 240, 20);
        }

        return switch (key) {
            // Main menu
            case "menu.singleplayer", "linkenvisuals.menu.singleplayer" -> new IconRef(ICONS_MAIN, 0, 0, 16, 16, 80, 16);
            case "menu.multiplayer", "linkenvisuals.menu.multiplayer" -> new IconRef(ICONS_MAIN, 16, 0, 16, 16, 80, 16);
            case "menu.online", "linkenvisuals.menu.alt_manager" -> new IconRef(ICONS_MAIN, 32, 0, 16, 16, 80, 16);
            case "menu.options", "linkenvisuals.menu.settings", "selectWorld.moreWorldOptions" -> new IconRef(ICONS_MAIN, 48, 0, 16, 16, 80, 16);
            case "menu.quit", "linkenvisuals.menu.quit", "loading.cancel" -> new IconRef(ICONS_MAIN, 64, 0, 16, 16, 80, 16);
            case "linkenvisuals.menu.mods" -> new IconRef(ICONS_MODS, 0, 0, 16, 16, 16, 16);

            // Pause menu
            case "menu.returnToGame", "multiplayer.back" -> new IconRef(ICONS_ESCAPE, 0, 0, 16, 16, 112, 16);
            case "gui.advancements" -> new IconRef(ICONS_ESCAPE, 16, 0, 16, 16, 112, 16);
            case "gui.stats" -> new IconRef(ICONS_ESCAPE, 32, 0, 16, 16, 112, 16);
            case "menu.sendFeedback", "menu.feedback" -> new IconRef(ICONS_ESCAPE, 48, 0, 16, 16, 112, 16);
            case "menu.reportBugs" -> new IconRef(ICONS_ESCAPE, 64, 0, 16, 16, 112, 16);
            case "menu.shareToLan" -> new IconRef(ICONS_ESCAPE, 80, 0, 16, 16, 112, 16);
            case "menu.disconnect", "menu.returnToMenu" -> new IconRef(ICONS_ESCAPE, 96, 0, 16, 16, 112, 16);
            case "linkenvisuals.gui.macros" -> new IconRef(ICONS_MACROS, 0, 0, 16, 16, 16, 16);

            // Options categories & sub-menus
            case "controls.keybinds" -> new IconRef(ICONS_CONTROLS, 20, 0, 20, 20, 40, 20);
            case "options.mouse_settings" -> new IconRef(ICONS_CONTROLS, 0, 0, 20, 20, 40, 20);
            case "options.fov" -> new IconRef(ICONS_OPTIONS, 0, 0, 20, 20, 240, 20);
            case "options.skinCustomisation" -> new IconRef(ICONS_OPTIONS, 40, 0, 20, 20, 240, 20);
            case "options.sounds", "options.music_frequency", "options.audioDevice" -> new IconRef(ICONS_OPTIONS, 60, 0, 20, 20, 240, 20);
            case "options.video", "options.graphics" -> new IconRef(ICONS_OPTIONS, 80, 0, 20, 20, 240, 20);
            case "options.controls", "options.invertMouse", "options.invertMouseX", "options.invertMouseY",
                 "options.rawMouseInput", "options.mouseWheelSensitivity", "options.discrete_mouse_scroll" -> new IconRef(ICONS_OPTIONS, 100, 0, 20, 20, 240, 20);
            case "options.language" -> new IconRef(ICONS_OPTIONS, 120, 0, 20, 20, 240, 20);
            case "options.chat" -> new IconRef(ICONS_OPTIONS, 140, 0, 20, 20, 240, 20);
            case "options.resourcepack", "pack.openFolder", "addServer.resourcePack" -> new IconRef(ICONS_OPTIONS, 160, 0, 20, 20, 240, 20);
            case "options.accessibility", "accessibility.onboarding.accessibility.button" -> new IconRef(ICONS_OPTIONS, 180, 0, 20, 20, 240, 20);
            case "options.telemetry" -> new IconRef(ICONS_OPTIONS, 200, 0, 20, 20, 240, 20);
            case "options.credits_and_attribution" -> new IconRef(ICONS_OPTIONS, 220, 0, 20, 20, 240, 20);

            // Worlds & servers
            // NB: "selectWorld.recreate" deliberately has no icon (it must not be confused with "create").
            case "selectServer.direct" -> new IconRef(ICONS_DIRECT, 0, 0, 16, 16, 16, 16);
            case "selectWorld.select", "selectServer.select", "multiplayer.join" -> new IconRef(ICONS_SINGLEPLAYER, 0, 0, 16, 16, 64, 16);
            // Own multiplayer toolbar: keep the shipped look of the "direct" button (multiplayer glyph).
            case "multiplayer.direct" -> new IconRef(ICONS_MAIN, 16, 0, 16, 16, 80, 16);
            case "selectWorld.create", "selectServer.add", "multiplayer.add" -> new IconRef(ICONS_MULTIPLAYER, 0, 0, 16, 16, 32, 16);
            case "selectWorld.edit", "selectServer.edit", "multiplayer.edit" -> new IconRef(ICONS_SINGLEPLAYER, 32, 0, 16, 16, 64, 16);
            case "selectWorld.delete", "selectServer.delete", "multiplayer.delete" -> new IconRef(ICONS_SINGLEPLAYER, 48, 0, 16, 16, 64, 16);
            case "selectServer.refresh", "multiplayer.refresh" -> new IconRef(ICONS_MULTIPLAYER, 16, 0, 16, 16, 32, 16);
            case "gui.done" -> new IconRef(ICONS_VIDEO, 0, 40, 20, 20, 240, 60);
            case "gui.cancel", "gui.back", "gui.toTitle", "gui.toMenu" -> new IconRef(ICONS_BACK, 0, 0, 16, 16, 16, 16);

            default -> null;
        };
    }

    /** Icon lookup for a widget: only the widget's translation key is used, never its localized text. */
    public static IconRef getIconForWidget(ClickableWidget widget) {
        return getIconForKey(TextKeys.keyOf(widget.getMessage()));
    }

    public static boolean renderButton(DrawContext context, ClickableWidget widget, int mouseX, int mouseY, float delta) {
        int x = widget.getX();
        int y = widget.getY();
        int w = widget.getWidth();
        int h = widget.getHeight();
        boolean hovered = widget.isSelected() || widget.isHovered() || (mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h);
        Text msg = widget.getMessage();
        String originalStr = msg != null ? msg.getString() : "";

        IconRef icon = getIconForWidget(widget);
        if (w <= 24 && icon == null) {
            // Let vanilla render small icon buttons (like vanilla's accessibility button)
            return false;
        }

        MinecraftClient mc = MinecraftClient.getInstance();
        boolean isLanguage = "options.language".equals(TextKeys.keyOf(msg));
        String str = originalStr;
        if (w <= 28 || (isLanguage && (w <= 36 || (mc != null && mc.currentScreen instanceof net.minecraft.client.gui.screen.TitleScreen)))) {
            str = "";
        }

        renderButton(context, x, y, w, h, str, icon, widget.active, hovered);
        return true;
    }

    public static void renderButton(DrawContext context, int x, int y, int w, int h, String text, IconRef icon, boolean active, boolean hovered) {
        // vBase in widgets.png (256x256):
        // 46: disabled, 66: normal, 86: hovered
        float vBase = !active ? 46.0f : (hovered ? 86.0f : 66.0f);
        int halfW = w / 2;
        int restW = w - halfW;
        int renderH = Math.min(20, h);
        int renderY = y + (h - renderH) / 2;

        // Draw left side of button (up to halfW)
        context.drawTexture(RenderPipelines.GUI_TEXTURED, WIDGETS,
            x, renderY, 0.0f, vBase,
            halfW, renderH, halfW, renderH,
            256, 256, 0xFFFFFFFF);

        // Draw right side of button
        context.drawTexture(RenderPipelines.GUI_TEXTURED, WIDGETS,
            x + halfW, renderY, 200.0f - restW, vBase,
            restW, renderH, restW, renderH,
            256, 256, 0xFFFFFFFF);

        // Text & Icon rendering
        MinecraftClient client = MinecraftClient.getInstance();
        TextRenderer tr = client.textRenderer;
        int textColor = !active ? 0xFFA0A0A0 : (hovered ? 0xFFFFFFA0 : 0xFFFFFFFF);

        if (icon != null) {
            int isz = Math.min(16, Math.min(w - 4, h - 4));
            int textW = (text != null && !text.isBlank()) ? tr.getWidth(text) : 0;
            int gap = 4;
            int totalContentW = isz + (textW > 0 ? gap + textW : 0);

            if (textW > 0 && (totalContentW > w - 8 || w <= 80)) {
                isz = Math.min(12, h - 4);
                gap = 3;
            }

            int availTextW = Math.max(10, w - 8 - isz - gap);
            float textScale = (textW > availTextW) ? Math.max(0.65f, (float) availTextW / (float) textW) : 1.0f;
            float scaledTextW = textW * textScale;
            float scaledTotalW = isz + (scaledTextW > 0 ? gap + scaledTextW : 0);

            float startX = (scaledTotalW <= w - 8)
                ? (float) x + (w - scaledTotalW) * 0.5f
                : (float) x + 4.0f;

            float ix = startX;
            float iy = (float) y + (h - isz) * 0.5f;
            context.drawTexture(RenderPipelines.GUI_TEXTURED, icon.texture,
                Math.round(ix), Math.round(iy), (float) icon.u, (float) icon.v,
                isz, isz, icon.width, icon.height,
                icon.sheetW, icon.sheetH, active ? 0xFFFFFFFF : 0x88FFFFFF);

            if (text != null && !text.isBlank()) {
                float tx = ix + isz + gap;
                float ty = (float) y + (h - 8.0f * textScale) * 0.5f;
                if (textScale < 0.999f) {
                    var matrices = context.getMatrices();
                    matrices.pushMatrix();
                    matrices.translate(tx, ty);
                    matrices.scale(textScale, textScale);
                    context.drawTextWithShadow(tr, text, 0, 0, textColor);
                    matrices.popMatrix();
                } else {
                    context.drawTextWithShadow(tr, text, Math.round(tx), Math.round(ty), textColor);
                }
            }
        } else {
            if (text != null && !text.isBlank()) {
                int textW = tr.getWidth(text);
                int availTextW = Math.max(10, w - 6);
                float textScale = (textW > availTextW) ? Math.max(0.65f, (float) availTextW / (float) textW) : 1.0f;
                float scaledTextW = textW * textScale;
                float tx = (float) x + (w - scaledTextW) * 0.5f;
                float ty = (float) y + (h - 8.0f * textScale) * 0.5f;

                if (textScale < 0.999f) {
                    var matrices = context.getMatrices();
                    matrices.pushMatrix();
                    matrices.translate(tx, ty);
                    matrices.scale(textScale, textScale);
                    context.drawTextWithShadow(tr, text, 0, 0, textColor);
                    matrices.popMatrix();
                } else {
                    context.drawTextWithShadow(tr, text, Math.round(tx), Math.round(ty), textColor);
                }
            }
        }
    }

    public static void renderSlider(DrawContext context, int x, int y, int w, int h, double value, Text message, boolean active, boolean hovered) {
        int halfW = w / 2;
        int restW = w - halfW;
        int renderH = Math.min(20, h);
        int renderY = y + (h - renderH) / 2;

        // 1. Draw slider track (v=46 disabled button look)
        context.drawTexture(RenderPipelines.GUI_TEXTURED, WIDGETS,
            x, renderY, 0.0f, 46.0f,
            halfW, renderH, halfW, renderH,
            256, 256, 0xFFFFFFFF);
        context.drawTexture(RenderPipelines.GUI_TEXTURED, WIDGETS,
            x + halfW, renderY, 200.0f - restW, 46.0f,
            restW, renderH, restW, renderH,
            256, 256, 0xFFFFFFFF);

        // 2. Draw slider thumb (width = 8px)
        int thumbW = 8;
        int thumbX = x + (int) (Math.max(0.0, Math.min(1.0, value)) * (w - thumbW));
        float thumbV = hovered ? 86.0f : 66.0f;

        // Thumb left 4px
        context.drawTexture(RenderPipelines.GUI_TEXTURED, WIDGETS,
            thumbX, renderY, 0.0f, thumbV,
            4, renderH, 4, renderH,
            256, 256, 0xFFFFFFFF);
        // Thumb right 4px
        context.drawTexture(RenderPipelines.GUI_TEXTURED, WIDGETS,
            thumbX + 4, renderY, 196.0f, thumbV,
            4, renderH, 4, renderH,
            256, 256, 0xFFFFFFFF);

        // 3. Draw text label
        if (message != null) {
            MinecraftClient client = MinecraftClient.getInstance();
            TextRenderer tr = client.textRenderer;
            String text = message.getString();
            int textW = tr.getWidth(text);
            int tx = x + (w - textW) / 2;
            int ty = y + (h - 8) / 2;
            int textColor = !active ? 0xFFA0A0A0 : (hovered ? 0xFFFFFFA0 : 0xFFFFFFFF);
            context.drawTextWithShadow(tr, text, tx, ty, textColor);
        }
    }

    public static void renderOptionsBackground(DrawContext context, int width, int height) {
        renderVanillaPanorama(context, width, height);
        context.fillGradient(0, 0, width, height, 0x1A000000, 0x30000000);

        // Header and footer separators (seamlessly repeated across screen width)
        int sepTiles = (width + 31) / 32;
        for (int i = 0; i < sepTiles; i++) {
            int w = Math.min(32, width - i * 32);
            context.drawTexture(RenderPipelines.GUI_TEXTURED, HEADER_SEP,
                i * 32, 32, 0.0f, 0.0f,
                w, 2, w, 2,
                32, 2, 0xFFFFFFFF);
            context.drawTexture(RenderPipelines.GUI_TEXTURED, FOOTER_SEP,
                i * 32, height - 36, 0.0f, 0.0f,
                w, 2, w, 2,
                32, 2, 0xFFFFFFFF);
        }

        // Top and bottom header/footer shadow darkening
        context.fillGradient(0, 0, width, 32, 0xAA000000, 0x00000000);
        context.fillGradient(0, height - 36, width, height, 0x00000000, 0xAA000000);
    }

    public static void renderDirtBackground(DrawContext context, int width, int height) {
        renderVanillaPanorama(context, width, height);
        context.fillGradient(0, 0, width, height, 0x1A000000, 0x30000000);
    }

    /**
     * Renders the stock vanilla rotating panorama (cube map from the game's own assets),
     * with a dark gradient fallback when the renderer is unavailable.
     */
    public static void renderVanillaPanorama(DrawContext context, int width, int height) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null && client.gameRenderer != null) {
            RotatingCubeMapRenderer renderer = client.gameRenderer.getRotatingPanoramaRenderer();
            if (renderer != null) {
                try {
                    renderer.render(context, width, height, true);
                    return;
                } catch (Throwable t) {
                    // Logged once: the menu renders every frame, and the gradient
                    // fallback below keeps the menu usable.
                    if (!panoramaFailureLogged) {
                        panoramaFailureLogged = true;
                        LinkenVisuals.LOGGER.warn(
                            "Vanilla panorama renderer failed, using gradient fallback: {}",
                            t.toString());
                    }
                }
            }
        }
        context.fillGradient(0, 0, width, height, 0xFF141720, 0xFF0E1017);
    }

    public static void drawBorder(DrawContext context, int x, int y, int w, int h, int color) {
        context.fill(x, y, x + w, y + 1, color);
        context.fill(x, y + h - 1, x + w, y + h, color);
        context.fill(x, y + 1, x + 1, y + h - 1, color);
        context.fill(x + w - 1, y + 1, x + w, y + h - 1, color);
    }
}
