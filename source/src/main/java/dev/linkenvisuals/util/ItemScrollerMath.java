package dev.linkenvisuals.util;

/**
 * Pure logic for {@code ItemScroller}: modifier-to-mode resolution and the
 * anti-kick move budget, extracted so both invariants are unit-tested without
 * a Minecraft bootstrap.
 */
public final class ItemScrollerMath {
    /** Donor safety floor: one inventory move per 50 ms even at delay 0. */
    public static final long MIN_DELAY_MS = 50L;
    /** Hard cap on server-visible moves per drag call (donor's anti-kick guard). */
    public static final int MAX_MOVES_PER_CALL = 3;

    public enum DragMode {
        NONE,
        MOVE_STACKS,
        MOVE_ONE,
        MOVE_MATCHING
    }

    private ItemScrollerMath() {
    }

    /**
     * Maps the click button and held modifiers to the donor's drag modes:
     * {@code alt+shift}/{@code shift} move whole stacks, {@code alt} moves only
     * matching items, {@code ctrl} moves one item. Right/middle clicks never
     * start a drag.
     */
    public static DragMode resolveDragMode(int button, boolean shiftDown, boolean ctrlDown,
                                           boolean altDown) {
        if (button != 0) {
            return DragMode.NONE;
        }
        if (altDown && shiftDown) {
            return DragMode.MOVE_STACKS;
        }
        if (altDown) {
            return DragMode.MOVE_MATCHING;
        }
        if (ctrlDown) {
            return DragMode.MOVE_ONE;
        }
        if (shiftDown) {
            return DragMode.MOVE_STACKS;
        }
        return DragMode.NONE;
    }

    /** Effective delay never drops below {@link #MIN_DELAY_MS}. */
    public static long effectiveDelayMs(long configuredDelayMs) {
        return Math.max(configuredDelayMs, MIN_DELAY_MS);
    }

    /** Clamps a raw allowance to {@link #MAX_MOVES_PER_CALL}. */
    public static int clampBudget(long rawBudget) {
        return (int) Math.max(0L, Math.min(MAX_MOVES_PER_CALL, rawBudget));
    }

    /** Monotonic time-based allowance, mirroring the donor's scheduling. */
    public static final class MoveBudget {
        private long nextMoveAt;

        /** Moves allowed at {@code nowMs}; 0 while throttled. */
        public int rawBudget(long nowMs, long delayMs) {
            long delay = effectiveDelayMs(delayMs);
            if (nowMs < nextMoveAt) {
                return 0;
            }
            return 1 + (int) ((nowMs - nextMoveAt) / delay);
        }

        /** Pushes the next move slot; the cadence stays monotonic when behind. */
        public void schedule(long nowMs, long delayMs) {
            long delay = effectiveDelayMs(delayMs);
            if (nextMoveAt <= nowMs) {
                nextMoveAt = nowMs + delay;
            } else {
                nextMoveAt += delay;
            }
        }

        public void reset() {
            nextMoveAt = 0L;
        }
    }
}
