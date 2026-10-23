package dev.linkenvisuals.mixin;

import dev.linkenvisuals.module.impl.OptimizationModule;
import dev.linkenvisuals.util.optimization.OcclusionCuller;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public abstract class OptimizationWorldRendererMixin {
    /** Resets the per-frame ray budget and drops stale world entries. */
    @Inject(method = "render", at = @At("HEAD"))
    private void linkenvisuals$beginCullFrame(CallbackInfo ci) {
        OcclusionCuller.beginFrame();
    }

    @Inject(method = "renderClouds", at = @At("HEAD"), cancellable = true)
    private void linkenvisuals$cullClouds(CallbackInfo ci) {
        if (OptimizationModule.cullClouds()) {
            ci.cancel();
        }
    }
}
