package dev.linkenvisuals.mixin;

import dev.linkenvisuals.module.Modules;
import net.minecraft.client.particle.ParticleManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ParticleManager.class)
public abstract class ParticleManagerMixin {
    @Inject(method = "renderParticles", at = @At("HEAD"), cancellable = true)
    private void linkenvisuals$noParticles(CallbackInfo ci) {
        var noRender = Modules.noRender();
        if (noRender != null && noRender.enabled() && noRender.particles.value()) {
            ci.cancel();
        }
    }
}
