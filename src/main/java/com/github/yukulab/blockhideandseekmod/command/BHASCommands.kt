package com.github.yukulab.blockhideandseekmod.command

import dev.uten2c.cmdlib.registerCommand

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

}