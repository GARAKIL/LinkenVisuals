package dev.linkenvisuals.module.impl;

import dev.linkenvisuals.clickgui.Category;
import dev.linkenvisuals.clickgui.setting.ModeSetting;
import dev.linkenvisuals.clickgui.setting.Setting;
import dev.linkenvisuals.module.Module;
import net.minecraft.client.MinecraftClient;

import java.util.List;

/**
 * TimeChanger (Phase 5.7): client-side world time override.
 * Donor's {@code TimeChanger} (394 lines, +glyphs) simplified: time presets only.
 */
public final class TimeChangerModule extends Module {
    public final ModeSetting preset;

    public TimeChangerModule() {
        this(new ModeSetting("Time", List.of("Day", "Noon", "Sunset", "Night", "Midnight"), 0));
    }

    private TimeChangerModule(ModeSetting preset) {
        super("timechanger", "Time Changer", "client-side time",
            Category.VISUAL,
            List.of(),
            List.<Setting>of(preset));
        this.preset = preset;
    }

    /** Called each tick to apply the time. */
    public static void tick() {
        TimeChangerModule module = dev.linkenvisuals.module.Modules.timeChanger();
        if (module == null || !module.enabled()) {
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) {
            return;
        }
        long time = switch (module.preset.value()) {
            case "Noon" -> 6000L;
            case "Sunset" -> 12000L;
            case "Night" -> 13000L;
            case "Midnight" -> 18000L;
            default -> 1000L; // Day
        };
        client.world.setTimeOfDay(time);
    }
}
