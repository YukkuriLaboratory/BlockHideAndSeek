package com.iduki.blockhideandseekmod.game;

import com.iduki.blockhideandseekmod.BlockHideAndSeekMod;
import com.iduki.blockhideandseekmod.config.ModConfig;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.effect.StatusEffectInstance;
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
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;


/**
 * GameStartで呼び出される準備時間の処理
 * １つのクラスだとなんだかめんどくさいので分けます
 */

public class PreparationTime {

    /**
     * 非同期処理の状態確認用
     * <p>
     */
    private static volatile boolean isPreparationTime = false;

    /**
     * 時間計測用
     * 準備時間
     */
    private static Instant preparationTime;

    /**
     * 残り時間表示用のボスバー
     * 準備時間用
     */
    private static final ServerBossBar preparationtimeProgress = new ServerBossBar(Text.of(""), BossBar.Color.BLUE, BossBar.Style.NOTCHED_20);

    /**
     * 非同期処理用
     * マインクラフトを動かしているスレッドとは別のスレッドで処理を行うことができます.
     */
    private static final ThreadPoolExecutor EXECUTOR = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);

    //毎回クラス名入力するのがダルいので定数として扱う
    private static final MinecraftServer server = BlockHideAndSeekMod.SERVER;

    //準備時間開始時にチーム別にタイトル表示
    public static void teamMessage() {
        var playerManager = server.getPlayerManager();
        var scoreboard = server.getScoreboard();

        Team seekers = scoreboard.getTeam("Seekers");
        var startSeekerMessage = new LiteralText("[鬼はスタートしたらミミックを探そう!]").setStyle(Style.EMPTY.withColor(Formatting.GREEN));
        List<ServerPlayerEntity> seekersList = seekers.getPlayerList()
                .stream().map(player -> seekers.getName())
                .map(uuid -> playerManager.getPlayer(uuid))
                .toList();
        seekersList.forEach(player -> player.sendMessage(startSeekerMessage, false));

        Team hiders = scoreboard.getTeam("Hiders");
        var startHiderMessage = new LiteralText("[ミミックは準備時間終了までに隠れよう!]").setStyle(Style.EMPTY.withColor(Formatting.GREEN));
        List<ServerPlayerEntity> hidersList = hiders.getPlayerList()
                .stream().map(player -> hiders.getName())
                .map(uuid -> playerManager.getPlayer(uuid))
                .toList();
        hidersList.forEach(player -> player.sendMessage(startHiderMessage, false));
    }


    public static boolean checkTeamcount() {
        var playerManager = server.getPlayerManager();
        var scoreboard = server.getScoreboard();

        Team seekersteam = scoreboard.getTeam("Seekers");
        Team hidersteam = scoreboard.getTeam("Hiders");
        if (seekersteam.getPlayerList().isEmpty() || hidersteam.getPlayerList().isEmpty()) {
            var message = new LiteralText("")
                    .append(new LiteralText("プレイヤーがいないためゲームを開始できません").setStyle(Style.EMPTY.withColor(Formatting.RED)));
            var startMessage = new TitleS2CPacket(new LiteralText("ゲームを開始できません").setStyle(Style.EMPTY.withColor(Formatting.GREEN)));
            playerManager.getPlayerList().forEach(player -> player.networkHandler.sendPacket(startMessage));
            playerManager.getPlayerList().forEach(allplayer -> allplayer.sendMessage(message, false));
            isPreparationTime = false;
            preparationtimeProgress.setVisible(false);
            TeamCreateandDelete.deleteTeam();
            return false;
        }
        return true;
    }

    /**
     * 準備時間のカウントを開始します．
     * これのメソッドが呼ばれて以降，このクラスによってゲームの開始まで進行が管理されます
     */
    public static void startPreparation() {

        //各種変数の初期化
        preparationTime = Instant.now();


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
        preparationtimeProgress.addPlayer(player);
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
        preparationtimeProgress.removePlayer(player);
    }

    public static void SlownessSeekers() {
        var playerManager = server.getPlayerManager();
        var scoreboard = server.getScoreboard();
        //準備時間中鬼側に移動制限と盲目を設けます
        Team seekersteam = scoreboard.getTeam("Seekers");
        seekersteam.getPlayerList().forEach(player -> {
            var players = playerManager.getPlayer(player);
            if (players != null) {
                players.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 50, 200, false, false, false));
                players.addStatusEffect(new StatusEffectInstance(StatusEffects.JUMP_BOOST, 50, 240, false, false, false));
                players.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 50, 1, false, false, false));
            }
        });
    }




    /**
     *
     */
    public static void update() {
        var playerManager = server.getPlayerManager();

        SlownessSeekers();

        //準備の制限時間(毎回入力するのがダルいので定数化．クラス内定数にしないのは途中でConfig変えられたりする可能性を考えているため)
        var prepareTime = ModConfig.SystemConfig.Times.prepareTime;

        //現在の時間
        var now = Instant.now();

        //経過時間
        var currentTime = Duration.between(preparationTime, now);

        //残り時間
        var remainsTime = Duration.ofSeconds(prepareTime).minus(currentTime);


        //残り時間が０以下のとき
        if (remainsTime.isNegative()) {
            isPreparationTime = false;
            //ボスバーを非表示にする
            preparationtimeProgress.setVisible(false);
            //タイトルバーにSTARTと表示
            var startMessage = new TitleS2CPacket(new LiteralText("-GAME-START-").setStyle(Style.EMPTY.withColor(Formatting.YELLOW)));
            playerManager.getPlayerList().forEach(player -> player.networkHandler.sendPacket(startMessage));

            playerManager.getPlayerList().forEach(player -> player.playSound(SoundEvents.BLOCK_ANVIL_PLACE, SoundCategory.PLAYERS, 1.0f, 1.0f));

            //ゲーム開始フェーズへの移行
            GameStart.startGame();
            return;
        }

        //ボスバーのテキストを更新
        var timeText = Text.of("準備時間: " + remainsTime.toSeconds() + "秒");
        preparationtimeProgress.setName(timeText);
        //ゲージ更新
        preparationtimeProgress.setPercent(MathHelper.clamp(remainsTime.toSeconds() / ((float) prepareTime), 0, 1));


        var isDisplayTime = Math.floor(currentTime.toMillis() / 100f / 5) % 2 == 0;

        if (currentTime.toSeconds() >= prepareTime - 4 && isDisplayTime) {
            playerManager.getPlayerList().forEach(player -> player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_HAT, SoundCategory.PLAYERS, 1.0f, 1.0f));
        }


    }

    //Thread.sleepは多くの場合で冗長とされてwaringの対象となっているが，今回の場合は正しい使用方法と判断できるため警告を抑制している
    @SuppressWarnings("BusyWait")
    private static void registerMessage() {
        isPreparationTime = true;

        //ボスバーを表示
        preparationtimeProgress.setVisible(true);

        if (!checkTeamcount()) {
            return;
        }
        //うごかないよ
        //teamMessage();

        //非同期スレッドの呼び出し
        EXECUTOR.execute(() -> {
            //準備時間中常に実行
            while (isPreparationTime) {
                //現在の時間の取得
                var startTime = Instant.now();
                //マインクラフトの実行スレッドを呼び出して,処理が終了するまで待機させる
                //実はserverはそれ自体が実行スレッドとして扱われているため，このように非同期スレッドからマイクラの実行スレッドに処理を渡すことができる
                server.submitAndJoin(PreparationTime::update);
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
        preparationtimeProgress.setVisible(false);


    }

}
