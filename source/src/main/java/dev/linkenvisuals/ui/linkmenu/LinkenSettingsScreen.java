package dev.linkenvisuals.ui.linkmenu;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;

import java.util.List;

/**
 * LINKEN settings hub. Main option pages are rendered by the shared LINKEN
 * sub-settings shell; language, skin and resource packs have dedicated LINKEN
 * list screens.
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

        out.add(new Entry("Video", "GRAPHICS, DISTANCE AND PARTICLES", LinkenMenuChrome.ICON_GEAR,
            false, false, () -> mc.setScreen(
                new LinkenSubSettingsScreen(this, "Video", LinkenSettingsCatalog.graphics()))));
        out.add(new Entry("Controls", "MOUSE, KEYBINDS AND MOVEMENT", LinkenMenuChrome.ICON_PERSON,
            false, false, () -> mc.setScreen(
                new LinkenSubSettingsScreen(this, "Controls", LinkenSettingsCatalog.controls()))));
        out.add(new Entry("Language", "INTERFACE LANGUAGE", LinkenMenuChrome.ICON_PEOPLE,
            false, false, () -> mc.setScreen(new LinkenLanguageScreen(this))));
        out.add(new Entry("Skin", "PLAYER APPEARANCE", LinkenMenuChrome.ICON_PERSON,
            false, false, () -> mc.setScreen(new LinkenSkinScreen(this))));
        out.add(new Entry("Sound", "MASTER, MUSIC AND AMBIENCE", LinkenMenuChrome.ICON_PEOPLE,
            false, false, () -> mc.setScreen(
                new LinkenSubSettingsScreen(this, "Sound", LinkenSettingsCatalog.sound()))));
        out.add(new Entry("Chat", "VISIBILITY, OPACITY AND SCALE", LinkenMenuChrome.ICON_PEOPLE,
            false, false, () -> mc.setScreen(
                new LinkenSubSettingsScreen(this, "Chat", LinkenSettingsCatalog.chat()))));
        out.add(new Entry("Accessibility", "VISION, COMFORT AND NARRATOR", LinkenMenuChrome.ICON_PERSON,
            false, false, () -> mc.setScreen(
                new LinkenSubSettingsScreen(this, "Accessibility", LinkenSettingsCatalog.accessibility()))));
        out.add(new Entry("Resource Packs", "TEXTURES AND SOUND PACKS", LinkenMenuChrome.ICON_GEAR,
            false, false, () -> mc.setScreen(new LinkenResourcePacksScreen(this))));
    }
}
