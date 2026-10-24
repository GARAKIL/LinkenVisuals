package dev.linkenvisuals.ui.linkmenu;

import dev.linkenvisuals.i18n.I18n;
import dev.linkenvisuals.mixin.TextFieldWidgetAccessor;
import dev.linkenvisuals.render.GlowPipeline;
import dev.linkenvisuals.render.Pipelines;
import dev.linkenvisuals.render.WidgetStylePipeline;
import dev.linkenvisuals.ui.style.MenuStyle;
import dev.linkenvisuals.ui.style.StyleManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.screen.world.WorldCreator;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.TabButtonWidget;
import net.minecraft.client.gui.widget.TabNavigationWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Language;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * LINKEN skin for the vanilla {@link CreateWorldScreen}.
 *
 * <p>The vanilla screen keeps ownership of the world-creation logic, tab
 * routing and widget input. Its chrome is suppressed by the widget mixins and
 * this renderer paints the whole screen on the LINKEN 1920x1080 canvas: the
 * editorial backdrop, the tagged tab rail, one tall glass panel around the
 * live form and an at-a-glance world summary above the footer.
 *
 * <p>Widget geometry is read from the vanilla widgets, so the skin follows
 * every layout the game computes for the current GUI scale and window size.
 */
public final class LinkenCreateWorldSkin {
    private static final float PAD_X = 52.0f;
    private static final float PAD_TOP = 52.0f;
    private static final float CARD_RADIUS = 14.0f;

    private LinkenCreateWorldSkin() {
    }

    /** True when the vanilla screen must keep its widgets hidden. */
    public static boolean shouldSkin(Screen screen) {
        if (!(screen instanceof CreateWorldScreen)) {
            return false;
        }
        return StyleManager.getStyle() == MenuStyle.LINKEN && Pipelines.resourcesReady();
    }

    public static void render(DrawContext context, CreateWorldScreen screen,
                              int mouseX, int mouseY, float delta) {
        if (!shouldSkin(screen) || screen.width <= 0 || screen.height <= 0) {
            return;
        }
        LinkenMenuLayout.Metrics metrics = LinkenMenuLayout.compute(screen.width, screen.height);
        float k = 1.0f / metrics.scale();
        float mx = mouseX * k;
        float my = mouseY * k;

        context.getMatrices().pushMatrix();
        context.getMatrices().scale(metrics.scale(), metrics.scale());

        drawBackdrop(context, metrics);
        drawTopBar(context, metrics);

        TabNavigationWidget tabNavigation = null;
        List<ClickableWidget> content = new ArrayList<>();
        List<ClickableWidget> footer = new ArrayList<>();
        for (Element element : screen.children()) {
            if (element instanceof TabNavigationWidget navigation) {
                tabNavigation = navigation;
            } else if (element instanceof ClickableWidget widget && widget.visible
                && widget.getWidth() > 0 && widget.getHeight() > 0 && widget.getAlpha() > 0.01f) {
                if (isFooterButton(widget)) {
                    footer.add(widget);
                } else {
                    content.add(widget);
                }
            }
        }

        if (content.isEmpty()) {
            context.getMatrices().popMatrix();
            return;
        }

        float minX = Float.MAX_VALUE;
        float minY = Float.MAX_VALUE;
        float maxX = -Float.MAX_VALUE;
        float maxY = -Float.MAX_VALUE;
        for (ClickableWidget widget : content) {
            minX = Math.min(minX, widget.getX() * k);
            minY = Math.min(minY, widget.getY() * k);
            maxX = Math.max(maxX, (widget.getX() + widget.getWidth()) * k);
            maxY = Math.max(maxY, (widget.getY() + widget.getHeight()) * k);
        }

        float tabBottom = tabNavigation == null ? 90.0f : tabNavigation.getNavigationFocus().getBottom() * k;
        float footerTop = metrics.height() - 40.0f;
        for (ClickableWidget widget : footer) {
            footerTop = Math.min(footerTop, widget.getY() * k - 26.0f);
        }

        // One stable shell width for all three tabs. Without the 1024 floor the
        // card jumped between the narrow Game tab and the two-column World tab.
        float cardWidth = Math.min(metrics.width() - 112.0f,
            Math.max(620.0f, Math.max((maxX - minX) + PAD_X * 2.0f, 1024.0f)));
        float cardX = (metrics.width() - cardWidth) * 0.5f;
        float cardTop = Math.max(tabBottom + 22.0f, minY - PAD_TOP);
        // Reserve room for the at-a-glance summary on every tab, not only on
        // More: the panel must stay at least 250 px taller than the form.
        float cardBottom = Math.min(footerTop - 22.0f,
            Math.max(maxY + 250.0f, minY + 600.0f));
        if (cardBottom < maxY + 26.0f) {
            cardBottom = maxY + 26.0f;
        }

        if (tabNavigation != null) {
            alignTabs(tabNavigation, cardX, cardWidth, k);
            drawTabs(context, metrics, tabNavigation, screen, mx, my, delta, k);
        }

        drawPanel(context, metrics, screen, cardX, cardTop, cardWidth, cardBottom, maxY);
        drawContent(context, metrics, screen, content, mx, my, delta, k);
        drawFooter(context, metrics, footer, screen, mx, my, delta, k);

        context.getMatrices().popMatrix();
    }

