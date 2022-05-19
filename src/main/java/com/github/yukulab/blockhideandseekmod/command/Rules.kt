package com.github.yukulab.blockhideandseekmod.command

import com.github.yukulab.blockhideandseekmod.item.BhasItems
import dev.uten2c.strobo.command.CommandBuilder
import net.minecraft.item.ItemStack

object Rules : BHASCommand {
    override val builder: CommandBuilder.() -> Unit = {
        literal("rules") {
            executes {
                player.inventory.insertStack(ItemStack(BhasItems.RuleBook))
            }

            players { getPlayer ->
                requires {
                    it.hasPermissionLevel(it.server.opPermissionLevel)
                }
                executes {
                    getPlayer().forEach {
                        it.inventory.insertStack(ItemStack(BhasItems.RuleBook))
                    }
                }
            }
        }
    }
}