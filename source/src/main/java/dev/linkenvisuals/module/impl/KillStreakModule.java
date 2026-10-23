package dev.linkenvisuals.module.impl;

import dev.linkenvisuals.clickgui.Category;
import dev.linkenvisuals.clickgui.setting.BooleanSetting;
import dev.linkenvisuals.clickgui.setting.Setting;
import dev.linkenvisuals.clickgui.setting.SliderSetting;
import dev.linkenvisuals.module.Module;
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
import java.util.concurrent.ConcurrentHashMap;

/**
 * Donor visual KillStreak tracker. The visual card lives in
 * {@link dev.linkenvisuals.hud.KillStreakHud}; SoundsModule keeps its own
 * streak voice logic, so this module only counts and tiers kills.
 */
public final class KillStreakModule extends Module {
    private static final long RETENTION_MS = 4_500L;
    private static final float DESPAWN_KILL_HP = 6.0f;
    private static final int MAX_TIER = 4;
    private static final Map<Integer, Mark> DAMAGED = new ConcurrentHashMap<>();

    public final SliderSetting streakWindow;
    public final BooleanSetting countMobs;
    public final BooleanSetting comboTimer;
    public final BooleanSetting themeAccent;

    private static int streak;
    private static long lastKillMs;
    private static int tier;

    public KillStreakModule() {
        this(new SliderSetting("Streak Window", 2.0f, 60.0f, 1.0f, 10.0f, "s"),
            new BooleanSetting("Count Mobs", true),
            new BooleanSetting("Combo Timer", true),
            new BooleanSetting("Theme Accent", true));
    }

    private KillStreakModule(SliderSetting streakWindow, BooleanSetting countMobs,
                             BooleanSetting comboTimer, BooleanSetting themeAccent) {
        super("killstreak", "Kill Streak", "tiered kill-streak HUD card",
            Category.VISUAL, List.<Setting>of(streakWindow, countMobs),
            List.<Setting>of(comboTimer, themeAccent));
        this.streakWindow = streakWindow;
        this.countMobs = countMobs;
        this.comboTimer = comboTimer;
        this.themeAccent = themeAccent;
        info().withClassicCategory(dev.linkenvisuals.clickgui.classic.ClassicCategory.WORLD);
        info().toggleSilently(true);
    }

    @Override
    protected void onDisable() {
        DAMAGED.clear();
        reset();
    }

    public static int streak() {
        return streak;
    }

    public static long lastKillMs() {
        return lastKillMs;
    }

    public static int tier() {
        return tier;
    }

    public static boolean themeAccentEnabled() {
        KillStreakModule module = dev.linkenvisuals.module.Modules.killStreak();
        return module == null || module.themeAccent.value();
    }

    public static boolean comboTimerEnabled() {
        KillStreakModule module = dev.linkenvisuals.module.Modules.killStreak();
        return module == null || module.comboTimer.value();
    }

    public static void reset() {
        streak = 0;
        lastKillMs = 0L;
        tier = 0;
    }

    public static void noteHit(LivingEntity target) {
        KillStreakModule module = dev.linkenvisuals.module.Modules.killStreak();
        if (module == null || !module.enabled() || target == null || !module.isCounted(target)) {
            return;
        }
        if (!target.isAlive() || target.getHealth() <= 0.0f) {
            return;
        }
        DAMAGED.put(target.getId(), new Mark(System.currentTimeMillis(), target.getHealth()));
    }

    public static void noteDamage(EntityDamageS2CPacket packet) {
        KillStreakModule module = dev.linkenvisuals.module.Modules.killStreak();
        MinecraftClient client = MinecraftClient.getInstance();
        if (module == null || !module.enabled() || packet == null
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
        if (target instanceof LivingEntity living && module.isCounted(living)) {
            DAMAGED.put(targetId, new Mark(System.currentTimeMillis(), living.getHealth()));
        }
    }

    public static void tick(MinecraftClient client) {
        KillStreakModule module = dev.linkenvisuals.module.Modules.killStreak();
        if (module == null || !module.enabled() || client == null
            || client.world == null || client.player == null) {
            DAMAGED.clear();
            reset();
            return;
        }
        long now = System.currentTimeMillis();
        if (streak > 0 && now - lastKillMs > module.windowMs()) {
            reset();
        }
        Iterator<Map.Entry<Integer, Mark>> iterator = DAMAGED.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, Mark> entry = iterator.next();
            Mark mark = entry.getValue();
            if (now - mark.at > RETENTION_MS) {
                iterator.remove();
                continue;
            }
            Entity entity = client.world.getEntityById(entry.getKey());
            if (entity instanceof LivingEntity living) {
                if (living.isAlive() && living.getHealth() > 0.0f) {
                    entry.setValue(new Mark(mark.at, living.getHealth()));
                    continue;
                }
                module.onKill(now);
                iterator.remove();
            } else if (entity == null) {
                if (mark.lastHealth <= DESPAWN_KILL_HP) {
                    module.onKill(now);
                }
                iterator.remove();
            }
        }
    }

    private void onKill(long now) {
        if (streak > 0 && now - lastKillMs > windowMs()) {
            reset();
        }
        streak++;
        lastKillMs = now;
        tier = Math.max(0, Math.min(MAX_TIER, streak - 2));
    }

    private boolean isCounted(LivingEntity entity) {
        if (entity.getId() < 0) {
            return false;
        }
        if (entity instanceof MobEntity) {
            return countMobs.value();
        }
        return entity instanceof PlayerEntity
            && entity != MinecraftClient.getInstance().player;
    }

    private long windowMs() {
        return (long) (streakWindow.value() * 1000.0f);
    }

    private record Mark(long at, float lastHealth) {
    }
}
