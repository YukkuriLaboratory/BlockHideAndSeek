package com.github.yukulab.blockhideandseekmod.util.extention

import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ThreadedAnvilChunkStorage

val ServerPlayerEntity.playerTracker: ThreadedAnvilChunkStorage.EntityTracker
    get() = getWorld().chunkManager.threadedAnvilChunkStorage.entityTrackers.get(id)