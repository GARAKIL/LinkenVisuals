package dev.linkenvisuals.mixin;

import dev.linkenvisuals.module.Modules;
import net.minecraft.client.gui.hud.InGameOverlayRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameOverlayRenderer.class)
public abstract class InGameOverlayRendererMixin {
    @Inject(method = "renderFireOverlay", at = @At("HEAD"), cancellable = true)
    private static void linkenvisuals$noFireOverlay(CallbackInfo ci) {
        var noRender = Modules.noRender();
        if (noRender != null && noRender.enabled() && noRender.fire.value()) {
            ci.cancel();
        }
    }
}