    /**
     * Centres the vanilla tab buttons on the LINKEN shell. Input still goes
     * through the same {@link TabButtonWidget} instances, so the painted pills
     * and their hit boxes stay identical.
     */
    private static void alignTabs(TabNavigationWidget navigation, float x, float width, float k) {
        List<TabButtonWidget> tabs = new ArrayList<>();
        for (Element element : navigation.children()) {
            if (element instanceof TabButtonWidget tab && tab.visible) {
                tabs.add(tab);
            }
        }
        if (tabs.isEmpty()) {
            return;
        }
        float gap = 6.0f;
        float pill = (width - gap * (tabs.size() - 1)) / tabs.size();
        for (int i = 0; i < tabs.size(); i++) {
            tabs.get(i).setX(Math.round((x + i * (pill + gap)) / k));
            tabs.get(i).setWidth(Math.round(pill / k));
        }
    }

    // ---------------------------------------------------------------- backdrop

    private static void drawBackdrop(DrawContext context, LinkenMenuLayout.Metrics metrics) {
        LinkenMenuChrome.drawBackground(context, metrics, 0.0f, 0.0f, 1.0f);
        context.fill(0, 0, Math.round(metrics.width()), Math.round(metrics.height()),
            LinkenMenuChrome.rgba(0x040706, 168));
        GlowPipeline.radial(context, metrics.width() * 0.5f, 330.0f, 520.0f,
            LinkenMenuChrome.rgba(LinkenMenuChrome.ACCENT, 12));
        context.fillGradient(0, Math.round(metrics.height()) - 210,
            Math.round(metrics.width()), Math.round(metrics.height()),
            LinkenMenuChrome.rgba(0x020403, 0), LinkenMenuChrome.rgba(0x020403, 190));
    }

    private static void drawTopBar(DrawContext context, LinkenMenuLayout.Metrics metrics) {
        LinkenMenuChrome.drawWordmark(context, 1.0f);
        LinkenMenuChrome.drawRightText(context, LinkenFonts.mono(),
            LinkenMenuChrome.version(), metrics.width() - LinkenMenuLayout.MARGIN, 40.0f, 9.0f,
            LinkenMenuChrome.rgba(LinkenMenuChrome.MUTED3, 1.0f));
    }

