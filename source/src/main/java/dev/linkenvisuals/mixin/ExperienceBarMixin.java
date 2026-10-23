package dev.linkenvisuals.mixin;

import dev.linkenvisuals.hud.CustomVitalsHud;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.bar.ExperienceBar;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Hides the vanilla XP bar/level while Custom Vitals owns the hotbar strip. */
@Mixin(ExperienceBar.class)
public abstract class ExperienceBarMixin {
    @Inject(method = "renderBar", at = @At("HEAD"), cancellable = true)
    private void linkenvisuals$hideBar(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (CustomVitalsHud.active()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderAddons", at = @At("HEAD"), cancellable = true)
    private void linkenvisuals$hideLevel(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (CustomVitalsHud.active()) {
            ci.cancel();
        }
    }
}
