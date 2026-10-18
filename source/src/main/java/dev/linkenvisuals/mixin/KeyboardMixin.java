package dev.linkenvisuals.mixin;

import dev.linkenvisuals.clickgui.ModuleRegistry;
import dev.linkenvisuals.macro.MacroManager;
import net.minecraft.client.Keyboard;
import net.minecraft.client.input.KeyInput;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public abstract class KeyboardMixin {
    @Inject(method = "onKey", at = @At("HEAD"))
    private void linkenvisuals$onMacroKey(long window, int action, KeyInput input, CallbackInfo ci) {
        if (action == GLFW.GLFW_PRESS) {
            MacroManager.onKeyPressed(input.key());
            ModuleRegistry.onKeyPressed(input.key());
        }
    }
}
