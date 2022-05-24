package com.github.yukulab.blockhideandseekmod.util

import net.minecraft.block.BlockState
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.function.BooleanBiFunction
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.shape.VoxelShapes
import net.minecraft.world.World

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
        val boxWidth = player.width * 0.8f
        var hitBox = Box.of(player.eyePos, boxWidth.toDouble(), 1.0E-6, boxWidth.toDouble())
        if (isInsideWall(hitBox, currentState, world)) {
            do {
                target = target.up(3)
                hitBox = hitBox.offset(0.0, target.y.toDouble(), 0.0)
                currentState = world.getBlockState(target)
            } while (isInsideWall(hitBox, currentState, world) && world.topY >= target.y)
            player.refreshPositionAfterTeleport(target.x.toDouble(), target.y.toDouble(), target.z.toDouble())
        }
    }

    private fun isInsideWall(hitBox: Box, blockState: BlockState, world: World): Boolean =
        BlockPos.stream(hitBox).anyMatch {
            !blockState.isAir && blockState.shouldSuffocate(world, it) && VoxelShapes.matchesAnywhere(
                blockState.getCollisionShape(world, it)
                    .offset(it.x.toDouble(), it.y.toDouble(), it.z.toDouble()),
                VoxelShapes.cuboid(hitBox),
                BooleanBiFunction.AND
            )
        }
}