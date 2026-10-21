package dev.linkenvisuals.module.impl;

import dev.linkenvisuals.audio.LinkenSounds;
import dev.linkenvisuals.clickgui.Category;
import dev.linkenvisuals.clickgui.setting.BooleanSetting;
import dev.linkenvisuals.clickgui.setting.ModeSetting;
import dev.linkenvisuals.clickgui.setting.Setting;
import dev.linkenvisuals.module.Module;
import dev.linkenvisuals.module.Modules;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvent;

import java.util.List;

/**
 * HitSound (Phase 5.7): plays a sound on hit.
 * Donor's {@code HitSound} (102 lines) simplified.
 */
public final class HitSoundModule extends Module {
    public final ModeSetting sound;
    public final BooleanSetting onlyCrit;

    public HitSoundModule() {
        this(new ModeSetting("Sound", List.of("Bell", "Crime", "Nya", "Uwu"), 0),
             new BooleanSetting("Only Crit", false));
    }

    private HitSoundModule(ModeSetting sound, BooleanSetting onlyCrit) {
        super("hitsound", "Hit Sound", "sound on hit",
            Category.VISUAL,
            List.of(),
            List.<Setting>of(sound, onlyCrit));
        this.sound = sound;
        this.onlyCrit = onlyCrit;
    }

    /**
     * Plays the configured hit sound for the local player. Called from the
     * attack hook in {@code ClientPlayerInteractionManagerMixin} (real entity
     * hits) and from {@code MinecraftClientAttackMixin} (fake player hits).
     *
     * @param crit whether the hit satisfies the vanilla crit conditions
     */
    public static void tryPlay(MinecraftClient client, boolean crit) {
        HitSoundModule module = Modules.hitSound();
        if (module == null || !module.enabled() || (module.onlyCrit.value() && !crit)) {
            return;
        }
        if (client == null || client.getSoundManager() == null) {
            return;
        }
        SoundEvent event = switch (module.sound.value()) {
            case "Crime" -> LinkenSounds.CRIME;
            case "Nya" -> LinkenSounds.NYA;
            case "Uwu" -> LinkenSounds.UWU;
            default -> LinkenSounds.BELL;
        };
        client.getSoundManager().play(PositionedSoundInstance.ui(event, 1.0f, 1.0f));
    }

    /**
     * Vanilla crit conditions (see {@code PlayerEntity#attack}): falling,
     * airborne, not climbing/swimming/blind/riding, living target.
     */
    public static boolean isCrit(PlayerEntity player, Entity target) {
        return target instanceof LivingEntity
            && player.fallDistance > 0.0
            && !player.isOnGround()
            && !player.isClimbing()
            && !player.isTouchingWater()
            && !player.hasStatusEffect(StatusEffects.BLINDNESS)
            && !player.hasVehicle();
    }
}
