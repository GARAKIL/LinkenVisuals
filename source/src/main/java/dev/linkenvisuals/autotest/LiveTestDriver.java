package dev.linkenvisuals.autotest;

import dev.linkenvisuals.clickgui.ModuleRegistry;
import dev.linkenvisuals.module.Modules;
import dev.linkenvisuals.module.impl.ItemRadiusModule;
import dev.linkenvisuals.render.ItemRadiusRenderer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

/**
 * Manual-server smoke test used by {@code runLiveTest}: joins a real Paper
 * server, waits for the {@code SecondPlayer} bot, then captures the two live
 * cases that cannot be proven in singleplayer: the tab list with a team prefix
 * and the splash proximity label over a real remote player.
 *
 * <p>The bot is a real protocol client (mineflayer), not a second human; the
 * output report must say so explicitly. The test restores every module/window
 * setting it changes before quitting, so a developer run does not pollute the
 * normal config.
 */
public final class LiveTestDriver {
    private static final String PROPERTY = "linkenvisuals.livetest";
    private static final String BOT_NAME = "SecondPlayer";
    private static final String PREFIX = "[MVP]";
    private static final int SETTLE_TICKS = 140;

    private static boolean registered;
    private static boolean finished;
    private static boolean stateSaved;
    private static int ticks;
    private static int phase;
    private static int phaseTicks;
    private static String failure;

    private static int previousWidth;
    private static int previousHeight;
    private static int previousGuiScale;
    private static Perspective previousPerspective;
    private static boolean streamerWasEnabled;
    private static boolean selfNametagsWereEnabled;
    private static boolean nameTagsWereEnabled;
    private static boolean itemRadiusWasEnabled;
    private static boolean splashWasEnabled;
    private static float splashRadius;
    private static String previousCustomName;
    private static ItemStack previousMainHand;

    private LiveTestDriver() {
    }

    public static void register() {
        if (!Boolean.getBoolean(PROPERTY) || registered) {
            return;
        }
        registered = true;
        ClientTickEvents.END_CLIENT_TICK.register(LiveTestDriver::tick);
    }

    private static void tick(MinecraftClient client) {
        if (finished || client.player == null || client.world == null) {
            return;
        }
        ticks++;
        if (ticks < 60) {
            return;
        }
        if (client.currentScreen != null) {
            client.setScreen(null);
        }
        PlayerEntity bot = remoteBot(client);
        switch (phase) {
            case 0 -> {
                if (bot == null) {
                    if (ticks > 900) {
                        fail(client, "SecondPlayer did not appear in the remote player list");
                    }
                    return;
                }
                saveState(client);
                // A 1280x720 GUI-scale-1 frame keeps the tab list clear of the
                // watermark and matches the documentation screenshot size.
                client.getWindow().setWindowedSize(1280, 720);
                if (client.options.getGuiScale().getValue() != 1) {
                    client.options.getGuiScale().setValue(1);
                }
                client.onResolutionChanged();
                Modules.streamerMode().customName.setValue("Hidden");
                Modules.streamerMode().info().toggleSilently(true);
                Modules.selfNametags().info().toggleSilently(true);
                ModuleRegistry.nameTags().toggleSilently(false);
                phase = 1;
                phaseTicks = 0;
            }
            case 1 -> {
                // The "chat messages can't be verified" toast lives ~5 s and would
                // cover the centre of the tab list; let it expire first.
                phaseTicks++;
                if (phaseTicks < SETTLE_TICKS) {
                    return;
                }
                String display = client.player.getDisplayName().getString();
                if (!display.contains("Hidden")) {
                    fail(client, "self display name is not sanitized: " + display);
                    return;
                }
                // The dev-run nickname changes between launches (Player288, Player446, ...),
                // so server-side team membership is proven on the stable SecondPlayer bot.
                String botDisplay = bot.getDisplayName().getString();
                if (!botDisplay.contains(PREFIX)) {
                    fail(client, "team prefix missing from SecondPlayer: " + botDisplay);
                    return;
                }
                ItemRadiusModule radius = Modules.itemRadius();
                if (radius == null) {
                    fail(client, "ItemRadius module is unavailable");
                    return;
                }
                radius.info().toggleSilently(true);
                radius.explosivePotion.setValue(true);
                radius.explosivePotionRadius.setValue(30.0f);
                if (!client.player.getMainHandStack().isOf(Items.SPLASH_POTION)) {
                    client.player.setStackInHand(net.minecraft.util.Hand.MAIN_HAND,
                        new ItemStack(Items.SPLASH_POTION));
                }
                lookAt(client, bot.getEyePos());
                phase = 2;
                phaseTicks = 0;
            }
            case 2 -> {
                phaseTicks++;
                if (phaseTicks < 25) {
                    return;
                }
                if (!ItemRadiusRenderer.lastLabelVisible()) {
                    fail(client, "splash proximity label did not render for SecondPlayer");
                    return;
                }
                screenshot(client, "item-radius-live");
                phase = 3;
                phaseTicks = 0;
            }
            case 3 -> {
                phaseTicks++;
                client.options.playerListKey.setPressed(true);
                if (phaseTicks < 30) {
                    return;
                }
                boolean listed = false;
                if (client.getNetworkHandler() != null) {
                    for (PlayerListEntry entry : client.getNetworkHandler().getPlayerList()) {
                        if (entry.getProfile().name().equals(BOT_NAME)) {
                            listed = true;
                            break;
                        }
                    }
                }
                if (!listed) {
                    fail(client, "SecondPlayer missing from tab/player list");
                    return;
                }
                if (!client.options.playerListKey.isPressed()) {
                    fail(client, "tab key state was not held for the screenshot");
                    return;
                }
                screenshot(client, "tab-live");
                client.options.playerListKey.setPressed(false);
                finish(client, "live checks passed: " + displaySummary(client));
            }
            default -> {
            }
        }
    }

