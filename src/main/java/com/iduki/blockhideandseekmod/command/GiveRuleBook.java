package com.iduki.blockhideandseekmod.command;

import com.iduki.blockhideandseekmod.BlockHideAndSeekMod;
import com.iduki.blockhideandseekmod.item.BhasItems;
import com.mojang.brigadier.Command;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;

import static net.minecraft.server.command.CommandManager.literal;

public class GiveRuleBook {
    public static void registerCommands() {
        CommandRegistrationCallback.EVENT.register(
                (dispatcher, dedicated) ->
                        dispatcher.register(literal("bhas")
                                .then(literal("rules")
                                        .then(CommandManager.argument("targets", EntityArgumentType.players())
                                                .requires(source -> source.hasPermissionLevel(BlockHideAndSeekMod.SERVER.getOpPermissionLevel()))
                                                .executes(context -> {
                                                            var target = EntityArgumentType.getPlayers(context, "targets");
                                                            target.forEach(
                                                                    player -> player.getInventory().insertStack(new ItemStack(BhasItems.RuleBook)));
                                                            return Command.SINGLE_SUCCESS;
                                                        }
                                                )
                                        )
                                )
                        )
        );
    }
}