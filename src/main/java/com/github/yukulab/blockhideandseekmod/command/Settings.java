package com.github.yukulab.blockhideandseekmod.command;

import com.github.yukulab.blockhideandseekmod.BlockHideAndSeekMod;
import com.github.yukulab.blockhideandseekmod.game.GameController;
import com.google.common.collect.Sets;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.lortseam.completeconfig.data.Cluster;
import me.lortseam.completeconfig.data.Entry;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import oshi.util.tuples.Pair;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Set;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

//登録だけ
public class Settings {
    public static void registerCommands() {
        CommandRegistrationCallback.EVENT.register(
                (dispatcher, dedicated) ->
                        dispatcher.register(
                                literal("bhas")
                                        .then(registerSettings())
                        )
        );
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static LiteralArgumentBuilder<ServerCommandSource> registerSettings() {
        LiteralArgumentBuilder<ServerCommandSource> settings = LiteralArgumentBuilder.literal("settings");
        settings.requires(source -> source.hasPermissionLevel(source.getServer().getOpPermissionLevel()));
        var config = BlockHideAndSeekMod.CONFIG;
        Set<Pair<Cluster, Entry>> entries = Sets.newHashSet();
        config.getClusters()
                .forEach(cluster -> entries.addAll(expandEntries(cluster)));
        for (Pair<Cluster, Entry> pair : entries) {
            var cluster = pair.getA();
            var clusterName = cluster.getId();
            var entry = pair.getB();
            var fieldName = entry.getId();
            var fieldValue = entry.getValue();

            var targetId = clusterName + "." + fieldName;

            ArgumentType<?> argumentType;
            if (fieldValue instanceof Integer) {
                argumentType = IntegerArgumentType.integer();
            } else if (fieldValue instanceof Double) {
                argumentType = DoubleArgumentType.doubleArg();
            } else if (fieldValue instanceof Boolean) {
                argumentType = BoolArgumentType.bool();
            } else {
                throw new IllegalArgumentException("config [" + targetId + "] is not implemented type");
            }
            settings.then(literal(targetId)
                    .executes(context -> {
                        Field commentField;
                        try {
                            commentField = entry.getClass().getDeclaredField("comment");
                            commentField.setAccessible(true);
                            var result = commentField.get(entry);
                            context.getSource().sendFeedback(Text.of("[BHAS] " + targetId + ": " + result.toString()), false);
                        } catch (Throwable throwable) {
                            BlockHideAndSeekMod.LOGGER.throwing(throwable);
                        }
                        return Command.SINGLE_SUCCESS;
                    })
                    .then(argument(targetId, argumentType)
                            .suggests((content, builder) -> {
                                //常に最新の値を取得するためgetValueしてる
                                builder.suggest(entry.getValue().toString());
                                return builder.buildFuture();
                            })
                            .executes(context -> {
                                if (GameController.isGameRunning()) {
                                    context.getSource().sendError(BHASCommands.getGAME_IS_RUNNING());
                                    return Command.SINGLE_SUCCESS;
                                }
                                try {
                                    var arg = context.getArgument(targetId, fieldValue.getClass());
                                    if (arg != null) {
                                        entry.setValue(arg);
                                        BlockHideAndSeekMod.CONFIG.save();
                                        context.getSource().sendFeedback(Text.of("[BHAS] " + targetId + "を" + arg + "に変更しました"), true);
                                    }
                                } catch (Throwable throwable) {
                                    BlockHideAndSeekMod.LOGGER.throwing(throwable);
                                    context.getSource().sendError(Text.of("[Bhas] エラーが発生しました"));
                                }
                                return Command.SINGLE_SUCCESS;
                            })
                    ));
        }

        return settings;
    }

    @SuppressWarnings("rawtypes")
    private static Collection<Pair<Cluster, Entry>> expandEntries(Cluster cluster) {
        Set<Pair<Cluster, Entry>> entries = Sets.newHashSet();
        cluster.getEntries().forEach(entry -> entries.add(new Pair<>(cluster, entry)));
        for (Cluster internalCluster : cluster.getClusters()) {
            entries.addAll(expandEntries(internalCluster));
        }
        return entries;
    }

}