    private static void drawTabs(DrawContext context, LinkenMenuLayout.Metrics metrics,
                                 TabNavigationWidget navigation, CreateWorldScreen screen,
                                 float mx, float my, float delta, float k) {
        float radius = 10.0f;
        for (Element element : navigation.children()) {
            if (!(element instanceof TabButtonWidget tab) || !tab.visible) {
                continue;
            }
            float x = tab.getX() * k + 3.0f;
            float y = tab.getY() * k + 5.0f;
            float width = tab.getWidth() * k - 6.0f;
            float height = tab.getHeight() * k - 10.0f;
            boolean active = tab.isCurrentTab();
            boolean hovered = mx >= x && mx <= x + width && my >= y && my <= y + height;
            float hover = WidgetStylePipeline.updateHover(tab, hovered, delta);

            int fill = active ? LinkenMenuChrome.rgba(LinkenMenuChrome.ACCENT,
                Math.round(26.0f + 10.0f * hover)) : LinkenMenuChrome.rgba(
                LinkenMenuChrome.mix(0x0B0F0D, 0x161D18, hover), 216);
            LinkenMenuChrome.roundedFill(context, x, y, width, height, radius, fill, metrics.scale());
            LinkenMenuChrome.roundedOutline(context, x, y, width, height, radius, 1.0f,
                LinkenMenuChrome.rgba(active ? LinkenMenuChrome.ACCENT : 0xFFFFFF,
                    active ? 128 : Math.round(18.0f + 34.0f * hover)), metrics.scale());
            if (active) {
                LinkenMenuChrome.roundedFill(context, x + width * 0.5f - 22.0f,
                    y + height - 2.0f, 44.0f, 2.0f, 1.0f,
                    LinkenMenuChrome.rgba(LinkenMenuChrome.ACCENT, 220), metrics.scale());
            }

            String label = tab.getTab().getTitle().getString().toUpperCase(Locale.ROOT);
            float size = 11.5f;
            float tracking = 1.9f;
            float labelWidth = LinkenMenuChrome.textWidth(LinkenFonts.headingLight(), label, size)
                + tracking * Math.max(0, label.length() - 1);
            int color = active ? LinkenMenuChrome.ACCENT
                : LinkenMenuChrome.mix(LinkenMenuChrome.MUTED, LinkenMenuChrome.TEXT2, hover);
            LinkenMenuChrome.drawSpaced(context, LinkenFonts.headingLight(), label,
                x + (width - labelWidth) * 0.5f, y + (height - size) * 0.5f,
                size, tracking, LinkenMenuChrome.rgba(color, 1.0f));
        }
    }

    // ---------------------------------------------------------------- panel

    private static void drawPanel(DrawContext context, LinkenMenuLayout.Metrics metrics,
                                  CreateWorldScreen screen, float x, float y, float width,
                                  float bottom, float contentBottom) {
        float height = bottom - y;
        LinkenMenuChrome.roundedFill(context, x, y, width, height, CARD_RADIUS,
            LinkenMenuChrome.rgba(0x0E1411, 246), metrics.scale());
        LinkenMenuChrome.roundedOutline(context, x, y, width, height, CARD_RADIUS, 1.0f,
            LinkenMenuChrome.rgba(0xFFFFFF, 26), metrics.scale());
        LinkenMenuChrome.roundedFill(context, x + 26.0f, y + 1.0f, width - 52.0f, 1.0f, 0.5f,
            LinkenMenuChrome.rgba(0xFFFFFF, 42), metrics.scale());
        LinkenMenuChrome.roundedFill(context, x, y + 24.0f, 2.0f, 64.0f, 1.0f,
            LinkenMenuChrome.rgba(LinkenMenuChrome.ACCENT, 150), metrics.scale());

        LinkenMenuChrome.drawSpaced(context, LinkenFonts.body(),
            I18n.get("linkenvisuals.world_setup"), x + 32.0f, y + 22.0f, 9.0f, 1.7f,
            LinkenMenuChrome.rgba(LinkenMenuChrome.ACCENT, 0.9f));

        WorldCreator creator = screen.getWorldCreator();
        String hint = creator.getWorldDirectoryName();
        if (hint != null && !hint.isBlank()) {
            String line = I18n.get("selectWorld.targetFolder", hint);
            LinkenMenuChrome.drawRightText(context, LinkenFonts.mono(),
                LinkenMenuChrome.ellipsize(LinkenFonts.mono(), line, width * 0.5f, 8.5f),
                x + width - 32.0f, y + 22.0f, 8.5f,
                LinkenMenuChrome.rgba(LinkenMenuChrome.MUTED3, 0.9f));
        }

        float summaryTop = bottom - 178.0f;
        if (summaryTop > contentBottom + 56.0f) {
            drawSummary(context, metrics, screen, creator, x + 32.0f, width - 64.0f,
                summaryTop, bottom);
        }
    }

