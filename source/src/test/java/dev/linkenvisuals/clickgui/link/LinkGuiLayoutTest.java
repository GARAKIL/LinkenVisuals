package dev.linkenvisuals.clickgui.link;

import dev.linkenvisuals.clickgui.link.LinkGuiLayout.Metrics;
import dev.linkenvisuals.clickgui.link.LinkGuiLayout.Rect;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LinkGuiLayoutTest {
    private static final int[][] RESOLUTIONS = {
        {1280, 720},
        {1440, 900},
        {1920, 1080},
        {2560, 1440},
        {3440, 1440},
        {3840, 2160}
    };

    @Test
    void donorPanelsStayInsideTheCanvasAndDoNotOverlap() {
        for (int[] resolution : RESOLUTIONS) {
            int width = resolution[0];
            int height = resolution[1];
            Metrics metrics = LinkGuiLayout.compute(width, height);
            String label = width + "x" + height;

            assertTrue(metrics.scale() > 0.0f, label);
            assertTrue(metrics.center().inside(metrics.width(), metrics.height()), label);
            assertTrue(metrics.nav().inside(metrics.width(), metrics.height()), label);
            assertTrue(metrics.settings().inside(metrics.width(), metrics.height()), label);
            assertTrue(metrics.profile().inside(metrics.width(), metrics.height()), label);
            assertTrue(metrics.theme().inside(metrics.width(), metrics.height()), label);
            assertFalse(metrics.center().intersects(metrics.settings()), label);
            assertFalse(metrics.center().intersects(metrics.profile()), label);
            assertFalse(metrics.center().intersects(metrics.theme()), label);
            assertFalse(metrics.center().intersects(metrics.nav()), label);
            assertFalse(metrics.settings().intersects(metrics.profile()), label);
            assertFalse(metrics.settings().intersects(metrics.theme()), label);
            assertFalse(metrics.profile().intersects(metrics.theme()), label);
            assertTrue(metrics.cards().width() > 0.0f, label);
            assertTrue(metrics.settingsContent().width() > 0.0f, label);
        }
    }

    @Test
    void configUsesTheMainModuleWindow() {
        for (int[] resolution : RESOLUTIONS) {
            Metrics metrics = LinkGuiLayout.compute(resolution[0], resolution[1]);
            assertEquals(metrics.center(), metrics.config());
        }
    }

    @Test
    void navigationItemsFitTheHorizontalDock() {
        for (int[] resolution : RESOLUTIONS) {
            Metrics metrics = LinkGuiLayout.compute(resolution[0], resolution[1]);
            for (int i = 0; i < LinkGuiLayout.NAV_ITEMS; i++) {
                Rect item = LinkGuiLayout.navItem(metrics, i);
                assertTrue(item.x() >= metrics.nav().x(), resolution[0] + "x" + resolution[1]);
                assertTrue(item.right() <= metrics.nav().right(), resolution[0] + "x" + resolution[1]);
                assertTrue(item.y() >= metrics.nav().y(), resolution[0] + "x" + resolution[1]);
                assertTrue(item.bottom() <= metrics.nav().bottom(), resolution[0] + "x" + resolution[1]);
            }
        }
    }

    @Test
    void cardGridUsesTwoColumnsAndStaysInsideTheList() {
        Metrics metrics = LinkGuiLayout.compute(1920, 1080);
        Rect first = LinkGuiLayout.card(metrics, 0);
        Rect second = LinkGuiLayout.card(metrics, 1);
        Rect third = LinkGuiLayout.card(metrics, 2);

        assertEquals(first.y(), second.y(), 0.01f);
        assertTrue(second.x() > first.right());
        assertEquals(first.x(), third.x(), 0.01f);
        assertTrue(third.y() > first.bottom());
        assertTrue(first.x() >= metrics.cards().x());
        assertTrue(second.right() <= metrics.cards().right());
    }

    @Test
    void cardContentUsesDonorScaledGeometry() {
        Metrics metrics = LinkGuiLayout.compute(1920, 1080);
        Rect card = LinkGuiLayout.card(metrics, 0);
        LinkGuiLayout.CardMetrics cm = LinkGuiLayout.cardMetrics(card);

        assertEquals(51.0f, cm.badge().width(), 0.01f);
        assertEquals(51.0f, cm.badge().height(), 0.01f);
        assertEquals(81.0f, cm.title().x() - card.x(), 0.01f);
        assertEquals(34.5f, cm.title().y() - card.y(), 0.01f);
        assertEquals(54.0f, cm.toggle().width(), 0.01f);
        assertEquals(27.0f, cm.toggle().height(), 0.01f);
        assertEquals(15.0f, card.right() - cm.toggle().right(), 0.01f);
        assertEquals(69.0f, card.right() - cm.toggle().x(), 0.01f);
        assertEquals(15.0f, LinkGuiLayout.CARD_RADIUS, 0.01f);
        assertTrue(cm.badge().x() >= card.x());
        assertTrue(cm.badge().right() <= card.right());
        assertTrue(cm.badge().y() >= card.y());
        assertTrue(cm.badge().bottom() <= card.bottom());
        assertTrue(cm.bind().bottom() <= card.bottom());
        assertFalse(cm.title().intersects(cm.toggle()));
        assertFalse(cm.description().intersects(cm.toggle()));
    }

    @Test
    void narrowWindowKeepsUsableColumns() {
        Metrics metrics = LinkGuiLayout.compute(1024, 768);
        assertNotNull(metrics);
        assertTrue(metrics.width() >= 1280.0f);
        assertTrue(metrics.center().width() >= 600.0f);
        assertTrue(metrics.settings().width() >= 220.0f);
        assertTrue(metrics.settings().right() <= metrics.width() - LinkGuiLayout.MARGIN + 0.01f);
    }
}
