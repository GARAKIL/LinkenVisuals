package dev.linkenvisuals.ui.linkmenu;

import dev.linkenvisuals.clickgui.link.LinkGuiScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.OpenToLanScreen;
import net.minecraft.client.gui.screen.StatsScreen;
import net.minecraft.client.gui.screen.advancement.AdvancementsScreen;
import net.minecraft.client.world.ClientWorld;

import java.util.List;

/**
 * LINKEN pause menu. It replaces the vanilla {@code GameMenuScreen} only while
 * the LINKEN menu style is selected; the world is paused for the lifetime of
 * this screen. Secondary destinations deliberately stay vanilla until their
 * own LINKEN ports exist.
 */
public final class LinkenPauseScreen extends LinkenHubScreen {
    public LinkenPauseScreen() {
        super(null, "PAUSED", pauseSubtitle());
    }

    private static String pauseSubtitle() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null) {
            return "GAME MENU";
        }
        if (mc.getCurrentServerEntry() != null && mc.getCurrentServerEntry().name != null) {
            return "MULTIPLAYER  /  " + mc.getCurrentServerEntry().name;
        }
        if (mc.getServer() != null && mc.getServer().getSaveProperties() != null) {
            return "SINGLEPLAYER  /  " + mc.getServer().getSaveProperties().getLevelName();
        }
        return "GAME MENU";
    }

    @Override
    public boolean shouldPause() {
        return true;
    }

    @Override
    protected void buildEntries(List<Entry> out) {
        MinecraftClient mc = client;
        if (mc == null) {
            return;
        }

        out.add(new Entry("Back to Game", "RETURN TO THE WORLD", LinkenMenuChrome.ICON_POWER,
            true, false, () -> mc.setScreen(null)));
        out.add(new Entry("Settings", "VIDEO, CONTROLS, LANGUAGE", LinkenMenuChrome.ICON_GEAR,
            false, false, () -> mc.setScreen(new LinkenSettingsScreen(this))));
        out.add(new Entry("LinkenVisuals", "CLIENT MODULES AND CONFIGS", LinkenMenuChrome.ICON_PERSON,
            false, false, () -> mc.setScreen(new LinkGuiScreen(this))));

        if (mc.getNetworkHandler() != null) {
            out.add(new Entry("Advancements", "WORLD PROGRESS", LinkenMenuChrome.ICON_PEOPLE,
                false, false, () -> mc.setScreen(
                    new AdvancementsScreen(mc.getNetworkHandler().getAdvancementHandler(), this))));
        }
        if (mc.player != null) {
            out.add(new Entry("Statistics", "YOUR WORLD NUMBERS", LinkenMenuChrome.ICON_PEOPLE,
                false, false, () -> mc.setScreen(new StatsScreen(this, mc.player.getStatHandler()))));
        }
        if (mc.isInSingleplayer()) {
            out.add(new Entry("Open to LAN", "SHARE THIS WORLD", LinkenMenuChrome.ICON_PEOPLE,
                false, false, () -> mc.setScreen(new OpenToLanScreen(this))));
        }
        String leave = mc.isInSingleplayer() ? "Save and Quit" : "Disconnect";
        out.add(new Entry(leave, mc.isInSingleplayer() ? "SAVE THE WORLD AND LEAVE" : "LEAVE THE SERVER",
            LinkenMenuChrome.ICON_POWER, false, true, () ->
                mc.disconnect(ClientWorld.QUITTING_MULTIPLAYER_TEXT)));
        out.add(new Entry("Quit Game", "CLOSE MINECRAFT", LinkenMenuChrome.ICON_POWER,
            false, true, mc::scheduleStop));
    }
}
