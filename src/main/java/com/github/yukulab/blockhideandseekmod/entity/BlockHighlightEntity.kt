package com.github.yukulab.blockhideandseekmod.entity

import com.github.yukulab.blockhideandseekmod.util.TeamCreateAndDelete
import net.minecraft.entity.EntityType
import net.minecraft.entity.mob.ShulkerEntity
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.world.World

class BlockHighlightEntity(world: World) : ShulkerEntity(EntityType.SHULKER, world), ModifiedTracker {

    init {
        isInvisible = true
        isGlowing = true
    }

    override fun canTrack(player: ServerPlayerEntity): Boolean {
        val team = player.scoreboardTeam
        val seekerTeam = TeamCreateAndDelete.getSeekers()
        return team == null || !team.isEqual(seekerTeam)
    }

    override fun tick() {}

    override fun initGoals() {}

}