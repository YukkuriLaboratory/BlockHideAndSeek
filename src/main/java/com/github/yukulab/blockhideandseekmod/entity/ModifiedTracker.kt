package com.github.yukulab.blockhideandseekmod.entity

import net.minecraft.server.network.ServerPlayerEntity

interface ModifiedTracker {
    fun canTrack(player: ServerPlayerEntity): Boolean
}