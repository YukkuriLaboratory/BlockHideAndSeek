package com.github.yukulab.blockhideandseekmod.game

import com.github.yukulab.blockhideandseekmod.item.BhasItems
import com.github.yukulab.blockhideandseekmod.util.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.time.delay
import net.minecraft.entity.boss.ServerBossBar
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.world.GameMode
import java.time.Duration
import java.time.Instant

object GameController {

    private var progressBar: ServerBossBar? = null

    @JvmStatic
    @Volatile
    var current: GameStatus? = null

    @JvmStatic
    val isGameRunning: Boolean
        get() = current != null

    private var job: Job = Job()

    fun startGame(): Boolean {
        if (current != null) {
            return false
        }
        current = SelectTeam()
        updateProgressBar()
        startLoop()
        return true
    }

    suspend fun suspend(): Boolean {
        if (!isGameRunning) {
            return false
        }
        job.cancelAndJoin()
        current?.onFinally()
        current?.onSuspend()
        progressBar?.isVisible = false
        current = null

        server.playerManager
            .playerList
            .forEach {
                it.changeGameMode(GameMode.SPECTATOR)
                // 擬態解除(事故ることはないのでここで呼んじゃう)
                HideController.cancelHiding(it)
                // Modアイテムの削除
                it.inventory
                    .remove(
                        { itemStack -> BhasItems.isModItem(itemStack.getItem()) },
                        64,
                        it.playerScreenHandler.craftingInput
                    )
            }
        TeamCreateAndDelete.deleteTeam()
        TeamPlayerListHeader.EmptyList()
        return true
    }

    /**
     * プレイヤーをボスバーの表示対象に加えるメソッド
     * ワールドに参加したタイミングで呼び出されています
     * 途中で退出して再参加した場合や，途中参加でも表示が正しく行われるように作成
     *
     * @param player 新たにサーバーに参加したプレイヤー
     */
    @JvmStatic
    fun addBossBarTarget(player: ServerPlayerEntity) {
        progressBar?.addPlayer(player)
    }

    /**
     * プレイヤーをボスバーの表示対象から外すメソッド
     * ワールドから退出したタイミングで呼び出されています
     * <p>
     * この処理がなかった場合，
     * プレイヤーが死亡，ワールド間の移動，サーバーの出入りを繰り返すたびにplayerのインスタンスが変更されて新たなplayerとして追加され，ServerBossBarのplayersが無限に肥大化してしまう
     *
     * @param player 退出したプレイヤー
     */
    @JvmStatic
    fun removeBossBarTarget(player: ServerPlayerEntity) {
        progressBar?.removePlayer(player)
    }

    private fun startLoop() {
        job = bhasScope.launch {
            while (true) {
                val startTime = Instant.now()
                val current = current ?: break
                if (current.onUpdate(progressBar)) {
                    current.onFinally()
                    val next = current.next()
                    this@GameController.current = next
                    updateProgressBar()
                }
                val lastTime = Duration.ofMillis(500) - Duration.between(startTime, Instant.now())
                if (!lastTime.isNegative) {
                    delay(lastTime)
                }
            }
        }
    }

    private fun updateProgressBar() {
        progressBar = current?.progressBar
        progressBar?.also {
            server.playerManager.playerList.forEach(it::addPlayer)
        }
    }
}