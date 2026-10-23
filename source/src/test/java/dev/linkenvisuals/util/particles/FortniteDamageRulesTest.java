package dev.linkenvisuals.util.particles;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Locks the donor FortniteDamage pop/scale/alpha curves. */
class FortniteDamageRulesTest {
    @Test
    void alphaFadesInAndOut() {
        assertEquals(0.0f, FortniteDamageRules.alpha(0.0f), 1.0e-6f);
        assertEquals(1.0f, FortniteDamageRules.alpha(0.4f), 1.0e-6f);
        assertEquals(0.0f, FortniteDamageRules.alpha(1.0f), 1.0e-6f);
    }

    @Test
    void scalePopsPastOneThenSettles() {
        assertEquals(0.45f, FortniteDamageRules.scale(0.0f), 1.0e-6f);
        assertTrue(FortniteDamageRules.scale(0.15f) > 1.0f);
        assertEquals(1.0f, FortniteDamageRules.scale(0.4f), 1.0e-6f);
    }

    @Test
    void riseMonotonicAndTextNeverZero() {
        assertTrue(FortniteDamageRules.rise(0.5f, 0.42f) > FortniteDamageRules.rise(0.2f, 0.42f));
        assertEquals("1", FortniteDamageRules.text(0.0f));
        assertEquals("7", FortniteDamageRules.text(6.6f));
        assertEquals("20", FortniteDamageRules.text(19.7f));
    }
}
