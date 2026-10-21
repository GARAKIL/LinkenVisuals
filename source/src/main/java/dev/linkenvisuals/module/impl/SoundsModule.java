package dev.linkenvisuals.module.impl;

import dev.linkenvisuals.audio.LinkenSounds;
import dev.linkenvisuals.clickgui.Category;
import dev.linkenvisuals.clickgui.setting.BooleanSetting;
import dev.linkenvisuals.clickgui.setting.Setting;
import dev.linkenvisuals.clickgui.setting.SliderSetting;
import dev.linkenvisuals.module.Module;
import dev.linkenvisuals.module.Modules;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.network.packet.s2c.play.EntityDamageS2CPacket;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Donor-style combat/assist sound module: kills, killstreak, totem pops,
 * low health and local death. Damage is inferred from entities the local
 * player hit; no extra network packet mixin is required.
 */
public final class SoundsModule extends Module {
    private static final long RETENTION_MS = 4_500L;
    private static final long STREAK_WINDOW_MS = 9_000L;
    private static final float DESPAWN_KILL_HP = 6.0f;

    private final SliderSetting volume;
    private final BooleanSetting kills;
    private final BooleanSetting killStreak;
    private final BooleanSetting totem;
    private final BooleanSetting death;
    private final BooleanSetting lowHealth;
    private final SliderSetting lowHealthAt;

    private static final Map<Integer, Mark> DAMAGED = new ConcurrentHashMap<>();
    private static final Random RANDOM = new Random();
    private static int streak;
    private static long lastKillMs;
    private static boolean deadFlag;
    private static boolean lowFlag;

    private record Mark(long at, boolean mob, boolean player, float lastHealth) {
    }

    public SoundsModule() {
        this(new SliderSetting("Volume", 0.0f, 200.0f, 5.0f, 130.0f, "%"),
            new BooleanSetting("Kills", true),
            new BooleanSetting("Kill Streak", true),
            new BooleanSetting("Totem Pop", true),
            new BooleanSetting("Death", true),
            new BooleanSetting("Low Health", true),
            new SliderSetting("Low Health At", 1.0f, 19.0f, 1.0f, 6.0f, " HP"));
    }

    private SoundsModule(SliderSetting volume, BooleanSetting kills, BooleanSetting killStreak,
                         BooleanSetting totem, BooleanSetting death, BooleanSetting lowHealth,
                         SliderSetting lowHealthAt) {
        super("sounds", "Sounds", "kill, assist, totem and death cues",
            Category.PLAYER, List.of(volume, kills, killStreak, totem),
            List.of(death, lowHealth, lowHealthAt));
        this.volume = volume;
        this.kills = kills;
        this.killStreak = killStreak;
        this.totem = totem;
        this.death = death;
        this.lowHealth = lowHealth;
        this.lowHealthAt = lowHealthAt;
        info().toggleSilently(true);
    }

    @Override
    protected void onDisable() {
        DAMAGED.clear();
        streak = 0;
        deadFlag = false;
        lowFlag = false;
    }

    /** Called from the attack hook for every real local-player hit. */
    public static void noteHit(LivingEntity target) {
        SoundsModule module = Modules.sounds();
        if (module == null || !module.enabled() || target == null) {
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || target == client.player || target.getId() < 0) {
            return;
        }
        DAMAGED.put(target.getId(), new Mark(System.currentTimeMillis(),
            target instanceof MobEntity, target instanceof PlayerEntity, target.getHealth()));
    }

    /**
     * Called from the damage packet path so projectiles and server-side damage
     * also mark a victim, matching the donor's {@code Sounds} behaviour.
     */
    public static void noteDamage(EntityDamageS2CPacket packet) {
        SoundsModule module = Modules.sounds();
        MinecraftClient client = MinecraftClient.getInstance();
        if (module == null || !module.enabled() || packet == null || client == null
            || client.world == null || client.player == null) {
            return;
        }
        int targetId = packet.entityId();
        if (targetId < 0 || targetId == client.player.getId()) {
            return;
        }
        boolean byPlayer = packet.sourceCauseId() == client.player.getId()
            || packet.sourceDirectId() == client.player.getId();
        if (!byPlayer) {
            Entity direct = client.world.getEntityById(packet.sourceDirectId());
            if (!(direct instanceof ProjectileEntity projectile)
                || projectile.getOwner() != client.player) {
                return;
            }
        }
        Entity target = client.world.getEntityById(targetId);
        if (target instanceof LivingEntity living) {
            mark(living);
        }
    }

