package dev.linkenvisuals.account;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Pure search/filter rules for the LINKEN account manager. */
public final class AltAccountFilter {
    private AltAccountFilter() {
    }

    public static List<StoredAccount> filter(List<StoredAccount> source, String query) {
        String needle = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        if (needle.isEmpty()) {
            return List.copyOf(source);
        }
        List<StoredAccount> out = new ArrayList<>();
        for (StoredAccount account : source) {
            if (account.username().toLowerCase(Locale.ROOT).contains(needle)) {
                out.add(account);
            }
        }
        return out;
    }

    public static int indexOf(List<StoredAccount> accounts, StoredAccount account) {
        if (account == null) {
            return -1;
        }
        for (int i = 0; i < accounts.size(); i++) {
            if (accounts.get(i).username().equalsIgnoreCase(account.username())) {
                return i;
            }
        }
        return -1;
    }
}
