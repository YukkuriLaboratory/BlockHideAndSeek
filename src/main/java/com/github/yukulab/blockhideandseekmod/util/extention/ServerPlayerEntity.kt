package com.github.yukulab.blockhideandseekmod.util.extention

import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ThreadedAnvilChunkStorage

val ServerPlayerEntity.playerTracker: ThreadedAnvilChunkStorage.EntityTracker
    get() = serverWorld.chunkManager.threadedAnvilChunkStorage.entityTrackers.get(id)