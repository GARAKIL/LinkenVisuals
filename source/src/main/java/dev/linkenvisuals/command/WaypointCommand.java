package dev.linkenvisuals.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.linkenvisuals.waypoint.Waypoint;
import dev.linkenvisuals.waypoint.WaypointStorage;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

/**
 * Client command {@code /way} (alias {@code /waypoint}), a port of the donor's
 * {@code commands.impl.WaypointCommand}: {@code add <name> me}, {@code add <name> <x> <y> <z>},
 * {@code remove <name>}, {@code list}, {@code clear}.
 * <p>
 * Registered through the Fabric client-command API (part of fabric-api): commands run
 * client-side and are never sent to the server. Multi-line donor UI cards are replaced
 * by plain translatable chat feedback (D37).
 */
public final class WaypointCommand {
    private WaypointCommand() {
    }

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, access) -> {
            dispatcher.register(build("way"));
            dispatcher.register(build("waypoint"));
        });
    }

    private static LiteralArgumentBuilder<FabricClientCommandSource> build(String name) {
        return ClientCommandManager.literal(name)
            .executes(context -> {
                context.getSource().sendFeedback(Text.translatable("linkenvisuals.command.way.usage"));
                return 1;
            })
            .then(ClientCommandManager.literal("add")
                .then(ClientCommandManager.argument("name", StringArgumentType.string())
                    .then(ClientCommandManager.literal("me")
                        .executes(context -> addHere(context.getSource(),
                            StringArgumentType.getString(context, "name"))))
                    .then(ClientCommandManager.argument("x", IntegerArgumentType.integer())
                        .then(ClientCommandManager.argument("y", IntegerArgumentType.integer())
                            .then(ClientCommandManager.argument("z", IntegerArgumentType.integer())
                                .executes(context -> addAt(context.getSource(),
                                    StringArgumentType.getString(context, "name"),
                                    IntegerArgumentType.getInteger(context, "x"),
                                    IntegerArgumentType.getInteger(context, "y"),
                                    IntegerArgumentType.getInteger(context, "z"))))))))
            .then(ClientCommandManager.literal("remove")
                .then(ClientCommandManager.argument("name", StringArgumentType.greedyString())
                    .executes(context -> remove(context.getSource(),
                        StringArgumentType.getString(context, "name")))))
            .then(ClientCommandManager.literal("list")
                .executes(context -> {
                    list(context.getSource());
                    return 1;
                }))
            .then(ClientCommandManager.literal("clear")
                .executes(context -> {
                    clear(context.getSource());
                    return 1;
                }));
    }

    private static int addAt(FabricClientCommandSource source, String name, int x, int y, int z) {
        String trimmed = name.trim();
        if (trimmed.isEmpty()) {
            source.sendError(Text.translatable("linkenvisuals.command.way.blank"));
            return 0;
        }
        if (!WaypointStorage.store().add(trimmed, x, y, z)) {
            source.sendError(Text.translatable("linkenvisuals.command.way.already", trimmed));
            return 0;
        }
        WaypointStorage.save();
        source.sendFeedback(Text.translatable("linkenvisuals.command.way.added", trimmed, x, y, z));
        return 1;
    }

    private static int addHere(FabricClientCommandSource source, String name) {
        MinecraftClient client = source.getClient();
        if (client.player == null) {
            source.sendError(Text.translatable("linkenvisuals.command.way.no_player"));
            return 0;
        }
        BlockPos pos = client.player.getBlockPos();
        return addAt(source, name, pos.getX(), pos.getY(), pos.getZ());
    }

    private static int remove(FabricClientCommandSource source, String name) {
        String trimmed = name.trim();
        if (!WaypointStorage.store().remove(trimmed)) {
            source.sendError(Text.translatable("linkenvisuals.command.way.notfound", trimmed));
            return 0;
        }
        WaypointStorage.save();
        source.sendFeedback(Text.translatable("linkenvisuals.command.way.removed", trimmed));
        return 1;
    }

    private static void list(FabricClientCommandSource source) {
        if (WaypointStorage.store().isEmpty()) {
            source.sendFeedback(Text.translatable("linkenvisuals.command.way.empty"));
            return;
        }
        for (Waypoint waypoint : WaypointStorage.store().sorted()) {
            source.sendFeedback(Text.translatable("linkenvisuals.command.way.list_entry",
                waypoint.name(), (int) waypoint.x(), (int) waypoint.y(), (int) waypoint.z()));
        }
    }

    private static void clear(FabricClientCommandSource source) {
        if (WaypointStorage.store().isEmpty()) {
            source.sendFeedback(Text.translatable("linkenvisuals.command.way.empty"));
            return;
        }
        WaypointStorage.store().clear();
        WaypointStorage.save();
        source.sendFeedback(Text.translatable("linkenvisuals.command.way.cleared"));
    }
}
