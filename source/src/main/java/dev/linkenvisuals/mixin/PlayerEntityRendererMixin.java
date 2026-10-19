package dev.linkenvisuals.mixin;

import dev.linkenvisuals.clickgui.ModuleRegistry;
import dev.linkenvisuals.cosmetic.character.RudyCharacter;
import dev.linkenvisuals.render.state.RudyRenderState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.entity.PlayerLikeEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Rudy (Phase 5.6): flags the local player's render state when the Cosmetics module has the
 * "Rudy" character selected. The donor resolved characters per synced user; without the
 * forbidden user sync this stays local-player-only (others see the vanilla model).
 */
@Mixin(PlayerEntityRenderer.class)
public abstract class PlayerEntityRendererMixin {
    @Inject(method = "updateRenderState(Lnet/minecraft/entity/PlayerLikeEntity;"
        + "Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;F)V",
        at = @At("TAIL"))
    private void linkenvisuals$rudyCharacter(PlayerLikeEntity entity, PlayerEntityRenderState state,
                                             float tickDelta, CallbackInfo ci) {
        boolean rudy = entity == MinecraftClient.getInstance().player
                && ModuleRegistry.cosmetics().enabled()
                && "Rudy".equals(ModuleRegistry.cosmeticSlot("character"))
                && RudyCharacter.available();
        ((RudyRenderState) state).linkenvisuals$setRudy(rudy);
    }
}
