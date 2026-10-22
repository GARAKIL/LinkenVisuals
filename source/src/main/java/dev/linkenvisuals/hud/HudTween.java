package dev.linkenvisuals.hud;

/**
 * Time-based donor-style value animation. This is the shared small helper for
 * HUD cards: linear for bars, ease-out-cubic for fades and width.
 */
final class HudTween {
    private final boolean easeOut;
    private float value;
    private float from;
    private float target;
    private long startedAt;
    private long durationMs = 1L;
    private boolean active;

    HudTween(boolean easeOut) {
        this.easeOut = easeOut;
    }

    float value(float next, long duration) {
        long now = System.nanoTime();
        if (!active || next != target) {
            from = value;
            target = next;
            startedAt = now;
            durationMs = Math.max(1L, duration);
            active = true;
        }
        float t = Math.min(1.0f, (now - startedAt) / (durationMs * 1_000_000.0f));
        float eased = easeOut ? 1.0f - (1.0f - t) * (1.0f - t) * (1.0f - t) : t;
        value = from + (target - from) * eased;
        return value;
    }
}
