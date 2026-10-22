package dev.linkenvisuals.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.linkenvisuals.anim.Animations;
import dev.linkenvisuals.clickgui.ModuleRegistry;
import dev.linkenvisuals.module.impl.StreamerModeModule;
import dev.linkenvisuals.render.RoundedRectPipeline;
import dev.linkenvisuals.ui.Theme;
import dev.linkenvisuals.util.ColorMath;
import dev.linkenvisuals.util.text.StreamerTextSanitizer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.text.Text;
import org.joml.Matrix3x2fStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerListHud.class)
public abstract class PlayerListHudMixin {
    @Unique
    private static float linkenvisuals$tabMinX;
    @Unique
    private static float linkenvisuals$tabMinY;
    @Unique
    private static float linkenvisuals$tabMaxX;
    @Unique
    private static float linkenvisuals$tabMaxY;
    @Unique
    private static boolean linkenvisuals$tabBounds;
    @Unique
    private static boolean linkenvisuals$tabCaptured;
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
        linkenvisuals$tabCaptured = false;
        if (ModuleRegistry.customTab() != null && ModuleRegistry.customTab().value()
            && linkenvisuals$tabBounds) {
            float pad = 2.0f;
            float x = linkenvisuals$tabMinX - pad;
            float y = linkenvisuals$tabMinY - pad;
            float width = linkenvisuals$tabMaxX - linkenvisuals$tabMinX + pad * 2.0f;
            float height = linkenvisuals$tabMaxY - linkenvisuals$tabMinY + pad * 2.0f;
            float alpha = ModuleRegistry.tabOpacity().value() / 100.0f;
            int color = ColorMath.withAlpha(ModuleRegistry.tabColor().rgb(), Math.round(215 * alpha));
            RoundedRectPipeline.draw(context, x, y, width, height, 6.0f, color);
            RoundedRectPipeline.drawOutline(context, x, y, width, height, 6.0f, 1.0f,
                ColorMath.withAlpha(Theme.accent(), Math.round(85 * alpha)));
        }
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

    @Redirect(method = "render", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/gui/DrawContext;fill(IIIII)V"), require = 0)
    private void linkenvisuals$captureTabFill(DrawContext context, int x1, int y1, int x2, int y2, int color) {
        int minX = Math.min(x1, x2);
        int minY = Math.min(y1, y2);
        int maxX = Math.max(x1, x2);
        int maxY = Math.max(y1, y2);
        if (!linkenvisuals$tabCaptured) {
            linkenvisuals$tabMinX = minX;
            linkenvisuals$tabMinY = minY;
            linkenvisuals$tabMaxX = maxX;
            linkenvisuals$tabMaxY = maxY;
            linkenvisuals$tabCaptured = true;
        } else {
            linkenvisuals$tabMinX = Math.min(linkenvisuals$tabMinX, minX);
            linkenvisuals$tabMinY = Math.min(linkenvisuals$tabMinY, minY);
            linkenvisuals$tabMaxX = Math.max(linkenvisuals$tabMaxX, maxX);
            linkenvisuals$tabMaxY = Math.max(linkenvisuals$tabMaxY, maxY);
        }
        if (ModuleRegistry.customTab() == null || !ModuleRegistry.customTab().value()) {
            context.fill(x1, y1, x2, y2, color);
        }
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void linkenvisuals$popTab(DrawContext context, int scaledWindowWidth, Scoreboard scoreboard,
                                       ScoreboardObjective objective, CallbackInfo ci) {
        context.getMatrices().popMatrix();
        if (linkenvisuals$tabCaptured) {
            linkenvisuals$tabBounds = true;
        }
    }
}
