package dev.linkenvisuals.ui.linkmenu;

/**
 * Snappy 130ms cubic ease-out hover animation matching 1.21.4 donor MenuChrome.
 */
public final class LinkenAnimation {
    private final long fullDurationMs;
    private boolean forward;
    private float startValue;
    private float targetValue;
    private long startTime;
    private long currentDurationMs;

    public LinkenAnimation(long durationMs) {
        this.fullDurationMs = Math.max(1L, durationMs);
        this.forward = false;
        this.startValue = 0.0f;
        this.targetValue = 0.0f;
        this.startTime = System.currentTimeMillis();
        this.currentDurationMs = 0L;
    }

    public static LinkenAnimation hover() {
        return new LinkenAnimation(130L);
    }

    public void update(boolean targetForward) {
        if (this.forward == targetForward) {
            return;
        }
        float current = get();
        this.forward = targetForward;
        this.startValue = current;
        this.targetValue = targetForward ? 1.0f : 0.0f;
        float distance = Math.abs(targetValue - startValue);
        this.currentDurationMs = Math.max(1L, Math.round(distance * fullDurationMs));
        this.startTime = System.currentTimeMillis();
    }

    public float get() {
        if (currentDurationMs <= 0L) {
            return targetValue;
        }
        long elapsed = System.currentTimeMillis() - startTime;
        if (elapsed >= currentDurationMs) {
            return targetValue;
        }
        float t = (float) elapsed / (float) currentDurationMs;
        float inv = 1.0f - t;
        float eased = 1.0f - inv * inv * inv; // ease-out cubic
        return startValue + (targetValue - startValue) * eased;
    }

    public float updateAndGet(boolean targetForward) {
        update(targetForward);
        return get();
    }
}
