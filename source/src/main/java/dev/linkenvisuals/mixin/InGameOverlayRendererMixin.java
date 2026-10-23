package dev.linkenvisuals.mixin;

import dev.linkenvisuals.module.Modules;
import net.minecraft.client.gui.hud.InGameOverlayRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameOverlayRenderer.class)
public abstract class InGameOverlayRendererMixin {
    /**
     * Only the fire texture is cancelled; underwater/in-wall overlays stay
     * vanilla.
     */
    @Inject(method = "renderFireOverlay", at = @At("HEAD"), cancellable = true)
    private static void linkenvisuals$noFireOverlay(MatrixStack matrices,
                                                     VertexConsumerProvider consumers,
                                                     Sprite sprite, CallbackInfo ci) {
        var noRender = Modules.noRender();
        if (noRender != null && noRender.enabled() && noRender.fire.value()) {
            ci.cancel();
        }
    }
}
