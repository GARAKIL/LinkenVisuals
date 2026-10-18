package dev.linkenvisuals.clickgui;

import dev.linkenvisuals.util.ColorMath;
import dev.linkenvisuals.LinkenVisuals;
import dev.linkenvisuals.render.GlyphAtlas;
import dev.linkenvisuals.render.MsdfText;
import dev.linkenvisuals.render.NeonEdge;
import dev.linkenvisuals.render.RoundedRectPipeline;
import dev.linkenvisuals.ui.Spring;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import dev.linkenvisuals.i18n.I18n;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/**
 * Config profile manager of the modern ClickGUI (CONFIG category), extracted
 * from {@link ClickGuiScreen} (Phase 3.2) without behavior changes: geometry,
 * colors and click handling are identical to the pre-extraction code.
 *
 * <p>Fields that were static in {@code ClickGuiScreen} (input text, scroll
 * target, status line) intentionally stay static: the typed profile name must
 * survive screen re-open exactly like before the extraction.
 */
final class ConfigPanel {
    /** Draws the shared vertical scrollbar; bound to {@code ClickGuiScreen.scrollbar}. */
    @FunctionalInterface
    interface Scrollbar {
        void draw(DrawContext context, float x, float y, float viewHeight, float contentSize, float scroll, float openT);
    }

    // Mirrors ClickGuiScreen MSDF metrics so text vertical centering stays pixel-identical.

    private GlyphAtlas text;
    private GlyphAtlas bold;
    private Scrollbar scrollbar;

    private static final StringBuilder nameInput = new StringBuilder();
    private static boolean inputFocused;
    private static float scrollTarget;
    private static String statusMessage = null;
    private static long statusTime = 0L;
    private static int statusColor = 0x4ADE80;

    private final Spring scrollSpring = new Spring(0.0, 1100.0, 60.0);
    private float contentHeight;
    private float viewHeight = 1.0f;

    private final List<ActionBounds> actionBounds = new ArrayList<>();
    private float inputX, inputY, inputW, inputH;
    private float createX, createY, createW, createH;
    private float folderX, folderY, folderW, folderH;
    private float refreshX, refreshY, refreshW, refreshH;
    private float autoX, autoY, autoW, autoH;

    void bind(GlyphAtlas text, GlyphAtlas bold, Scrollbar scrollbar) {
        this.text = text;
        this.bold = bold;
        this.scrollbar = scrollbar;
    }

