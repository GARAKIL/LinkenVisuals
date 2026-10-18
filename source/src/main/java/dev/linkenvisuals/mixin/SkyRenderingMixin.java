package dev.linkenvisuals.mixin;

import dev.linkenvisuals.clickgui.ModuleRegistry;
import dev.linkenvisuals.render.SkyRenderer;
import net.minecraft.client.render.SkyRendering;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SkyRendering.class)
public abstract class SkyRenderingMixin {
    private static boolean linkenvisuals$active() {
        return ModuleRegistry.shaderSky().enabled();
    }

    @Inject(method = "renderTopSky", at = @At("TAIL"))
    private void linkenvisuals$paintSky(int color, CallbackInfo ci) {
        SkyRenderer.render();
    }

    @Inject(method = "renderGlowingSky", at = @At("HEAD"), cancellable = true)
    private void linkenvisuals$glowingSky(MatrixStack matrices, float tickProgress, int color, CallbackInfo ci) {
        if (linkenvisuals$active()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderSkyDark", at = @At("HEAD"), cancellable = true)
    private void linkenvisuals$skyDark(CallbackInfo ci) {
        if (linkenvisuals$active()) {
            ci.cancel();
        }
    }
}
