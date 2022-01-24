package com.github.yukulab.blockhideandseekmod.config

import com.github.yukulab.blockhideandseekmod.BlockHideAndSeekMod
import io.github.redstoneparadox.paradoxconfig.codec.ConfigCodec
import io.github.redstoneparadox.paradoxconfig.config.ConfigCategory
import net.fabricmc.loader.api.FabricLoader
import java.io.File

object Config : ConfigCategory("config.json5") {
    object System : ConfigCategory("System") {
        @JvmStatic
        var seekerLimit by option(2, "SeekerLimit", "鬼の最大人数")

        object Time : ConfigCategory("Time") {
            @JvmStatic
            var voteTime by option(30, "VoteTime", "陣営の投票時間(単位:秒)")

            @JvmStatic
            var prepareTime by option(120, "PrepareTime", "準備時間(単位:秒)")

            @JvmStatic
            var playTime by option(300, "PlayTime", "ゲーム時間(単位:秒)")

            @JvmStatic
            var hideWaitTime by option(10, "HideWaitTime", "擬態にかかる時間(単位:Tick)")
        }
    }

    object Item : ConfigCategory("Item") {
        object Flyer : ConfigCategory("Flyer", "全員が持つ飛行用アイテム") {
            @JvmStatic
            var flyTime by option(3, "FlyTime", "最大飛行時間(単位:秒)")
        }

        object Detector : ConfigCategory("Detector", "鬼が持つ探知用アイテム") {
            @JvmStatic
            var coolTime by option(30, "CoolTime", "使用クールタイム(単位:Tick)")

            @JvmStatic
            var damageAmount by option(50, "DamageAmount", "ダメージ量")
        }

        object Scanner : ConfigCategory("Scanner", "鬼が持つスキャン用アイテム") {
            @JvmStatic
            var coolTime by option(10, "CoolTime", "使用クールタイム(単位:Tick)")

            @JvmStatic
            var scanLength by option(20, "ScanLength", "捜索半径")

            @JvmStatic
            var duration by option(60, "Duration", "効果時間")

            @JvmStatic
            var precision by option(3, "Precision", "スキャン精度(半径)")
        }

        object HidingBlockViewer : ConfigCategory("HidingBlockViewer", "鬼が持つミミックが擬態しているブロックを示すアイテム") {
            @JvmStatic
            var screenRow by option(1, 1..6, "ScreenRow", "表示する最大スロット数(x9)")

            @JvmStatic
            var notifyOnTitle by option(true, "NotifyOnTitle", "隠れているプレイヤーに対してサブタイトルに警告を表示するかどうか(false:アクションバー)")
        }

        object Blink : ConfigCategory("Blink", "ミミックが持つ透明化アイテム") {
            @JvmStatic
            var coolTime by option(100000, "CoolTime", "使用クールタイム(単位:Tick)")

            @JvmStatic
            var duration by option(80, "Duration", "効果時間(単位:Tick)")
        }

        object Jammer : ConfigCategory("Jammer", "ミミックが持つスキャン妨害用アイテム") {
            @JvmStatic
            var coolTime by option(10000, "CoolTime", "使用クールタイム(単位:Tick)")

            @JvmStatic
            var duration by option(200, "Duration", "効果時間(単位:Tick)")
        }

        object SurpriseBall : ConfigCategory("SurpriseBall", "ミミックが鬼に投げるお邪魔アイテム") {
            @JvmStatic
            var coolTime by option(140, "CoolTime", "使用クールタイム(単位:Tick)")

            @JvmStatic
            var duration by option(100, "Duration", "効果時間(単位：Tick)")
        }

        object FakeSummoner : ConfigCategory("FakeSummoner", "ミミックが持つデコイ召喚用アイテム") {
            @JvmStatic
            var deleteTime by option(10000, "DeleteTime", "デコイ消滅までの時間(単位:秒)")

            @JvmStatic
            var coolTime by option(2400, "CoolTime", "使用クールタイム(単位:Tick)")

            @JvmStatic
            var length by option(30, "Length", "届く最大の距離")
        }
    }

    private val ext = key.split('.').last()
    private val codec = ConfigCodec.getCodec(ext)
    private val fileName = "${BlockHideAndSeekMod.MOD_ID}/${key}"

    @JvmStatic
    fun save(): Result<Unit> {
        val file = File(FabricLoader.getInstance().configDir.toFile(), fileName)

        return kotlin.runCatching {
            if (!file.exists()) {
                file.parentFile.mkdirs()
                file.createNewFile()
            }

            file.writeText(codec.encode(this))
        }
    }

    fun reload(): Result<Unit> {
        val file = File(FabricLoader.getInstance().configDir.toFile(), fileName)

        if (file.exists()) {
            val result = kotlin.runCatching {
                val data = file.readText()
                codec.decode(data, this)
            }
            if (result.isFailure) return result
        }
        return save()
    }
}