    private static PlayerEntity remoteBot(MinecraftClient client) {
        for (PlayerEntity player : client.world.getPlayers()) {
            if (player != client.player && BOT_NAME.equals(player.getName().getString())) {
                return player;
            }
        }
        return null;
    }

    private static String displaySummary(MinecraftClient client) {
        PlayerEntity bot = remoteBot(client);
        return "self=" + (client.player == null ? "-" : client.player.getDisplayName().getString())
            + ", botDistance=" + (bot == null ? "-" : String.format(java.util.Locale.ROOT, "%.1f",
                Math.sqrt(bot.squaredDistanceTo(client.player))));
    }

    private static void saveState(MinecraftClient client) {
        if (stateSaved) {
            return;
        }
        stateSaved = true;
        previousWidth = client.getWindow().getWidth();
        previousHeight = client.getWindow().getHeight();
        previousGuiScale = client.options.getGuiScale().getValue();
        previousPerspective = client.options.getPerspective();
        streamerWasEnabled = Modules.streamerMode().info().enabled();
        previousCustomName = Modules.streamerMode().customName.value();
        selfNametagsWereEnabled = Modules.selfNametags().info().enabled();
        nameTagsWereEnabled = ModuleRegistry.nameTags().enabled();
        ItemRadiusModule radius = Modules.itemRadius();
        itemRadiusWasEnabled = radius.info().enabled();
        splashWasEnabled = radius.explosivePotion.value();
        splashRadius = radius.explosivePotionRadius.value();
        previousMainHand = client.player == null ? ItemStack.EMPTY : client.player.getMainHandStack().copy();
    }

    private static void restoreState(MinecraftClient client) {
        if (!stateSaved) {
            return;
        }
        stateSaved = false;
        client.options.playerListKey.setPressed(false);
        Modules.streamerMode().info().toggleSilently(streamerWasEnabled);
        Modules.streamerMode().customName.setValue(previousCustomName);
        Modules.selfNametags().info().toggleSilently(selfNametagsWereEnabled);
        ModuleRegistry.nameTags().toggleSilently(nameTagsWereEnabled);
        ItemRadiusModule radius = Modules.itemRadius();
        if (radius != null) {
            radius.info().toggleSilently(itemRadiusWasEnabled);
            radius.explosivePotion.setValue(splashWasEnabled);
            radius.explosivePotionRadius.setValue(splashRadius);
        }
        if (client.player != null && previousMainHand != null) {
            client.player.setStackInHand(net.minecraft.util.Hand.MAIN_HAND, previousMainHand.copy());
        }
        client.options.setPerspective(previousPerspective);
        client.options.getGuiScale().setValue(previousGuiScale);
        client.getWindow().setWindowedSize(previousWidth, previousHeight);
        client.onResolutionChanged();
    }

    private static void lookAt(MinecraftClient client, Vec3d target) {
        if (client.player == null) {
            return;
        }
        Vec3d delta = target.subtract(client.player.getEyePos());
        double horizontal = Math.sqrt(delta.x * delta.x + delta.z * delta.z);
        client.player.setYaw((float) Math.toDegrees(Math.atan2(-delta.x, delta.z)));
        client.player.setPitch((float) -Math.toDegrees(Math.atan2(delta.y, horizontal)));
    }

    private static void screenshot(MinecraftClient client, String label) {
        String name = "lv-live-" + label + ".png";
        ScreenshotRecorder.saveScreenshot(client.runDirectory, name, client.getFramebuffer(), 1,
            message -> {
            });
    }

    private static void fail(MinecraftClient client, String message) {
        failure = message;
        client.inGameHud.getChatHud().addMessage(Text.literal("[LVLive] FAIL " + message));
        finish(client, "live checks failed: " + message);
    }

    private static void finish(MinecraftClient client, String message) {
        if (finished) {
            return;
        }
        finished = true;
        restoreState(client);
        client.inGameHud.getChatHud().addMessage(Text.literal("[LVLive] " + message));
        dev.linkenvisuals.LinkenVisuals.LOGGER.info("[LVLive] {}", message);
        new Thread(() -> {
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            client.scheduleStop();
        }, "lv-live-quit").start();
    }

    public static String failure() {
        return failure;
    }
}
