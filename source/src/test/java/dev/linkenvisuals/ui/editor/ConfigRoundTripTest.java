package dev.linkenvisuals.ui.editor;

import dev.linkenvisuals.ui.style.MenuStyle;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigRoundTripTest {
    @Test
    void linkenStyleNameRoundTripsThroughTheConfigFormat() {
        String stored = MenuStyle.LINKEN.name();
        assertEquals(MenuStyle.LINKEN, MenuStyle.fromName(stored));
        assertEquals(MenuStyle.LINKEN, MenuStyle.fromName(MenuStyle.LINKEN.displayName()));
    }

    @Test
    void linkenPresetLeavesTheEditableLayoutUntouched() {
        MainMenuConfig config = new MainMenuConfig();
        config.elements.put("LOGO", new MainMenuConfig.ElementConfig("LOGO", 0.07f, 0.5f, 1.0f));
        MainMenuConfig.ElementConfig logo = config.elements.get("LOGO");
        logo.custom = true;
        logo.relX = 0.21f;
        logo.relY = 0.42f;
        logo.scale = 1.35f;
        logo.visible = false;

        config.backgroundMode = MainMenuConfig.BackgroundMode.CUSTOM_IMAGE;
        config.shaderPreset = MainMenuConfig.ShaderPreset.TOXIN;
        config.shaderOverlayEnabled = false;
        config.imageDarkening = 0.8f;
        config.useVanillaTitleScreen = true;
        config.customMultiplayerScreen = false;
        config.customSettingsScreen = false;
        config.customLoadingScreen = false;
        config.customPauseScreen = false;
        config.customHotbar = false;
        config.customStatusEffects = false;

        config.applyPreset(MenuStyle.LINKEN);

        assertSame(logo, config.elements.get("LOGO"), "existing element config must survive");
        assertTrue(logo.custom, "LINKEN must not reset the editable position");
        assertEquals(0.21f, logo.relX, 1.0e-6f);
        assertEquals(0.42f, logo.relY, 1.0e-6f);
        assertEquals(1.35f, logo.scale, 1.0e-6f);
        assertFalse(logo.visible, "visibility is owned by the Beta/Vanilla editor");
        assertEquals(MainMenuConfig.BackgroundMode.CUSTOM_IMAGE, config.backgroundMode);
        assertEquals(MainMenuConfig.ShaderPreset.TOXIN, config.shaderPreset);
        assertFalse(config.shaderOverlayEnabled);
        assertEquals(0.8f, config.imageDarkening, 1.0e-6f);
        assertTrue(config.useVanillaTitleScreen);
        assertFalse(config.customMultiplayerScreen);
        assertFalse(config.customSettingsScreen);
        assertFalse(config.customLoadingScreen);
        assertFalse(config.customPauseScreen);
        assertFalse(config.customHotbar);
        assertFalse(config.customStatusEffects);
    }

    @Test
    void switchingBackToBetaStillAppliesItsPreset() {
        MainMenuConfig config = new MainMenuConfig();
        config.elements.put("LOGO", new MainMenuConfig.ElementConfig("LOGO", 0.07f, 0.5f, 1.0f));
        MainMenuConfig.ElementConfig logo = config.elements.get("LOGO");
        logo.custom = true;
        logo.relX = 0.3f;
        logo.relY = 0.3f;
        logo.scale = 1.4f;
        logo.visible = false;
        config.backgroundMode = MainMenuConfig.BackgroundMode.CUSTOM_IMAGE;
        config.shaderPreset = MainMenuConfig.ShaderPreset.TOXIN;
        config.shaderOverlayEnabled = false;
        config.useVanillaTitleScreen = true;
        config.customMultiplayerScreen = false;

        config.applyPreset(MenuStyle.BETA);

        assertEquals(MainMenuConfig.BackgroundMode.SHADER, config.backgroundMode);
        assertEquals(MainMenuConfig.ShaderPreset.COSMOS, config.shaderPreset);
        assertTrue(config.shaderOverlayEnabled);
        assertEquals(MainMenuConfig.ShaderEffect.BLOOM, config.shaderEffect);
        assertFalse(config.useVanillaTitleScreen);
        assertTrue(config.customMultiplayerScreen);
        assertTrue(config.customSettingsScreen);
        assertTrue(config.customLoadingScreen);
        assertTrue(config.customPauseScreen);
        assertFalse(logo.custom, "Beta preset restores the default layout");
        assertEquals(1.0f, logo.scale, 1.0e-6f);
        assertTrue(logo.visible, "Beta preset restores visibility");
        assertTrue(config.elements.containsKey("PLAYER_CARD"));
    }

    @Test
    void vanillaPresetEnablesTheVanillaTitleAndDisablesTheOverlay() {
        MainMenuConfig config = new MainMenuConfig();

        config.applyPreset(MenuStyle.VANILLA);

        assertTrue(config.useVanillaTitleScreen);
        assertFalse(config.customMultiplayerScreen);
        assertTrue(config.customSettingsScreen);
        assertTrue(config.customLoadingScreen);
        assertTrue(config.customPauseScreen);
        assertFalse(config.shaderOverlayEnabled);
        assertEquals(MainMenuConfig.ShaderEffect.NONE, config.shaderEffect);
    }
}
