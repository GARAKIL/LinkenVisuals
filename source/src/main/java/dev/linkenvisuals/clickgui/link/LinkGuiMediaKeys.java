package dev.linkenvisuals.clickgui.link;

import dev.linkenvisuals.LinkenVisuals;

import java.awt.AWTException;
import java.awt.Robot;

/** Sends the OS media keys so the read-only strip can control the active player. */
public final class LinkGuiMediaKeys {
    private static final int VK_MEDIA_NEXT_TRACK = 0xB0;
    private static final int VK_MEDIA_PREV_TRACK = 0xB1;
    private static final int VK_MEDIA_PLAY_PAUSE = 0xB3;

    private LinkGuiMediaKeys() {
    }

    public static void previous() {
        press(VK_MEDIA_PREV_TRACK);
    }

    public static void next() {
        press(VK_MEDIA_NEXT_TRACK);
    }

    public static void playPause() {
        press(VK_MEDIA_PLAY_PAUSE);
    }

    private static void press(int keyCode) {
        try {
            Robot robot = new Robot();
            robot.keyPress(keyCode);
            robot.keyRelease(keyCode);
        } catch (AWTException | SecurityException | IllegalArgumentException failure) {
            LinkenVisuals.LOGGER.debug("LINKEN media key unavailable: {}", failure.getMessage());
        }
    }
}
