package dev.linkenvisuals.audio;

import dev.linkenvisuals.LinkenVisuals;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * Central registry and playback helpers for the LINKEN sound set.
 *
 * <p>All events are registered once from {@link LinkenVisuals#onInitializeClient()}.
 * The resources mirror the donor {@code sounds.json}; no vanilla UI/combat sounds
 * are used by module-triggered actions.</p>
 */
public final class LinkenSounds {
    private LinkenSounds() {
    }

    private static SoundEvent event(String path) {
        Identifier id = Identifier.of(LinkenVisuals.MOD_ID, path);
        SoundEvent sound = SoundEvent.of(id);
        Registry.register(Registries.SOUND_EVENT, id, sound);
        return sound;
    }

    public static final SoundEvent UI_CLICK = event("ui.click");
    public static final SoundEvent ENABLE = event("enable");
    public static final SoundEvent DISABLE = event("disable");
    public static final SoundEvent CRITICAL = event("critical");
    public static final SoundEvent CLICKGUI_OPEN = event("clickgui_open");
    public static final SoundEvent BELL = event("bell");
    public static final SoundEvent CRIME = event("crime");
    public static final SoundEvent NYA = event("nya");
    public static final SoundEvent UWU = event("uwu");
    public static final SoundEvent HELLO = event("hellodragons");
    public static final SoundEvent LOW_HEALTH = event("krithealth_lechisy");
    public static final SoundEvent KILL_MOLODEC = event("molodec_ubil");
    public static final SoundEvent KILL_UNICHTOZJEN = event("unichtozjen");
    public static final SoundEvent KILL_ZAPEN = event("zapen");
    public static final SoundEvent ARROW_HIT = event("vcheli");
    public static final SoundEvent SELF_RESPAWN = event("respawn");
    public static final SoundEvent SELF_RESPAWN_ALT = event("next_raz_povezyo");
    public static final SoundEvent TOTEM_POP = event("snes_totema");
    public static final SoundEvent TOTEM_POP_ALT = event("snesli_totem");
    public static final SoundEvent MULTI_KILL = event("multikill");
    public static final SoundEvent UNSTOPPABLE = event("neostonovim");
    public static final SoundEvent DOUBLE_KILL = event("dabkill");
    public static final SoundEvent DABKILL = DOUBLE_KILL;
    public static final SoundEvent DOTA_DOUBLE_KILL = event("dota_double_kill");
    public static final SoundEvent DOTA_TRIPLE_KILL = event("dota_triple_kill");
    public static final SoundEvent DOTA_MEGA_KILL = event("dota_mega_kill");
    public static final SoundEvent DOTA_MONSTER_KILL = event("dota_monster_kill");
    public static final SoundEvent DOTA_RAMPAGE = event("dota_rampage");
    public static final SoundEvent MAINMENU_TRACK = event("mainmenu/track");
    public static final SoundEvent COSMETIC_ITACHI = event("cosmetic/itachi");
    public static final SoundEvent COSMETIC_NARUTO = event("cosmetic/naruto_shadow_clones");

    /** No-op bootstrap hook so the class is loaded from mod init. */
    public static void register() {
    }

    public static void playUiClick() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null && client.getSoundManager() != null) {
            client.getSoundManager().play(PositionedSoundInstance.ui(UI_CLICK, 1.15f, 1.0f));
        }
    }

    public static void play(SoundEvent sound, float volume, float pitch) {
        if (sound == null) {
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null && client.getSoundManager() != null) {
            client.getSoundManager().play(PositionedSoundInstance.ui(sound, pitch, volume));
        }
    }

    public static void playAt(World world, Vec3d at, SoundEvent sound, float volume, float pitch) {
        if (world == null || at == null || sound == null) {
            return;
        }
        world.playSoundClient(at.x, at.y, at.z, sound, SoundCategory.PLAYERS, volume, pitch, false);
    }

    public enum Cue {
        KILL_MOLODEC(LinkenSounds.KILL_MOLODEC),
        KILL_UNICHTOZJEN(LinkenSounds.KILL_UNICHTOZJEN),
        KILL_ZAPEN(LinkenSounds.KILL_ZAPEN),
        TOTEM_POP(LinkenSounds.TOTEM_POP),
        LOW_HEALTH(LinkenSounds.LOW_HEALTH),
        SELF_RESPAWN(LinkenSounds.SELF_RESPAWN),
        SELF_RESPAWN_ALT(LinkenSounds.SELF_RESPAWN_ALT),
        DOTA_DOUBLE_KILL(LinkenSounds.DOTA_DOUBLE_KILL),
        DOTA_TRIPLE_KILL(LinkenSounds.DOTA_TRIPLE_KILL),
        DOTA_MEGA_KILL(LinkenSounds.DOTA_MEGA_KILL),
        DOTA_MONSTER_KILL(LinkenSounds.DOTA_MONSTER_KILL),
        DOTA_RAMPAGE(LinkenSounds.DOTA_RAMPAGE);

        private final SoundEvent sound;

        Cue(SoundEvent sound) {
            this.sound = sound;
        }

        public SoundEvent sound() {
            return sound;
        }
    }
}
