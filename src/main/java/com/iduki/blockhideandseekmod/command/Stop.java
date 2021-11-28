package com.iduki.blockhideandseekmod.command;

import com.iduki.blockhideandseekmod.BlockHideAndSeekMod;
import com.iduki.blockhideandseekmod.game.GameStart;
import com.iduki.blockhideandseekmod.game.GameState;
import com.iduki.blockhideandseekmod.game.PreparationTime;
import com.iduki.blockhideandseekmod.game.TeamSelector;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.network.MessageType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.UUID;

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
        var source = context.getSource();
        switch (GameState.getCurrentState()) {
            case IDLE -> source.sendError(Text.of("[BHAS] ゲームが開始されていません"));
            case SELECT_TEAM -> {
                TeamSelector.suspend();
                broadCastSuspendMessage();
            }
            case PREPARE -> {
                PreparationTime.stopGame();
                broadCastSuspendMessage();
            }
            case RUNNING -> {
                GameStart.stopGame();
                broadCastSuspendMessage();
            }
        }
        return 1;
    }

    private static void broadCastSuspendMessage() {
        BlockHideAndSeekMod.SERVER.getPlayerManager().broadcastChatMessage(Text.of("[BHAS] ゲームが中断されました"), MessageType.CHAT, UUID.randomUUID());
    }

}