package com.iduki.blockhideandseekmod.command;

import com.iduki.blockhideandseekmod.BlockHideAndSeekMod;
import com.iduki.blockhideandseekmod.game.TeamSelector;
import com.mojang.brigadier.Command;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;

import static net.minecraft.server.command.CommandManager.literal;

public class Start {
    public static void registerCommands() {
        CommandRegistrationCallback.EVENT.register(
                (dispatcher, dedicated) ->
                        dispatcher.register(
                                literal("bhas")
                                        .then(literal("start")
                                                .requires(source -> source.hasPermissionLevel(BlockHideAndSeekMod.SERVER.getOpPermissionLevel()))
                                                .executes(context -> {
                                                    TeamSelector.startVote();
                                                    return Command.SINGLE_SUCCESS;
                                                })
                                        )
                        )
        );
    }

}