    private static void drawSummary(DrawContext context, LinkenMenuLayout.Metrics metrics,
                                    CreateWorldScreen screen, WorldCreator creator,
                                    float x, float width, float top, float bottom) {
        context.fill(Math.round(x), Math.round(top), Math.round(x + width),
            Math.round(top + 1.0f), LinkenMenuChrome.rgba(0xFFFFFF, 18));
        LinkenMenuChrome.drawSpaced(context, LinkenFonts.body(),
            I18n.get("linkenvisuals.createworld.summary"), x, top + 20.0f,
            8.5f, 1.7f, LinkenMenuChrome.rgba(LinkenMenuChrome.MUTED2, 1.0f));

        String name = creator.getWorldName();
        if (name == null || name.isBlank()) {
            name = I18n.get("selectWorld.enterName");
        }
        float nameSize = LinkenMenuChrome.fitSize(LinkenFonts.heading(), name,
            width, 30.0f, 17.0f);
        LinkenMenuChrome.drawText(context, LinkenFonts.heading(), name,
            x, top + 38.0f, nameSize,
            LinkenMenuChrome.rgba(LinkenMenuChrome.TEXT, 1.0f));

        float columnY = top + 92.0f;
        float columnWidth = Math.min(220.0f, width / 3.0f);
        String[] labels = {
            I18n.get("selectWorld.gameMode"),
            I18n.get("options.difficulty"),
            I18n.get("selectWorld.allowCommands")
        };
        String[] values = {
            creator.getGameMode().name.getString(),
            creator.getDifficulty().getTranslatableName().getString(),
            creator.areCheatsEnabled() ? ScreenTexts.ON.getString() : ScreenTexts.OFF.getString()
        };
        for (int i = 0; i < labels.length; i++) {
            float cx = x + i * columnWidth;
            LinkenMenuChrome.drawSpaced(context, LinkenFonts.body(),
                labels[i].toUpperCase(Locale.ROOT), cx, columnY, 7.5f, 1.3f,
                LinkenMenuChrome.rgba(LinkenMenuChrome.MUTED3, 1.0f));
            LinkenMenuChrome.drawText(context, LinkenFonts.headingLight(), values[i],
                cx, columnY + 14.0f, 12.5f,
                LinkenMenuChrome.rgba(LinkenMenuChrome.TEXT3, 1.0f));
        }

        LinkenMenuChrome.drawRightText(context, LinkenFonts.mono(),
            LinkenMenuChrome.ellipsize(LinkenFonts.mono(), creator.getWorldDirectoryName(),
                width * 0.45f, 8.0f),
            x + width, bottom - 24.0f, 8.0f,
            LinkenMenuChrome.rgba(LinkenMenuChrome.MUTED4, 0.95f));
    }

    // ---------------------------------------------------------------- widgets

    private static void drawContent(DrawContext context, LinkenMenuLayout.Metrics metrics,
                                    CreateWorldScreen screen, List<ClickableWidget> widgets,
                                    float mx, float my, float delta, float k) {
        for (ClickableWidget widget : widgets) {
            float x = widget.getX() * k;
            float y = widget.getY() * k;
            float width = widget.getWidth() * k;
            float height = widget.getHeight() * k;
            boolean hovered = mx >= x && mx <= x + width && my >= y && my <= y + height;
            if (widget instanceof TextFieldWidget field) {
                drawField(context, metrics, screen, field, x, y, width, height);
            } else if (widget instanceof CheckboxWidget checkbox) {
                drawCheckbox(context, metrics, checkbox, x, y, width, height, hovered, mx, my, delta);
            } else if (widget instanceof CyclingButtonWidget<?> cycling) {
                drawCycle(context, metrics, cycling, x, y, width, height, hovered, mx, my, delta);
            } else if (widget instanceof ButtonWidget button) {
                drawAction(context, metrics, button, x, y, width, height, hovered, mx, my, delta,
                    isPrimary(button));
            } else if (widget instanceof net.minecraft.client.gui.widget.TextWidget text) {
                drawLabel(context, x, y, width, height, text.getMessage().getString());
            }
        }
    }

