package com.github.yukulab.blockhideandseekmod.command

import com.github.yukulab.blockhideandseekmod.game.GameState
import com.github.yukulab.blockhideandseekmod.game.TeamSelector
import dev.uten2c.cmdlib.CommandBuilder
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import net.minecraft.world.GameRules

object Start : BHASCommand {
    override val builder: CommandBuilder.() -> Unit = {
        literal("start") {
            requires { it.hasPermissionLevel(it.server.opPermissionLevel) }
            executes {
                if (!isGameRunning(source)) {
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
                    TeamSelector.startVote()
                }
            }
        }
    }

    @JvmStatic
    fun isGameRunning(source: ServerCommandSource): Boolean {
        if (GameState.getCurrentState() != GameState.Phase.IDLE) {
            source.sendError(Text.of("[BHAS] ゲーム進行中は実行できませsん"))
            return true
        }
        return false
    }
}