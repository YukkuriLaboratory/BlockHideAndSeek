package com.iduki.blockhideandseekmod.game;

import com.iduki.blockhideandseekmod.BlockHideAndSeekMod;
import com.iduki.blockhideandseekmod.config.ModConfig;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * ゲーム時間の計測＆表示//
 * ↓
 * ゲーム開始
 * ↓
 * Hidersが全滅した場合はSeekersの勝利
 * 時間までHidersが残っていた場合は残っているHidersの勝ち
 */
public class GameStart {

    /**
     * 非同期処理の状態確認用
     * <p>
     */
    private static volatile boolean isInGameTime = false;

    /**
     * 時間計測用
     * 準備時間
     */
    private static Instant ingameTime;

    /**
     * 残り時間表示用のボスバー
     * 準備時間用
     */
    private static final ServerBossBar ingametimeProgress = new ServerBossBar(Text.of(""), BossBar.Color.BLUE, BossBar.Style.NOTCHED_20);

    /**
     * 非同期処理用
     * マインクラフトを動かしているスレッドとは別のスレッドで処理を行うことができます.
     */
    private static final ThreadPoolExecutor EXECUTOR = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);

    //毎回クラス名入力するのがダルいので定数として扱う
    private static final MinecraftServer server = BlockHideAndSeekMod.SERVER;

    public static void ClearSlownessSeekers() {
        var playerManager = server.getPlayerManager();
        var scoreboard = server.getScoreboard();
        //鬼側のエフェクトを解除します
        Team seekersteam = scoreboard.getTeam("Seekers");
        seekersteam.getPlayerList().forEach(player -> {
            var players = playerManager.getPlayer(player);
            if (players != null) {
                players.removeStatusEffect(StatusEffects.SLOWNESS);
                players.removeStatusEffect(StatusEffects.BLINDNESS);
                players.removeStatusEffect(StatusEffects.JUMP_BOOST);
            }
        });
    }


    /**
     * ゲーム時間のカウントを開始します．
     * これのメソッドが呼ばれて以降，このクラスによってゲームの開始まで進行が管理されます
     */
    public static void startGame() {

        //各種変数の初期化
        ingameTime = Instant.now();
        //鬼側エフェクト削除
        ClearSlownessSeekers();
        registerMessage();


    }

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

        //制限時間(毎回入力するのがダルいので定数化．クラス内定数にしないのは途中でConfig変えられたりする可能性を考えているため)
        var gameTime = ModConfig.SystemConfig.Times.playTime;

        //現在の時間
        var now = Instant.now();

        //経過時間
        var currentTime = Duration.between(ingameTime, now);

        //残り時間
        var remainsTime = Duration.ofSeconds(gameTime).minus(currentTime);

        //ミミック陣営が0かどうかの確認
        var mimicEmpty = scoreboard.getTeam("Hiders").getPlayerList().isEmpty();

        //ミミック陣営の人数が0のとき
        if (mimicEmpty) {
            isInGameTime = false;
            //ボスバーを非表示にする
            ingametimeProgress.setVisible(false);
            //タイトルバーにGAMEOVERと表示
            var startMessage = new TitleS2CPacket(new LiteralText("-鬼陣営の勝利!-").setStyle(Style.EMPTY.withColor(Formatting.RED)));
            playerManager.getPlayerList().forEach(player -> player.networkHandler.sendPacket(startMessage));

            //ゲーム終了フェーズへの移行
            //TODO ゲームの終了.ゲームの勝敗を表示する(人数0なので鬼側の)
            TeamCreateandDelete.deleteTeam();

            return;
        }

        //残り時間が０以下のとき
        if (remainsTime.isNegative()) {
            isInGameTime = false;
            //ボスバーを非表示にする
            ingametimeProgress.setVisible(false);
            //タイトルバーにGAMEOVERと表示
            var startMessage = new TitleS2CPacket(new LiteralText("-ミミック陣営の勝利!-").setStyle(Style.EMPTY.withColor(Formatting.RED)));
            playerManager.getPlayerList().forEach(player -> player.networkHandler.sendPacket(startMessage));

            playerManager.getPlayerList().forEach(player -> player.playSound(SoundEvents.ENTITY_ENDER_DRAGON_GROWL, SoundCategory.PLAYERS, 1.0f, 1.0f));

            //ゲーム終了フェーズへの移行
            //TODO ゲームの終了.ゲームの勝敗を表示する(時間切れなのでミミック側の)
            TeamCreateandDelete.deleteTeam();


            return;
        }

        //ボスバーのテキストを更新
        var timeText = Text.of("残り時間: " + remainsTime.toSeconds() + "秒");
        ingametimeProgress.setName(timeText);
        //ゲージ更新
        ingametimeProgress.setPercent(MathHelper.clamp(remainsTime.toSeconds() / ((float) gameTime), 0, 1));

        var isDisplayTime = Math.floor(currentTime.toMillis() / 100f / 5) % 2 == 0;

        if (currentTime.toSeconds() >= gameTime - 4 && isDisplayTime) {
            playerManager.getPlayerList().forEach(player -> player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_HAT, SoundCategory.PLAYERS, 1.0f, 1.0f));
        }

    }


    //Thread.sleepは多くの場合で冗長とされてwaringの対象となっているが，今回の場合は正しい使用方法と判断できるため警告を抑制している
    @SuppressWarnings("BusyWait")
    private static void registerMessage() {
        isInGameTime = true;
        //ボスバーを表示
        ingametimeProgress.setVisible(true);

        //非同期スレッドの呼び出し
        EXECUTOR.execute(() -> {
            //準備時間中常に実行
            while (isInGameTime) {
                //現在の時間の取得
                var startTime = Instant.now();
                //マインクラフトの実行スレッドを呼び出して,処理が終了するまで待機させる
                //実はserverはそれ自体が実行スレッドとして扱われているため，このように非同期スレッドからマイクラの実行スレッドに処理を渡すことができる
                server.submitAndJoin(GameStart::update);
                try {
                    //0.5 - (作業時間)秒間待つ
                    Thread.sleep(Duration.ofMillis(500).minus(Duration.between(startTime, Instant.now())).toMillis());
                } catch (InterruptedException ignore) {
                }
            }
        });
    }

    static {
        //最初は非表示にしておく
        ingametimeProgress.setVisible(false);


    }


}