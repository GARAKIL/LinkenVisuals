package dev.linkenvisuals.mixin;

import dev.linkenvisuals.cosmetic.character.RudyCharacter;
import dev.linkenvisuals.module.Modules;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.state.BipedEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Rudy (Phase 5.6): armour boxes do not fit the character mesh, so they are skipped for Rudy
 * players (the donor hid them the same way).
 */
@Mixin(ArmorFeatureRenderer.class)
public abstract class ArmorFeatureRendererMixin {
    @Inject(method = "render(Lnet/minecraft/client/util/math/MatrixStack;"
        + "Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;I"
        + "Lnet/minecraft/client/render/entity/state/BipedEntityRenderState;FF)V",
        at = @At("HEAD"), cancellable = true)
    private void linkenvisuals$hideForRudy(MatrixStack matrices, OrderedRenderCommandQueue queue, int light,
                                           BipedEntityRenderState state, float limbAngle, float limbDistance,
                                           CallbackInfo ci) {
        var noRender = Modules.noRender();
        if (noRender != null && noRender.enabled() && noRender.armor.value()) {
            ci.cancel();
            return;
        }
        if (RudyCharacter.active(state)) {
            ci.cancel();
        }
    }
}
