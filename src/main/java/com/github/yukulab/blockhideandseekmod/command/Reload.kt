package com.github.yukulab.blockhideandseekmod.command;

import com.github.yukulab.blockhideandseekmod.BlockHideAndSeekMod;
import com.mojang.brigadier.Command;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.literal;

public class Reload {
    public static void registerCommands() {
        CommandRegistrationCallback.EVENT.register(
                (dispatcher, dedicated) ->
                        dispatcher.register(literal("bhas")
                                .then(literal("reload")
                                        .requires(source -> source.hasPermissionLevel(BlockHideAndSeekMod.SERVER.getOpPermissionLevel()))
                                        .executes(context -> {
                                            var source = context.getSource();
                                            if (Start.isGameRunning(source)) {
                                                source.sendError(Text.of("[Bhas] ゲーム実行中はリロードできません"));
                                                return Command.SINGLE_SUCCESS;
                                            }
                                            BlockHideAndSeekMod.CONFIG.load();
                                            source.sendFeedback(Text.of("[Bhas] 設定ファイルをリロードしました"), true);
                                            return Command.SINGLE_SUCCESS;
                                        })
                                )
                        )
        );
    }
}
