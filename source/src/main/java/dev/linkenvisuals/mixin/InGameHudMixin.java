package dev.linkenvisuals.mixin;

import dev.linkenvisuals.hud.HudManager;
import dev.linkenvisuals.module.Modules;
import dev.linkenvisuals.module.impl.CrosshairModule;
import dev.linkenvisuals.module.impl.StreamerModeModule;
import dev.linkenvisuals.render.CrosshairRenderer;
import dev.linkenvisuals.ui.editor.MainMenuConfig;
import dev.linkenvisuals.util.text.StreamerTextSanitizer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
    /** Стример-режим: actionbar-строки (часто донат-уведомления серверов) чистятся как чат. */
    @ModifyVariable(method = "setOverlayMessage", at = @At("HEAD"), argsOnly = true)
    private Text linkenvisuals$sanitizeOverlayMessage(Text message) {
        return StreamerTextSanitizer.sanitize(message, StreamerModeModule.rules());
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void linkenvisuals$render(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        HudManager.render(context);
    }

    @Inject(method = "renderHotbar", at = @At("HEAD"), cancellable = true)
    private void linkenvisuals$cancelVanillaHotbar(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (MainMenuConfig.get().customHotbar) {
            ci.cancel();
        }
    }

    @Inject(method = "renderStatusEffectOverlay", at = @At("HEAD"), cancellable = true)
    private void linkenvisuals$cancelVanillaStatusEffects(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (MainMenuConfig.get().customStatusEffects) {
            ci.cancel();
        }
    }

    @Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
    private void linkenvisuals$renderCustomCrosshair(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        CrosshairModule module = Modules.crosshair();
        if (module == null || !module.enabled()) {
            return;
        }
        ci.cancel();
        CrosshairRenderer.render(context, module);
    }
}
