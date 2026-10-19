package dev.linkenvisuals.mixin;

import dev.linkenvisuals.cosmetic.character.RudyCharacter;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Arm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Rudy (Phase 5.6): mirrors the render state flag onto the shared model (needed by
 * {@code setArmAngle}, which runs while the model is being posed) and applies Rudy's attachment
 * pivots after the vanilla pose.
 */
@Mixin(PlayerEntityModel.class)
public abstract class PlayerEntityModelMixin {
    @Unique
    private boolean linkenvisuals$rudy;

    @Inject(method = "setAngles(Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;)V",
        at = @At("HEAD"))
    private void linkenvisuals$rudyFlag(PlayerEntityRenderState state, CallbackInfo ci) {
        linkenvisuals$rudy = RudyCharacter.active(state);
    }

    @Inject(method = "setAngles(Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;)V",
        at = @At("TAIL"))
    private void linkenvisuals$rudyPose(PlayerEntityRenderState state, CallbackInfo ci) {
        if (linkenvisuals$rudy) {
            RudyCharacter.pose((PlayerEntityModel) (Object) this, state.sneaking);
        }
    }

    @Inject(method = "setArmAngle(Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;"
        + "Lnet/minecraft/util/Arm;Lnet/minecraft/client/util/math/MatrixStack;)V",
        at = @At("TAIL"))
    private void linkenvisuals$rudyGrip(PlayerEntityRenderState state, Arm arm, MatrixStack matrices, CallbackInfo ci) {
        if (linkenvisuals$rudy) {
            RudyCharacter.armGrip(arm, matrices);
        }
    }
}