    void render(DrawContext context, float wx, float wy, int mouseX, int mouseY, double deltaSeconds, float openT) {
        float areaLeft = wx + 12.0f;
        float areaRight = wx + ClickGuiScreen.RAIL_DIVIDER_X - 10.0f;
        float areaTop = wy + 38.0f;
        float areaBottom = wy + ClickGuiScreen.CONTENT_BOTTOM - 4.0f;

        // 1. Top Control Bar (Y = areaTop + 3.0f, Height = 22.0f)
        float topY = areaTop + 3.0f;
        inputX = areaLeft;
        inputY = topY;
        inputW = 158.0f;
        inputH = 22.0f;

        RoundedRectPipeline.draw(context, inputX, inputY, inputW, inputH,
            ColorMath.withAlpha(0x0C0E14, Math.round(230 * openT)));
        RoundedRectPipeline.drawOutline(context, inputX, inputY, inputW, inputH,
            inputFocused ? ColorMath.withAlpha(NeonEdge.cool(), Math.round(210 * openT))
                         : ColorMath.withAlpha(0xFFFFFF, Math.round(28 * openT)));

        float inputTextY = MsdfText.topFor(inputY + inputH * 0.5f, 6.6f);
        if (nameInput.length() == 0) {
            MsdfText.draw(context, text, I18n.get("gui.config.input_placeholder"),
                inputX + 8.0f, inputTextY, 6.6f, ColorMath.withAlpha(0x777E90, Math.round(180 * openT)));
        } else {
            MsdfText.draw(context, bold, nameInput.toString(), inputX + 8.0f, inputTextY, 6.6f,
                ColorMath.withAlpha(0xFFFFFF, Math.round(230 * openT)));
        }
        if (inputFocused && (System.currentTimeMillis() % 1000L) < 500L) {
            float curX = inputX + 8.0f + (nameInput.length() == 0 ? 0.0f : MsdfText.width(bold, nameInput.toString(), 6.6f) + 1.0f);
            context.fill(Math.round(curX), Math.round(inputY + 5.0f), Math.round(curX + 1.0f), Math.round(inputY + inputH - 5.0f),
                ColorMath.withAlpha(NeonEdge.cool(), Math.round(230 * openT)));
        }

        // Button "Create"
        createX = inputX + inputW + 6.0f;
        createY = topY;
        createW = 68.0f;
        createH = 22.0f;
        boolean createHov = mouseX >= createX && mouseX <= createX + createW && mouseY >= createY && mouseY <= createY + createH;
        RoundedRectPipeline.draw(context, createX, createY, createW, createH,
            createHov ? ColorMath.withAlpha(NeonEdge.cool(), Math.round(55 * openT)) : ColorMath.withAlpha(0xFFFFFF, Math.round(15 * openT)));
        RoundedRectPipeline.drawOutline(context, createX, createY, createW, createH,
            ColorMath.withAlpha(NeonEdge.cool(), Math.round((createHov ? 200 : 70) * openT)));
        MsdfText.drawCentered(context, bold, I18n.get("gui.config.create"), createX + createW * 0.5f,
            MsdfText.topFor(createY + createH * 0.5f, 6.2f), 6.2f,
            ColorMath.withAlpha(0xFFFFFF, Math.round((createHov ? 255 : 210) * openT)));

        // Button "Folder"
        folderX = createX + createW + 5.0f;
        folderY = topY;
        folderW = 56.0f;
        folderH = 22.0f;
        boolean folderHov = mouseX >= folderX && mouseX <= folderX + folderW && mouseY >= folderY && mouseY <= folderY + folderH;
        RoundedRectPipeline.draw(context, folderX, folderY, folderW, folderH,
            folderHov ? ColorMath.withAlpha(0xFFFFFF, Math.round(30 * openT)) : ColorMath.withAlpha(0xFFFFFF, Math.round(14 * openT)));
        RoundedRectPipeline.drawOutline(context, folderX, folderY, folderW, folderH,
            ColorMath.withAlpha(0xFFFFFF, Math.round((folderHov ? 90 : 35) * openT)));
        MsdfText.drawCentered(context, bold, I18n.get("gui.config.folder"), folderX + folderW * 0.5f,
            MsdfText.topFor(folderY + folderH * 0.5f, 6.2f), 6.2f,
            ColorMath.withAlpha(0xFFFFFF, Math.round((folderHov ? 255 : 200) * openT)));

        // Button "Refresh"
        refreshX = folderX + folderW + 5.0f;
        refreshY = topY;
        refreshW = 64.0f;
        refreshH = 22.0f;
        boolean refreshHov = mouseX >= refreshX && mouseX <= refreshX + refreshW && mouseY >= refreshY && mouseY <= refreshY + refreshH;
        RoundedRectPipeline.draw(context, refreshX, refreshY, refreshW, refreshH,
            refreshHov ? ColorMath.withAlpha(0xFFFFFF, Math.round(30 * openT)) : ColorMath.withAlpha(0xFFFFFF, Math.round(14 * openT)));
        RoundedRectPipeline.drawOutline(context, refreshX, refreshY, refreshW, refreshH,
            ColorMath.withAlpha(0xFFFFFF, Math.round((refreshHov ? 90 : 35) * openT)));
        MsdfText.drawCentered(context, bold, I18n.get("gui.config.refresh"), refreshX + refreshW * 0.5f,
            MsdfText.topFor(refreshY + refreshH * 0.5f, 6.0f), 6.0f,
            ColorMath.withAlpha(0xFFFFFF, Math.round((refreshHov ? 255 : 200) * openT)));

        // Button "AutoSave"
        autoX = refreshX + refreshW + 6.0f;
        autoY = topY;
        autoW = areaRight - autoX;
        autoH = 22.0f;
        boolean autoHov = mouseX >= autoX && mouseX <= autoX + autoW && mouseY >= autoY && mouseY <= autoY + autoH;
        boolean autoOn = ModuleRegistry.configAutoSave() != null && ModuleRegistry.configAutoSave().value();
        RoundedRectPipeline.draw(context, autoX, autoY, autoW, autoH,
            autoOn ? ColorMath.withAlpha(0x4ADE80, Math.round((autoHov ? 40 : 22) * openT))
                   : ColorMath.withAlpha(0xFFFFFF, Math.round((autoHov ? 25 : 12) * openT)));
        RoundedRectPipeline.drawOutline(context, autoX, autoY, autoW, autoH,
            autoOn ? ColorMath.withAlpha(0x4ADE80, Math.round(140 * openT))
                   : ColorMath.withAlpha(0xFFFFFF, Math.round(30 * openT)));
        MsdfText.drawCentered(context, bold, I18n.get(autoOn ? "gui.config.autosave_on" : "gui.config.autosave_off"),
            autoX + autoW * 0.5f, MsdfText.topFor(autoY + autoH * 0.5f, 5.8f), 5.8f,
            autoOn ? ColorMath.withAlpha(0x4ADE80, Math.round(230 * openT)) : ColorMath.withAlpha(0x999999, Math.round(180 * openT)));

        // 2. Sub-Bar (Y = topY + 27.0f)
        float subY = topY + 27.0f;
        String activePrefix = I18n.get("gui.config.active_prefix");
        MsdfText.draw(context, text, activePrefix, areaLeft + 2.0f, subY, 6.2f,
            ColorMath.withAlpha(0x888EA0, Math.round(190 * openT)));
        float prefixW = MsdfText.width(text, activePrefix, 6.2f);
        MsdfText.draw(context, bold, ConfigManager.getActiveProfile(), areaLeft + 2.0f + prefixW, subY, 6.2f,
            ColorMath.withAlpha(0x4ADE80, Math.round(240 * openT)));

        if (statusMessage != null && System.currentTimeMillis() - statusTime < 3500L) {
            float statW = MsdfText.width(bold, statusMessage, 6.2f);
            MsdfText.draw(context, bold, statusMessage, areaRight - statW - 2.0f, subY, 6.2f,
                ColorMath.withAlpha(statusColor, Math.round(230 * openT)));
        } else {
            List<String> profs = ConfigManager.getProfiles();
            String cntStr = I18n.get("gui.config.total", profs.size());
            float cntW = MsdfText.width(text, cntStr, 6.0f);
            MsdfText.draw(context, text, cntStr, areaRight - cntW - 2.0f, subY, 6.0f,
                ColorMath.withAlpha(0x666D80, Math.round(160 * openT)));
        }

        // 3. Config Cards List
        float listTop = subY + 12.0f;
        float listBottom = areaBottom;
        float listH = listBottom - listTop;
        viewHeight = listH;

        List<String> profiles = ConfigManager.getProfiles();
        float cardH = 27.0f;
        float cardGap = 4.0f;
        contentHeight = Math.max(0.0f, profiles.size() * (cardH + cardGap) - cardGap);
        scrollTarget = Math.max(0.0f, Math.min(scrollTarget, Math.max(0.0f, contentHeight - viewHeight)));
        scrollSpring.step(scrollTarget, deltaSeconds);
        float scroll = (float) scrollSpring.get();

        context.enableScissor(Math.round(areaLeft), Math.round(listTop), Math.round(areaRight), Math.round(listBottom));

        actionBounds.clear();
        for (int i = 0; i < profiles.size(); i++) {
            String profile = profiles.get(i);
            float cardY = listTop + i * (cardH + cardGap) - scroll;
            float cardW = areaRight - areaLeft;

            float loadW = 58.0f;
            float loadH = 18.0f;
            float loadX = areaLeft + cardW - 146.0f;
            float loadY = cardY + 4.5f;

            float saveW = 58.0f;
            float saveH = 18.0f;
            float saveX = areaLeft + cardW - 84.0f;
            float saveY = cardY + 4.5f;

            float delW = 18.0f;
            float delH = 18.0f;
            float delX = areaLeft + cardW - 22.0f;
            float delY = cardY + 4.5f;

            actionBounds.add(new ActionBounds(profile, areaLeft, cardY, cardW, cardH,
                loadX, loadY, loadW, loadH, saveX, saveY, saveW, saveH, delX, delY, delW, delH));

            if (cardY + cardH < listTop || cardY > listBottom) {
                continue;
            }

            boolean isActive = profile.equalsIgnoreCase(ConfigManager.getActiveProfile());
            boolean cardHov = mouseX >= areaLeft && mouseX <= areaRight && mouseY >= cardY && mouseY <= cardY + cardH;

            // Card background
            if (isActive) {
                RoundedRectPipeline.draw(context, areaLeft, cardY, cardW, cardH,
                    ColorMath.withAlpha(0x101D16, Math.round(235 * openT)));
                RoundedRectPipeline.drawOutline(context, areaLeft, cardY, cardW, cardH,
                    ColorMath.withAlpha(0x4ADE80, Math.round(90 * openT)));
                // Active green strip
                RoundedRectPipeline.draw(context, areaLeft, cardY, 3.0f, cardH,
                    ColorMath.withAlpha(0x4ADE80, Math.round(230 * openT)));
            } else {
                RoundedRectPipeline.draw(context, areaLeft, cardY, cardW, cardH,
                    ColorMath.withAlpha(0x0C0E14, Math.round(220 * openT)));
                RoundedRectPipeline.drawOutline(context, areaLeft, cardY, cardW, cardH,
                    ColorMath.withAlpha(0xFFFFFF, Math.round((cardHov ? 35 : 18) * openT)));
            }

            // Name & Info
            float nameX = areaLeft + 12.0f;
            float nameY = MsdfText.topFor(cardY + cardH * 0.5f, 7.0f);
            MsdfText.draw(context, bold, profile, nameX, nameY, 7.0f,
                isActive ? ColorMath.withAlpha(0x4ADE80, Math.round(240 * openT)) : ColorMath.withAlpha(0xFFFFFF, Math.round(220 * openT)));

            float nameW = MsdfText.width(bold, profile, 7.0f);
            if (isActive) {
                MsdfText.draw(context, bold, I18n.get("gui.config.active_badge"), nameX + nameW + 8.0f, nameY + 0.6f, 5.6f,
                    ColorMath.withAlpha(0x4ADE80, Math.round(200 * openT)));
            } else {
                MsdfText.draw(context, text, profile + ".json", nameX + nameW + 8.0f, nameY + 0.6f, 5.6f,
                    ColorMath.withAlpha(0x666D80, Math.round(150 * openT)));
            }

            // Load Button
            boolean loadHov = mouseX >= loadX && mouseX <= loadX + loadW && mouseY >= loadY && mouseY <= loadY + loadH;
            RoundedRectPipeline.draw(context, loadX, loadY, loadW, loadH,
                loadHov ? ColorMath.withAlpha(NeonEdge.cool(), Math.round(60 * openT)) : ColorMath.withAlpha(0xFFFFFF, Math.round(14 * openT)));
            RoundedRectPipeline.drawOutline(context, loadX, loadY, loadW, loadH,
                ColorMath.withAlpha(NeonEdge.cool(), Math.round((loadHov ? 180 : 50) * openT)));
            MsdfText.drawCentered(context, bold, I18n.get("gui.config.load"), loadX + loadW * 0.5f,
                MsdfText.topFor(loadY + loadH * 0.5f, 5.6f), 5.6f,
                ColorMath.withAlpha(0xFFFFFF, Math.round((loadHov ? 255 : 200) * openT)));

            // Save Button
            boolean saveHov = mouseX >= saveX && mouseX <= saveX + saveW && mouseY >= saveY && mouseY <= saveY + saveH;
            RoundedRectPipeline.draw(context, saveX, saveY, saveW, saveH,
                saveHov ? ColorMath.withAlpha(0x4ADE80, Math.round(50 * openT)) : ColorMath.withAlpha(0xFFFFFF, Math.round(14 * openT)));
            RoundedRectPipeline.drawOutline(context, saveX, saveY, saveW, saveH,
                ColorMath.withAlpha(0x4ADE80, Math.round((saveHov ? 160 : 50) * openT)));
            MsdfText.drawCentered(context, bold, I18n.get("gui.config.save"), saveX + saveW * 0.5f,
                MsdfText.topFor(saveY + saveH * 0.5f, 5.6f), 5.6f,
                ColorMath.withAlpha(0xFFFFFF, Math.round((saveHov ? 255 : 200) * openT)));

            // Delete Button
            boolean canDel = profiles.size() > 1;
            boolean delHov = canDel && mouseX >= delX && mouseX <= delX + delW && mouseY >= delY && mouseY <= delY + delH;
            RoundedRectPipeline.draw(context, delX, delY, delW, delH,
                delHov ? ColorMath.withAlpha(0xFF4444, Math.round(65 * openT)) : ColorMath.withAlpha(0xFFFFFF, Math.round(canDel ? 12 : 5 * openT)));
            RoundedRectPipeline.drawOutline(context, delX, delY, delW, delH,
                canDel ? (delHov ? ColorMath.withAlpha(0xFF4444, Math.round(180 * openT)) : ColorMath.withAlpha(0xFF4444, Math.round(55 * openT)))
                       : ColorMath.withAlpha(0xFFFFFF, Math.round(15 * openT)));
            MsdfText.drawCentered(context, bold, "✕", delX + delW * 0.5f,
                MsdfText.topFor(delY + delH * 0.5f, 6.2f), 6.2f,
                canDel ? (delHov ? ColorMath.withAlpha(0xFF6B6B, Math.round(255 * openT)) : ColorMath.withAlpha(0xDD6B6B, Math.round(180 * openT)))
                       : ColorMath.withAlpha(0x555555, Math.round(100 * openT)));
        }

        context.disableScissor();

        scrollbar.draw(context, areaRight - 1.0f, listTop, listH, contentHeight, scroll, openT);
    }

