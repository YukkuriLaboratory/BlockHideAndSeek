package com.github.yukulab.blockhideandseekmod.command;

import com.github.yukulab.blockhideandseekmod.screen.RuleBookScreen;
import com.mojang.brigadier.Command;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;

import static net.minecraft.server.command.CommandManager.literal;

public class Rules {
    public static void registerCommands() {
        CommandRegistrationCallback.EVENT.register(
                (dispatcher, dedicated) ->
                        dispatcher.register(literal("bhas")
                                .then(literal("rules")
                                        .executes(context -> {
                                            var source = context.getSource();
                                            RuleBookScreen.open(source.getPlayer());
                                            return Command.SINGLE_SUCCESS;
                                        })
                                )
                        )
        );
    }
}
