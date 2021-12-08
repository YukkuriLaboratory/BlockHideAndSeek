package com.github.yukulab.blockhideandseekmod.util

import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffects

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
}