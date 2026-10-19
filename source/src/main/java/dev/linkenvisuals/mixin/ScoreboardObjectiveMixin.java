package dev.linkenvisuals.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.linkenvisuals.module.impl.StreamerModeModule;
import dev.linkenvisuals.util.text.StreamerTextSanitizer;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Стример-режим: название цели скорборда (в т.ч. виджет в табе) проходит через замены
 * ника/доната/анархии. Санитизация на уровне данных чистит все пути отрисовки разом.
 */
@Mixin(ScoreboardObjective.class)
public abstract class ScoreboardObjectiveMixin {
    @ModifyReturnValue(method = "getDisplayName", at = @At("RETURN"))
    private Text linkenvisuals$sanitizeDisplayName(Text original) {
        return StreamerTextSanitizer.sanitize(original, StreamerModeModule.rules());
    }
}