    private static void mark(LivingEntity target) {
        if (target == null || target.getId() < 0) {
            return;
        }
        DAMAGED.put(target.getId(), new Mark(System.currentTimeMillis(),
            target instanceof MobEntity, target instanceof PlayerEntity, target.getHealth()));
    }

    /** Called from the totem status packet for the local player. */
    public static void noteTotemPop(PlayerEntity player) {
        SoundsModule module = Modules.sounds();
        if (module == null || !module.enabled() || player == null) {
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != player || !module.totem.value()) {
            return;
        }
        module.play(LinkenSounds.Cue.TOTEM_POP);
    }

    public void tick(MinecraftClient client) {
        if (!enabled() || client == null || client.world == null || client.player == null) {
            DAMAGED.clear();
            deadFlag = false;
            lowFlag = false;
            return;
        }

        long now = System.currentTimeMillis();
        if (streak > 0 && now - lastKillMs > STREAK_WINDOW_MS) {
            streak = 0;
        }

        boolean dead = client.player.isDead() || client.player.getHealth() <= 0.0f;
        if (dead) {
            if (!deadFlag) {
                deadFlag = true;
                streak = 0;
                if (death.value()) {
                    play(RANDOM.nextBoolean()
                        ? LinkenSounds.Cue.SELF_RESPAWN : LinkenSounds.Cue.SELF_RESPAWN_ALT);
                }
            }
        } else {
            deadFlag = false;
        }

        float hp = client.player.getHealth();
        if (!dead && hp > 0.0f && hp <= lowHealthAt.value()) {
            if (!lowFlag) {
                lowFlag = true;
                if (lowHealth.value()) {
                    play(LinkenSounds.Cue.LOW_HEALTH);
                }
            }
        } else if (hp > lowHealthAt.value() + 2.0f) {
            lowFlag = false;
        }

        Iterator<Map.Entry<Integer, Mark>> iterator = DAMAGED.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, Mark> entry = iterator.next();
            Mark mark = entry.getValue();
            if (now - mark.at() > RETENTION_MS) {
                iterator.remove();
                continue;
            }
            Entity entity = client.world.getEntityById(entry.getKey());
            if (entity instanceof LivingEntity living) {
                if (living.isAlive() && living.getHealth() > 0.0f) {
                    entry.setValue(new Mark(mark.at(), mark.mob(), mark.player(), living.getHealth()));
                    continue;
                }
                onKill(mark);
                iterator.remove();
            } else if (entity == null) {
                if (mark.lastHealth() <= DESPAWN_KILL_HP) {
                    onKill(mark);
                }
                iterator.remove();
            }
        }
    }

    private void onKill(Mark mark) {
        streak++;
        lastKillMs = System.currentTimeMillis();
        if (killStreak.value() && streak >= 2) {
            play(streakCue(Math.min(4, streak - 2)));
        } else if (kills.value()) {
            play(RANDOM.nextBoolean() ? LinkenSounds.Cue.KILL_MOLODEC
                : (RANDOM.nextBoolean() ? LinkenSounds.Cue.KILL_UNICHTOZJEN
                    : LinkenSounds.Cue.KILL_ZAPEN));
        }
    }

    private static LinkenSounds.Cue streakCue(int index) {
        return switch (index) {
            case 0 -> LinkenSounds.Cue.DOTA_DOUBLE_KILL;
            case 1 -> LinkenSounds.Cue.DOTA_TRIPLE_KILL;
            case 2 -> LinkenSounds.Cue.DOTA_MEGA_KILL;
            case 3 -> LinkenSounds.Cue.DOTA_MONSTER_KILL;
            default -> LinkenSounds.Cue.DOTA_RAMPAGE;
        };
    }

    private void play(LinkenSounds.Cue cue) {
        float value = volume.value() / 100.0f;
        if (value <= 0.0f) {
            return;
        }
        LinkenSounds.play(cue.sound(), value, 1.0f);
    }
}
