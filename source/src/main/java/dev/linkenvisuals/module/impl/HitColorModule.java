package dev.linkenvisuals.module.impl;

import dev.linkenvisuals.clickgui.Category;
import dev.linkenvisuals.clickgui.setting.ColorSetting;
import dev.linkenvisuals.clickgui.setting.Setting;
import dev.linkenvisuals.clickgui.setting.SliderSetting;
import dev.linkenvisuals.module.Module;
import dev.linkenvisuals.util.HitColorMath;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * HitColor (Phase 5.7): tints entities on hit.
 *
 * <p>Ported from the donor's {@code HitColor} (85 lines) and
 * {@code HitColorRenderHelper}. The hit marker is registered by
 * {@code ClientPlayerInteractionManagerMixin.attackEntity}; the tint itself is
 * applied in {@code LivingEntityRendererMixin} through vanilla's
 * {@code getMixColor} hook. Armor tinting is intentionally out of scope: the
 * 1.21.11 equipment path submits through {@code RenderCommandQueue.submitModel},
 * so it would need a separate mixin for a cosmetic detail.
 */
public final class HitColorModule extends Module {
    /** How long a hit keeps the tint at full strength. */
    private static final long HIT_WINDOW_MS = 500L;

    private static final Map<Integer, Long> RECENT_HITS = new ConcurrentHashMap<>();
    private static final Map<Integer, Float> STRENGTH = new ConcurrentHashMap<>();

    public final ColorSetting color;
    public final SliderSetting opacity;

    public HitColorModule() {
        this(new ColorSetting("Color", 0xFF6B6B),
             new SliderSetting("Opacity", 0.0f, 1000.0f, 1.0f, 140.0f, ""));
    }

    private HitColorModule(ColorSetting color, SliderSetting opacity) {
        super("hitcolor", "Hit Color", "tint entities on hit",
            Category.VISUAL,
            List.of(),
            List.<Setting>of(color, opacity));
        this.color = color;
        this.opacity = opacity;
    }

    @Override
    protected void onDisable() {
        RECENT_HITS.clear();
        STRENGTH.clear();
    }

    public static void markHit(int entityId) {
        RECENT_HITS.put(entityId, System.currentTimeMillis());
    }

    /** Advances the fade curve; called once per render-state update. */
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
        if (System.currentTimeMillis() - time > HIT_WINDOW_MS) {
            RECENT_HITS.remove(entityId);
            return false;
        }
        return true;
    }

    /** Tint entry point used by the renderer mixin; delegates to pure math. */
    public static int tint(int originalColor, int chosenRgb, float strength, float maxTint) {
        return HitColorMath.tint(originalColor, chosenRgb, strength, maxTint);
    }
}
