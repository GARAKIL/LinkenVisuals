package dev.linkenvisuals.clickgui.link;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LinkGuiThemeWindowLayoutTest {
    @Test
    void sixCardsFitInsideTheCentredPanel() {
        LinkGuiThemeWindowLayout.Layout layout = LinkGuiThemeWindowLayout.compute(1920.0f, 1080.0f, 6);
        assertEquals(3, LinkGuiThemeWindowLayout.COLUMNS);
        assertEquals(6, layout.cards().size());
        assertEquals(1920.0f * 0.5f, layout.panel().x() + layout.panel().width() * 0.5f, 1.0e-4f);
        assertTrue(layout.close().x() >= layout.panel().x());
        assertTrue(layout.close().right() <= layout.panel().right());
        assertTrue(layout.close().y() >= layout.panel().y());
        assertTrue(layout.close().bottom() <= layout.panel().bottom());
        for (LinkGuiLayout.Rect card : layout.cards()) {
            assertTrue(card.x() >= layout.panel().x());
            assertTrue(card.right() <= layout.panel().right());
            assertTrue(card.y() >= layout.panel().y());
            assertTrue(card.bottom() <= layout.panel().bottom());
        }
    }

    @Test
    void cardsDoNotOverlap() {
        LinkGuiThemeWindowLayout.Layout layout = LinkGuiThemeWindowLayout.compute(1920.0f, 1080.0f, 6);
        for (int i = 0; i < layout.cards().size(); i++) {
            for (int j = i + 1; j < layout.cards().size(); j++) {
                assertFalse(layout.cards().get(i).intersects(layout.cards().get(j)),
                    "cards " + i + " and " + j + " overlap");
            }
        }
    }

    @Test
    void extraRowsGrowThePanelInsteadOfOverflowing() {
        LinkGuiThemeWindowLayout.Layout layout = LinkGuiThemeWindowLayout.compute(1920.0f, 1080.0f, 9);
        assertEquals(9, layout.cards().size());
        for (LinkGuiLayout.Rect card : layout.cards()) {
            assertTrue(card.bottom() <= layout.panel().bottom());
        }
    }
}
