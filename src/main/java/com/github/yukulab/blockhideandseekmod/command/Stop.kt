package com.github.yukulab.blockhideandseekmod.command

import com.github.yukulab.blockhideandseekmod.game.GameStart
import com.github.yukulab.blockhideandseekmod.game.GameState
import com.github.yukulab.blockhideandseekmod.game.PreparationTime
import com.github.yukulab.blockhideandseekmod.game.TeamSelector
import dev.uten2c.cmdlib.CommandBuilder
import net.minecraft.network.MessageType
import net.minecraft.text.Text
import java.util.*

object Stop : BHASCommand {
    override val builder: CommandBuilder.() -> Unit = {
        literal("stop") {
            requires { it.hasPermissionLevel(it.server.opPermissionLevel) }
            executes {
                fun sendSuspendMessage() {
                    source.server.playerManager.broadcastChatMessage(
                        Text.of("[BHAS] ゲームが中断されました"),
                        MessageType.CHAT,
                        UUID.randomUUID()
                    )
                }

                when (GameState.getCurrentState()) {
                    GameState.Phase.IDLE -> source.sendError(Text.of("[BHAS] ゲームが開始されていません"))
                    GameState.Phase.SELECT_TEAM -> {
                        TeamSelector.suspend()
                        sendSuspendMessage()
                    }
                    GameState.Phase.PREPARE -> {
                        PreparationTime.stopGame()
                        sendSuspendMessage()
                    }
                    GameState.Phase.RUNNING -> {
                        sendSuspendMessage()
                    }
                    null -> {}
                }
                GameStart.stopGame()
            }
        }
    }

}