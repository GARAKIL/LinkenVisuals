package dev.linkenvisuals.mixin;

import dev.linkenvisuals.module.Modules;
import dev.linkenvisuals.module.impl.StreamerModeModule;
import dev.linkenvisuals.util.text.StreamerTextSanitizer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Donor SelfNametags: sanitize only the local player's display name. */
@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {
    @Inject(method = "getDisplayName", at = @At("RETURN"), cancellable = true)
    private void linkenvisuals$selfNametag(CallbackInfoReturnable<Text> cir) {
        PlayerEntity self = (PlayerEntity) (Object) this;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player != self
            || Modules.selfNametags() == null || !Modules.selfNametags().enabled()) {
            return;
        }
        cir.setReturnValue(StreamerTextSanitizer.sanitize(cir.getReturnValue(),
            StreamerModeModule.rules()));
    }
}
