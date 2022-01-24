package com.github.yukulab.blockhideandseekmod.command

import com.github.yukulab.blockhideandseekmod.command.BHASCommands.bhasMessage
import com.github.yukulab.blockhideandseekmod.config.Config
import com.github.yukulab.blockhideandseekmod.game.GameController
import dev.uten2c.cmdlib.CommandBuilder
import net.minecraft.text.Text

object Reload : BHASCommand {

    override val builder: CommandBuilder.() -> Unit = {
        literal("reload") {
            requires {
                it.hasPermissionLevel(it.server.opPermissionLevel)
            }

            executes {
                if (!GameController.isGameRunning) {
                    Config.reload()
                    source.sendFeedback(bhasMessage(Text.of("設定ファイルをリロードしました")), true)
                }
            }
        }
    }
}