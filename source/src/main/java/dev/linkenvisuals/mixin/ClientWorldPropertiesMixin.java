package dev.linkenvisuals.mixin;

import dev.linkenvisuals.clickgui.Ambience;
import dev.linkenvisuals.module.Modules;
import dev.linkenvisuals.module.impl.TimeChangerModule;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientWorld.Properties.class)
public abstract class ClientWorldPropertiesMixin {
    @Inject(method = "getTimeOfDay", at = @At("RETURN"), cancellable = true)
    private void linkenvisuals$overrideTime(CallbackInfoReturnable<Long> cir) {
        long vanilla = cir.getReturnValue();
        long override = Ambience.timeOfDay(vanilla);
        if (override != vanilla) {
            cir.setReturnValue(override);
            return;
        }
        // TimeChanger module override.
        TimeChangerModule timeChanger = Modules.timeChanger();
        if (timeChanger != null && timeChanger.enabled()) {
            long time = switch (timeChanger.preset.value()) {
                case "Noon" -> 6000L;
                case "Sunset" -> 12000L;
                case "Night" -> 13000L;
                case "Midnight" -> 18000L;
                default -> 1000L;
            };
            cir.setReturnValue(time);
        }
    }
}
