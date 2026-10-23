package dev.linkenvisuals.hands;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

/**
 * Lightweight first-person hand motion (Punchy-lite subset for 1.21.11).
 *
 * <p>The donor bundles a very large Punchy animation engine. This port keeps
 * the most visible part without its 180-class model/animation stack: a small
 * walk bob, stronger sprint sway, a lower hand while swimming, a small fall
 * lift and a subtle attack recoil. It is applied on top of HandPose and
 * ViewModel, and only while {@code Better Animations -> Hand Motion} is on.
 */
public final class HandMotion {
    private HandMotion() {
    }

    /** Pure part used by tests and the renderer. */
    public record Offset(float x, float y, float z, float pitch, float roll) {
    }

    public static Offset offset(float age, float speed, boolean sprinting, boolean inWater,
                                double fallDistance, float swing, boolean rightArm) {
        float phase = age * 0.15f;
        float walk = MathHelper.clamp(speed / 0.12f, 0.0f, 1.0f);
        float x = MathHelper.cos(phase) * 0.008f * walk;
        float y = MathHelper.sin(phase * 2.0f) * 0.006f * walk;
        float z = MathHelper.sin(phase * 2.0f + 0.5f) * 0.003f * walk;
        if (sprinting) {
            x *= 1.65f;
            y *= 1.35f;
        }
        if (inWater) {
            y -= 0.025f;
            z += 0.02f;
        }
        if (fallDistance > 0.1) {
            y += (float) Math.min(0.04, fallDistance * 0.008);
        }

        float arc = swing <= 0.0f ? 0.0f
            : MathHelper.sin(MathHelper.sqrt(MathHelper.clamp(swing, 0.0f, 1.0f)) * (float) Math.PI);
        float pitch = arc * 4.0f;
        float roll = arc * (rightArm ? 3.0f : -3.0f);
        return new Offset(x, y, z, pitch, roll);
    }

    public static void apply(MatrixStack matrices, AbstractClientPlayerEntity player,
                             Hand hand, float tickProgress) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player != player) {
            return;
        }
        boolean rightArm = player.getMainArm() == Arm.RIGHT;
        float speed = (float) Math.sqrt(player.getVelocity().horizontalLengthSquared());
        boolean inWater = player.isSwimming() || player.isTouchingWater();
        float swing = hand == Hand.MAIN_HAND ? player.handSwingProgress : 0.0f;
        Offset offset = offset(player.age + tickProgress, speed, player.isSprinting(), inWater,
            player.fallDistance, swing, rightArm);

        if (Math.abs(offset.pitch()) > 0.001f) {
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(offset.pitch()));
        }
        if (Math.abs(offset.roll()) > 0.001f) {
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(offset.roll()));
        }
        matrices.translate(offset.x(), offset.y(), offset.z());
    }
}