    boolean mouseClicked(float mx, float my) {
        // Input box
        if (mx >= inputX && mx <= inputX + inputW && my >= inputY && my <= inputY + inputH) {
            inputFocused = true;
            return true;
        } else if (my < inputY + inputH + 6.0f) {
            inputFocused = false;
        }

        // Create button
        if (mx >= createX && mx <= createX + createW && my >= createY && my <= createY + createH) {
            createFromInput();
            return true;
        }

        // Folder button
        if (mx >= folderX && mx <= folderX + folderW && my >= folderY && my <= folderY + folderH) {
            ConfigManager.openFolder();
            playUiSound(SoundEvents.UI_BUTTON_CLICK.value());
            return true;
        }

        // Refresh button
        if (mx >= refreshX && mx <= refreshX + refreshW && my >= refreshY && my <= refreshY + refreshH) {
            ConfigManager.reloadProfiles();
            setStatus(I18n.get("gui.config.status.refreshed"), 0x4ADE80);
            playUiSound(SoundEvents.UI_BUTTON_CLICK.value());
            return true;
        }

        // AutoSave toggle
        if (mx >= autoX && mx <= autoX + autoW && my >= autoY && my <= autoY + autoH) {
            if (ModuleRegistry.configAutoSave() != null) {
                ModuleRegistry.configAutoSave().toggle();
                playUiSound(SoundEvents.UI_BUTTON_CLICK.value());
            }
            return true;
        }

        // Config actions
        for (ActionBounds b : actionBounds) {
            if (my < b.cardY || my > b.cardY + b.cardH || mx < b.cardX || mx > b.cardX + b.cardW) {
                continue;
            }
            // Load button
            if (mx >= b.loadX && mx <= b.loadX + b.loadW && my >= b.loadY && my <= b.loadY + b.loadH) {
                if (ConfigManager.loadProfile(b.profile)) {
                    setStatus(I18n.get("gui.config.status.loaded", b.profile), 0x4ADE80);
                    playUiSound(LinkenVisuals.TOGGLE_ON_SOUND);
                }
                return true;
            }
            // Save button
            if (mx >= b.saveX && mx <= b.saveX + b.saveW && my >= b.saveY && my <= b.saveY + b.saveH) {
                if (ConfigManager.saveProfile(b.profile)) {
                    setStatus(I18n.get("gui.config.status.saved", b.profile), 0x4ADE80);
                    playUiSound(LinkenVisuals.TOGGLE_ON_SOUND);
                }
                return true;
            }
            // Delete button
            if (mx >= b.delX && mx <= b.delX + b.delW && my >= b.delY && my <= b.delY + b.delH) {
                if (ConfigManager.deleteProfile(b.profile)) {
                    setStatus(I18n.get("gui.config.status.deleted", b.profile), 0xFF6B6B);
                    playUiSound(LinkenVisuals.TOGGLE_OFF_SOUND);
                }
                return true;
            }
            // Clicking anywhere on card loads it
            if (ConfigManager.loadProfile(b.profile)) {
                setStatus(I18n.get("gui.config.status.loaded", b.profile), 0x4ADE80);
                playUiSound(LinkenVisuals.TOGGLE_ON_SOUND);
            }
            return true;
        }

        return false;
    }

