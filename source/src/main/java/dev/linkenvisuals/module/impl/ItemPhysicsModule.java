package dev.linkenvisuals.module.impl;

import dev.linkenvisuals.clickgui.Category;
import dev.linkenvisuals.clickgui.setting.Setting;
import dev.linkenvisuals.clickgui.setting.SliderSetting;
import dev.linkenvisuals.module.Module;

import java.util.List;

/**
 * ItemPhysics (Phase 5.7): item entity physics.
 * Donor's {@code ItemPhysics} (108 lines) — renderer mixin is a follow-up.
 * Module stub registered.
 */
public final class ItemPhysicsModule extends Module {
    public final SliderSetting rotationSpeed;
    public final SliderSetting scale;

    public ItemPhysicsModule() {
        this(new SliderSetting("Rotation Speed", 0.0f, 8.0f, 0.1f, 1.0f, ""),
             new SliderSetting("Scale", 0.5f, 1.5f, 0.05f, 1.0f, ""));
    }

    private ItemPhysicsModule(SliderSetting rotationSpeed, SliderSetting scale) {
        super("itemphysics", "Item Physics", "item entity physics",
            Category.VISUAL,
            List.of(),
            List.<Setting>of(rotationSpeed, scale));
        this.rotationSpeed = rotationSpeed;
        this.scale = scale;
    }
}
