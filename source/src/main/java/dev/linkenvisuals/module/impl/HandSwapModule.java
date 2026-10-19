package dev.linkenvisuals.module.impl;

import dev.linkenvisuals.clickgui.Category;
import dev.linkenvisuals.clickgui.setting.BooleanSetting;
import dev.linkenvisuals.clickgui.setting.KeySetting;
import dev.linkenvisuals.clickgui.setting.Setting;
import dev.linkenvisuals.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Arm;
import org.lwjgl.glfw.GLFW;

import java.util.List;

/**
 * HandSwap (Phase 5.7): toggles main hand (left/right) on key press.
 * Donor's {@code HandSwap} (55 lines) ported.
 */
public final class HandSwapModule extends Module {
    public final KeySetting key;
    public final BooleanSetting notify;

    public HandSwapModule() {
        this(new KeySetting("Key", GLFW.GLFW_KEY_UNKNOWN),
             new BooleanSetting("Notify", true));
    }

    private HandSwapModule(KeySetting key, BooleanSetting notify) {
        super("handswap", "Hand Swap", "toggle main hand",
            Category.PLAYER,
            List.of(),
            List.<Setting>of(key, notify));
        this.key = key;
        this.notify = notify;
    }

    /** Called on key press to swap the hand. */
    public static void onKeyPress() {
        HandSwapModule module = dev.linkenvisuals.module.Modules.handSwap();
        if (module == null || !module.enabled()) {
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.options == null) {
            return;
        }
        Arm current = client.options.getMainArm().getValue();
        Arm next = current == Arm.LEFT ? Arm.RIGHT : Arm.LEFT;
        client.options.getMainArm().setValue(next);
        if (module.notify.value()) {
            // Notification via chat (simplified).
            client.player.sendMessage(
                net.minecraft.text.Text.literal("Main hand: " + next.name()), true);
        }
    }
}
