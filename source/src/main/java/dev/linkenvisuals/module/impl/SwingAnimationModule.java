package dev.linkenvisuals.module.impl;

import dev.linkenvisuals.clickgui.Category;
import dev.linkenvisuals.clickgui.setting.ModeSetting;
import dev.linkenvisuals.clickgui.setting.Setting;
import dev.linkenvisuals.clickgui.setting.SliderSetting;
import dev.linkenvisuals.module.Module;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Arm;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

import java.util.List;

/**
 * SwingAnimation (Phase 5.7): custom hand swing animations.
 * Donor's {@code SwingAnimation} (140 lines) simplified: CLASSIC mode only.
 */
public final class SwingAnimationModule extends Module {
    public final ModeSetting mode;
    public final SliderSetting power;

    public SwingAnimationModule() {
        this(new ModeSetting("Mode", List.of("Classic", "Riposte"), 0),
             new SliderSetting("Power", 1.0f, 10.0f, 0.05f, 5.0f, ""));
    }

    private SwingAnimationModule(ModeSetting mode, SliderSetting power) {
        super("swingan", "Swing Animation", "custom swing",
            Category.VISUAL,
            List.of(),
            List.<Setting>of(mode, power));
        this.mode = mode;
        this.power = power;
    }

    public static boolean isEnabled() {
        SwingAnimationModule m = dev.linkenvisuals.module.Modules.swingAnimation();
        return m != null && m.enabled();
    }

    /** Applies the swing animation transformation. */
    public static void apply(MatrixStack matrices, float swingProgress, Arm arm) {
        SwingAnimationModule m = dev.linkenvisuals.module.Modules.swingAnimation();
        if (m == null || !m.enabled()) {
            return;
        }
        float p = m.power.value();
        float anim = MathHelper.sin(MathHelper.sqrt(swingProgress) * (float) Math.PI);
        int direction = arm == Arm.RIGHT ? 1 : -1;

        if ("Riposte".equals(m.mode.value()) && swingProgress > 0) {
            matrices.translate(direction * 0.56f, -0.5f, -0.7f);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(direction * 45));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(anim * -85.0f * p / 5.0f));
        } else {
            // Classic.
            matrices.translate(direction * 0.56f, -0.52f, -0.72f);
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(anim * -60.0f * p / 5.0f));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(direction * anim * -30.0f * p / 5.0f));
        }
    }
}
