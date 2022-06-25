package com.github.yukulab.blockhideandseekmod.data

import com.github.yukulab.blockhideandseekmod.BlockHideAndSeekMod
import com.github.yukulab.blockhideandseekmod.util.server
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.nbt.NbtList
import java.io.File

object DataIO {
    private val json = Json
    private val targetFile =
        File(FabricLoader.getInstance().configDir.toFile(), "${BlockHideAndSeekMod.MOD_ID}/data.json")
    private lateinit var lastUpdatedTime: Instant

    fun update(): Result<Unit> = kotlin.runCatching {
        lastUpdatedTime = Clock.System.now()
        val playersInvData = server.playerManager.playerList.associate {
            it.uuid to Pair(it.inventory.writeNbt(NbtList()), it.interactionManager.gameMode)
        }
        val data = PlayerData(lastUpdatedTime, false, playersInvData)
        writeData(data).getOrThrow()
    }

    fun readAndApply(force: Boolean = false): Result<Unit> = kotlin.runCatching {
        val decodedData = readData().getOrThrow()
        if (decodedData.restored) {
            throw SerializationException("既に復元済みです")
        }
        if (!force && lastUpdatedTime < decodedData.date) {
            throw SerializationException("データの保存日時が不正です")
        }
        server.playerManager.playerList.forEach {
            val playerData = decodedData.playerInvAndGameMode[it.uuid]
            if (playerData != null) {
                val targetNbt = playerData.first
                it.inventory.readNbt(targetNbt)
                it.changeGameMode(playerData.second)
            }
        }
        writeData(decodedData.copy(restored = true)).getOrThrow()
    }

    private fun readData(): Result<PlayerData> = kotlin.runCatching {
        val rawString = targetFile.readText()
        return json.decodeFromString(rawString)
    }

    private fun writeData(data: PlayerData): Result<Unit> = kotlin.runCatching {
        val encodedData = json.encodeToString(data)
        targetFile.writeText(encodedData)
    }
}