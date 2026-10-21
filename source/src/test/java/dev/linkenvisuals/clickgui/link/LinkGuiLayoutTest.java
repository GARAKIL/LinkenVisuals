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
            assertEquals(1920.0f, metrics.width(), 0.01f, label);
            assertEquals(1080.0f, metrics.height(), 0.01f, label);
            assertTrue(metrics.center().inside(metrics.width(), metrics.height()), label);
            assertTrue(metrics.nav().inside(metrics.width(), metrics.height()), label);
            assertTrue(metrics.profile().inside(metrics.width(), metrics.height()), label);
            assertTrue(metrics.theme().inside(metrics.width(), metrics.height()), label);
            assertFalse(metrics.center().intersects(metrics.profile()), label);
            assertFalse(metrics.center().intersects(metrics.theme()), label);
            assertFalse(metrics.center().intersects(metrics.nav()), label);
            assertFalse(metrics.profile().intersects(metrics.theme()), label);
            assertTrue(metrics.cards().width() > 0.0f, label);
            assertTrue(metrics.settingsContent().width() > 0.0f, label);
        }
    }

    @Test
    void mainWindowAndSidePanelsUseDonorCoordinates() {
        Metrics metrics = LinkGuiLayout.compute(1920, 1080);
        assertEquals(410.0f, metrics.center().x(), 0.01f);
        assertEquals(90.0f, metrics.center().y(), 0.01f);
        assertEquals(1090.0f, metrics.center().width(), 0.01f);
        assertEquals(830.0f, metrics.center().height(), 0.01f);
        assertEquals(1540.0f, metrics.theme().x(), 0.01f);
        assertEquals(340.0f, metrics.theme().width(), 0.01f);
        assertEquals(920.0f, metrics.theme().height(), 0.01f);
        assertEquals(40.0f, metrics.profile().x(), 0.01f);
        assertEquals(330.0f, metrics.profile().width(), 0.01f);
        assertEquals(358.0f, metrics.profile().height(), 0.01f);
        assertEquals(1080.0f - 45.0f - 52.0f, metrics.nav().y(), 0.01f);
    }

    @Test
    void configUsesTheMainModuleWindow() {
        for (int[] resolution : RESOLUTIONS) {
            Metrics metrics = LinkGuiLayout.compute(resolution[0], resolution[1]);
            assertEquals(metrics.center(), metrics.config());
        }
    }

    @Test
    void cardGridUsesThreeColumnsAndStaysInsideTheList() {
        Metrics metrics = LinkGuiLayout.compute(1920, 1080);
        Rect first = LinkGuiLayout.card(metrics, 0);
        Rect second = LinkGuiLayout.card(metrics, 1);
        Rect third = LinkGuiLayout.card(metrics, 2);
        Rect fourth = LinkGuiLayout.card(metrics, 3);

        assertEquals(first.y(), second.y(), 0.01f);
        assertEquals(first.y(), third.y(), 0.01f);
        assertTrue(second.x() > first.right());
        assertTrue(third.x() > second.right());
        assertEquals(first.x(), fourth.x(), 0.01f);
        assertTrue(fourth.y() > first.bottom());
        assertTrue(first.x() >= metrics.cards().x());
        assertTrue(third.right() <= metrics.cards().right());
        assertEquals(102.0f, first.height(), 0.01f);
        assertEquals(12.0f, LinkGuiLayout.CARD_GAP, 0.01f);
    }

    @Test
    void headerAndGridUseDonorGeometry() {
        Metrics metrics = LinkGuiLayout.compute(1920, 1080);
        assertEquals(52.0f, metrics.all().width(), 0.01f);
        assertEquals(34.0f, metrics.all().height(), 0.01f);
        assertEquals(34.0f, metrics.close().width(), 0.01f);
        assertEquals(34.0f, metrics.close().height(), 0.01f);
        assertEquals(metrics.center().right() - 54.0f, metrics.close().x(), 0.01f);
        assertEquals(metrics.center().width() - 40.0f, metrics.cards().width(), 0.01f);
        assertEquals(metrics.center().x() + 20.0f, metrics.cards().x(), 0.01f);
        assertEquals(metrics.center().y() + LinkGuiLayout.HEADER_HEIGHT + 20.0f, metrics.cards().y(), 0.01f);
    }

    @Test
    void cardContentUsesDonorGeometry() {
        Metrics metrics = LinkGuiLayout.compute(1920, 1080);
        Rect card = LinkGuiLayout.card(metrics, 0);
        LinkGuiLayout.CardMetrics cm = LinkGuiLayout.cardMetrics(card);

        assertEquals(17.0f, cm.icon().width(), 0.01f);
        assertEquals(17.0f, cm.icon().height(), 0.01f);
        assertEquals(card.x() + 12.0f, cm.icon().x(), 0.01f);
        assertEquals(card.y() + 11.0f, cm.icon().y(), 0.01f);
        assertEquals(card.x() + 12.0f + 17.0f + 9.0f, cm.title().x(), 0.01f);
        assertEquals(32.0f, cm.toggle().width(), 0.01f);
        assertEquals(18.0f, cm.toggle().height(), 0.01f);
        assertEquals(12.0f, card.right() - cm.toggle().right(), 0.01f);
        assertEquals(12.0f, card.right() - cm.more().right(), 0.01f);
        assertEquals(10.0f, LinkGuiLayout.CARD_RADIUS, 0.01f);
        assertTrue(cm.icon().inside(card.right(), card.bottom()));
        assertTrue(cm.title().inside(card.right(), card.bottom()));
        assertTrue(cm.bind().bottom() <= card.bottom());
        assertFalse(cm.title().intersects(cm.toggle()));
    }

    @Test
    void interfaceScaleIsClampedToTheScreenFit() {
        Metrics normal = LinkGuiLayout.compute(1920, 1080, 1.0f);
        Metrics large = LinkGuiLayout.compute(1920, 1080, 1.5f);
        Metrics uhd = LinkGuiLayout.compute(3840, 2160, 1.5f);

        assertEquals(1056.0f / 1080.0f, normal.scale(), 0.001f);
        assertEquals(1.0f, large.scale(), 0.001f);
        assertEquals(2.0f, uhd.scale(), 0.001f);
        assertTrue(uhd.scale() > normal.scale());
    }

    @Test
    void narrowWindowKeepsTheFixedCanvasUsable() {
        Metrics metrics = LinkGuiLayout.compute(1024, 768);
        assertNotNull(metrics);
        assertEquals(1920.0f, metrics.width(), 0.01f);
        assertEquals(1090.0f, metrics.center().width(), 0.01f);
        assertEquals(330.0f, metrics.profile().width(), 0.01f);
        assertTrue(metrics.scale() > 0.0f);
    }
}
