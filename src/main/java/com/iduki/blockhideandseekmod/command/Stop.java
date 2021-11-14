package com.iduki.blockhideandseekmod.command;

import com.iduki.blockhideandseekmod.game.TeamCreateandDelete;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.server.command.ServerCommandSource;

import static net.minecraft.server.command.CommandManager.literal;

public class Stop {
    public static void registerCommands() {
        CommandRegistrationCallback.EVENT.register(
                (dispatcher, dedicated) ->
                        dispatcher.register(
                                literal("bhas")
                                        .then(literal("stop")
                                                .executes(Stop::stopGame)
                                        )
                        )
        );
    }

    public static int stopGame(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        final ServerCommandSource source = context.getSource();

        TeamCreateandDelete.deleteTeam();
        return 1;
    }
}