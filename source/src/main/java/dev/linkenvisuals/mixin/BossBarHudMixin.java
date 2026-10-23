package dev.linkenvisuals.mixin;

import dev.linkenvisuals.module.Modules;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.BossBarHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BossBarHud.class)
public abstract class BossBarHudMixin {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void linkenvisuals$noBossBar(DrawContext context, CallbackInfo ci) {
        var noRender = Modules.noRender();
        if (noRender != null && noRender.enabled() && noRender.bossBar.value()) {
            ci.cancel();
        }
    }
}
