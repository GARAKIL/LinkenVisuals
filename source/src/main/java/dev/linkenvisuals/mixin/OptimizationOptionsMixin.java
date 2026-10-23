package dev.linkenvisuals.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.linkenvisuals.module.impl.OptimizationModule;
import net.minecraft.client.option.GameOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GameOptions.class)
public abstract class OptimizationOptionsMixin {
    @ModifyReturnValue(method = "getClampedViewDistance", at = @At("RETURN"))
    private int linkenvisuals$capRenderDistance(int original) {
        return OptimizationModule.capEffectiveRenderDistance(original);
    }
}
