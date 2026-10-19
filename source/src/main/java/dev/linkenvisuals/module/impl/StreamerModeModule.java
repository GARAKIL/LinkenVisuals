package dev.linkenvisuals.module.impl;

import dev.linkenvisuals.clickgui.Category;
import dev.linkenvisuals.clickgui.setting.BooleanSetting;
import dev.linkenvisuals.clickgui.setting.Setting;
import dev.linkenvisuals.clickgui.setting.StringSetting;
import dev.linkenvisuals.module.Module;

import java.util.List;

/**
 * StreamerMode (Phase 5.7): hides sensitive info for streamers.
 * Donor's {@code StreamerMode} (73 lines) ported. The 3424-line
 * {@code StreamerModeUtils} pattern table is a follow-up (extract to resource
 * per master prompt).
 */
public final class StreamerModeModule extends Module {
    public final BooleanSetting hideAnarchy;
    public final StringSetting customName;
    public final StringSetting customDonate;
    public final StringSetting customCoins;
    public final StringSetting friendNicknames;

    public StreamerModeModule() {
        this(new BooleanSetting("Hide Anarchy", true),
             new StringSetting("Custom Name", ""),
             new StringSetting("Custom Donate", ""),
             new StringSetting("Custom Coins", ""),
             new StringSetting("Friend Nicknames", ""));
    }

    private StreamerModeModule(BooleanSetting hideAnarchy, StringSetting customName,
                               StringSetting customDonate, StringSetting customCoins,
                               StringSetting friendNicknames) {
        super("streamermode", "Streamer Mode", "hide sensitive info",
            Category.MISC,
            List.of(),
            List.<Setting>of(hideAnarchy, customName, customDonate, customCoins, friendNicknames));
        this.hideAnarchy = hideAnarchy;
        this.customName = customName;
        this.customDonate = customDonate;
        this.customCoins = customCoins;
        this.friendNicknames = friendNicknames;
    }

    public static boolean shouldHideAnarchy() {
        StreamerModeModule m = dev.linkenvisuals.module.Modules.streamerMode();
        return m != null && m.enabled() && m.hideAnarchy.value();
    }
}
