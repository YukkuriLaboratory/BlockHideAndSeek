package com.github.yukulab.blockhideandseekmod.game;

import com.github.yukulab.blockhideandseekmod.BlockHideAndSeekMod;
import com.github.yukulab.blockhideandseekmod.config.ModConfig;
import com.github.yukulab.blockhideandseekmod.item.BhasItems;
import com.github.yukulab.blockhideandseekmod.util.CoroutineProvider;
import com.github.yukulab.blockhideandseekmod.util.HudDisplay;
import com.github.yukulab.blockhideandseekmod.util.TeamCreateandDelete;
import com.github.yukulab.blockhideandseekmod.util.TeamPlayerListHeader;
import com.google.common.collect.Maps;
import kotlinx.coroutines.Job;
import kotlinx.coroutines.JobKt;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.GameMode;

import java.time.Duration;
import java.time.Instant;
import java.util.*;


/**
 * GameStartで呼び出される準備時間の処理
 * １つのクラスだとなんだかめんどくさいので分けます
 */

public class PreparationTime {

    /**
     * 非同期処理の状態確認用
     * <p>
     */
    private static Job job = JobKt.Job(null);

    /**
     * 時間計測用
     * 変数をセットしたときのシステム時間を記録します
     */
    private static Instant startedTime;

    /**
     * 時間計測用
     * 全員が擬態を終えてからの時間
     */
    private static Instant lastMimickedTime;

    /**
     * 残り時間表示用のボスバー
     * 準備時間用
     */
    private static final ServerBossBar preparationtimeProgress = new ServerBossBar(Text.of(""), BossBar.Color.BLUE, BossBar.Style.NOTCHED_20);

    private static final Map<UUID, Entity> lockerEntities = Maps.newHashMap();

    private static final String BLIND_MESSAGE = "blindMessage";

    //毎回クラス名入力するのがダルいので定数として扱う
    private static final MinecraftServer server = BlockHideAndSeekMod.SERVER;

    //準備時間開始時にチーム別にタイトル表示
    public static void teamMessage() {
        var playerManager = server.getPlayerManager();

        Team seekers = TeamCreateandDelete.getSeekers();
        var startSeekerMessage = new LiteralText("").append(Text.of("\n"))
                .append(new LiteralText("[鬼はスタートしたらミミックを探そう!]").setStyle(Style.EMPTY.withColor(Formatting.GREEN)));
        List<ServerPlayerEntity> seekersList = seekers.getPlayerList()
                .stream()
                .map(playerManager::getPlayer)
                .toList();
        seekersList.forEach(player -> player.sendMessage(startSeekerMessage, false));

        Team hiders = TeamCreateandDelete.getHiders();
        var startHiderMessage = new LiteralText("").append(Text.of("\n"))
                .append(new LiteralText("[ミミックは準備時間終了までに隠れよう!]").setStyle(Style.EMPTY.withColor(Formatting.GREEN)));
        List<ServerPlayerEntity> hidersList = hiders.getPlayerList()
                .stream()
                .map(playerManager::getPlayer)
                .toList();
        hidersList.forEach(player -> player.sendMessage(startHiderMessage, false));
    }


