package com.iduki.blockhideandseekmod.game;

import com.google.common.collect.Lists;
import com.iduki.blockhideandseekmod.BlockHideAndSeekMod;
import com.iduki.blockhideandseekmod.config.ModConfig;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * ゲームを開始する前に，プレイヤーに陣営選択をさせるためのクラスです．
 * 基本的には以下のフローにおける処理を行います．
 * ```
 * startコマンドにより発火
 * ↓
 * 全員の選択待ち(制限時間込み)
 * ↓
 * プレイヤーのチームの割り振り
 * ↓
 * ゲームの開始
 * ```
 */
public class TeamSelector {

    /**
     * 鬼陣営の集計用
     */
    private static final ArrayList<UUID> seekers = Lists.newArrayList();

    /**
     * 隠れる陣営の集計用
     */
    private static final ArrayList<UUID> hiders = Lists.newArrayList();

    /**
     * 非同期処理の状態確認用
     * 投票を受け付けている間のみtrue
     * <p>
     * tips: volatileとは非同期処理向けの変数のmodifierで，変数の値をCPUでキャッシングしないように命令しメモリを常に参照することで最新の変数の状態を確認し続けます
     */
    private static volatile boolean isVoteTime = false;

    /**
     * 時間計測用
     * 変数をセットしたときのシステム時間を記録します
     */
    private static Instant startedTime;

    /**
     * 時間計測用
     * 全員が投票を終えてからの時間
     */
    private static Instant lastAllVotedTime;

    /**
     * 残り時間表示用のボスバー
     */
    private static final ServerBossBar timeProgress = new ServerBossBar(Text.of(""), BossBar.Color.WHITE, BossBar.Style.NOTCHED_6);

