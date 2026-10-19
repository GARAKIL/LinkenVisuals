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
 * SwingAnimation (Phase 5.7): custom first-person swing animations.
 *
 * <p>The transformation is applied as a <b>delta</b> on top of the vanilla
 * {@code HeldItemRenderer.swingArm} transform (see {@code HeldItemRendererMixin}),
 * so first-person item placement and the equip offset stay vanilla-correct even
 * when a mode adds large rotations. The seven modes mirror the donor module.
 */
public final class SwingAnimationModule extends Module {
    public static final List<String> MODES =
        List.of("Classic", "Riposte", "Lunge", "Cleave", "Whirl", "Cascade", "Orbit");

    public final ModeSetting mode;
    public final SliderSetting power;

    public SwingAnimationModule() {
        this(new ModeSetting("Mode", MODES, 0),
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

    /** Vanilla swing curve: 0 at the start and end, 1 in the middle. */
    public static float curve(float swingProgress) {
        return MathHelper.sin(MathHelper.sqrt(MathHelper.clamp(swingProgress, 0.0f, 1.0f)) * (float) Math.PI);
    }

    /** Pure rotation helper used by tests: X angle in degrees for a mode. */
    public static float xAngle(String mode, float swingProgress, float power) {
        float anim = curve(swingProgress);
        float boost = power / 5.0f;
        return switch (mode) {
            case "Riposte" -> anim * -85.0f * boost;
            case "Lunge" -> 50.0f;
            case "Cleave" -> 50.0f;
            case "Whirl" -> -110.0f * MathHelper.sin(swingProgress * (float) Math.PI) * boost;
            case "Cascade" -> -(power * 10.0f) * anim;
            case "Orbit" -> -140.0f * anim * boost;
            default -> anim * -60.0f * boost;
        };
    }

    /**
     * Applies the configured swing delta. Must be called after the vanilla
     * {@code swingArm} transform so the equip offset is already in the stack.
     */
    public void applyDelta(MatrixStack matrices, float swingProgress, Arm arm) {
        float p = MathHelper.clamp(swingProgress, 0.0f, 1.0f);
        float powerValue = power.value();
        float anim = curve(p);
        int direction = arm == Arm.RIGHT ? 1 : -1;
        String selected = mode.value();

        switch (selected) {
            case "Riposte" -> {
                if (p <= 0.0f) {
                    return;
                }
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(direction * 45.0f));
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(xAngle(selected, p, powerValue)));
                matrices.translate(direction * -0.1f, 0.28f, 0.2f);
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-85.0f));
            }
            case "Lunge" -> {
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(xAngle(selected, p, powerValue)));
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(direction * -60.0f));
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(
                    direction * (110.0f + 20.0f * anim * powerValue / 5.0f)));
            }
            case "Cleave" -> {
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(xAngle(selected, p, powerValue)));
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(
                    direction * (-30.0f * (1.0f - anim) - 30.0f)));
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(direction * 110.0f));
            }
            case "Whirl" -> {
                float swingCurve = MathHelper.sin(p * (float) Math.PI);
                float boost = powerValue / 5.0f;
                matrices.translate(0.0f, -0.28f + swingCurve * -0.18f, -0.48f + swingCurve * -0.12f);
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(direction * 95.0f * swingCurve * boost));
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-110.0f * swingCurve * boost));
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(direction * 35.0f * swingCurve));
                matrices.scale(0.9f + 0.08f * swingCurve, 0.92f + 0.05f * swingCurve, 0.9f);
            }
            case "Cascade" -> {
                matrices.scale(0.5f, 0.5f, 0.5f);
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(direction * 15.0f * anim));
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(xAngle(selected, p, powerValue)));
            }
            case "Orbit" -> {
                matrices.scale(0.6f, 0.5f, 0.4f);
                matrices.translate(direction * 0.2f * anim, 0.0f, -0.5f);
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(
                    direction * 90.0f * anim * powerValue / 5.0f));
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(xAngle(selected, p, powerValue)));
            }
            default -> {
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(xAngle(selected, p, powerValue)));
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(
                    direction * anim * -30.0f * powerValue / 5.0f));
            }
        }
    }
}
