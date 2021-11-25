package com.iduki.blockhideandseekmod.command;

import com.iduki.blockhideandseekmod.BlockHideAndSeekMod;
import com.mojang.brigadier.Command;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;

import static net.minecraft.server.command.CommandManager.literal;

public class Reload {
    public static void registerCommands() {
        CommandRegistrationCallback.EVENT.register(
                (dispatcher, dedicated) ->
                        dispatcher.register(literal("bhas")
                                .then(literal("reload")
                                        .executes(context -> {
                                            BlockHideAndSeekMod.CONFIG.load();
                                            return Command.SINGLE_SUCCESS;
                                        })
                                )
                        )
        );
    }
}