    /**
     * 非同期処理用
     * マインクラフトを動かしているスレッドとは別のスレッドで処理を行うことができます.
     */
    private static final ThreadPoolExecutor EXECUTOR = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);

    /**
     * {@link HideController}用のID
     */
    private final static String VOTE_PROGRESS = "voteProgress";

    //毎回クラス名入力するのがダルいので定数として扱う
    private static final MinecraftServer server = BlockHideAndSeekMod.SERVER;

    /**
     * 陣営の投票を開始します．
     * これのメソッドが呼ばれて以降，このクラスによってゲームの開始まで進行が管理されます
     */
    public static void startVote() {
        //各種変数の初期化
        startedTime = Instant.now();
        seekers.clear();
        hiders.clear();
        registerMessage();
        //テキストにカーソルを合わせると表示されるやつ
        var hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of("クリックして参加"));
        /*
        表示用テキスト
        """
        陣営の投票を受け付けています．
        鬼陣営に登録する / ミミック陣営に登録する
        """
         */
        var text = new LiteralText("")
                .append(new LiteralText("陣営の投票を受け付けています．").setStyle(Style.EMPTY.withColor(Formatting.GREEN)))
                .append(Text.of("\n"))
                .append(new LiteralText("鬼陣営に参加する").setStyle(Style.EMPTY.withColor(Formatting.RED).withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/bhas team seeker")).withHoverEvent(hoverEvent)))
                .append(Text.of(" / "))
                .append(new LiteralText("ミミック陣営に参加する").setStyle(Style.EMPTY.withColor(Formatting.GREEN).withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/bhas team hider")).withHoverEvent(hoverEvent)));
        server.getPlayerManager().getPlayerList().forEach(p -> p.sendMessage(text, false));
    }

    /**
     * 陣営登録用のメソッド
     * 鬼側の陣営に登録します
     *
     * @param player 対象プレイヤー
     */
    public static boolean addSeeker(ServerPlayerEntity player) {
        if (isVoteTime) {
            var uuid = player.getUuid();
            hiders.remove(uuid);
            seekers.add(uuid);
            return true;
        }
        return false;
    }

    /**
     * 陣営登録用のメソッド
     * ミミック側の陣営に登録します
     *
     * @param player 対象プレイヤー
     */
    public static boolean addHider(ServerPlayerEntity player) {
        if (isVoteTime) {
            var uuid = player.getUuid();
            seekers.remove(uuid);
            hiders.add(uuid);
            return true;
        }
        return false;
    }

    /**
     * プレイヤーをボスバーの表示対象に加えるメソッド
     * ワールドに参加したタイミングで呼び出されています
     * 途中で退出して再参加した場合や，途中参加でも表示が正しく行われるように作成
     *
     * @param player 新たにサーバーに参加したプレイヤー
     */
    public static void addBossBarTarget(ServerPlayerEntity player) {
        timeProgress.addPlayer(player);
    }

    /**
     * プレイヤーをボスバーの表示対象から外すメソッド
     * ワールドから退出したタイミングで呼び出されています
     * <p>
     * この処理がなかった場合，
     * プレイヤーが死亡，ワールド間の移動，サーバーの出入りを繰り返すたびにplayerのインスタンスが変更されてServerBossBarのplayersが無限に肥大化してしまう
     *
     * @param player 退出したプレイヤー
     */
    public static void removeBossBarTarget(ServerPlayerEntity player) {
        timeProgress.removePlayer(player);
    }

    /**
     * 投票を受け付けている間，各陣営の人数を表示，内容の更新を行う用
     * <p>
     * ついでに投票時間の終了を管理している
     */
    private static void update() {
        var playerManager = server.getPlayerManager();

        //投票の制限時間(毎回入力するのがダルいので定数化．クラス内定数にしないのは途中でConfig変えられたりする可能性を考えているため)
        var voteTime = ModConfig.SystemConfig.Times.voteTime;
        //現在の時間
        var now = Instant.now();

        //全プレイヤーが投票を終えているか
        var isAllPlayerVoted = seekers.size() + hiders.size() == playerManager.getPlayerList().size();

        if (isAllPlayerVoted && (lastAllVotedTime == null || Duration.between(lastAllVotedTime, now).toSeconds() > 5)) {
            lastAllVotedTime = now;
        }

        //経過時間(三項演算子使って条件分岐してる)↓評価内容     ↓trueの場合                                                                ↓falseの場合
        var currentTime = isAllPlayerVoted ? Duration.ofSeconds(voteTime - 5).plus(Duration.between(lastAllVotedTime, now)) : Duration.between(startedTime, now);
        //残り時間
        var remainsTime = Duration.ofSeconds(voteTime).minus(currentTime);
        //残り時間が０以下のとき
        if (remainsTime.isNegative()) {
            isVoteTime = false;
            //ボスバーを非表示にする
            timeProgress.setVisible(false);
            //アクションバーのメッセージを非表示にする
            playerManager.getPlayerList().forEach(player -> HudDisplay.removeActionbarText(player.getUuid(), VOTE_PROGRESS));
            //鬼側が上限を超えていた際にミミック側に移動させる
            while (seekers.size() > ModConfig.SystemConfig.seekerLimit) {
                var random = new Random();
                var uuid = seekers.remove(random.nextInt(seekers.size() - 1));
                hiders.add(uuid);
            }
            //タイトルバーにSTARTと表示
            var startMessage = new TitleS2CPacket(new LiteralText("START").setStyle(Style.EMPTY.withColor(Formatting.GREEN)));
            playerManager.getPlayerList().forEach(player -> player.networkHandler.sendPacket(startMessage));
            /*
            各陣営のメンバー通知用メッセージ.表示は以下のような感じ
            """
            参加状況
            鬼:(赤色) NAME/NAME/NAME
            ミミック:(緑色) NAME/NAME/NAME
            """
            */
            var message = new LiteralText("")
                    .append(new LiteralText("参加状況").setStyle(Style.EMPTY.withColor(Formatting.GOLD)))
                    .append(Text.of("\n"))
                    .append(new LiteralText("鬼: ").setStyle(Style.EMPTY.withColor(Formatting.RED)));

            //forEachで二回同じ処理書くの面倒くさいなーっておもって楽しようとしたらトリッキーなやり方になった...
            //forEachの回数カウント用．何故か非同期処理扱いになったためIDEAから怒られないようにとスレッドセーフなこいつが登場してる
            AtomicInteger count = new AtomicInteger(seekers.size());
            //プレイヤーの名前をTextに連結してるだけここではただ処理を書いてるだけ，実際には下のforEachでこいつが動いてる
            Consumer<UUID> appendPlayer = (uuid) -> {
                var player = playerManager.getPlayer(uuid);
                if (player != null) {
                    message.append(player.getDisplayName());
                    //この名前が最後じゃないかどうか
                    if (count.decrementAndGet() > 0) {
                        // NAME/NAME みたいな感じにつなげるためのやつ
                        message.append(Text.of("/"));
                    }
                }
            };
            //上の処理を走らせてる
            seekers.forEach(appendPlayer);
            message.append(Text.of("\n"))
                    .append(new LiteralText("ミミック: ").setStyle(Style.EMPTY.withColor(Formatting.GREEN)));
            //カウントをミミック陣営の人数にセットしなおしてる
            count.set(hiders.size());
            //同じように処理が走ってる
            hiders.forEach(appendPlayer);
            //全プレイヤーのチャット欄に送信
            playerManager.getPlayerList().forEach(player -> player.sendMessage(message, false));

            //TODO call game start method
            return;
        }

        //ボスバーのテキストを更新
        var timeText = Text.of("残り時間: " + remainsTime.toSeconds() + "秒");
        timeProgress.setName(timeText);
        //ゲージ更新
        timeProgress.setPercent(MathHelper.clamp(remainsTime.toSeconds() / ((float) voteTime), 0, 1));

        //アクションバー表示用テキスト. "陣営選択中です 鬼:n/{上限}人(橙色) ミミック:n人(水色) 残り時間:n秒" と表示される
        var text = new LiteralText("陣営の選択を待っています ")
                .append(new LiteralText("鬼:" + seekers.size() + "/" + ModConfig.SystemConfig.seekerLimit + "人 ").setStyle(Style.EMPTY.withColor(Formatting.GOLD)))
                .append(new LiteralText("ミミック:" + hiders.size() + "人 ").setStyle(Style.EMPTY.withColor(Formatting.AQUA)));

        //鬼の上限人数を超えていた場合に警告
        if (seekers.size() > ModConfig.SystemConfig.seekerLimit) {
            text.append(new LiteralText(" 警告:鬼が上限を超えています").setStyle(Style.EMPTY.withColor(Formatting.RED)));
        }

        //全プレイヤーを取得して，全員に上のテキストを表示するようにしている
        playerManager.getPlayerList()
                .forEach(p -> {
                    var uuid = p.getUuid();
                    HudDisplay.setActionBarText(uuid, VOTE_PROGRESS, text);
                });

        //今回の処理が表示してよいタイミングかを判断する用(各秒数に合わせて表示させたいためこうなった)
        /*
        これは経過時間のミリ秒から千と百の位(N.N秒の部分)を取得して5で割った値の整数部分を2で割ったときの余りが0かどうかを見ている
        こうすることでN.0~N.4秒の間のみtrueになるため，0.5秒毎にしか実行されないことから２回に１回のみtrueとなる
        */
        //これのためだけにクラス内変数を持たせるのがなんとなく嫌だったためこんなクソダルい処理をしているが，
        //普通にbooleanの値を毎回切り替えてtrueのときのみ実行したほうが楽だしCPU的にも優しい(なんでこんなことしてるの？)
        var isDisplayTime = Math.floor(currentTime.toMillis() / 100f / 5) % 2 == 0;

        //最後三秒をタイトル表示する
        if (currentTime.toSeconds() >= voteTime - 4 && isDisplayTime) {
            var remainsTimeText = new LiteralText("" + remainsTime.toSeconds()).setStyle(Style.EMPTY.withColor(Formatting.GREEN));
            var packet = new TitleS2CPacket(remainsTimeText);
            playerManager.getPlayerList().forEach(player -> player.networkHandler.sendPacket(packet));
        }
    }

    /**
     * 投票を受け付けている間，陣営の人数表示を毎秒更新し続けます
     */
    //Thread.sleepは多くの場合で冗長とされてwaringの対象となっているが，今回の場合は正しい使用方法と判断できるため警告を抑制している
    @SuppressWarnings("BusyWait")
    private static void registerMessage() {
        isVoteTime = true;
        //ボスバーを表示
        timeProgress.setVisible(true);
        //非同期スレッドの呼び出し
        EXECUTOR.execute(() -> {
            //投票時間中常に実行
            while (isVoteTime) {
                //現在の時間の取得
                var startTime = Instant.now();
                //マインクラフトの実行スレッドを呼び出して,処理が終了するまで待機させる
                //実はserverはそれ自体が実行スレッドとして扱われているため，このように非同期スレッドからマイクラの実行スレッドに処理を渡すことができる
                server.submitAndJoin(TeamSelector::update);
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
        timeProgress.setVisible(false);
    }
}
