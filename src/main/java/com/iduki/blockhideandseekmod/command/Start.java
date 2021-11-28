package com.iduki.blockhideandseekmod.command;

import com.iduki.blockhideandseekmod.BlockHideAndSeekMod;
import com.iduki.blockhideandseekmod.game.TeamSelector;
import com.mojang.brigadier.Command;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.text.Text;
import net.minecraft.world.GameRules;

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
                                                    var gameRules = context.getSource().getPlayer().world.getGameRules();
                                                    var keepInventory = gameRules.get(GameRules.KEEP_INVENTORY);
                                                    var naturalRegeneration = gameRules.get(GameRules.NATURAL_REGENERATION);
                                                    if (!keepInventory.get()) {
                                                        keepInventory.set(true, context.getSource().getServer());
                                                        var ruleChangeMessage = Text.of("Info: KeepInventoryを有効化しました");
                                                        context.getSource().sendFeedback(ruleChangeMessage, true);
                                                    }
                                                    if (naturalRegeneration.get()) {
                                                        naturalRegeneration.set(false, context.getSource().getServer());
                                                        var ruleChangeMessage = Text.of("Info: naturalRegenerationを無効化しました");
                                                        context.getSource().sendFeedback(ruleChangeMessage, true);
                                                    }
                                                    TeamSelector.startVote();
                                                    return Command.SINGLE_SUCCESS;
                                                })
                                        )
                        )
        );
    }

}