package com.iduki.blockhideandseekmod.command;

import com.iduki.blockhideandseekmod.BlockHideAndSeekMod;
import com.iduki.blockhideandseekmod.game.GameStart;
import com.iduki.blockhideandseekmod.game.PreparationTime;
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
                                        .requires(source -> source.hasPermissionLevel(BlockHideAndSeekMod.SERVER.getOpPermissionLevel()))
                                        .then(literal("stop")
                                                .executes(Stop::stopGame)
                                        )
                        )
        );
    }

    public static int stopGame(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        PreparationTime.stopGame();
        GameStart.stopGame();
        return 1;
    }
}