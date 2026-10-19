package dev.linkenvisuals.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.linkenvisuals.anim.Animations;
import dev.linkenvisuals.module.Modules;
import dev.linkenvisuals.module.impl.FreelookModule;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(Camera.class)
public abstract class CameraMixin {
    // Freelook: both yaw/pitch reads in update() (minecart and regular branches,
    // verified with javap: getYaw@139/174, getPitch@145/180) go through Entity.getYaw(F)
    // and Entity.getPitch(F), so a ModifyExpressionValue pair covers both branches
    // without ordinals.
    @ModifyExpressionValue(method = "update",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;getYaw(F)F"))
    private float linkenvisuals$freelookYaw(float original,
                                            @com.llamalad7.mixinextras.sugar.Local(argsOnly = true) Entity entity) {
        FreelookModule module = Modules.freelook();
        return module != null && module.shouldOverrideCamera(entity) ? module.getCameraYaw() : original;
    }

    @ModifyExpressionValue(method = "update",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;getPitch(F)F"))
    private float linkenvisuals$freelookPitch(float original,
                                              @com.llamalad7.mixinextras.sugar.Local(argsOnly = true) Entity entity) {
        FreelookModule module = Modules.freelook();
        return module != null && module.shouldOverrideCamera(entity)
            ? module.getCameraPitch() : original;
    }

    // Vanilla method arguments cannot be matched by name in production (Mojang jars ship without
    // local variable tables), so the argument ordinal is the only stable anchor here. Ordinals 0/1
    // are the two boolean parameters of update(BlockView, Entity, boolean thirdPerson,
    // boolean inverseView, float) — verified via javap on the yarn build.4 jar (iload_3 / iload_4).
    @ModifyVariable(method = "update", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private boolean linkenvisuals$holdThirdPerson(boolean thirdPerson) {
        float progress = Animations.perspective();
        if (progress < 0.0f) {
            return thirdPerson;
        }
        return thirdPerson || progress > 0.01f;
    }

    @ModifyVariable(method = "update", at = @At("HEAD"), argsOnly = true, ordinal = 1)
    private boolean linkenvisuals$holdFrontView(boolean inverseView) {
        float flip = Animations.flip();
        if (flip < 0.0f) {
            return inverseView;
        }
        return inverseView || flip > 0.005f;
    }

    @ModifyReturnValue(method = "clipToSpace", at = @At("RETURN"))
    private float linkenvisuals$easeDistance(float original) {
        float progress = Animations.perspective();
        return progress < 0.0f ? original : original * progress;
    }

    // Inverse (front) view branch of the third person camera: the rotation is computed from the
    // Camera.yaw/pitch fields and applied right before the camera-distance block. Anchored with
    // named bytecode landmarks instead of an ordinal:
    //   from: the only yaw field read in update()
    //   to:   the clipToSpace() call of the camera-distance block
    // Verified via javap on the yarn build.4 jar: yaw@258, setRotation@269, clipToSpace@379.
    // NOTE: the to-landmark MUST remap into intermediary in the built jar. A former
    // LivingEntity.getScale() landmark shipped unremapped (yarn name kept) and crashed the game
    // on load ("Invalid name: getScale:") - every new @At target must be re-verified with
    // javap on the built jar (targets must be class_*/method_*/field_* names).
    @ModifyArgs(method = "update",
        slice = @Slice(
            from = @At(value = "FIELD", target = "Lnet/minecraft/client/render/Camera;yaw:F"),
            to = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;clipToSpace(F)F")),
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;setRotation(FF)V"))
    private void linkenvisuals$easeFlip(Args args) {
        float flip = Animations.flip();
        if (flip < 0.0f) {
            return;
        }
        float turned = args.<Float>get(0);
        float inverted = args.<Float>get(1);

        args.set(0, turned - 180.0f + 180.0f * flip);
        args.set(1, -inverted * (1.0f - 2.0f * flip));
    }
}
