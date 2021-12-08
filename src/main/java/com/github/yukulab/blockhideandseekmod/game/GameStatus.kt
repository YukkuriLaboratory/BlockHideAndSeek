package com.github.yukulab.blockhideandseekmod.game

import net.minecraft.entity.boss.ServerBossBar

interface GameStatus {

    /**
     * ボスバーのコンストラクタを渡す用
     */
    val progressBar: ServerBossBar

    /**
     * 更新処理
     */
    fun onUpdate(progressBar: ServerBossBar?): Boolean

    /**
     * 終了処理
     */
    fun onFinally()

    /**
     * 中断処理
     * [onFinally]の後に呼び出される
     */
    fun onSuspend()

    /**
     * 次の状態への移行処理
     * [onFinally]の後に呼び出される
     *
     * @return 次の[GameStatus]
     */
    fun next(): GameStatus?
}