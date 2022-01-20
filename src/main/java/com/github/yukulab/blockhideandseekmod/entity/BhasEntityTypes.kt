package com.github.yukulab.blockhideandseekmod.entity

import com.github.yukulab.blockhideandseekmod.BlockHideAndSeekMod
import net.fabricmc.fabric.api.`object`.builder.v1.entity.FabricDefaultAttributeRegistry
import net.fabricmc.fabric.api.`object`.builder.v1.entity.FabricEntityTypeBuilder
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityDimensions
import net.minecraft.entity.EntityType
import net.minecraft.entity.SpawnGroup
import net.minecraft.entity.mob.ShulkerEntity
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry

object BhasEntityTypes {

    @JvmField
    val BLOCKHIGHLIGHT = register("blockhighlight", SpawnGroup.MISC, ::BlockHighlightEntity) {
        dimensions(EntityDimensions.fixed(1f, 1f))
        fireImmune()
        trackRangeBlocks(10)
    }

    @JvmField
    val DECOY = register("decoy", SpawnGroup.MISC, ::DecoyEntity){
        dimensions(EntityDimensions.fixed(1f,1f))
        fireImmune()
        trackRangeBlocks(10)
    }

    /**
     * たしかにこのエラーは納得なんですが，なるべくあとから別の場所でも使えるような形で書きたいので警告抑制
     */
    @Suppress("SameParameterValue")
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
     * クラス呼んでEntityType登録+Attributeの登録用
     */
    @JvmStatic
    fun register() {
        FabricDefaultAttributeRegistry.register(BLOCKHIGHLIGHT, ShulkerEntity.createShulkerAttributes())
        FabricDefaultAttributeRegistry.register(DECOY, ShulkerEntity.createShulkerAttributes())
    }
}