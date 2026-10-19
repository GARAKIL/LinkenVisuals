package dev.linkenvisuals.mixin;

import dev.linkenvisuals.module.Modules;
import dev.linkenvisuals.module.impl.HitSoundModule;
import dev.linkenvisuals.player.FakePlayer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientAttackMixin {
    @Inject(method = "doAttack", at = @At("HEAD"), cancellable = true)
    private void linkenvisuals$hitDummy(CallbackInfoReturnable<Boolean> info) {
        MinecraftClient client = (MinecraftClient) (Object) this;
        if (FakePlayer.tryHit(client)) {
            info.setReturnValue(true);
        }
    }

    @Inject(method = "doAttack", at = @At("RETURN"))
    private void linkenvisuals$hitSound(CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue()) {
            return;
        }
        HitSoundModule module = Modules.hitSound();
        if (module == null || !module.enabled()) {
            return;
        }
        MinecraftClient client = (MinecraftClient) (Object) this;
        if (client.getSoundManager() == null) {
            return;
        }
        // Mark hit for HitColor.
        if (client.crosshairTarget instanceof net.minecraft.util.hit.EntityHitResult hit) {
            dev.linkenvisuals.module.impl.HitColorModule.markHit(hit.getEntity().getId());
        }
        SoundEvent sound = switch (module.sound.value()) {
            case "Click" -> SoundEvents.UI_BUTTON_CLICK.value();
            case "Pop" -> SoundEvents.ENTITY_CHICKEN_EGG.value();
            default -> SoundEvents.BLOCK_NOTE_BLOCK_BELL.value();
        };
        client.getSoundManager().play(PositionedSoundInstance.ui(sound, 1.0f, 1.0f));
    }
}
