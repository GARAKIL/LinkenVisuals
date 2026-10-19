package dev.linkenvisuals.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.linkenvisuals.anim.Animations;
import dev.linkenvisuals.module.impl.StreamerModeModule;
import dev.linkenvisuals.util.text.StreamerTextSanitizer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.text.Text;
import org.joml.Matrix3x2fStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerListHud.class)
public abstract class PlayerListHudMixin {
    /** Стример-режим: ник записи таба (с командными префиксами/суффиксами) чистится на возврате. */
    @ModifyReturnValue(method = "getPlayerName(Lnet/minecraft/client/network/PlayerListEntry;)Lnet/minecraft/text/Text;",
        at = @At("RETURN"))
    private Text linkenvisuals$sanitizePlayerName(Text original) {
        return StreamerTextSanitizer.sanitize(original, StreamerModeModule.rules());
    }

    @ModifyVariable(method = "setHeader", at = @At("HEAD"), argsOnly = true)
    private Text linkenvisuals$sanitizeHeader(Text header) {
        return StreamerTextSanitizer.sanitize(header, StreamerModeModule.rules());
    }

    @ModifyVariable(method = "setFooter", at = @At("HEAD"), argsOnly = true)
    private Text linkenvisuals$sanitizeFooter(Text footer) {
        return StreamerTextSanitizer.sanitize(footer, StreamerModeModule.rules());
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void linkenvisuals$pushTab(DrawContext context, int scaledWindowWidth, Scoreboard scoreboard,
                                        ScoreboardObjective objective, CallbackInfo ci) {
        Matrix3x2fStack matrices = context.getMatrices();
        matrices.pushMatrix();
        float open = Animations.ease(Animations.tab());
        if (open < 0.999f) {
            float scale = 0.88f + 0.12f * open;
            matrices.translate(scaledWindowWidth * 0.5f, 0.0f);
            matrices.scale(scale, scale);
            matrices.translate(-scaledWindowWidth * 0.5f, -(1.0f - open) * 34.0f);
        }
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void linkenvisuals$popTab(DrawContext context, int scaledWindowWidth, Scoreboard scoreboard,
                                       ScoreboardObjective objective, CallbackInfo ci) {
        context.getMatrices().popMatrix();
    }
}
