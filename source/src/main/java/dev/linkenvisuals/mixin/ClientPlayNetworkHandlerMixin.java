package dev.linkenvisuals.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.linkenvisuals.module.Modules;
import dev.linkenvisuals.module.impl.KillStreakModule;
import dev.linkenvisuals.module.impl.SoundsModule;
import dev.linkenvisuals.module.impl.TotemTrackerModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityDamageS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin {
    @Inject(method = "onEntityDamage", at = @At("TAIL"))
    private void linkenvisuals$noteDamage(EntityDamageS2CPacket packet, CallbackInfo ci) {
        SoundsModule.noteDamage(packet);
        KillStreakModule.noteDamage(packet);
    }

    @WrapOperation(
        method = "onPlayerList",
        at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V")
    )
    private void linkenvisuals$silenceUnknownPlayerWarning(Logger logger, String format, Object arg1, Object arg2, Operation<Void> original) {
        if (format != null && format.contains("Ignoring player info update")) {
            // Suppress benign proxy / hub player list warning on servers like FunTime
            return;
        }
        original.call(logger, format, arg1, arg2);
    }

    @Inject(method = "onEntityStatus", at = @At("HEAD"))
    private void linkenvisuals$trackTotemPop(EntityStatusS2CPacket packet, CallbackInfo ci) {
        if (packet.getStatus() != EntityStatuses.USE_TOTEM_OF_UNDYING) {
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) {
            return;
        }
        if (packet.getEntity(client.world) instanceof PlayerEntity player) {
            SoundsModule.noteTotemPop(player);
            TotemTrackerModule module = Modules.totemTracker();
            if (module != null && module.enabled()) {
                module.onTotemPop(player, client);
            }
        }
    }
}
