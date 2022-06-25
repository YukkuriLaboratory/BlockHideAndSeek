package com.github.yukulab.blockhideandseekmod.command

import com.github.yukulab.blockhideandseekmod.BlockHideAndSeekMod
import com.github.yukulab.blockhideandseekmod.command.BHASCommands.infoMessage
import com.github.yukulab.blockhideandseekmod.data.DataIO
import dev.uten2c.strobo.command.CommandBuilder
import dev.uten2c.strobo.command.CommandContext

object Restore : BHASCommand {
    override val builder: CommandBuilder.() -> Unit = {
        literal("restore") {
            requires { it.hasPermissionLevel(it.server.opPermissionLevel) }

            fun CommandContext.restore(force: Boolean = false) {
                sendFeedback(infoMessage(dev.uten2c.strobo.util.text("復元中です...")))
                DataIO.readAndApply(force).onSuccess {
                    sendFeedback(infoMessage(dev.uten2c.strobo.util.text("復元しました")))
                }.onFailure {
                    sendError(infoMessage(dev.uten2c.strobo.util.text("復元に失敗しました.詳細はコンソールをご確認ください")))
                    BlockHideAndSeekMod.LOGGER.throwing(it)
                }
            }
            executes {
                restore()
            }
            boolean { force ->
                executes {
                    restore(force())
                }
            }
        }
    }
}