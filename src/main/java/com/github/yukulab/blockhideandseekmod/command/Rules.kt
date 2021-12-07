package com.github.yukulab.blockhideandseekmod.command

import com.github.yukulab.blockhideandseekmod.item.BhasItems
import dev.uten2c.cmdlib.CommandBuilder
import net.minecraft.item.ItemStack

object Rules : BHASCommand {
    private const val targets = "targets"

    override val builder: CommandBuilder.() -> Unit = {
        literal("rules") {
            executes {
                player.inventory.insertStack(ItemStack(BhasItems.RuleBook))
            }

            players(targets) {
                requires {
                    it.hasPermissionLevel(it.server.opPermissionLevel)
                }
                executes {
                    getPlayers(targets).forEach {
                        it.inventory.insertStack(ItemStack(BhasItems.RuleBook))
                    }
                }
            }
        }
    }
}