package dev.linkenvisuals.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.linkenvisuals.module.impl.StreamerModeModule;
import dev.linkenvisuals.util.text.StreamerTextSanitizer;
import net.minecraft.scoreboard.ScoreboardEntry;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Стример-режим: строки сайдбара скорборда. Сайдбар рисует
 * {@code renderScoreboardSidebar} через {@code name()/formatted()}, поэтому чистим оба
 * (плюс {@code display()} — запись-компонент для прочих потребителей).
 */
@Mixin(ScoreboardEntry.class)
public abstract class ScoreboardEntryMixin {
    @ModifyReturnValue(method = "name", at = @At("RETURN"))
    private Text linkenvisuals$sanitizeName(Text original) {
        return StreamerTextSanitizer.sanitize(original, StreamerModeModule.rules());
    }

    @ModifyReturnValue(method = "display", at = @At("RETURN"))
    private Text linkenvisuals$sanitizeDisplay(Text original) {
        return StreamerTextSanitizer.sanitize(original, StreamerModeModule.rules());
    }

    @ModifyReturnValue(method = "formatted", at = @At("RETURN"))
    private MutableText linkenvisuals$sanitizeFormatted(MutableText original) {
        return StreamerTextSanitizer.sanitizeMutable(original, StreamerModeModule.rules());
    }
}
