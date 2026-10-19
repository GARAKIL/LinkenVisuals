package dev.linkenvisuals.util.text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.ToIntFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Скомпилированные правила стример-режима: замена настоящего ника/доната/монет,
 * скрытие номера анархии/грif-сервера и маскирование парольных команд.
 * Чистая логика без зависимости от Minecraft — тестируется без bootstrap.
 * <p>
 * Порт {@code StreamerModeUtils} (донор, MC 1.21.4) без его сегментной машинерии:
 * замена идёт по plain-строке с картой индексов, чтобы не ломать legacy-коды
 * форматирования ({@code §}/{@code &}).
 */
public final class StreamerRules {

    private static final String HIDDEN_MODE_ANARCHY = "999";
    private static final String HIDDEN_MODE_GRIEF = "666";
    private static final char MASK_CHAR = '*';
    private static final Pattern PLAYER_PLACEHOLDER = Pattern.compile(Pattern.quote("%player%"));
    private static final String FORMAT_CODES = "0123456789abcdefklmnorABCDEFKLMNOR";

    private record NameRule(String token, String replacement, Pattern pattern) {
    }

    /** Строка без legacy-кодов + карта plain-индекс → индекс в исходной строке. */
    private record Mapping(String plain, int[] origIndex) {
        static Mapping of(String raw) {
            StringBuilder plain = new StringBuilder(raw.length());
            int[] origIndex = new int[raw.length()];
            int size = 0;
            for (int i = 0; i < raw.length(); ) {
                char c = raw.charAt(i);
                if ((c == '\u00A7' || c == '&') && i + 1 < raw.length() && raw.charAt(i + 1) == '#'
                    && isHexRange(raw, i + 2, i + 8)) {
                    i += 8;
                    continue;
                }
                if ((c == '\u00A7' || c == '&') && i + 13 < raw.length()
                    && (raw.charAt(i + 1) == 'x' || raw.charAt(i + 1) == 'X')
                    && isHexPairSequence(raw, i + 2)) {
                    i += 14;
                    continue;
                }
                if ((c == '\u00A7' || c == '&') && i + 1 < raw.length() && isFormatCode(raw.charAt(i + 1))) {
                    i += 2;
                    continue;
                }
                plain.append(c);
                origIndex[size++] = i;
                i++;
            }
            return new Mapping(plain.toString(), origIndex);
        }

        int origFrom(int plainIndex) {
            return origIndex[plainIndex];
        }

        int origTo(int plainEndExclusive) {
            return origIndex[plainEndExclusive - 1] + 1;
        }

        private static boolean isFormatCode(char c) {
            return FORMAT_CODES.indexOf(c) >= 0;
        }

        private static boolean isHexRange(String raw, int start, int end) {
            if (start < 0 || end > raw.length()) {
                return false;
            }
            for (int i = start; i < end; i++) {
                if (Character.digit(raw.charAt(i), 16) < 0) {
                    return false;
                }
            }
            return true;
        }

        /** Шесть пар {@code §<hex>} подряд — формат {@code §x§R§R§G§G§B§B}. */
        private static boolean isHexPairSequence(String raw, int start) {
            for (int pair = 0; pair < 6; pair++) {
                int i = start + pair * 2;
                if (i + 1 >= raw.length() || raw.charAt(i) != '\u00A7'
                    || Character.digit(raw.charAt(i + 1), 16) < 0) {
                    return false;
                }
            }
            return true;
        }
    }

    private final String replacementName;
    private final String replacementDonate;
    private final String replacementCoins;
    private final boolean hideAnarchy;
    private final List<NameRule> nameRules;
    private final Pattern prefilter;

    public StreamerRules(String replacementName, String replacementDonate, String replacementCoins,
                         boolean hideAnarchy, List<String> selfNames, Map<String, String> friendNicknames) {
        this.replacementName = clean(replacementName);
        this.replacementDonate = clean(replacementDonate);
        this.replacementCoins = clean(replacementCoins);
        this.hideAnarchy = hideAnarchy;
        this.nameRules = buildNameRules(this.replacementName, selfNames, friendNicknames);
        this.prefilter = buildPrefilter(this.replacementName, this.replacementDonate, this.replacementCoins,
            hideAnarchy, this.nameRules);
    }

    /** Дешёвый предфильтр: без совпадения полная замена не запускается. */
    public boolean couldMatch(String input) {
        return input != null && !input.isEmpty() && prefilter.matcher(input).find();
    }

    /** Замены сохраняют порядок из {@code StreamerModeUtils.sanitizeString} (без секционной машинерии). */
    public String sanitize(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        StreamerPatterns.Table table = StreamerPatterns.table();
        String result = input;
        if (!replacementName.isEmpty()) {
            result = replaceRanges(result, PLAYER_PLACEHOLDER, Matcher::start, Matcher::end, replacementName);
        }
        for (NameRule rule : nameRules) {
            result = replaceRanges(result, rule.pattern(), Matcher::start, Matcher::end, rule.replacement());
        }
        if (!replacementDonate.isEmpty()) {
            result = replaceRanges(result, table.donateLabel(), m -> m.end(1), m -> m.end(2),
                " " + replacementDonate);
        }
        if (!replacementCoins.isEmpty()) {
            result = replaceRanges(result, table.coinLabel(), m -> m.start(2), m -> m.end(2), replacementCoins);
        }
        if (hideAnarchy) {
            result = replaceRanges(result, table.anarchy(), m -> m.start(3), m -> m.end(3), HIDDEN_MODE_ANARCHY);
            result = replaceRanges(result, table.grief(), m -> m.start(3), m -> m.end(3), HIDDEN_MODE_GRIEF);
        }
        return maskSensitiveLine(result);
    }

