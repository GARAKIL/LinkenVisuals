package dev.linkenvisuals.mixin;

import dev.linkenvisuals.module.Modules;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.SubmittableBatch;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ParticleManager.class)
public abstract class ParticleManagerMixin {
    /**
     * 1.21.11 no longer has {@code renderParticles}; the visible particle pass
     * is submitted through {@code addToBatch}. Cancelling it hides every
     * vanilla particle sheet while NoRender particles is enabled.
     */
    @Inject(method = "addToBatch", at = @At("HEAD"), cancellable = true)
    private void linkenvisuals$noParticles(SubmittableBatch batch, Frustum frustum,
                                           Camera camera, float tickProgress, CallbackInfo ci) {
        var noRender = Modules.noRender();
        if (noRender != null && noRender.enabled() && noRender.particles.value()) {
            ci.cancel();
        }
    }
}
