package dev.linkenvisuals.module.impl;

import dev.linkenvisuals.clickgui.Category;
import dev.linkenvisuals.clickgui.setting.KeySetting;
import dev.linkenvisuals.clickgui.setting.ModeSetting;
import dev.linkenvisuals.clickgui.setting.Setting;
import dev.linkenvisuals.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.Perspective;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

import java.util.List;

/**
 * Freelook (Phase 5.7): rotates the third-person camera without turning the
 * player. Ported from the donor's {@code Freelook} (237 lines).
 *
 * <p>The key is polled once per client tick (same pattern as {@code HandSwap}
 * and {@code Zoom}); the camera override happens in {@code CameraMixin} and the
 * mouse capture/restore in {@code MouseMixin}. The donor's vanilla-perspective
 * key suppression is not ported: the default bind is Left Alt, so no vanilla
 * key conflicts.
 */
public final class FreelookModule extends Module {
    public final KeySetting key;
    public final ModeSetting activationMode;

    private boolean active;
    private boolean keyWasDown;
    private float cameraYaw;
    private float cameraPitch;
    private Perspective previousPerspective = Perspective.FIRST_PERSON;

    private boolean mouseCaptured;
    private float capturedYaw;
    private float capturedPitch;
    private float capturedLastYaw;
    private float capturedLastPitch;
    private float capturedBodyYaw;
    private float capturedHeadYaw;

    public FreelookModule() {
        this(new KeySetting("Key", GLFW.GLFW_KEY_LEFT_ALT),
             new ModeSetting("Mode", List.of("Hold", "Toggle"), 0));
    }

    private FreelookModule(KeySetting key, ModeSetting activationMode) {
        super("freelook", "Freelook", "free camera look",
            Category.VISUAL,
            List.of(),
            List.<Setting>of(key, activationMode));
        this.key = key;
        this.activationMode = activationMode;
    }

    @Override
    protected void onDisable() {
        stop();
    }

    /** Polls the bind and drives activation; called from the client tick. */
    public void tick(MinecraftClient client) {
        if (!enabled() || client.player == null || client.currentScreen != null) {
            stop();
            keyWasDown = false;
            return;
        }
        int keyCode = key.value();
        if (keyCode == GLFW.GLFW_KEY_UNKNOWN) {
            return;
        }
        boolean down = GLFW.glfwGetKey(client.getWindow().getHandle(), keyCode) == GLFW.GLFW_PRESS;
        boolean pressed = down && !keyWasDown;
        keyWasDown = down;

        if ("Toggle".equals(activationMode.value())) {
            if (pressed) {
                if (active) {
                    stop();
                } else {
                    start(client);
                }
            }
            return;
        }
        if (down && !active) {
            start(client);
        } else if (!down && active) {
            stop();
        }
    }

    private void start(MinecraftClient client) {
        if (active || client.player == null || client.options == null) {
            return;
        }
        active = true;
        previousPerspective = client.options.getPerspective();
        cameraYaw = client.player.getYaw();
        cameraPitch = client.player.getPitch();
        client.options.setPerspective(Perspective.THIRD_PERSON_BACK);
    }

    private void stop() {
        if (!active) {
            mouseCaptured = false;
            return;
        }
        active = false;
        mouseCaptured = false;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.options != null) {
            client.options.setPerspective(previousPerspective);
        }
        previousPerspective = Perspective.FIRST_PERSON;
    }

    public boolean isActive() {
        return enabled() && active;
    }

    /** True when the camera should use the freelook rotation for this entity. */
    public boolean shouldOverrideCamera(Entity entity) {
        MinecraftClient client = MinecraftClient.getInstance();
        return entity != null && entity == client.player && isActive();
    }

    public float getCameraYaw() {
        return cameraYaw;
    }

    public float getCameraPitch() {
        return cameraPitch;
    }

    /** Saves the player rotation before vanilla mouse look is applied. */
    public void captureMouseUpdate(ClientPlayerEntity player) {
        if (!isActive() || player == null) {
            return;
        }
        mouseCaptured = true;
        capturedYaw = player.getYaw();
        capturedPitch = player.getPitch();
        capturedLastYaw = player.lastYaw;
        capturedLastPitch = player.lastPitch;
        capturedBodyYaw = player.getBodyYaw();
        capturedHeadYaw = player.getHeadYaw();
    }

    /** Moves the mouse delta into the camera and restores the player rotation. */
    public void releaseMouseUpdate(ClientPlayerEntity player) {
        if (!mouseCaptured || player == null) {
            return;
        }
        mouseCaptured = false;
        if (active) {
            float yawDelta = MathHelper.wrapDegrees(player.getYaw() - capturedYaw);
            float pitchDelta = player.getPitch() - capturedPitch;
            cameraYaw = MathHelper.wrapDegrees(cameraYaw + yawDelta);
            cameraPitch = MathHelper.clamp(cameraPitch + pitchDelta, -90.0f, 90.0f);
        }
        player.setYaw(capturedYaw);
        player.setPitch(capturedPitch);
        player.lastYaw = capturedLastYaw;
        player.lastPitch = capturedLastPitch;
        player.setBodyYaw(capturedBodyYaw);
        player.setHeadYaw(capturedHeadYaw);
    }
}
