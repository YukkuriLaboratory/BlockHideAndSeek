package com.iduki.blockhideandseekmod.command;

import com.iduki.blockhideandseekmod.BlockHideAndSeekMod;
import com.iduki.blockhideandseekmod.game.GameStart;
import com.iduki.blockhideandseekmod.game.PreparationTime;
import com.mojang.brigadier.context.CommandContext;
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
                                                .requires(source -> source.hasPermissionLevel(BlockHideAndSeekMod.SERVER.getOpPermissionLevel()))
                                                .executes(Stop::stopGame)
                                        )
                        )
        );
    }

    public static int stopGame(CommandContext<ServerCommandSource> context) {
        PreparationTime.stopGame();
        GameStart.stopGame();
        return 1;
    }
}