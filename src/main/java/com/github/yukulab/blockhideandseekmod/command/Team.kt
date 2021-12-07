package com.github.yukulab.blockhideandseekmod.command

import com.github.yukulab.blockhideandseekmod.game.TeamSelector
import dev.uten2c.cmdlib.CommandBuilder
import net.minecraft.text.LiteralText
import net.minecraft.text.Text

object Team : BHASCommand {
    override val builder: CommandBuilder.() -> Unit = {
        literal("team") {
            executes {
                source.sendFeedback(Text.of("チーム選択を行うためのコマンドです.基本的にこのコマンドを入力する必要はありません."), false)
            }
            val seeker = "seeker"
            val hider = "hider"
            val teams = listOf(seeker, hider)
            teams.forEach {
                literal(it) {
                    executes {
                        when (it) {
                            seeker -> {
                                if (TeamSelector.addSeeker(player)) {
                                    source.sendFeedback(Text.of("鬼陣営に投票しました"), false)
                                } else {
                                    source.sendError(Text.of("投票時間中のみ有効です"))
                                }
                            }
                            hider -> {
                                if (TeamSelector.addHider(player)) {
                                    source.sendFeedback(LiteralText("ミミック陣営に投票しました"), false)
                                } else {
                                    source.sendError(Text.of("投票時間中のみ有効です"))
                                }
                            }
                            else -> source.sendError(Text.of("不正な文字列です"))
                        }
                    }
                }
            }
        }
    }
}