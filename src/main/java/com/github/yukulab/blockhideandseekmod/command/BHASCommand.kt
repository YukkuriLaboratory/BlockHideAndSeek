package com.github.yukulab.blockhideandseekmod.command

import dev.uten2c.cmdlib.CommandBuilder

interface BHASCommand {
    val builder: CommandBuilder.() -> Unit
}