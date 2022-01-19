package com.github.yukulab.blockhideandseekmod.entity

import com.github.yukulab.blockhideandseekmod.BlockHideAndSeekMod
import net.fabricmc.fabric.api.`object`.builder.v1.entity.FabricEntityTypeBuilder
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityDimensions
import net.minecraft.entity.EntityType
import net.minecraft.entity.SpawnGroup
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry

object BhasEntityTypes {

    @JvmField
    val BLOCKHIGHLIGHT = register("blockhighlight", SpawnGroup.MISC, ::BlockHighlightEntity) {
        dimensions(EntityDimensions.fixed(1f, 1f))
        fireImmune()
        trackRangeBlocks(10)
    }

    private fun <T : Entity> register(
        id: String,
        spawnGroup: SpawnGroup,
        factory: EntityType.EntityFactory<T>,
        builder: FabricEntityTypeBuilder<T>.() -> Unit
    ): EntityType<T> = Registry.register(
        Registry.ENTITY_TYPE,
        Identifier(BlockHideAndSeekMod.MOD_ID, id),
        FabricEntityTypeBuilder.create(spawnGroup, factory).apply(builder).build()
    )

    /**
     * このクラスを呼ぶためだけの子
     */
    @JvmStatic
    fun register() = Unit
}