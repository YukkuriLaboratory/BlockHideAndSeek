package com.iduki.blockhideandseekmod.command;

import com.google.common.collect.Sets;
import com.iduki.blockhideandseekmod.BlockHideAndSeekMod;
import com.iduki.blockhideandseekmod.util.StringSuggestionProvider;
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
        Set<Entry> entries = Sets.newHashSet();
        config.getClusters()
                .forEach(cluster -> entries.addAll(expandEntries(cluster)));
        for (Entry<Object> entry : entries) {
            var fieldName = entry.getId();
            var fieldValue = entry.getValue();

            ArgumentType<?> argumentType;
            if (fieldValue instanceof Integer) {
                argumentType = IntegerArgumentType.integer();
            } else if (fieldValue instanceof Double) {
                argumentType = DoubleArgumentType.doubleArg();
            } else if (fieldValue instanceof Boolean) {
                argumentType = BoolArgumentType.bool();
            } else {
                throw new IllegalArgumentException("config [" + fieldName + "] is not implemented type");
            }
            settings.then(literal(fieldName)
                    .executes(context -> {
                        Field commentField;
                        try {
                            commentField = entry.getClass().getDeclaredField("comment");
                            commentField.setAccessible(true);
                            var result = commentField.get(entry);
                            context.getSource().sendFeedback(Text.of(result.toString()), false);
                        } catch (Throwable throwable) {
                            BlockHideAndSeekMod.LOGGER.throwing(throwable);
                        }
                        return Command.SINGLE_SUCCESS;
                    })
                    .then(argument(fieldName, argumentType)
                            .suggests(new StringSuggestionProvider(fieldValue.toString()))
                            .executes(context -> {
                                try {
                                    var arg = context.getArgument(fieldName, fieldValue.getClass());
                                    if (arg != null) {
                                        entry.setValue(arg);
                                        BlockHideAndSeekMod.CONFIG.save();
                                        context.getSource().sendFeedback(Text.of(fieldName + "を" + arg + "に変更しました"), true);
                                    }
                                } catch (Throwable throwable) {
                                    BlockHideAndSeekMod.LOGGER.throwing(throwable);
                                    context.getSource().sendError(Text.of("エラーが発生しました"));
                                }
                                return Command.SINGLE_SUCCESS;
                            })
                    ));
        }

        return settings;
    }

    @SuppressWarnings("rawtypes")
    private static Collection<Entry> expandEntries(Cluster cluster) {
        var entries = Sets.newHashSet(cluster.getEntries());
        for (Cluster internalCluster : cluster.getClusters()) {
            entries.addAll(expandEntries(internalCluster));
        }
        return entries;
    }

}
