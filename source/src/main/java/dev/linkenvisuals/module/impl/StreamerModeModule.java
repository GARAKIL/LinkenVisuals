package dev.linkenvisuals.module.impl;

import dev.linkenvisuals.clickgui.Category;
import dev.linkenvisuals.clickgui.setting.BooleanSetting;
import dev.linkenvisuals.clickgui.setting.Setting;
import dev.linkenvisuals.clickgui.setting.StringSetting;
import dev.linkenvisuals.module.Module;
import dev.linkenvisuals.module.Modules;
import dev.linkenvisuals.util.text.StreamerRules;
import net.minecraft.client.MinecraftClient;

import java.util.ArrayList;
import java.util.List;

/**
 * StreamerMode (Ф5.7): прячет настоящий ник/донат/монеты и номер анархии/грif-сервера,
 * маскирует ввод парольных команд. Замены применяются к чату, табу, скорборду и
 * actionbar (см. D36); сама логика — {@link StreamerRules} (тестируется без bootstrap).
 * <p>
 * Донор (MC 1.21.4) держал всё в 3048-строчном {@code StreamerModeUtils}; у нас нет его
 * сегментной машинерии, TabProfiler и FriendManager — пары «друг=ник» задаются строкой
 * в настройке Friend Nicknames.
 */
public final class StreamerModeModule extends Module {
    public final BooleanSetting hideAnarchy;
    public final StringSetting customName;
    public final StringSetting customDonate;
    public final StringSetting customCoins;
    public final StringSetting friendNicknames;

    private StreamerRules cachedRules;
    private String cachedSession;
    private String cachedName;
    private String cachedDonate;
    private String cachedCoins;
    private String cachedFriends;
    private boolean cachedHideAnarchy;

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

    /** Правила активного модуля; {@code null} — модуль выключен, вызовы санитайзера ничего не делают. */
    public static StreamerRules rules() {
        StreamerModeModule module = Modules.streamerMode();
        return module == null || !module.enabled() ? null : module.currentRules();
    }

    /** Пересобирает правила только при смене настроек или ника сессии (иначе — кеш). */
    private synchronized StreamerRules currentRules() {
        String session = sessionName();
        if (cachedRules != null
            && session.equals(cachedSession)
            && customName.value().equals(cachedName)
            && customDonate.value().equals(cachedDonate)
            && customCoins.value().equals(cachedCoins)
            && friendNicknames.value().equals(cachedFriends)
            && hideAnarchy.value() == cachedHideAnarchy) {
            return cachedRules;
        }
        List<String> selfNames = new ArrayList<>(2);
        if (!session.isBlank()) {
            selfNames.add(session);
        }
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null && client.player != null && client.player.getGameProfile() != null) {
            String profileName = client.player.getGameProfile().name();
            if (profileName != null && !profileName.isBlank() && !profileName.equalsIgnoreCase(session)) {
                selfNames.add(profileName);
            }
        }
        cachedRules = new StreamerRules(customName.value(), customDonate.value(), customCoins.value(),
            hideAnarchy.value(), selfNames, StreamerRules.parseFriendNicknames(friendNicknames.value()));
        cachedSession = session;
        cachedName = customName.value();
        cachedDonate = customDonate.value();
        cachedCoins = customCoins.value();
        cachedFriends = friendNicknames.value();
        cachedHideAnarchy = hideAnarchy.value();
        return cachedRules;
    }

    private static String sessionName() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.getSession() == null) {
            return "";
        }
        String name = client.getSession().getUsername();
        return name == null ? "" : name;
    }
}
