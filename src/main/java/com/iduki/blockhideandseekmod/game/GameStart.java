package com.iduki.blockhideandseekmod.game;

import com.iduki.blockhideandseekmod.BlockHideAndSeekMod;
import com.iduki.blockhideandseekmod.config.ModConfig;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.GameMode;

import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 準備時間の計測＆表示//
 * ↓
 * ゲーム開始
 * ↓
 * Hidersが全滅した場合はSeekersの勝利
 * 時間までHidersが残っていた場合は残っているHidersの勝ち
 */
public class GameStart {

    /**
     * 時間計測用
     * ゲーム時間
     */
    private static Instant ingameTime;

    /**
     * 残り時間表示用のボスバー
     * ゲーム時間用
     */
    private static final ServerBossBar ingametimeProgress = new ServerBossBar(Text.of(""), BossBar.Color.BLUE, BossBar.Style.NOTCHED_20);

    /**
     * 非同期処理用
     * マインクラフトを動かしているスレッドとは別のスレッドで処理を行うことができます.
     */
    private static final ThreadPoolExecutor EXECUTOR = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);

    //毎回クラス名入力するのがダルいので定数として扱う
    private static final MinecraftServer server = BlockHideAndSeekMod.SERVER;

    /**
     * プレイヤーをボスバーの表示対象に加えるメソッド
     * ワールドに参加したタイミングで呼び出されています
     * 途中で退出して再参加した場合や，途中参加でも表示が正しく行われるように作成
     *
     * @param player 新たにサーバーに参加したプレイヤー
     */
    public static void addBossBarTarget(ServerPlayerEntity player) {
        ingametimeProgress.addPlayer(player);
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
    public static void removeBossBarTarget(ServerPlayerEntity player) {
        ingametimeProgress.removePlayer(player);
    }

    /**
     *
     */
    public static void update() {
        var playerManager = server.getPlayerManager();
        var scoreboard = server.getScoreboard();

        //ゲームの制限時間(以下ry)
        var playTime = ModConfig.SystemConfig.Times.playTime;

        //現在の時間
        var now = Instant.now();

        //チームの取得
        Team seekersteam = scoreboard.getTeam("Seekers");
        Team hidersteam = scoreboard.getTeam("Hiders");

        //ミミック陣営の数が0かどうか
        var isHidersWon = hidersteam.getPlayerList().size() == 0;


    }


    private void tekitou() {
        var player = BlockHideAndSeekMod.SERVER.getPlayerManager().getPlayer("");
        player.changeGameMode(GameMode.CREATIVE);
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 999, 999));
    }
}
