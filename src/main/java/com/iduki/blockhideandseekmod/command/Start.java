package com.iduki.blockhideandseekmod.command;

import com.iduki.blockhideandseekmod.BlockHideAndSeekMod;
import com.iduki.blockhideandseekmod.game.GameState;
import com.iduki.blockhideandseekmod.game.TeamSelector;
import com.mojang.brigadier.Command;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.server.command.ServerCommandSource;
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
                                                    if (isGameRunning(context.getSource())) {
                                                        return Command.SINGLE_SUCCESS;
                                                    }
                                                    var gameRules = context.getSource().getPlayer().world.getGameRules();
                                                    var keepInventory = gameRules.get(GameRules.KEEP_INVENTORY);
                                                    if (!keepInventory.get()) {
                                                        keepInventory.set(true, context.getSource().getServer());
                                                        var ruleChangeMessage = Text.of("Info: KeepInventoryを有効化しました");
                                                        context.getSource().sendFeedback(ruleChangeMessage, true);
                                                    }
                                                    TeamSelector.startVote();
                                                    return Command.SINGLE_SUCCESS;
                                                })
                                        )
                        )
        );
    }

    public static boolean isGameRunning(ServerCommandSource source) {
        if (GameState.getCurrentState() != GameState.Phase.IDLE) {
            source.sendError(Text.of("[BHAS] ゲーム進行中は実行できませsん"));
            return true;
        }
        return false;
    }

}