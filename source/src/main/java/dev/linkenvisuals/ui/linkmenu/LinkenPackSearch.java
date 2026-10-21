package dev.linkenvisuals.ui.linkmenu;

import java.util.Locale;

/** Pure search/display helpers for the LINKEN resource-pack list. */
public final class LinkenPackSearch {
    private LinkenPackSearch() {
    }

    public static boolean matches(String name, String description, String id, String query) {
        String needle = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        if (needle.isEmpty()) {
            return true;
        }
        return contains(name, needle) || contains(description, needle) || contains(id, needle);
    }

    public static String initials(String name) {
        if (name == null || name.isBlank()) {
            return "RP";
        }
        String[] words = name.trim().split("\\s+");
        StringBuilder out = new StringBuilder(2);
        for (String word : words) {
            if (word.isEmpty() || out.length() >= 2) {
                continue;
            }
            out.appendCodePoint(Character.toUpperCase(word.codePointAt(0)));
        }
        return out.isEmpty() ? "RP" : out.toString();
    }

    private static boolean contains(String value, String needle) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(needle);
    }
}
