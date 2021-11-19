package com.iduki.blockhideandseekmod.command;

import com.iduki.blockhideandseekmod.game.PreparationTimer;
import com.iduki.blockhideandseekmod.game.TeamCreateandDelete;
import com.iduki.blockhideandseekmod.game.TeamSelector;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.server.command.ServerCommandSource;

import static net.minecraft.server.command.CommandManager.literal;

public class Start {
    public static void registerCommands() {
        CommandRegistrationCallback.EVENT.register(
                (dispatcher, dedicated) ->
                        dispatcher.register(
                                literal("bhas")
                                        .then(literal("start")
//                                                .executes(Start::startGame)
                                                        .executes(context -> {
                                                            TeamSelector.startVote();
                                                            return Command.SINGLE_SUCCESS;
                                                        })
                                        )
                        )
        );
    }

    public static int startGame(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        final ServerCommandSource source = context.getSource();

        //こいつらはチームの作成のみ
        TeamCreateandDelete.addSeeker(source.getPlayer());
        TeamCreateandDelete.addHider(source.getPlayer());
        TeamCreateandDelete.addObserver(source.getPlayer());

        //次の処理でチーム分けブロックの上に乗っているプレイヤーをチームに入れる

        //準備時間の表示と計測
        PreparationTimer.Preparationtime();


        return 1;
    }
}