    private static void drawField(DrawContext context, LinkenMenuLayout.Metrics metrics,
                                  CreateWorldScreen screen, TextFieldWidget field,
                                  float x, float y, float width, float height) {
        boolean focused = screen.getFocused() == field;
        float radius = 8.0f;
        LinkenMenuChrome.roundedFill(context, x, y, width, height, radius,
            LinkenMenuChrome.rgba(0x080C0A, 246), metrics.scale());
        LinkenMenuChrome.roundedOutline(context, x, y, width, height, radius, 1.0f,
            LinkenMenuChrome.rgba(focused ? LinkenMenuChrome.ACCENT : 0xFFFFFF,
                focused ? 150 : 30), metrics.scale());
        if (focused) {
            LinkenMenuChrome.roundedFill(context, x, y + 12.0f, 2.0f, height - 24.0f, 1.0f,
                LinkenMenuChrome.rgba(LinkenMenuChrome.ACCENT, 190), metrics.scale());
        }

        float size = Math.min(15.0f, height * 0.30f);
        float textX = x + 18.0f;
        float centerY = y + height * 0.5f;
        String text = field.getText();
        if (text == null) {
            text = "";
        }
        if (text.isEmpty()) {
            Text placeholder = ((TextFieldWidgetAccessor) field).linkenvisuals$placeholder();
            String hint = placeholder == null ? "" : placeholder.getString();
            LinkenMenuChrome.drawOn(context, LinkenFonts.body(),
                LinkenMenuChrome.ellipsize(LinkenFonts.body(), hint, width - 36.0f, size),
                textX, centerY, size, LinkenMenuChrome.rgba(LinkenMenuChrome.MUTED3, 1.0f));
            return;
        }
        String shown = LinkenMenuChrome.ellipsize(LinkenFonts.body(), text, width - 36.0f, size);
        LinkenMenuChrome.drawOn(context, LinkenFonts.body(), shown, textX, centerY, size,
            LinkenMenuChrome.rgba(LinkenMenuChrome.TEXT, 1.0f));
        if (focused && (System.currentTimeMillis() / 500L) % 2L == 0L) {
            int cursor = Math.max(0, Math.min(field.getCursor(), text.length()));
            float caret = textX + LinkenMenuChrome.textWidth(LinkenFonts.body(),
                text.substring(0, cursor), size);
            caret = Math.min(caret, x + width - 14.0f);
            context.fill(Math.round(caret), Math.round(y + height * 0.28f),
                Math.round(caret + 1.4f), Math.round(y + height * 0.72f),
                LinkenMenuChrome.rgba(LinkenMenuChrome.ACCENT, 225));
        }
    }

    private static void drawLabel(DrawContext context, float x, float y, float width,
                                  float height, String label) {
        String text = label == null ? "" : label.toUpperCase(Locale.ROOT);
        LinkenMenuChrome.drawSpaced(context, LinkenFonts.body(),
            LinkenMenuChrome.ellipsize(LinkenFonts.body(), text, width, 9.5f),
            x + 2.0f, y + (height - 9.5f) * 0.5f, 9.5f, 1.1f,
            LinkenMenuChrome.rgba(LinkenMenuChrome.MUTED2, 1.0f));
    }

    private static void drawCycle(DrawContext context, LinkenMenuLayout.Metrics metrics,
                                  CyclingButtonWidget<?> widget, float x, float y, float width,
                                  float height, boolean hovered, float mx, float my, float delta) {
        float hover = WidgetStylePipeline.updateHover(widget, hovered, delta);
        drawRow(context, metrics, x, y, width, height, hover, widget.active);
        int textColor = widget.active
            ? LinkenMenuChrome.mix(LinkenMenuChrome.TEXT2, 0xFFFFFF, hover)
            : LinkenMenuChrome.MUTED3;
        String label = widget.getMessage().getString();
        LinkenMenuChrome.drawOn(context, LinkenFonts.headingLight(),
            LinkenMenuChrome.ellipsize(LinkenFonts.headingLight(), label, width - 64.0f, 12.5f),
            x + 20.0f, y + height * 0.5f, 12.5f,
            LinkenMenuChrome.rgba(textColor, 1.0f));

        int chevron = LinkenMenuChrome.rgba(
            widget.active ? LinkenMenuChrome.mix(LinkenMenuChrome.MUTED3, LinkenMenuChrome.ACCENT, hover)
                : LinkenMenuChrome.MUTED4, 1.0f);
        LinkenMenuChrome.drawOn(context, LinkenFonts.mono(), "<", x + width - 30.0f,
            y + height * 0.5f, 11.0f, chevron);
        LinkenMenuChrome.drawOn(context, LinkenFonts.mono(), ">", x + width - 16.0f,
            y + height * 0.5f, 11.0f, chevron);
    }

