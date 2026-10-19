package dev.linkenvisuals.module.impl;

import dev.linkenvisuals.clickgui.Category;
import dev.linkenvisuals.clickgui.setting.BooleanSetting;
import dev.linkenvisuals.clickgui.setting.Setting;
import dev.linkenvisuals.module.Module;

import java.util.List;

/**
 * ChatHelper (Phase 5.7): chat history, anti-spam, timestamps.
 * Donor's {@code ChatHelper} (45 lines) ported.
 */
public final class ChatHelperModule extends Module {
    public final BooleanSetting chatHistory;
    public final BooleanSetting antiSpam;
    public final BooleanSetting showTime;

    public ChatHelperModule() {
        this(new BooleanSetting("Chat History", true),
             new BooleanSetting("Anti Spam", true),
             new BooleanSetting("Show Time", false));
    }

    private ChatHelperModule(BooleanSetting chatHistory, BooleanSetting antiSpam, BooleanSetting showTime) {
        super("chathelper", "Chat Helper", "chat improvements",
            Category.PLAYER,
            List.of(),
            List.<Setting>of(chatHistory, antiSpam, showTime));
        this.chatHistory = chatHistory;
        this.antiSpam = antiSpam;
        this.showTime = showTime;
    }

    public static boolean isChatHistoryEnabled() {
        ChatHelperModule module = dev.linkenvisuals.module.Modules.chatHelper();
        return module != null && module.enabled() && module.chatHistory.value();
    }

    public static boolean isAntiSpamEnabled() {
        ChatHelperModule module = dev.linkenvisuals.module.Modules.chatHelper();
        return module != null && module.enabled() && module.antiSpam.value();
    }

    public static boolean isShowTimeEnabled() {
        ChatHelperModule module = dev.linkenvisuals.module.Modules.chatHelper();
        return module != null && module.enabled() && module.showTime.value();
    }
}
