@file:UseSerializers(UuidSerializer::class, NbtListSerializer::class)

package com.github.yukulab.blockhideandseekmod.data

import kotlinx.datetime.Instant
import kotlinx.serialization.UseSerializers
import net.lepinoid.uuidserializer.UuidSerializer
import net.minecraft.nbt.NbtList
import net.minecraft.world.GameMode
import java.util.*

@kotlinx.serialization.Serializable
data class PlayerData(
    val date: Instant,
    val restored: Boolean,
    val playerInvAndGameMode: Map<UUID, Pair<NbtList, GameMode>>
)