    private static void drawAction(DrawContext context, LinkenMenuLayout.Metrics metrics,
                                   ButtonWidget widget, float x, float y, float width, float height,
                                   boolean hovered, float mx, float my, float delta, boolean primary) {
        boolean enabled = widget.active;
        float hover = enabled ? WidgetStylePipeline.updateHover(widget, hovered, delta) : 0.0f;
        String label = widget.getMessage().getString();

        if (primary && enabled) {
            LinkenMenuChrome.roundedFill(context, x + 4.0f, y + height - 6.0f, width - 8.0f,
                12.0f, 8.0f, LinkenMenuChrome.rgba(LinkenMenuChrome.ACCENT,
                    Math.round(52.0f + 34.0f * hover)), metrics.scale());
            LinkenMenuChrome.roundedFill(context, x, y, width, height, 8.0f,
                LinkenMenuChrome.rgba(LinkenMenuChrome.mix(LinkenMenuChrome.ACCENT, 0xFFFFFF,
                    hover * 0.08f), 255), metrics.scale());
        } else if (primary) {
            LinkenMenuChrome.roundedFill(context, x, y, width, height, 8.0f,
                LinkenMenuChrome.rgba(0x1A231D, 190), metrics.scale());
        } else {
            drawRow(context, metrics, x, y, width, height, enabled ? hover : 0.0f, enabled);
        }

        int color;
        if (!enabled) {
            color = LinkenMenuChrome.MUTED3;
        } else if (primary) {
            color = LinkenMenuChrome.INK;
        } else {
            color = LinkenMenuChrome.mix(LinkenMenuChrome.TEXT2, 0xFFFFFF, hover);
        }
        LinkenMenuChrome.drawCentered(context, LinkenFonts.heading(),
            LinkenMenuChrome.ellipsize(LinkenFonts.heading(), label, width - 24.0f, 12.5f),
            x + width * 0.5f, y + height * 0.5f, 12.5f,
            LinkenMenuChrome.rgba(color, 1.0f));
    }

    private static void drawCheckbox(DrawContext context, LinkenMenuLayout.Metrics metrics,
                                     CheckboxWidget widget, float x, float y, float width,
                                     float height, boolean hovered, float mx, float my, float delta) {
        boolean enabled = widget.active;
        float hover = enabled ? WidgetStylePipeline.updateHover(widget, hovered, delta) : 0.0f;
        float box = Math.min(height * 0.58f, 30.0f);
        float boxX = x + 2.0f;
        float boxY = y + (height - box) * 0.5f;

        LinkenMenuChrome.roundedFill(context, boxX, boxY, box, box, 6.0f,
            LinkenMenuChrome.rgba(widget.isChecked() ? LinkenMenuChrome.ACCENT : 0x0A0F0C, 255),
            metrics.scale());
        LinkenMenuChrome.roundedOutline(context, boxX, boxY, box, box, 6.0f, 1.0f,
            LinkenMenuChrome.rgba(widget.isChecked() ? LinkenMenuChrome.ACCENT : 0xFFFFFF,
                widget.isChecked() ? 200 : Math.round(44.0f + 56.0f * hover)), metrics.scale());

        if (widget.isChecked()) {
            int mark = LinkenMenuChrome.rgba(LinkenMenuChrome.INK, 1.0f);
            context.getMatrices().pushMatrix();
            context.getMatrices().translate(boxX + box * 0.38f, boxY + box * 0.54f);
            context.getMatrices().rotate((float) Math.toRadians(-45.0));
            context.fill(Math.round(-box * 0.16f), -1, Math.round(box * 0.06f), 1, mark);
            context.getMatrices().popMatrix();
            context.getMatrices().pushMatrix();
            context.getMatrices().translate(boxX + box * 0.48f, boxY + box * 0.54f);
            context.getMatrices().rotate((float) Math.toRadians(45.0));
            context.fill(Math.round(-box * 0.06f), -1, Math.round(box * 0.26f), 1, mark);
            context.getMatrices().popMatrix();
        }

        String label = widget.getMessage().getString();
        int color = !enabled ? LinkenMenuChrome.MUTED3
            : LinkenMenuChrome.mix(LinkenMenuChrome.TEXT3, 0xFFFFFF, hover);
        LinkenMenuChrome.drawOn(context, LinkenFonts.headingLight(),
            LinkenMenuChrome.ellipsize(LinkenFonts.headingLight(), label,
                width - box - 24.0f, 12.5f),
            boxX + box + 14.0f, y + height * 0.5f, 12.5f,
            LinkenMenuChrome.rgba(color, 1.0f));
    }

