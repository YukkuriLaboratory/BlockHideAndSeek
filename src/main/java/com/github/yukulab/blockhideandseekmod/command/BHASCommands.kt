package com.github.yukulab.blockhideandseekmod.command

import dev.uten2c.cmdlib.registerCommand
import net.minecraft.text.LiteralText
import net.minecraft.text.MutableText
import net.minecraft.text.Text

object BHASCommands {
    @JvmStatic
    fun register() {
        registerCommand("bhas") {
            apply(Rules.builder)
            apply(Reload.builder)
            apply(Start.builder)
            apply(Stop.builder)
            apply(Team.builder)
        }
    }

    @JvmStatic
    val GAME_IS_RUNNING: Text = Text.of("[BHAS] ゲーム進行中は実行できません")

    @JvmStatic
    fun bhasMessage(text: Text): MutableText = LiteralText("[BHAS] ").append(text)
    fun infoMessage(text: Text): MutableText = bhasMessage(Text.of("Info:")).append(text)
}