    boolean charTyped(CharInput input) {
        if (!inputFocused || !input.isValidChar()) {
            return false;
        }
        char c = input.asString().charAt(0);
        if (Character.isLetterOrDigit(c) || c == '_' || c == '-' || c == ' ') {
            if (nameInput.length() < 24) {
                nameInput.append(c);
                return true;
            }
        }
        return false;
    }

    boolean keyPressed(KeyInput input) {
        if (!inputFocused) {
            return false;
        }
        if (input.key() == GLFW.GLFW_KEY_BACKSPACE && nameInput.length() > 0) {
            nameInput.deleteCharAt(nameInput.length() - 1);
            return true;
        }
        if (input.key() == GLFW.GLFW_KEY_ENTER || input.key() == GLFW.GLFW_KEY_KP_ENTER) {
            createFromInput();
            return true;
        }
        if (input.key() == GLFW.GLFW_KEY_ESCAPE) {
            inputFocused = false;
            return true;
        }
        return false;
    }

    void scroll(double verticalAmount) {
        scrollTarget -= (float) verticalAmount * 24.0f;
        scrollTarget = Math.max(0.0f, Math.min(scrollTarget, Math.max(0.0f, contentHeight - viewHeight)));
    }

    void resetInput() {
        inputFocused = false;
        nameInput.setLength(0);
    }

