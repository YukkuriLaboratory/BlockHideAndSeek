package com.github.yukulab.blockhideandseekmod.game;

import com.github.yukulab.blockhideandseekmod.BlockHideAndSeekMod;
import com.github.yukulab.blockhideandseekmod.config.Config;
import com.github.yukulab.blockhideandseekmod.data.DataIO;
import com.github.yukulab.blockhideandseekmod.item.BhasItems;
import com.github.yukulab.blockhideandseekmod.item.ItemJammerJava;
import com.github.yukulab.blockhideandseekmod.util.HideController;
import com.github.yukulab.blockhideandseekmod.util.HudDisplay;
import com.github.yukulab.blockhideandseekmod.util.PlayerUtil;
import com.github.yukulab.blockhideandseekmod.util.TeamCreateAndDelete;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.GameMode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Stream;


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
public class SelectTeam implements GameStatus {

    /**
     * 鬼陣営の集計用
     */
    private final ArrayList<UUID> seekers = Lists.newArrayList();

    /**
     * 隠れる陣営の集計用
     */
    private final ArrayList<UUID> hiders = Lists.newArrayList();

    /**
     * 投票済み集計用
     */
    private final HashSet<UUID> votedList = Sets.newHashSet();

    /**
     * 時間計測用
     * 変数をセットしたときのシステム時間を記録します
     */
    private final Instant startedTime;

    /**
     * 時間計測用
     * 全員が投票を終えてからの時間
     */
    private Instant lastAllVotedTime;

    /**
     * {@link HideController}用のID
     */
    private static final String VOTE_PROGRESS = "voteProgress";

    //毎回クラス名入力するのがダルいので定数として扱う
    private final MinecraftServer server = BlockHideAndSeekMod.SERVER;

