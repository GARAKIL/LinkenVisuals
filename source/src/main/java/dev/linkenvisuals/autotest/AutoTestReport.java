package dev.linkenvisuals.autotest;

import java.util.ArrayList;
import java.util.List;

/**
 * Result accumulator for the in-game auto-test driver. Deliberately free of
 * Minecraft types so the counting and formatting contract is unit-testable
 * without a game bootstrap.
 */
public final class AutoTestReport {
    private static final int MAX_DETAIL_LINES = 40;

    private int passed;
    private int failed;
    private int skipped;
    private final List<String> failures = new ArrayList<>();
    private final List<String> skips = new ArrayList<>();

    public void pass() {
        passed++;
    }

    public void fail(String step, Throwable error) {
        failed++;
        addBounded(failures, step + ": " + describe(error));
    }

    public void fail(String step, String reason) {
        failed++;
        addBounded(failures, step + ": " + reason);
    }

    public void skip(String step, String reason) {
        skipped++;
        addBounded(skips, step + ": " + reason);
    }

    public int passed() {
        return passed;
    }

    public int failed() {
        return failed;
    }

    public int skipped() {
        return skipped;
    }

    public List<String> failures() {
        return List.copyOf(failures);
    }

    public List<String> skips() {
        return List.copyOf(skips);
    }

    public boolean ok() {
        return failed == 0;
    }

    public String summary() {
        return "steps=" + (passed + failed + skipped)
            + " ok=" + passed
            + " fail=" + failed
            + " skip=" + skipped;
    }

    static String describe(Throwable error) {
        String message = error.getMessage();
        return message == null || message.isBlank()
            ? error.getClass().getSimpleName()
            : error.getClass().getSimpleName() + ": " + message;
    }

    private static void addBounded(List<String> lines, String line) {
        if (lines.size() < MAX_DETAIL_LINES) {
            lines.add(line);
        } else if (lines.size() == MAX_DETAIL_LINES) {
            lines.add("... (details truncated)");
        }
    }
}
