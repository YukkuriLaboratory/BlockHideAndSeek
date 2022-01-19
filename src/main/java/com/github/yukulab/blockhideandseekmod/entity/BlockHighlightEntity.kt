package com.github.yukulab.blockhideandseekmod.entity

import com.github.yukulab.blockhideandseekmod.util.TeamCreateAndDelete
import net.minecraft.entity.EntityType
import net.minecraft.entity.mob.ShulkerEntity
import net.minecraft.network.Packet
import net.minecraft.network.packet.s2c.play.MobSpawnS2CPacket
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.registry.Registry
import net.minecraft.world.World

class BlockHighlightEntity(type: EntityType<ShulkerEntity>, world: World) : ShulkerEntity(type, world),
    ModifiedTracker {

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

    override fun createSpawnPacket(): Packet<*> = MobSpawnS2CPacket(this).apply {
        entityTypeId = Registry.ENTITY_TYPE.getRawId(EntityType.SHULKER)
    }
}