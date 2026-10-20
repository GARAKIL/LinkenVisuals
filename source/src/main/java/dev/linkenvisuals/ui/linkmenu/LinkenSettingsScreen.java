package dev.linkenvisuals.ui.linkmenu;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.AccessibilityOptionsScreen;
import net.minecraft.client.gui.screen.option.ChatOptionsScreen;
import net.minecraft.client.gui.screen.option.ControlsOptionsScreen;
import net.minecraft.client.gui.screen.option.LanguageOptionsScreen;
import net.minecraft.client.gui.screen.option.SkinOptionsScreen;
import net.minecraft.client.gui.screen.option.SoundOptionsScreen;
import net.minecraft.client.gui.screen.option.VideoOptionsScreen;
import net.minecraft.client.gui.screen.pack.PackScreen;
import net.minecraft.text.Text;

import java.util.List;

/**
 * LINKEN settings hub. The hub is fully custom; the individual option pages
 * remain the vanilla screens (their LINKEN material pass is a later milestone),
 * so opening them cannot regress the Beta/Vanilla shells.
 */
public final class LinkenSettingsScreen extends LinkenHubScreen {
    public LinkenSettingsScreen(Screen parent) {
        super(parent, "SETTINGS", "VIDEO, CONTROLS, LANGUAGE AND ACCESSIBILITY");
    }

    @Override
    protected void buildEntries(List<Entry> out) {
        MinecraftClient mc = client;
        if (mc == null) {
            return;
        }

        out.add(new Entry("Video", "RENDER DISTANCE, GUI SCALE, STYLE", LinkenMenuChrome.ICON_GEAR,
            false, false, () -> mc.setScreen(new VideoOptionsScreen(this, mc, mc.options))));
        out.add(new Entry("Controls", "KEYBOARD, MOUSE AND BINDS", LinkenMenuChrome.ICON_PERSON,
            false, false, () -> mc.setScreen(new ControlsOptionsScreen(this, mc.options))));
        out.add(new Entry("Language", "INTERFACE LANGUAGE", LinkenMenuChrome.ICON_PEOPLE,
            false, false, () -> mc.setScreen(
                new LanguageOptionsScreen(this, mc.options, mc.getLanguageManager()))));
        out.add(new Entry("Skin", "PLAYER APPEARANCE", LinkenMenuChrome.ICON_PERSON,
            false, false, () -> mc.setScreen(new SkinOptionsScreen(this, mc.options))));
        out.add(new Entry("Sound", "MASTER, MUSIC AND AMBIENCE", LinkenMenuChrome.ICON_PEOPLE,
            false, false, () -> mc.setScreen(new SoundOptionsScreen(this, mc.options))));
        out.add(new Entry("Chat", "CHAT VISIBILITY AND OPACITY", LinkenMenuChrome.ICON_PEOPLE,
            false, false, () -> mc.setScreen(new ChatOptionsScreen(this, mc.options))));
        out.add(new Entry("Accessibility", "VISION, CONTROLS AND COMFORT", LinkenMenuChrome.ICON_PERSON,
            false, false, () -> mc.setScreen(new AccessibilityOptionsScreen(this, mc.options))));
        out.add(new Entry("Resource Packs", "TEXTURES AND SOUND PACKS", LinkenMenuChrome.ICON_GEAR,
            false, false, () -> mc.setScreen(new PackScreen(
                mc.getResourcePackManager(),
                manager -> mc.reloadResources(),
                mc.getResourcePackDir(),
                Text.translatable("resourcePack.title")))));
    }
}
