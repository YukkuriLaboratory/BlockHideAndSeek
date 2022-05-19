package com.github.yukulab.blockhideandseekmod.command

import com.github.yukulab.blockhideandseekmod.command.BHASCommands.bhasMessage
import com.github.yukulab.blockhideandseekmod.game.GameController
import com.github.yukulab.blockhideandseekmod.game.SelectTeam
import dev.uten2c.strobo.command.CommandBuilder
import net.minecraft.text.LiteralText
import net.minecraft.text.Text

object Team : BHASCommand {
    override val builder: CommandBuilder.() -> Unit = {
        literal("team") {
            executes {
                source.sendFeedback(bhasMessage(Text.of(" チーム選択を行うためのコマンドです.基本的にこのコマンドを入力する必要はありません.")), false)
            }
            val seeker = "seeker"
            val hider = "hider"
            val teams = listOf(seeker, hider)
            teams.forEach {
                literal(it) {
                    executes {
                        val current = GameController.current
                        if (current is SelectTeam) {
                            when (it) {
                                seeker -> {
                                    current.addSeeker(player)
                                    source.sendFeedback(bhasMessage(Text.of(" 鬼陣営に投票しました")), false)
                                }
                                hider -> {
                                    current.addHider(player)
                                    source.sendFeedback(bhasMessage(LiteralText(" ミミック陣営に投票しました")), false)
                                }
                                else -> source.sendError(bhasMessage(Text.of(" 不正な文字列です")))
                            }
                        } else {
                            source.sendError(bhasMessage(Text.of(" 投票時間中のみ有効です")))
                        }
                    }
                }
            }
        }
    }
}