package dev.linkenvisuals.autotest;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

/**
 * Client command {@code /lvtest} (alias {@code /lvautotest}) for the in-game
 * smoke test. Runs purely client-side and never needs server-side registration.
 */
public final class AutoTestCommand {
    private AutoTestCommand() {
    }

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, access) -> {
            dispatcher.register(build("lvtest"));
            dispatcher.register(build("lvautotest"));
        });
    }

    private static LiteralArgumentBuilder<FabricClientCommandSource> build(String name) {
        return ClientCommandManager.literal(name)
            .executes(context -> status(context.getSource()))
            .then(ClientCommandManager.literal("start")
                .executes(context -> {
                    String message = AutoTestDriver.start(MinecraftClient.getInstance());
                    context.getSource().sendFeedback(Text.literal("[LVTest] " + message));
                    return 1;
                }))
            .then(ClientCommandManager.literal("stop")
                .executes(context -> {
                    String message = AutoTestDriver.stop("остановлено командой");
                    context.getSource().sendFeedback(Text.literal("[LVTest] " + message));
                    return 1;
                }))
            .then(ClientCommandManager.literal("status")
                .executes(context -> status(context.getSource())));
    }

    private static int status(FabricClientCommandSource source) {
        source.sendFeedback(Text.literal("[LVTest] " + AutoTestDriver.statusLine()));
        return 1;
    }
}
