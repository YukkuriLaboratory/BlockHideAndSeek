package com.github.yukulab.blockhideandseekmod.game;

import com.github.yukulab.blockhideandseekmod.BlockHideAndSeekMod;
import com.github.yukulab.blockhideandseekmod.config.ModConfig;
import com.github.yukulab.blockhideandseekmod.util.*;
import com.google.common.collect.Maps;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.Instant;
import java.util.*;


/**
 * GameStartで呼び出される準備時間の処理
 */
public class Prepare implements GameStatus {

    /**
     * 時間計測用
     * 変数をセットしたときのシステム時間を記録します
     */
    private final Instant startedTime;

    /**
     * 時間計測用
     * 全員が擬態を終えてからの時間
     */
    private Instant lastMimickedTime;


    private final Map<UUID, Entity> lockerEntities = Maps.newHashMap();

    private static final String BLIND_MESSAGE = "blindMessage";

    //毎回クラス名入力するのがダルいので定数として扱う
    private static final MinecraftServer server = BlockHideAndSeekMod.SERVER;

    public Prepare() {
        BlockHideAndSeekMod.SERVER
                .getPlayerManager()
                .getPlayerList()
                .forEach(p -> p.addStatusEffect(new StatusEffectInstance(StatusEffects.INSTANT_HEALTH,10,99,false,false,false)));
        startedTime = Instant.now();
        var seekersTeam = TeamCreateAndDelete.getSeekers();
        if (seekersTeam != null) {
            seekersTeam.getPlayerList()
                    .stream()
                    .map(server.getPlayerManager()::getPlayer)
                    .filter(Objects::nonNull)
                    .forEach(this::lockPlayerMovement);
        }
        //準備時間開始時にチーム別にタイトル表示
        var playerManager = server.getPlayerManager();
        Team seekers = TeamCreateAndDelete.getSeekers();
        var startSeekerMessage = new LiteralText("").append(Text.of("\n"))
                .append(new LiteralText("[鬼はスタートしたらミミックを探そう!]").setStyle(Style.EMPTY.withColor(Formatting.GREEN)));
        List<ServerPlayerEntity> seekersList = seekers.getPlayerList()
                .stream()
                .map(playerManager::getPlayer)
                .filter(Objects::nonNull)
                .toList();
        seekersList.forEach(player -> player.sendMessage(startSeekerMessage, false));

        Team hiders = TeamCreateAndDelete.getHiders();
        var startHiderMessage = new LiteralText("").append(Text.of("\n"))
                .append(new LiteralText("[ミミックは準備時間終了までに隠れよう!]").setStyle(Style.EMPTY.withColor(Formatting.GREEN)));
        List<ServerPlayerEntity> hidersList = hiders.getPlayerList()
                .stream()
                .map(playerManager::getPlayer)
                .filter(Objects::nonNull)
                .toList();
        hidersList.forEach(player -> player.sendMessage(startHiderMessage, false));
    }

    /**
     * プレイヤーに偽のEntity情報を送り，そのEntityの視点に固定します．
     * サーバー側からなにかアクションを加えるか，Playerがリログするまで操作はできません(リログは対策済み)
     */
    public void lockPlayerMovement(ServerPlayerEntity player) {
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

    private void unlockPlayerMovement(ServerPlayerEntity player) {
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

    private Set<BlockPos> getAround(BlockPos pos) {
        return Set.of(pos.up(), pos.down(), pos.east(), pos.west(), pos.north(), pos.south());
    }

    @NotNull
    @Override
    public ServerBossBar getProgressBar() {
        return new ServerBossBar(Text.of(""), BossBar.Color.BLUE, BossBar.Style.NOTCHED_20);
    }

    @Override
    public boolean onUpdate(@Nullable ServerBossBar progressBar) {
        var playerManager = server.getPlayerManager();

        PlayerUtil.setMaxStamina();
        TeamPlayerListHeader.TeamList();

        //準備の制限時間(毎回入力するのがダルいので定数化．クラス内定数にしないのは途中でConfig変えられたりする可能性を考えているため)
        var prepareTime = ModConfig.SystemConfig.Times.prepareTime;

        //現在の時間
        var now = Instant.now();

        var hideMimicList = HideController.getHidingPlayers();
        var isAllPlayerMimicked = TeamCreateAndDelete.getHiders().getPlayerList().size() == hideMimicList.size();

        if (isAllPlayerMimicked && (lastMimickedTime == null || Duration.between(lastMimickedTime, now).toSeconds() > 5)) {
            lastMimickedTime = now;
        }

        //経過時間
        var currentTime = isAllPlayerMimicked ? Duration.ofSeconds(prepareTime - 5).plus(Duration.between(lastMimickedTime, now)) : Duration.between(startedTime, now);

        //残り時間
        var remainsTime = Duration.ofSeconds(prepareTime).minus(currentTime);

        //残り時間が０以下のとき
        if (remainsTime.isNegative()) {
            return true;
        }

        if (progressBar != null) {
            //ボスバーのテキストを更新
            var timeText = Text.of("準備時間: " + remainsTime.toSeconds() + "秒");
            progressBar.setName(timeText);
            //ゲージ更新
            progressBar.setPercent(MathHelper.clamp(remainsTime.toSeconds() / ((float) prepareTime), 0, 1));
        }

        var isDisplayTime = Math.floor(currentTime.toMillis() / 100f / 5) % 2 == 0;

        if (currentTime.toSeconds() >= prepareTime - 4 && isDisplayTime) {
            playerManager.getPlayerList().forEach(player -> player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_HAT, SoundCategory.PLAYERS, 1.0f, 1.0f));
        }
        return false;
    }

    @Override
    public void onSuspend() {
        var seekerTeam = TeamCreateAndDelete.getSeekers();
        if (seekerTeam != null) {
            seekerTeam.getPlayerList()
                    .stream()
                    .map(server.getPlayerManager()::getPlayer)
                    .filter(Objects::nonNull)
                    .forEach(this::unlockPlayerMovement);
        }
    }

    @Nullable
    @Override
    public GameStatus next() {
        var playerManager = server.getPlayerManager();
        var seekerTeam = TeamCreateAndDelete.getSeekers();
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
                        //透明化消えてね(はーと)
                        player.setInvisible(false);
                    });
        }
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

        return new MainGame();
    }

    @Override
    public void onFinally() {
        //nothing//
    }
}
