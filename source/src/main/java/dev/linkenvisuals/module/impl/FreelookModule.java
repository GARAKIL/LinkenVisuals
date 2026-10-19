package dev.linkenvisuals.module.impl;

import dev.linkenvisuals.clickgui.Category;
import dev.linkenvisuals.clickgui.setting.KeySetting;
import dev.linkenvisuals.clickgui.setting.ModeSetting;
import dev.linkenvisuals.clickgui.setting.Setting;
import dev.linkenvisuals.module.Module;
import org.lwjgl.glfw.GLFW;

import java.util.List;

/**
 * Freelook (Phase 5.7): free camera rotation on key hold.
 * Donor's {@code Freelook} (237 lines) simplified: state only.
 * Camera/mouse mixins are a follow-up.
 */
public final class FreelookModule extends Module {
    public final KeySetting key;
    public final ModeSetting activationMode;

    private boolean active;
    private float cameraYaw;
    private float cameraPitch;

    public FreelookModule() {
        this(new KeySetting("Key", GLFW.GLFW_KEY_F5),
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
        active = false;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public float getCameraYaw() {
        return cameraYaw;
    }

    public void setCameraYaw(float yaw) {
        this.cameraYaw = yaw;
    }

    public float getCameraPitch() {
        return cameraPitch;
    }

    public void setCameraPitch(float pitch) {
        this.cameraPitch = pitch;
    }
}
