package dev.linkenvisuals.util;

import dev.linkenvisuals.util.text.StreamerPatterns;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StreamerPatternsTest {

    @Test
    void tableLoadsFromResource() {
        StreamerPatterns.Table table = StreamerPatterns.table();
        assertNotNull(table.donateLabel());
        assertNotNull(table.coinLabel());
        assertNotNull(table.anarchy());
        assertNotNull(table.grief());
        assertNotNull(table.sensitiveCommand());
        assertTrue(table.donateWords().size() >= 6);
        assertTrue(table.coinWords().size() >= 8);
        assertTrue(table.anarchyWords().size() >= 2);
        assertTrue(table.griefWords().size() >= 3);
    }

    @Test
    void sensitiveCommandMatchesPasswordCommandsOnly() {
        var pattern = StreamerPatterns.table().sensitiveCommand();
        assertTrue(pattern.matcher("/login secret").find());
        assertTrue(pattern.matcher("  /l pass").find());
        assertTrue(pattern.matcher("/setpass:new").find());
        assertFalse(pattern.matcher("/help").find());
        assertFalse(pattern.matcher("/loginx").find());
        assertFalse(pattern.matcher("login secret").find(), "без слэша не команда");
    }

    @Test
    void labelPatternsMatchServerFormats() {
        StreamerPatterns.Table table = StreamerPatterns.table();
        assertTrue(table.donateLabel().matcher("Донат: VIP").find());
        assertTrue(table.donateLabel().matcher("Ранг:VIP").find());
        assertTrue(table.coinLabel().matcher("Монеты: 1 234").find());
        assertTrue(table.coinLabel().matcher("balance:999").find());
        assertTrue(table.anarchy().matcher("Анархия-3").find());
        assertTrue(table.grief().matcher("Гриф #7").find());
        assertFalse(table.anarchy().matcher("Анархия").find(), "без номера паттерн не срабатывает");
    }
}