    private static void drawRow(DrawContext context, LinkenMenuLayout.Metrics metrics,
                                float x, float y, float width, float height,
                                float hover, boolean enabled) {
        LinkenMenuChrome.roundedFill(context, x, y, width, height, 8.0f,
            LinkenMenuChrome.rgba(enabled ? LinkenMenuChrome.mix(0x151B18, 0x1B2620, hover) : 0x0D1210,
                enabled ? 232 : 170), metrics.scale());
        LinkenMenuChrome.roundedOutline(context, x, y, width, height, 8.0f, 1.0f,
            LinkenMenuChrome.rgba(0xFFFFFF,
                Math.round((enabled ? 20.0f : 10.0f) + 34.0f * hover)), metrics.scale());
        if (hover > 0.01f) {
            LinkenMenuChrome.roundedFill(context, x, y + 10.0f, 2.0f, height - 20.0f, 1.0f,
                LinkenMenuChrome.rgba(LinkenMenuChrome.ACCENT, Math.round(150.0f * hover)),
                metrics.scale());
        }
    }

    // ---------------------------------------------------------------- footer

    private static void drawFooter(DrawContext context, LinkenMenuLayout.Metrics metrics,
                                   List<ClickableWidget> footer, CreateWorldScreen screen,
                                   float mx, float my, float delta, float k) {
        if (footer.isEmpty()) {
            return;
        }
        float lineY = footer.get(0).getY() * k - 22.0f;
        float minX = Float.MAX_VALUE;
        float maxX = -Float.MAX_VALUE;
        for (ClickableWidget widget : footer) {
            minX = Math.min(minX, widget.getX() * k);
            maxX = Math.max(maxX, (widget.getX() + widget.getWidth()) * k);
        }
        float inset = 60.0f;
        context.fill(Math.round(Math.max(inset, minX - 40.0f)), Math.round(lineY),
            Math.round(Math.min(metrics.width() - inset, maxX + 40.0f)),
            Math.round(lineY + 1.0f), LinkenMenuChrome.rgba(0xFFFFFF, 18));

        for (ClickableWidget widget : footer) {
            float x = widget.getX() * k;
            float y = widget.getY() * k;
            float width = widget.getWidth() * k;
            float height = widget.getHeight() * k;
            boolean hovered = mx >= x && mx <= x + width && my >= y && my <= y + height;
            if (widget instanceof ButtonWidget button) {
                drawAction(context, metrics, button, x, y, width, height, hovered, mx, my, delta,
                    isPrimary(button));
            }
        }
    }

    // ---------------------------------------------------------------- helpers

    private static boolean isPrimary(ClickableWidget widget) {
        return widget.getMessage() != null
            && widget.getMessage().getString().equals(createLabel());
    }

    private static String createLabel() {
        return Text.translatable("selectWorld.create").getString();
    }

    private static boolean isFooterButton(ClickableWidget widget) {
        if (!(widget instanceof ButtonWidget)) {
            return false;
        }
        String label = widget.getMessage().getString();
        return label.equals(createLabel()) || label.equals(ScreenTexts.CANCEL.getString());
    }
}
