package dev.linkenvisuals.util.text;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Таблица паттернов стример-режима. Донор (MC 1.21.4) держал её прямо в коде
 * ({@code utils/text/streamer/StreamerModePatterns.java}); мастер-промт требует вынести
 * таблицу в ресурс — она лежит в {@code assets/linkenvisuals/streamer/patterns.json},
 * здесь только компиляция в {@link Pattern} и ленивое кеширование.
 */
public final class StreamerPatterns {

    private static final String RESOURCE = "/assets/linkenvisuals/streamer/patterns.json";
    private static final Gson GSON = new Gson();
    /** Класс значения после донат-лейбла — как у донора (буквы, цифры, глифы, скобки). */
    private static final String LABEL_VALUE = "[\\[\\]★✦✧⚝⚜\\p{L}\\p{N}_+\\- ]{1,32}";

    private static volatile Table table;

    /**
     * @param donateLabel      {@code (label)(value)}: "Ранг: VIP"
     * @param coinLabel        {@code (label)(number)}: "Монеты: 1 234"
     * @param anarchy          {@code (word)(separator)(number)}: "Анархия-3"
     * @param grief            то же для "Гриф #7"
     * @param sensitiveCommand строка, начинающаяся с парольной команды (см. {@code patterns.json})
     */
    public record Table(Pattern donateLabel, Pattern coinLabel, Pattern anarchy, Pattern grief,
                        Pattern sensitiveCommand,
                        List<String> donateWords, List<String> coinWords,
                        List<String> anarchyWords, List<String> griefWords) {
    }

    private StreamerPatterns() {
    }

    public static Table table() {
        Table cached = table;
        if (cached != null) {
            return cached;
        }
        synchronized (StreamerPatterns.class) {
            if (table == null) {
                table = load();
            }
            return table;
        }
    }

    private static Table load() {
        Json json = readJson();
        List<String> donateLabels = requireList(json.donateLabels, "donateLabels");
        List<String> coinLabels = requireList(json.coinLabels, "coinLabels");
        List<String> anarchyWords = requireList(json.anarchyWords, "anarchyWords");
        List<String> griefWords = requireList(json.griefWords, "griefWords");
        List<String> sensitiveCommands = requireList(json.sensitiveCommands, "sensitiveCommands");

        Pattern donateLabel = Pattern.compile("(?iu)((?:" + alternation(donateLabels)
            + ")\\s*[:=\\-»>]?)\\s*(" + LABEL_VALUE + ")");
        Pattern coinLabel = Pattern.compile("(?iu)((?:" + alternation(coinLabels)
            + ")\\s*[:=\\-»>]?)\\s*(\\d[\\d\\s.,_]*)");
        Pattern anarchy = Pattern.compile("(?iu)(" + alternation(anarchyWords) + ")([\\s#\\-]*)(\\d+)");
        Pattern grief = Pattern.compile("(?iu)(" + alternation(griefWords) + ")([\\s#\\-]*)(\\d+)");
        Pattern sensitiveCommand = Pattern.compile("(?i)^\\s*/(?:" + alternation(sensitiveCommands)
            + ")(?=\\s|$|[:=])");

        return new Table(donateLabel, coinLabel, anarchy, grief, sensitiveCommand,
            donateLabels, coinLabels, anarchyWords, griefWords);
    }

    private static Json readJson() {
        try (InputStream in = StreamerPatterns.class.getResourceAsStream(RESOURCE)) {
            if (in == null) {
                throw new IllegalStateException("Streamer patterns resource is missing: " + RESOURCE);
            }
            Json json = GSON.fromJson(new InputStreamReader(in, StandardCharsets.UTF_8), Json.class);
            if (json == null) {
                throw new IllegalStateException("Streamer patterns resource is empty: " + RESOURCE);
            }
            return json;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read " + RESOURCE, e);
        } catch (JsonParseException e) {
            throw new IllegalStateException("Failed to parse " + RESOURCE, e);
        }
    }

    private static List<String> requireList(List<String> values, String field) {
        if (values == null || values.isEmpty() || values.stream().anyMatch(v -> v == null || v.isBlank())) {
            throw new IllegalStateException("Streamer patterns field '" + field + "' is missing or empty in " + RESOURCE);
        }
        return List.copyOf(values);
    }

    private static String alternation(List<String> values) {
        StringBuilder pattern = new StringBuilder();
        for (String value : values) {
            if (pattern.length() > 0) {
                pattern.append('|');
            }
            pattern.append(Pattern.quote(value));
        }
        return pattern.toString();
    }

    @SuppressWarnings("unused") // заполняется Gson по именам полей
    private static final class Json {
        private List<String> donateLabels;
        private List<String> coinLabels;
        private List<String> anarchyWords;
        private List<String> griefWords;
        private List<String> sensitiveCommands;
    }
}
