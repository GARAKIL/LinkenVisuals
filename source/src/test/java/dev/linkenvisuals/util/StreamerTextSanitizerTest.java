package dev.linkenvisuals.util;

import dev.linkenvisuals.util.text.StreamerRules;
import dev.linkenvisuals.util.text.StreamerTextSanitizer;
import net.minecraft.text.Text;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Deterministic team-prefix scenario for SelfNametags: the server-provided
 * prefix stays intact while only the local player's own name is replaced.
 */
class StreamerTextSanitizerTest {
    private static StreamerRules rules() {
        return new StreamerRules("Streamer", "", "", false, List.of("Ivan"), Map.of());
    }

    @Test
    void keepsScoreboardTeamPrefixAndReplacesOnlyTheSelfName() {
        Text label = Text.literal("[MVP] ").append(Text.literal("Ivan"));
        Text sanitized = StreamerTextSanitizer.sanitize(label, rules());
        assertEquals("[MVP] Streamer", sanitized.getString());
    }

    @Test
    void keepsLegacyTeamPrefixCodes() {
        Text label = Text.literal("\u00A77[Team] \u00A7fIvan");
        Text sanitized = StreamerTextSanitizer.sanitize(label, rules());
        assertEquals("\u00A77[Team] \u00A7fStreamer", sanitized.getString());
    }

    @Test
    void leavesOtherPlayersNamesAlone() {
        Text label = Text.literal("[MVP] ").append(Text.literal("Vasya"));
        Text sanitized = StreamerTextSanitizer.sanitize(label, rules());
        assertSame(label, sanitized);
        assertEquals("[MVP] Vasya", sanitized.getString());
    }

    @Test
    void prefixContainingSelfNameIsNotTouchedWhenItIsASeparateToken() {
        Text label = Text.literal("Ivanov [MVP] Ivan");
        Text sanitized = StreamerTextSanitizer.sanitize(label, rules());
        assertEquals("Ivanov [MVP] Streamer", sanitized.getString());
    }
}