    /** Маскирует пароль в строке, начинающейся с парольной команды ({@code /login secret}). */
    public static String maskSensitiveLine(String input) {
        return maskFromIndex(input, sensitiveCommandEnd(input));
    }

    /** Индекс конца парольной команды или -1, если строка не начинается с неё. */
    public static int sensitiveCommandEnd(String input) {
        if (input == null || input.isEmpty()) {
            return -1;
        }
        Matcher matcher = StreamerPatterns.table().sensitiveCommand().matcher(input);
        if (!matcher.find()) {
            return -1;
        }
        int end = matcher.end();
        if (end < input.length()) {
            char next = input.charAt(end);
            if (next == ':' || next == '=') {
                end++;
            }
        }
        return end;
    }

    /** Всё, что после {@code maskStart}, превращается в {@code *}; пробелы сохраняются (как у донора). */
    public static String maskFromIndex(String input, int maskStart) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        if (maskStart < 0 || maskStart >= input.length()) {
            return input;
        }
        StringBuilder masked = new StringBuilder(input.length());
        masked.append(input, 0, maskStart);
        for (int i = maskStart; i < input.length(); i++) {
            char c = input.charAt(i);
            masked.append(Character.isWhitespace(c) ? c : MASK_CHAR);
        }
        return masked.toString();
    }

    /** Пары {@code real=nickname}, разделённые {@code ,;} или переводом строки (без FriendManager донора). */
    public static Map<String, String> parseFriendNicknames(String raw) {
        if (raw == null || raw.isBlank()) {
            return Map.of();
        }
        Map<String, String> parsed = new LinkedHashMap<>();
        for (String part : raw.split("[,;\\n]")) {
            int separator = part.indexOf('=');
            if (separator <= 0 || separator >= part.length() - 1) {
                continue;
            }
            String name = part.substring(0, separator).trim();
            String nickname = part.substring(separator + 1).trim();
            if (!name.isEmpty() && !nickname.isEmpty()) {
                parsed.put(name, nickname);
            }
        }
        return Collections.unmodifiableMap(parsed);
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim();
    }

    private static List<NameRule> buildNameRules(String replacementName, List<String> selfNames,
                                                 Map<String, String> friendNicknames) {
        List<NameRule> rules = new ArrayList<>();
        if (!replacementName.isEmpty() && selfNames != null) {
            for (String selfName : selfNames) {
                addNameRule(rules, selfName, replacementName);
            }
        }
        if (friendNicknames != null) {
            for (Map.Entry<String, String> entry : friendNicknames.entrySet()) {
                addNameRule(rules, entry.getKey(), entry.getValue());
            }
        }
        return List.copyOf(rules);
    }

    private static void addNameRule(List<NameRule> rules, String token, String replacement) {
        String cleanedToken = clean(token);
        String cleanedReplacement = clean(replacement);
        if (cleanedToken.isEmpty() || cleanedReplacement.isEmpty()
            || cleanedToken.equalsIgnoreCase(cleanedReplacement)) {
            return;
        }
        Pattern pattern = Pattern.compile("(?iu)(?<![\\p{L}\\p{N}_])"
            + Pattern.quote(cleanedToken) + "(?![\\p{L}\\p{N}_])");
        rules.add(new NameRule(cleanedToken, cleanedReplacement, pattern));
    }

    private static Pattern buildPrefilter(String replacementName, String replacementDonate,
                                          String replacementCoins, boolean hideAnarchy, List<NameRule> nameRules) {
        StreamerPatterns.Table table = StreamerPatterns.table();
        List<String> parts = new ArrayList<>();
        parts.add("^\\s*/");
        if (!replacementName.isEmpty()) {
            parts.add(Pattern.quote("%player%"));
        }
        for (NameRule rule : nameRules) {
            parts.add(Pattern.quote(rule.token()));
        }
        if (!replacementDonate.isEmpty()) {
            for (String word : table.donateWords()) {
                parts.add(Pattern.quote(word));
            }
        }
        if (!replacementCoins.isEmpty()) {
            for (String word : table.coinWords()) {
                parts.add(Pattern.quote(word));
            }
        }
        if (hideAnarchy) {
            for (String word : table.anarchyWords()) {
                parts.add(Pattern.quote(word));
            }
            for (String word : table.griefWords()) {
                parts.add(Pattern.quote(word));
            }
        }
        return Pattern.compile("(?iu)(?:" + String.join("|", parts) + ")");
    }

    /**
     * Замена попаданий {@code pattern} в {@code raw} с сохранением legacy-кодов:
     * границы попадания берутся из plain-строки и пересчитываются в индексы исходной.
     */
    private static String replaceRanges(String raw, Pattern pattern, ToIntFunction<Matcher> fromPlain,
                                        ToIntFunction<Matcher> toPlain, String replacement) {
        Mapping mapping = Mapping.of(raw);
        if (mapping.plain().isEmpty()) {
            return raw;
        }
        Matcher matcher = pattern.matcher(mapping.plain());
        if (!matcher.find()) {
            return raw;
        }
        StringBuilder out = new StringBuilder(raw.length() + 16);
        int last = 0;
        boolean changed = false;
        do {
            int from = fromPlain.applyAsInt(matcher);
            int to = toPlain.applyAsInt(matcher);
            if (from < 0 || to <= from || to > mapping.plain().length()) {
                continue;
            }
            out.append(raw, last, mapping.origFrom(from)).append(replacement);
            last = mapping.origTo(to);
            changed = true;
        } while (matcher.find());
        if (!changed) {
            return raw;
        }
        out.append(raw, last, raw.length());
        return out.toString();
    }
}
