package dev.linkenvisuals.module.impl;

import dev.linkenvisuals.clickgui.Category;
import dev.linkenvisuals.clickgui.setting.BooleanSetting;
import dev.linkenvisuals.clickgui.setting.Setting;
import dev.linkenvisuals.clickgui.setting.SliderSetting;
import dev.linkenvisuals.module.Module;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * HitColor (Phase 5.7): tints entities on hit.
 * Donor's {@code HitColor} (85 lines) simplified: hit tracking only.
 * Renderer tint hook is a follow-up (requires LivingEntityRenderer mixin).
 */
public final class HitColorModule extends Module {
    private static final Map<Integer, Long> RECENT_HITS = new ConcurrentHashMap<>();
    private static final Map<Integer, Float> STRENGTH = new ConcurrentHashMap<>();

    public final SliderSetting opacity;
    public final BooleanSetting colorArmor;

    public HitColorModule() {
        this(new SliderSetting("Opacity", 0.0f, 1000.0f, 1.0f, 140.0f, ""),
             new BooleanSetting("Color Armor", false));
    }

    private HitColorModule(SliderSetting opacity, BooleanSetting colorArmor) {
        super("hitcolor", "Hit Color", "tint entities on hit",
            Category.VISUAL,
            List.of(),
            List.<Setting>of(opacity, colorArmor));
        this.opacity = opacity;
        this.colorArmor = colorArmor;
    }

    @Override
    protected void onDisable() {
        RECENT_HITS.clear();
        STRENGTH.clear();
    }

    public static void markHit(int entityId) {
        RECENT_HITS.put(entityId, System.currentTimeMillis());
    }

    public static float advanceStrength(int entityId, boolean hitByMe) {
        Float cur = STRENGTH.get(entityId);
        if (!hitByMe && cur == null) {
            return 0.0f;
        }
        float current = cur == null ? 0.0f : cur;
        float target = hitByMe ? 1.0f : 0.0f;
        float speed = target > current ? 0.85f : 0.18f;
        float next = current + (target - current) * speed;
        if (next <= 0.001f) {
            if (cur != null) {
                STRENGTH.remove(entityId);
            }
            return 0.0f;
        }
        STRENGTH.put(entityId, next);
        return next;
    }

    public static boolean wasHitByMe(int entityId) {
        Long time = RECENT_HITS.get(entityId);
        if (time == null) {
            return false;
        }
        // Expire after 500ms.
        if (System.currentTimeMillis() - time > 500L) {
            RECENT_HITS.remove(entityId);
            return false;
        }
        return true;
    }
}
