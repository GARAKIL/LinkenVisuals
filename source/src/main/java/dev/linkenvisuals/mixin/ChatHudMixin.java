package dev.linkenvisuals.mixin;

import dev.linkenvisuals.anim.Animations;
import dev.linkenvisuals.module.impl.StreamerModeModule;
import dev.linkenvisuals.util.text.StreamerTextSanitizer;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.text.Text;
import org.joml.Matrix3x2fStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatHud.class)
public abstract class ChatHudMixin {
    /** Стример-режим: настоящий ник/донат/монеты в чате подменяются настройками модуля. */
    @ModifyVariable(method = "addMessage(Lnet/minecraft/text/Text;)V", at = @At("HEAD"), argsOnly = true)
    private Text linkenvisuals$sanitizeChatMessage(Text message) {
        return StreamerTextSanitizer.sanitize(message, StreamerModeModule.rules());
    }

    @ModifyVariable(method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;Lnet/minecraft/client/gui/hud/MessageIndicator;)V",
        at = @At("HEAD"), argsOnly = true)
    private Text linkenvisuals$sanitizeSignedChatMessage(Text message) {
        return StreamerTextSanitizer.sanitize(message, StreamerModeModule.rules());
    }

    @Inject(method = "render(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/font/TextRenderer;IIIZZ)V",
        at = @At("HEAD"))
    private void linkenvisuals$pushChat(DrawContext context, TextRenderer textRenderer, int currentTick,
                                         int mouseX, int mouseY, boolean focused, boolean hasScreen,
                                         CallbackInfo ci) {
        Matrix3x2fStack matrices = context.getMatrices();
        matrices.pushMatrix();
        float open = Animations.ease(Animations.chat());
        if (open < 0.999f) {
            float bottom = context.getScaledWindowHeight();
            float scale = 0.93f + 0.07f * open;
            matrices.translate(0.0f, bottom);
            matrices.scale(scale, scale);
            matrices.translate(0.0f, -bottom + (1.0f - open) * 26.0f);
        }
    }

    @Inject(method = "render(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/font/TextRenderer;IIIZZ)V",
        at = @At("RETURN"))
    private void linkenvisuals$popChat(DrawContext context, TextRenderer textRenderer, int currentTick,
                                        int mouseX, int mouseY, boolean focused, boolean hasScreen,
                                        CallbackInfo ci) {
        context.getMatrices().popMatrix();
    }
}
