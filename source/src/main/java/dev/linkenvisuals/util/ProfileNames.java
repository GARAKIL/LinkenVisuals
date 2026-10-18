package dev.linkenvisuals.util;

/**
 * Filesystem-safe profile-name sanitizer for config storage.
 * <p>
 * CWE-22 guard: strips illegal FS characters AND dot-segments, so names like
 * {@code ".."}, {@code "../x"} or {@code "a..b"} can never escape the configs
 * directory. Letters/digits/space/_/- plus non-control Unicode are kept,
 * everything else collapses to {@code "_"}; "." / ".." are rejected outright.
 */
public final class ProfileNames {
    /** Maximum length of a sanitized profile name. */
    public static final int MAX_LENGTH = 64;

    private ProfileNames() {
    }

    public static String sanitize(String name) {
        if (name == null) {
            return "";
        }
        String clean = name.trim().replaceAll("[\\\\/:*?\"<>|]", "_");
        clean = clean.replaceAll("[\\p{Cntrl}.]", "_").trim();
        // Remove leading dots/underscores left from ".." sequences ("..foo" -> "foo")
        clean = clean.replaceAll("^[._\\s]+", "");
        if (clean.equals(".") || clean.equals("..")) {
            return "";
        }
        if (clean.length() > MAX_LENGTH) {
            clean = clean.substring(0, MAX_LENGTH).trim();
        }
        // Final guard: no separators or parent refs survived
        if (clean.isEmpty() || clean.contains("..") || clean.contains("/") || clean.contains("\\")) {
            return "";
        }
        return clean;
    }
}
