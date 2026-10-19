package dev.linkenvisuals.mixin;

import dev.linkenvisuals.module.impl.HitSoundModule;
import dev.linkenvisuals.player.FakePlayer;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientAttackMixin {
    @Inject(method = "doAttack", at = @At("HEAD"), cancellable = true)
    private void linkenvisuals$hitDummy(CallbackInfoReturnable<Boolean> info) {
        MinecraftClient client = (MinecraftClient) (Object) this;
        if (FakePlayer.tryHit(client)) {
            // Real entity hits play the sound via ClientPlayerInteractionManagerMixin;
            // this cancelled path never reaches it, so trigger it here too.
            HitSoundModule.tryPlay(client, false);
            info.setReturnValue(true);
        }
    }
}