    private void createFromInput() {
        String name = nameInput.toString().trim();
        if (!name.isEmpty()) {
            boolean ok = ConfigManager.createProfile(name);
            if (ok) {
                setStatus(I18n.get("gui.config.status.created", name), 0x4ADE80);
                playUiSound(LinkenVisuals.TOGGLE_ON_SOUND);
            }
            nameInput.setLength(0);
            inputFocused = false;
        }
    }

    private static void setStatus(String message, int color) {
        statusMessage = message;
        statusColor = color;
        statusTime = System.currentTimeMillis();
    }

    private void playUiSound(SoundEvent sound) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null) {
            client.getSoundManager().play(PositionedSoundInstance.ui(sound, 1.0f, 1.0f));
        }
    }

    private static final class ActionBounds {
        final String profile;
        final float cardX, cardY, cardW, cardH;
        final float loadX, loadY, loadW, loadH;
        final float saveX, saveY, saveW, saveH;
        final float delX, delY, delW, delH;

        ActionBounds(String profile, float cardX, float cardY, float cardW, float cardH,
                     float loadX, float loadY, float loadW, float loadH,
                     float saveX, float saveY, float saveW, float saveH,
                     float delX, float delY, float delW, float delH) {
            this.profile = profile;
            this.cardX = cardX;
            this.cardY = cardY;
            this.cardW = cardW;
            this.cardH = cardH;
            this.loadX = loadX;
            this.loadY = loadY;
            this.loadW = loadW;
            this.loadH = loadH;
            this.saveX = saveX;
            this.saveY = saveY;
            this.saveW = saveW;
            this.saveH = saveH;
            this.delX = delX;
            this.delY = delY;
            this.delW = delW;
            this.delH = delH;
        }
    }
}
