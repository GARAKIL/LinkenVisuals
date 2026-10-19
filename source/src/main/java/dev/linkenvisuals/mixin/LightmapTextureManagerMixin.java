package dev.linkenvisuals.mixin;

import dev.linkenvisuals.module.Modules;
import dev.linkenvisuals.module.impl.FullbrightModule;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.render.LightmapTextureManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fullbright (Phase 5.7): clears the lightmap when the module is enabled,
 * giving maximum brightness. Ported from the donor's
 * {@code LightmapTextureManagerMixin}.
 */
@Mixin(LightmapTextureManager.class)
public abstract class LightmapTextureManagerMixin {
    @Final
    @Shadow
    private SimpleFramebuffer lightmapFramebuffer;

    @Inject(method = "update", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/gl/SimpleFramebuffer;endWrite()V",
        shift = At.Shift.BEFORE))
    private void linkenvisuals$onLightmapUpdate(float delta, CallbackInfo ci) {
        FullbrightModule module = Modules.fullbright();
        if (module != null && module.enabled()) {
            lightmapFramebuffer.clear();
        }
    }
}
