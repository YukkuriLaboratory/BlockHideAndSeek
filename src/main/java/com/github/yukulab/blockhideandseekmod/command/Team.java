package com.github.yukulab.blockhideandseekmod.command;

import com.github.yukulab.blockhideandseekmod.game.TeamSelector;
import com.mojang.brigadier.Command;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.string;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

/**
 * チーム登録用のコマンド
 * 基本的には{@link TeamSelector}のstartVoteメソッドで呼び出される
 * /bhas team [seeker/hider]
 */
public class Team {
    public static void registerCommands() {
        CommandRegistrationCallback.EVENT.register(
                (dispatcher, dedicated) ->
                        dispatcher.register(literal("bhas")
                                .then(literal("team")
                                        //説明用
                                        .executes(context -> {
                                            context.getSource().sendFeedback(Text.of("チーム選択を行うためのコマンドです.基本的にこのコマンドを入力する必要はありません."), false);
                                            return Command.SINGLE_SUCCESS;
                                        })
                                        //振り分け
                                        .then(argument("team", string())
                                                .executes(context -> {
                                                    var arg = getString(context, "team");
                                                    var source = context.getSource();
                                                    var player = source.getPlayer();
                                                    switch (arg) {
                                                        case "seeker" -> {
                                                            if (TeamSelector.addSeeker(player)) {
                                                                source.sendFeedback(new LiteralText("鬼陣営に投票しました"), false);
                                                            } else {
                                                                source.sendError(Text.of("投票時間中のみ有効です"));
                                                            }
                                                        }
                                                        case "hider" -> {
                                                            if (TeamSelector.addHider(player)) {
                                                                source.sendFeedback(new LiteralText("ミミック陣営に投票しました"), false);
                                                            } else {
                                                                source.sendError(Text.of("投票時間中のみ有効です"));
                                                            }
                                                        }
                                                        default -> source.sendError(Text.of("不正な文字列です"));
                                                    }
                                                    return Command.SINGLE_SUCCESS;
                                                })
                                        )
                                )
                        )
        );
    }
}
