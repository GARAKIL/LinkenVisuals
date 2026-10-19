package dev.linkenvisuals.util;

import dev.linkenvisuals.util.text.StreamerRules;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StreamerRulesTest {

    private static StreamerRules active() {
        return new StreamerRules("Streamer", "STREAM", "777", true,
            List.of("Ivan"), Map.of("Vasya", "Petya"));
    }

    private static StreamerRules disabled() {
        return new StreamerRules("", "", "", false, List.of(), Map.of());
    }

    @Test
    void replacesSelfNameAsWholeTokenOnly() {
        StreamerRules rules = active();
        assertEquals("Streamer hello", rules.sanitize("Ivan hello"));
        assertEquals("Streamer", rules.sanitize("IVAN"), "замена ника регистронезависимая");
        assertEquals("Ivanov hello", rules.sanitize("Ivanov hello"), "частичные совпадения не трогаем");
    }

    @Test
    void keepsLegacyFormattingAroundReplacement() {
        StreamerRules rules = active();
        assertEquals("\u00A7cStreamer\u00A7r", rules.sanitize("\u00A7cIvan\u00A7r"));
        assertEquals("&cStreamer&r", rules.sanitize("&cIvan&r"));
    }

    @Test
    void replacesPlayerPlaceholder() {
        assertEquals("Привет, Streamer!", active().sanitize("Привет, %player%!"));
    }

    @Test
    void replacesDonateValueKeepingLabel() {
        StreamerRules rules = active();
        assertEquals("Ранг: STREAM", rules.sanitize("Ранг: VIP"));
        assertEquals("Донат: STREAM", rules.sanitize("Донат: [VIP]"));
        assertEquals("Donate: STREAM", rules.sanitize("Donate:VIP"),
            "пробел после разделителя нормализуется (как у донора)");
    }

    @Test
    void replacesCoinAmounts() {
        assertEquals("Баланс: 777", active().sanitize("Баланс: 12 345"));
    }

    @Test
    void hidesAnarchyAndGriefNumbers() {
        StreamerRules rules = active();
        assertEquals("Анархия-999", rules.sanitize("Анархия-3"));
        assertEquals("Гриф #666", rules.sanitize("Гриф #7"));
        assertEquals("Анархия-3", disabled().sanitize("Анархия-3"), "с выключенным hideAnarchy номер остаётся");
    }

    @Test
    void replacesFriendNickname() {
        assertEquals("Petya said hi", active().sanitize("Vasya said hi"));
    }

    @Test
    void masksSensitiveCommandLine() {
        assertEquals("/login *******", StreamerRules.maskSensitiveLine("/login hunter2"));
        assertEquals("/l:***", StreamerRules.maskSensitiveLine("/l:abc"));
        assertEquals("/help me", StreamerRules.maskSensitiveLine("/help me"), "незнакомые команды не маскируются");
        assertEquals("  /pass ** **", StreamerRules.maskSensitiveLine("  /pass ab cd"),
            "пробелы внутри пароля сохраняются");
    }

    @Test
    void maskFromIndexKeepsWhitespaceAndPrefix() {
        assertEquals("abc ***", StreamerRules.maskFromIndex("abc def", 4));
        assertEquals("abc def", StreamerRules.maskFromIndex("abc def", -1));
        assertEquals("abc def", StreamerRules.maskFromIndex("abc def", 99));
    }

    @Test
    void disabledRulesStillMaskPasswordsOnly() {
        StreamerRules rules = disabled();
        assertEquals("Ivan bought VIP", rules.sanitize("Ivan bought VIP"));
        assertEquals("  /login ***", rules.sanitize("  /login abc"));
    }

    @Test
    void couldMatchPrefilter() {
        StreamerRules rules = active();
        assertFalse(rules.couldMatch("просто текст"));
        assertTrue(rules.couldMatch("Ivan на месте"));
        assertTrue(rules.couldMatch("Анархия-1"));
        assertTrue(rules.couldMatch("/whatever"));
        assertFalse(disabled().couldMatch("Ivan на месте"), "без настроек ник не ищем");
        assertTrue(disabled().couldMatch("/x"), "парольные строки проходят всегда");
    }

    @Test
    void parsesFriendNicknamePairs() {
        Map<String, String> parsed = StreamerRules.parseFriendNicknames("Vasya=Petya,\nKolya=Masha;broken;=x;a=");
        assertEquals(2, parsed.size());
        assertEquals("Petya", parsed.get("Vasya"));
        assertEquals("Masha", parsed.get("Kolya"));
        assertTrue(StreamerRules.parseFriendNicknames(null).isEmpty());
        assertTrue(StreamerRules.parseFriendNicknames("   ").isEmpty());
    }
}
