package com.github.yukulab.blockhideandseekmod.command

import com.github.yukulab.blockhideandseekmod.command.BHASCommands.GAME_IS_RUNNING
import com.github.yukulab.blockhideandseekmod.game.GameController
import dev.uten2c.cmdlib.CommandBuilder
import net.minecraft.text.Text
import net.minecraft.world.GameRules

object Start : BHASCommand {
    override val builder: CommandBuilder.() -> Unit = {
        literal("start") {
            requires { it.hasPermissionLevel(it.server.opPermissionLevel) }
            executes {
                if (GameController.startGame()) {
                    val gameRules = source.player.world.gameRules
                    val keepInventory = gameRules.get(GameRules.KEEP_INVENTORY)
                    if (!keepInventory.get()) {
                        keepInventory.set(true, source.server)
                        val ruleChangeMessage = Text.of("Info: KeepInventoryを有効化しました")
                        source.sendFeedback(ruleChangeMessage, true)
                    }

                    val naturalRegeneration = gameRules.get(GameRules.NATURAL_REGENERATION)
                    if (!naturalRegeneration.get()) {
                        naturalRegeneration.set(false, source.server)
                        val ruleChangeMessage = Text.of("Info: naturalRegenerationを無効化しました")
                        source.sendFeedback(ruleChangeMessage, true)
                    }
                } else {
                    source.sendError(GAME_IS_RUNNING)
                }
            }
        }
    }
}