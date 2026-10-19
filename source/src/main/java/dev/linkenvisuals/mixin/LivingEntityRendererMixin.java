package dev.linkenvisuals.mixin;

import dev.linkenvisuals.module.Modules;
import dev.linkenvisuals.module.impl.HitColorModule;
import dev.linkenvisuals.render.state.HitColorRenderState;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * HitColor: advances the per-render-state tint strength and feeds the tint into
 * vanilla's {@code getMixColor} return value. Vanilla already mixes that value
 * into the model color ({@code ColorHelper.mix(base, mixColor)}), so no
 * {@code submitModel} redirection is needed for the body.
 */
@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {
    @Inject(method = "updateRenderState(Lnet/minecraft/entity/LivingEntity;"
        + "Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;F)V",
        at = @At("TAIL"))
    private void linkenvisuals$advanceHitColor(LivingEntity entity, LivingEntityRenderState state,
                                               float tickDelta, CallbackInfo ci) {
        HitColorRenderState hitState = (HitColorRenderState) state;
        hitState.linkenvisuals$setHitColorPlayer(entity instanceof PlayerEntity);

        HitColorModule module = Modules.hitColor();
        if (module == null || !module.enabled()) {
            hitState.linkenvisuals$setHitColorStrength(0.0f);
            return;
        }
        boolean hitByMe = HitColorModule.wasHitByMe(entity.getId());
        hitState.linkenvisuals$setHitColorStrength(
            HitColorModule.advanceStrength(entity.getId(), hitByMe));
    }

    @Inject(method = "getMixColor(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;)I",
        at = @At("RETURN"), cancellable = true)
    private void linkenvisuals$hitColorTint(LivingEntityRenderState state,
                                            CallbackInfoReturnable<Integer> cir) {
        HitColorModule module = Modules.hitColor();
        if (module == null || !module.enabled()) {
            return;
        }
        HitColorRenderState hitState = (HitColorRenderState) state;
        if (!hitState.linkenvisuals$isHitColorPlayer()) {
            return;
        }
        float strength = hitState.linkenvisuals$getHitColorStrength();
        if (strength <= 0.001f) {
            return;
        }
        int tinted = HitColorModule.tint(cir.getReturnValue(), module.color.rgb(),
            strength, module.opacity.value() / 255.0f);
        cir.setReturnValue(tinted);
    }
}
