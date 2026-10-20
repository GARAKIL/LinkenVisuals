package dev.linkenvisuals.ui.linkmenu;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.AttackIndicator;
import net.minecraft.client.option.CloudRenderMode;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.GraphicsMode;
import net.minecraft.client.option.NarratorMode;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.client.render.ChunkBuilderMode;
import net.minecraft.network.message.ChatVisibility;
import net.minecraft.particle.ParticlesMode;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Arm;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.DoubleFunction;
import java.util.function.Function;
import java.util.function.IntFunction;

/** Vanilla option bindings for the LINKEN settings sub-pages. */
public final class LinkenSettingsCatalog {
    private LinkenSettingsCatalog() {
    }

    public static List<LinkenSettingsOption> graphics() {
        GameOptions o = MinecraftClient.getInstance().options;
        List<LinkenSettingsOption> out = new ArrayList<>();
        out.add(cycle("Graphics", o.getPreset(), GraphicsMode.values(), mode -> mode.name()));
        out.add(intSlider("Render Distance", o.getViewDistance(), 2, 32, v -> v + " chunks"));
        out.add(intSlider("Simulation Distance", o.getSimulationDistance(), 5, 32, v -> v + " chunks"));
        out.add(cycle("Chunk Builder", o.getChunkBuilderMode(), ChunkBuilderMode.values(), mode -> mode.name()));
        out.add(intSlider("Max Framerate", o.getMaxFps(), 10, 260, v -> v >= 260 ? "Unlimited" : v + " fps"));
        out.add(toggle("Smooth Lighting", o.getAo()));
        out.add(toggle("V-Sync", o.getEnableVsync()));
        out.add(intSlider("GUI Scale", o.getGuiScale(), 0, 4, v -> v == 0 ? "Auto" : String.valueOf(v)));
        out.add(cycle("Clouds", o.getCloudRenderMode(), CloudRenderMode.values(), mode -> mode.name()));
        out.add(doubleSlider("Brightness", o.getGamma(), 0.0, 1.0,
            v -> v <= 0.01 ? "Moody" : v >= 0.99 ? "Bright" : Math.round(v * 100) + "%"));
        out.add(cycle("Particles", o.getParticles(), ParticlesMode.values(), mode -> mode.name()));
        out.add(toggle("Entity Shadows", o.getEntityShadows()));
        out.add(doubleSlider("Entity Distance", o.getEntityDistanceScaling(), 0.5, 5.0,
            v -> Math.round(v * 100) + "%"));
        out.add(doubleSlider("Glint Speed", o.getGlintSpeed(), 0.0, 1.0,
            v -> Math.round(v * 100) + "%"));
        out.add(doubleSlider("Glint Strength", o.getGlintStrength(), 0.0, 1.0,
            v -> Math.round(v * 100) + "%"));
        out.add(intSlider("Menu Blur", o.getMenuBackgroundBlurriness(), 0, 10, String::valueOf));
        out.add(toggle("View Bobbing", o.getBobView()));
        out.add(cycle("Attack Indicator", o.getAttackIndicator(), AttackIndicator.values(), mode -> mode.name()));
        return out;
    }

    public static List<LinkenSettingsOption> sound() {
        GameOptions o = MinecraftClient.getInstance().options;
        List<LinkenSettingsOption> out = new ArrayList<>();
        for (SoundCategory category : SoundCategory.values()) {
            String label = titleCase(category.name());
            out.add(doubleSlider(label, o.getSoundVolumeOption(category), 0.0, 1.0,
                v -> v <= 0.005 ? "Muted" : Math.round(v * 100) + "%"));
        }
        out.add(toggle("Directional Audio", o.getDirectionalAudio()));
        out.add(toggle("Subtitles", o.getShowSubtitles()));
        out.add(LinkenSettingsOption.display("Sound Device", () -> {
            String device = o.getSoundDevice().getValue();
            return device == null || device.isBlank() ? "Default" : device;
        }));
        return out;
    }

    public static List<LinkenSettingsOption> chat() {
        GameOptions o = MinecraftClient.getInstance().options;
        List<LinkenSettingsOption> out = new ArrayList<>();
        out.add(cycle("Chat Visibility", o.getChatVisibility(), ChatVisibility.values(), mode -> mode.name()));
        out.add(toggle("Chat Colors", o.getChatColors()));
        out.add(toggle("Chat Links", o.getChatLinks()));
        out.add(toggle("Prompt on Links", o.getChatLinksPrompt()));
        out.add(toggle("Command Suggestions", o.getAutoSuggestions()));
        out.add(toggle("Hide Matched Names", o.getHideMatchedNames()));
        out.add(doubleSlider("Chat Opacity", o.getChatOpacity(), 0.0, 1.0, percent()));
        out.add(doubleSlider("Text Background", o.getTextBackgroundOpacity(), 0.0, 1.0, percent()));
        out.add(doubleSlider("Chat Scale", o.getChatScale(), 0.0, 1.0, percent()));
        out.add(doubleSlider("Chat Width", o.getChatWidth(), 0.0, 1.0,
            v -> Math.round(40 + v * 280) + " px"));
        out.add(doubleSlider("Focused Height", o.getChatHeightFocused(), 0.0, 1.0,
            v -> Math.round(20 + v * 160) + " px"));
        out.add(doubleSlider("Unfocused Height", o.getChatHeightUnfocused(), 0.0, 1.0,
            v -> Math.round(20 + v * 160) + " px"));
        out.add(doubleSlider("Line Spacing", o.getChatLineSpacing(), 0.0, 1.0, percent()));
        out.add(doubleSlider("Chat Delay", o.getChatDelay(), 0.0, 6.0,
            v -> v <= 0.01 ? "Instant" : String.format(Locale.ROOT, "%.1f s", v)));
        out.add(cycle("Narrator", o.getNarrator(), NarratorMode.values(), mode -> mode.name()));
        return out;
    }