    public static boolean checkTeamcount() {
        var playerManager = server.getPlayerManager();

        Team seekersteam = TeamCreateandDelete.getSeekers();
        Team hidersteam = TeamCreateandDelete.getHiders();
        if (seekersteam.getPlayerList().isEmpty() || hidersteam.getPlayerList().isEmpty()) {
            var message = new LiteralText("")
                    .append(new LiteralText("プレイヤーがいないためゲームを開始できません").setStyle(Style.EMPTY.withColor(Formatting.RED)));
            var startMessage = new TitleS2CPacket(new LiteralText("ゲームを開始できません").setStyle(Style.EMPTY.withColor(Formatting.GREEN)));
            playerManager.getPlayerList().forEach(player -> player.networkHandler.sendPacket(startMessage));
            playerManager.getPlayerList().forEach(allplayer -> allplayer.sendMessage(message, false));
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
        GameState.setCurrentState(GameState.Phase.PREPARE);
        //各種変数の初期化
        startedTime = Instant.now();

        registerMessage();
    }

    /**
     * プレイヤーに偽のEntity情報を送り，そのEntityの視点に固定します．
     * サーバー側からなにかアクションを加えるか，Playerがリログするまで操作はできません(リログは対策済み)
     */
    public static void lockPlayerMovement(ServerPlayerEntity player) {
        var uuid = player.getUuid();
        var fakeEntity = lockerEntities.containsKey(uuid) ? lockerEntities.get(uuid) : EntityType.VILLAGER.create(player.getServerWorld());
        if (fakeEntity != null) {
            var blockPos = player.getBlockPos();
            fakeEntity.setPosition(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5);
            fakeEntity.setPitch(90);
            fakeEntity.setInvisible(true);
            player.networkHandler.sendPacket(new EntitySpawnS2CPacket(fakeEntity));
            player.networkHandler.sendPacket(new SetCameraEntityS2CPacket(fakeEntity));
            player.requestTeleport(fakeEntity.getX(), fakeEntity.getY(), fakeEntity.getZ());
            getAround(blockPos.up()).forEach(pos -> player.networkHandler.sendPacket(new BlockUpdateS2CPacket(pos, Blocks.BEDROCK.getDefaultState())));
            lockerEntities.put(uuid, fakeEntity);
            HudDisplay.setActionBarText(uuid, BLIND_MESSAGE, new LiteralText("目隠し中").setStyle(Style.EMPTY.withColor(Formatting.RED)));
        } else {
            BlockHideAndSeekMod.LOGGER.error("cannot get villager entity");
        }
        player.changeGameMode(GameMode.SPECTATOR);
    }

    private static void unlockPlayerMovement(ServerPlayerEntity player) {
        var uuid = player.getUuid();
        var entity = lockerEntities.get(uuid);
        if (entity != null) {
            player.networkHandler.sendPacket(new SetCameraEntityS2CPacket(player));
            player.networkHandler.sendPacket(new EntitiesDestroyS2CPacket(entity.getId()));
            getAround(player.getBlockPos().up()).forEach(pos -> player.networkHandler.sendPacket(new BlockUpdateS2CPacket(pos, Blocks.AIR.getDefaultState())));
            lockerEntities.remove(uuid);
            HudDisplay.removeActionbarText(uuid, BLIND_MESSAGE);
        }
    }

    private static Set<BlockPos> getAround(BlockPos pos) {
        return Set.of(pos.up(), pos.down(), pos.east(), pos.west(), pos.north(), pos.south());
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

    public static void maxStamina() {
        var playerManager = server.getPlayerManager();
        playerManager.getPlayerList().forEach(player -> player.addStatusEffect(new StatusEffectInstance(StatusEffects.SATURATION, 50, 200, false, false, false)));
    }


    public static void update() {
        var playerManager = server.getPlayerManager();

        maxStamina();
        TeamPlayerListHeader.TeamList();

        //準備の制限時間(毎回入力するのがダルいので定数化．クラス内定数にしないのは途中でConfig変えられたりする可能性を考えているため)
        var prepareTime = ModConfig.SystemConfig.Times.prepareTime;

        //現在の時間
        var now = Instant.now();

        var hideMimicList = HideController.getHidingPlayers();
        var isAllPlayerMimicked = TeamCreateandDelete.getHiders().getPlayerList().size() == hideMimicList.size();

        if (isAllPlayerMimicked && (lastMimickedTime == null || Duration.between(lastMimickedTime, now).toSeconds() > 5)) {
            lastMimickedTime = now;
        }

        //経過時間
        var currentTime = isAllPlayerMimicked ? Duration.ofSeconds(prepareTime - 5).plus(Duration.between(lastMimickedTime, now)) : Duration.between(startedTime, now);

        //残り時間
        var remainsTime = Duration.ofSeconds(prepareTime).minus(currentTime);

        //残り時間が０以下のとき
        if (remainsTime.isNegative()) {
            var seekerTeam = TeamCreateandDelete.getSeekers();
            if (seekerTeam != null) {
                seekerTeam.getPlayerList()
                        .stream()
                        .map(playerManager::getPlayer)
                        .filter(Objects::nonNull)
                        .forEach(player -> {
                            unlockPlayerMovement(player);
                            player.changeGameMode(GameMode.ADVENTURE);
                            player.interactionManager.changeGameMode(GameMode.SURVIVAL);
                            player.getAbilities().allowFlying = true;
                            player.sendAbilitiesUpdate();
                        });
            }

            suspendGame();
            //タイトルバーにSTARTと表示
            var startMessage = new TitleS2CPacket(new LiteralText("     ").setStyle(Style.EMPTY.withFormatting(Formatting.UNDERLINE))
                    .append(new LiteralText("Block").setStyle(Style.EMPTY.withColor(Formatting.WHITE).withFormatting(Formatting.UNDERLINE)))
                    .append(new LiteralText("Hide").setStyle(Style.EMPTY.withColor(Formatting.GREEN).withFormatting(Formatting.UNDERLINE)))
                    .append(new LiteralText("And").setStyle(Style.EMPTY.withColor(Formatting.YELLOW).withFormatting(Formatting.UNDERLINE)))
                    .append(new LiteralText("Seek").setStyle(Style.EMPTY.withColor(Formatting.RED).withFormatting(Formatting.UNDERLINE)))
                    .append(new LiteralText("     ").setStyle(Style.EMPTY.withFormatting(Formatting.UNDERLINE))));
            var startsubMessage = new SubtitleS2CPacket(new LiteralText("START").setStyle(Style.EMPTY.withColor(Formatting.YELLOW)));
            playerManager.getPlayerList().forEach(player -> player.networkHandler.sendPacket(startMessage));
            playerManager.getPlayerList().forEach(player -> player.networkHandler.sendPacket(startsubMessage));

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

    private static void suspendGame() {
        job.cancel(null);
        preparationtimeProgress.setVisible(false);
    }

    public static void stopGame() {
        suspendGame();
        server.getPlayerManager().getPlayerList()
                .forEach(player -> {
                    player.changeGameMode(GameMode.SPECTATOR);
                    player.getInventory().remove(itemStack -> BhasItems.isModItem(itemStack.getItem()), 64, player.playerScreenHandler.getCraftingInput());
                });
        GameState.setCurrentState(GameState.Phase.IDLE);
    }

    private static void registerMessage() {
        //ボスバーを表示
        preparationtimeProgress.setVisible(true);

        if (!FabricLoader.getInstance().isDevelopmentEnvironment() && !checkTeamcount()) {
            stopGame();
            return;
        }

        var seekersTeam = TeamCreateandDelete.getSeekers();
        if (seekersTeam != null) {
            seekersTeam.getPlayerList()
                    .stream()
                    .map(server.getPlayerManager()::getPlayer)
                    .filter(Objects::nonNull)
                    .forEach(PreparationTime::lockPlayerMovement);
        }

        teamMessage();

        //非同期スレッドの呼び出し
        job = CoroutineProvider.loop(Duration.ofMillis(500), () -> {
            //マインクラフトの実行スレッドを呼び出して,処理が終了するまで待機させる
            //実はserverはそれ自体が実行スレッドとして扱われているため，このように非同期スレッドからマイクラの実行スレッドに処理を渡すことができる
            server.submitAndJoin(PreparationTime::update);
        });
    }

    static {
        //最初は非表示にしておく
        preparationtimeProgress.setVisible(false);
    }

}
