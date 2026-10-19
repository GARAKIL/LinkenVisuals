package dev.linkenvisuals.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import dev.linkenvisuals.module.Modules;
import dev.linkenvisuals.module.impl.FreelookModule;
import net.minecraft.client.Mouse;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Freelook mouse handling: vanilla applies the mouse delta directly to the
 * player in {@code updateMouse}. We snapshot the player rotation on entry and
 * restore it on exit, moving the delta into the freelook camera instead.
 */
@Mixin(Mouse.class)
public abstract class MouseMixin {
    @Shadow
    private MinecraftClient client;

    @Inject(method = "updateMouse", at = @At("HEAD"))
    private void linkenvisuals$captureFreelook(double delta, CallbackInfo ci) {
        FreelookModule module = Modules.freelook();
        ClientPlayerEntity player = client.player;
        if (module == null || player == null) {
            return;
        }
        module.captureMouseUpdate(player);
    }

    @Inject(method = "updateMouse", at = @At("RETURN"))
    private void linkenvisuals$restoreFreelook(double delta, CallbackInfo ci) {
        FreelookModule module = Modules.freelook();
        ClientPlayerEntity player = client.player;
        if (module == null || player == null) {
            return;
        }
        module.releaseMouseUpdate(player);
    }
}
