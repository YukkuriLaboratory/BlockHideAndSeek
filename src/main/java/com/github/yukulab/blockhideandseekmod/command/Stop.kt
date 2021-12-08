package com.github.yukulab.blockhideandseekmod.command

import com.github.yukulab.blockhideandseekmod.game.GameController
import dev.uten2c.cmdlib.CommandBuilder
import kotlinx.coroutines.runBlocking
import net.minecraft.network.MessageType
import net.minecraft.text.Text
import java.util.*

object Stop : BHASCommand {
    override val builder: CommandBuilder.() -> Unit = {
        literal("stop") {
            requires { it.hasPermissionLevel(it.server.opPermissionLevel) }
            executes {
                runBlocking {
                    if (GameController.suspend()) {
                        source.server.playerManager.broadcastChatMessage(
                            Text.of("[BHAS] ゲームが中断されました"),
                            MessageType.CHAT,
                            UUID.randomUUID()
                        )
                    } else {
                        source.sendError(Text.of("[BHAS] ゲームが開始されていません"))
                    }
                }
            }
        }
    }

}