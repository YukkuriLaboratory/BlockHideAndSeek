package com.github.yukulab.blockhideandseekmod.util

import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.server.network.ServerPlayerEntity

object PlayerUtil {
    @JvmStatic
    fun setMaxStamina() {
        server.playerManager.playerList.forEach {
            it.addStatusEffect(
                StatusEffectInstance(
                    StatusEffects.SATURATION, 50, 200, false, false, false
                )
            )
        }
    }

    /**
     * プレイヤーがブロックに埋まっていた場合、上方向に脱出させます
     */
    @JvmStatic
    fun escapeFromBlock(player: ServerPlayerEntity) {
        val world = player.world
        var target = player.blockPos
        var currentState = world.getBlockState(target)
        if (currentState.material.isSolid) {
            do {
                target = target.up(3)
                currentState = world.getBlockState(target)
            } while (currentState.material.isSolid && world.topY >= target.y)
            player.refreshPositionAfterTeleport(target.x.toDouble(), target.y.toDouble(), target.z.toDouble())
        }
    }
}