    //テキストにカーソルを合わせると表示されるやつ
    private static final HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of("クリックして参加"));
    /*
        表示用テキスト
        """
        陣営の投票を受け付けています．
        鬼陣営に登録する / ミミック陣営に登録する
        """
    */
    public static final Text selectMessage = new LiteralText("")
            .append(new LiteralText("陣営の投票を受け付けています．").setStyle(Style.EMPTY.withColor(Formatting.GREEN)))
            .append(Text.of("\n"))
            .append(new LiteralText("鬼陣営に参加する").setStyle(Style.EMPTY.withColor(Formatting.RED).withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/bhas team seeker")).withHoverEvent(hoverEvent)))
            .append(Text.of(" / "))
            .append(new LiteralText("ミミック陣営に参加する").setStyle(Style.EMPTY.withColor(Formatting.GREEN).withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/bhas team hider")).withHoverEvent(hoverEvent)))
            .append("/")
            .append(new LiteralText("観戦する").setStyle(Style.EMPTY.withColor(Formatting.DARK_GRAY).withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/bhas team observer")).withHoverEvent(hoverEvent)));


    public SelectTeam() {
        startedTime = Instant.now();
        server.getPlayerManager().getPlayerList().forEach(p -> p.sendMessage(selectMessage, false));

        ItemJammerJava.clearJamming();
    }

    @NotNull
    @Override
    public ServerBossBar getProgressBar() {
        return new ServerBossBar(Text.of(""), BossBar.Color.WHITE, BossBar.Style.NOTCHED_6);
    }

    /**
     * 陣営登録用のメソッド
     * 鬼側の陣営に登録します
     *
     * @param player 対象プレイヤー
     */
    public void addSeeker(ServerPlayerEntity player) {
        var uuid = player.getUuid();
        markVoted(player);
        if (!seekers.contains(uuid)) {
            hiders.remove(uuid);
            seekers.add(uuid);
        }
    }

    /**
     * 陣営登録用のメソッド
     * ミミック側の陣営に登録します
     *
     * @param player 対象プレイヤー
     */
    public void addHider(ServerPlayerEntity player) {
        var uuid = player.getUuid();
        markVoted(player);
        if (!hiders.contains(uuid)) {
            seekers.remove(uuid);
            hiders.add(uuid);
        }
    }

    public void markVoted(ServerPlayerEntity player) {
        votedList.add(player.getUuid());
    }

    @Override
    public boolean onUpdate(@Nullable ServerBossBar timeProgress) {
        var playerManager = server.getPlayerManager();

        //投票の制限時間(毎回入力するのがダルいので定数化．クラス内定数にしないのは途中でConfig変えられたりする可能性を考えているため)
        var voteTime = Config.System.Time.getVoteTime();
        //現在の時間
        var now = Instant.now();

        //全プレイヤーが投票を終えているか
        var isAllPlayerVoted = votedList.size() == playerManager.getPlayerList().size();

        if (isAllPlayerVoted && (lastAllVotedTime == null || Duration.between(lastAllVotedTime, now).toSeconds() > 5)) {
            lastAllVotedTime = now;
        }

        //経過時間(三項演算子使って条件分岐してる)↓評価内容     ↓trueの場合                                                                ↓falseの場合
        var currentTime = isAllPlayerVoted ? Duration.ofSeconds(voteTime - 5).plus(Duration.between(lastAllVotedTime, now)) : Duration.between(startedTime, now);
        //残り時間
        var remainsTime = Duration.ofSeconds(voteTime).minus(currentTime);
        //残り時間が０以下のとき
        if (remainsTime.isNegative()) {
            return true;
        }

        if (timeProgress != null) {
            //ボスバーのテキストを更新
            var timeText = Text.of("残り時間: " + remainsTime.toSeconds() + "秒");
            timeProgress.setName(timeText);
            //ゲージ更新
            timeProgress.setPercent(MathHelper.clamp(remainsTime.toSeconds() / ((float) voteTime), 0, 1));
        }

        //アクションバー表示用テキスト. "陣営選択中です 鬼:n/{上限}人(橙色) ミミック:n人(水色) 残り時間:n秒" と表示される
        var text = new LiteralText("陣営の選択を待っています ")
                .append(new LiteralText("鬼:" + seekers.size() + "/" + Config.System.getSeekerLimit() + "人 ").setStyle(Style.EMPTY.withColor(Formatting.GOLD)))
                .append(new LiteralText("ミミック:" + hiders.size() + "人 ").setStyle(Style.EMPTY.withColor(Formatting.AQUA)));

        //鬼の上限人数を超えていた場合に警告
        if (seekers.size() > Config.System.getSeekerLimit()) {
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
            var remainsTimeText = new LiteralText(String.valueOf(remainsTime.toSeconds())).setStyle(Style.EMPTY.withColor(Formatting.GREEN));
            var packet = new TitleS2CPacket(remainsTimeText);
            playerManager.getPlayerList().forEach(player -> player.networkHandler.sendPacket(packet));
            //カウントダウン時の音
            playerManager.getPlayerList().forEach(player -> player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_HAT, SoundCategory.PLAYERS, 1.0f, 1.0f));
        }

        return false;
    }

    @Override
    public void onSuspend() {
        //nothing//
    }

    @Nullable
    @Override
    public GameStatus next() {
        var playerManager = server.getPlayerManager();

        //鬼側が上限を超えていた際にミミック側に移動させる
        //鬼からミミック陣営に移動した人のUUIDを集める用
        Set<UUID> notificationTargets = Sets.newHashSet();
        //鬼が上限より多い限り実行し続ける
        while (seekers.size() > Config.System.getSeekerLimit()) {
            var random = new Random();
            //鬼からランダムに一人を選出して削除
            var uuid = seekers.remove(random.nextInt(seekers.size() - 1));
            //ミミック陣営に追加
            hiders.add(uuid);
            //通知対象に追加
            notificationTargets.add(uuid);
        }
        if (!notificationTargets.isEmpty()) {
            var notifyText = new LiteralText("鬼の人数が上限を上回っていたため，ミミック陣営に移動しました")
                    .setStyle(Style.EMPTY.withColor(Formatting.GREEN));
            //鬼から移動した人に通知(3秒間)
            notificationTargets.forEach(uuid -> HudDisplay.setActionBarText(uuid, "teamNotify", notifyText, 60L));
        }
        //タイトルバーにSTARTと表示
        var startMessage = new TitleS2CPacket(new LiteralText("READY").setStyle(Style.EMPTY.withColor(Formatting.GREEN)));
        playerManager.getPlayerList().forEach(player -> player.networkHandler.sendPacket(startMessage));

        playerManager.getPlayerList().forEach(player -> player.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 1.0f, 1.0f));
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

        var playerSeekers = seekers.stream()
                .map(playerManager::getPlayer)
                .filter(Objects::nonNull)
                .toList();
        var playerHiders = hiders.stream()
                .map(playerManager::getPlayer)
                .filter(Objects::nonNull)
                .toList();

        // プレイヤーデータの保存
        if (!DataIO.updateWithResultBoolean()) {
            var errorMessage = new LiteralText("プレイヤーデータの保存に失敗しました").setStyle(Style.EMPTY.withColor(Formatting.RED));
            playerManager.getPlayerList().forEach(player -> player.sendMessage(errorMessage, false));
            return null;
        }

        //アイテムの削除
        server.getPlayerManager().getPlayerList()
                .forEach(player -> player.getInventory().clear());
        // アイテムの付与
        playerSeekers.forEach(
                player -> {
                    var inventory = player.getInventory();
                    BhasItems.seekerItems.stream()
                            .map(Item::getDefaultStack)
                            .forEach(inventory::insertStack);
                });
        playerHiders.forEach(
                player -> {
                    var inventory = player.getInventory();
                    BhasItems.hiderItems.stream()
                            .map(Item::getDefaultStack)
                            .forEach(inventory::insertStack);
                });

        //ゲームモードをサバイバルに
        Stream.concat(playerHiders.stream(), playerSeekers.stream())
                .forEach(player -> {
                    PlayerUtil.escapeFromBlock(player);
                    //こうすることでクライアントサイドではアドベンチャーモードとして扱われるが，実際はサバイバルモードとなる
                    player.changeGameMode(GameMode.ADVENTURE);
                    player.interactionManager.changeGameMode(GameMode.SURVIVAL);
                    //飛行を許可し続ける
                    player.getAbilities().allowFlying = true;
                    player.sendAbilitiesUpdate();
                    //透明化消えてね(はーと)
                    player.setInvisible(false);
                });

        //各チームの作成
        var scoreboard = server.getScoreboard();
        TeamCreateAndDelete.addSeeker();
        TeamCreateAndDelete.addHider();
        TeamCreateAndDelete.addObserver();

        Team seekerTeam = TeamCreateAndDelete.getSeekers();
        Team hidersTeam = TeamCreateAndDelete.getHiders();
        //各チームにプレイヤーを振り分けする
        playerSeekers.stream()
                .map(PlayerEntity::getEntityName)
                .forEach(player -> scoreboard.addPlayerToTeam(player, seekerTeam));
        playerHiders.stream()
                .map(PlayerEntity::getEntityName)
                .forEach(player -> scoreboard.addPlayerToTeam(player, hidersTeam));
        var observerTeam = TeamCreateAndDelete.getObservers();
        playerManager.getPlayerList()
                .stream()
                .filter(player -> !playerSeekers.contains(player) && !playerHiders.contains(player))
                .forEach(player -> {
                    scoreboard.addPlayerToTeam(player.getEntityName(), observerTeam);
                    player.changeGameMode(GameMode.SPECTATOR);
                });

        if (seekerTeam.getPlayerList().isEmpty() || hidersTeam.getPlayerList().isEmpty()) {
            var errorMessage = new LiteralText("")
                    .append(new LiteralText("プレイヤーがいないためゲームを開始できません").setStyle(Style.EMPTY.withColor(Formatting.RED)));
            var titleError = new TitleS2CPacket(new LiteralText("ゲームを開始できません").setStyle(Style.EMPTY.withColor(Formatting.GREEN)));
            playerManager.getPlayerList().forEach(player -> {
                player.networkHandler.sendPacket(titleError);
                player.sendMessage(errorMessage, false);
            });
            return null;
        }

        return new Prepare();
    }

    @Override
    public void onFinally() {
        server.getPlayerManager()
                .getPlayerList()
                .forEach(player -> HudDisplay.removeActionbarText(player.getUuid(), VOTE_PROGRESS));
    }
}
