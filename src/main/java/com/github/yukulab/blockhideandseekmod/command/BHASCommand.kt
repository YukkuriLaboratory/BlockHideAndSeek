package com.github.yukulab.blockhideandseekmod.command

import dev.uten2c.strobo.command.CommandBuilder

interface BHASCommand {
    val builder: CommandBuilder.() -> Unit
}