    public static List<LinkenSettingsOption> controls() {
        GameOptions o = MinecraftClient.getInstance().options;
        List<LinkenSettingsOption> out = new ArrayList<>();
        out.add(LinkenSettingsOption.action("Keybinds", () -> "Open", () -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.currentScreen != null) {
                mc.setScreen(new LinkenKeybindsScreen(mc.currentScreen));
            }
        }));
        out.add(doubleSlider("Mouse Sensitivity", o.getMouseSensitivity(), 0.0, 1.0,
            v -> Math.round(v * 200) + "%"));
        out.add(toggle("Invert Mouse Y", o.getInvertMouseY()));
        out.add(toggle("Auto Jump", o.getAutoJump()));
        out.add(toggle("Sneak Toggle", o.getSneakToggled()));
        out.add(toggle("Sprint Toggle", o.getSprintToggled()));
        out.add(toggle("Raw Mouse Input", o.getRawMouseInput()));
        out.add(toggle("Discrete Scrolling", o.getDiscreteMouseScroll()));
        out.add(toggle("Touchscreen", o.getTouchscreen()));
        out.add(toggle("Operator Items Tab", o.getOperatorItemsTab()));
        out.add(doubleSlider("Mouse Wheel Sensitivity", o.getMouseWheelSensitivity(), 0.01, 10.0,
            v -> String.format(Locale.ROOT, "%.2f", v)));
        out.add(LinkenSettingsOption.display("Main Arm", () -> o.getMainArm().getValue() == Arm.LEFT
            ? "Left" : "Right"));
        return out;
    }

    public static List<LinkenSettingsOption> accessibility() {
        GameOptions o = MinecraftClient.getInstance().options;
        List<LinkenSettingsOption> out = new ArrayList<>();
        out.add(cycle("Narrator", o.getNarrator(), NarratorMode.values(), mode -> mode.name()));
        out.add(toggle("Subtitles", o.getShowSubtitles()));
        out.add(doubleSlider("Text Background", o.getTextBackgroundOpacity(), 0.0, 1.0, percent()));
        out.add(toggle("Chat Background Only", () -> !o.getBackgroundForChatOnly().getValue(),
            () -> o.getBackgroundForChatOnly().setValue(!o.getBackgroundForChatOnly().getValue())));
        out.add(doubleSlider("Damage Tilt", o.getDamageTiltStrength(), 0.0, 1.0,
            v -> v <= 0.001 ? "Off" : percent().apply(v)));
        out.add(doubleSlider("Distortion Effects", o.getDistortionEffectScale(), 0.0, 1.0,
            v -> v <= 0.001 ? "Off" : percent().apply(v)));
        out.add(doubleSlider("FOV Effects", o.getFovEffectScale(), 0.0, 1.0,
            v -> v <= 0.001 ? "Off" : percent().apply(v)));
        out.add(toggle("Monochrome Logo", o.getMonochromeLogo()));
        out.add(toggle("Hide Splash Text", o.getHideSplashTexts()));
        out.add(doubleSlider("Notification Time", o.getNotificationDisplayTime(), 0.5, 10.0,
            v -> String.format(Locale.ROOT, "x%.1f", v)));
        out.add(intSlider("Menu Blur", o.getMenuBackgroundBlurriness(), 0, 10,
            v -> v == 0 ? "Off" : String.valueOf(v)));
        out.add(doubleSlider("Panorama Speed", o.getPanoramaSpeed(), 0.0, 1.0, percent()));
        out.add(toggle("High Contrast", o.getHighContrast()));
        return out;
    }

    private static LinkenSettingsOption toggle(String label, SimpleOption<Boolean> option) {
        return LinkenSettingsOption.toggle(label, option::getValue,
            () -> option.setValue(!option.getValue()));
    }

    private static LinkenSettingsOption toggle(String label,
                                               java.util.function.BooleanSupplier get, Runnable toggle) {
        return LinkenSettingsOption.toggle(label, get, toggle);
    }

    private static LinkenSettingsOption intSlider(String label, SimpleOption<Integer> option,
                                                  int min, int max, IntFunction<String> display) {
        return LinkenSettingsOption.slider(label,
            () -> display.apply(option.getValue()),
            () -> (option.getValue() - min) / (double) (max - min),
            value -> option.setValue((int) Math.round(min + value * (max - min))));
    }

    private static LinkenSettingsOption doubleSlider(String label, SimpleOption<Double> option,
                                                     double min, double max, DoubleFunction<String> display) {
        return LinkenSettingsOption.slider(label,
            () -> display.apply(option.getValue()),
            () -> (option.getValue() - min) / (max - min),
            value -> option.setValue(min + value * (max - min)));
    }

    private static <T> LinkenSettingsOption cycle(String label, SimpleOption<T> option,
                                                  T[] values, Function<T, String> display) {
        return LinkenSettingsOption.cycle(label,
            () -> display.apply(option.getValue()),
            () -> {
                T current = option.getValue();
                int index = 0;
                for (int i = 0; i < values.length; i++) {
                    if (values[i] == current) {
                        index = i;
                        break;
                    }
                }
                option.setValue(values[(index + 1) % values.length]);
            });
    }

    private static DoubleFunction<String> percent() {
        return value -> Math.round(value * 100) + "%";
    }

    private static String titleCase(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        String lower = value.toLowerCase(Locale.ROOT).replace('_', ' ');
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }
}
