package com.github.yukulab.blockhideandseekmod.command

import com.github.yukulab.blockhideandseekmod.BlockHideAndSeekMod
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
                    BlockHideAndSeekMod.CONFIG.load()
                    source.sendFeedback(Text.of("[Bhas] 設定ファイルをリロードしました"), true)
                }
            }
        }
    }
}