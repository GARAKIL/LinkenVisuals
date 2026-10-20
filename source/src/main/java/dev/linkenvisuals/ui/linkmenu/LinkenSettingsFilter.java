package dev.linkenvisuals.ui.linkmenu;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Pure search filter shared by the LINKEN settings sub-pages. */
public final class LinkenSettingsFilter {
    private LinkenSettingsFilter() {
    }

    public static List<LinkenSettingsOption> filter(List<LinkenSettingsOption> source, String query) {
        String needle = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        if (needle.isEmpty()) {
            return List.copyOf(source);
        }
        List<LinkenSettingsOption> out = new ArrayList<>();
        for (LinkenSettingsOption option : source) {
            if (option.label().toLowerCase(Locale.ROOT).contains(needle)) {
                out.add(option);
            }
        }
        return out;
    }
}
