package dev.linkenvisuals.account;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AltAccountFilterTest {
    private static final StoredAccount ALPHA = new StoredAccount("Alpha");
    private static final StoredAccount BETA = new StoredAccount("Beta");
    private static final StoredAccount GAMMA = new StoredAccount("Gamma");

    @Test
    void emptyQueryKeepsEveryAccount() {
        assertEquals(List.of(ALPHA, BETA, GAMMA),
            AltAccountFilter.filter(List.of(ALPHA, BETA, GAMMA), "  "));
    }

    @Test
    void queryIsCaseInsensitiveAndTrimmed() {
        assertEquals(List.of(BETA), AltAccountFilter.filter(List.of(ALPHA, BETA, GAMMA), " be "));
    }

    @Test
    void unmatchedQueryReturnsEmptyList() {
        assertEquals(List.of(), AltAccountFilter.filter(List.of(ALPHA, BETA), "zzz"));
        assertEquals(-1, AltAccountFilter.indexOf(List.of(ALPHA, BETA), GAMMA));
        assertEquals(1, AltAccountFilter.indexOf(List.of(ALPHA, BETA), new StoredAccount("beta")));
    }
}
