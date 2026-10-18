package dev.linkenvisuals.render;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** LRU semantics of the string-width cache (donor's width-cache win). */
class WidthCacheTest {
    @Test
    void hitAfterPut() {
        GlyphAtlas.WidthCache cache = new GlyphAtlas.WidthCache(16);
        assertNull(cache.get("hello", 6.0f));
        cache.put("hello", 6.0f, 42.5f);
        assertEquals(42.5f, cache.get("hello", 6.0f));
        assertEquals(1, cache.size());
    }

    @Test
    void sizeDistinguishesEntries() {
        GlyphAtlas.WidthCache cache = new GlyphAtlas.WidthCache(16);
        cache.put("hello", 6.0f, 42.5f);
        cache.put("hello", 7.0f, 50.0f);
        assertEquals(42.5f, cache.get("hello", 6.0f));
        assertEquals(50.0f, cache.get("hello", 7.0f));
    }

    @Test
    void eldestEvictedPastCapacity() {
        GlyphAtlas.WidthCache cache = new GlyphAtlas.WidthCache(4);
        cache.put("a", 1.0f, 1.0f);
        cache.put("b", 1.0f, 2.0f);
        cache.put("c", 1.0f, 3.0f);
        cache.put("d", 1.0f, 4.0f);
        assertEquals(4, cache.size());
        // Touch "a" so "b" becomes eldest.
        assertEquals(1.0f, cache.get("a", 1.0f));
        cache.put("e", 1.0f, 5.0f);
        assertEquals(4, cache.size());
        assertNull(cache.get("b", 1.0f), "eldest entry must be evicted");
        assertEquals(1.0f, cache.get("a", 1.0f));
        assertEquals(5.0f, cache.get("e", 1.0f));
    }
}
