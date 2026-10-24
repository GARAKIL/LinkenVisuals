package dev.linkenvisuals.ui;

/**
 * Donor {@code AnimatedString} without Lombok: a short crossfade between the
 * previous and current text value. The donor uses it for the LinkGUI search
 * field (150 ms) and for mode/keybind values (160 ms), both with
 * {@code EASE_OUT_CUBIC}; {@link #progress()} applies exactly that curve.
 */
public final class AnimatedString {

    private final long duration;
    private String current;
    private String previous;
    private long startTime;

    public AnimatedString(long duration, String initial) {
        this.duration = Math.max(1L, duration);
        this.current = initial != null ? initial : "";
        this.previous = this.current;
    }

    /** Starts a new crossfade only when the value actually changed. */
    public void update(String target) {
        String normalized = target != null ? target : "";
        if (normalized.equals(current)) {
            if (!isAnimating()) {
                previous = current;
            }
            return;
        }
        previous = current;
        current = normalized;
        startTime = System.currentTimeMillis();
    }

    /** Eased 0..1 progress of the active crossfade; finishes and resets at 1. */
    public float progress() {
        if (!isAnimating()) {
            return 1.0f;
        }
        float raw = (System.currentTimeMillis() - startTime) / (float) duration;
        if (raw >= 1.0f) {
            startTime = 0L;
            previous = current;
            return 1.0f;
        }
        float clamped = Math.max(0.0f, raw);
        float inverse = 1.0f - clamped;
        return 1.0f - inverse * inverse * inverse;
    }

    public boolean isAnimating() {
        return startTime > 0L;
    }

    public String current() {
        return current;
    }

    public String previous() {
        return previous;
    }
}
