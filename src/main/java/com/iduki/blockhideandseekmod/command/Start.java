package com.iduki.blockhideandseekmod.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.server.command.ServerCommandSource;

import static net.minecraft.server.command.CommandManager.literal;

public class Start {
    public static void registerCommands() {
        CommandRegistrationCallback.EVENT.register(
                (dispatcher, dedicated) ->
                        dispatcher.register(
                                literal("bhas")
                                        .then(literal("start")
                                                .executes(context -> startGame(context))
                                        )
                        )
        );
    }

    public static int startGame(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        final ServerCommandSource source = context.getSource();

        return 1;
    }
}