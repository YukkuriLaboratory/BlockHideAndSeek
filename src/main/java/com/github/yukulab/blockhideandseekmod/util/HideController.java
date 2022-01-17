package com.github.yukulab.blockhideandseekmod.util;

import com.github.yukulab.blockhideandseekmod.BlockHideAndSeekMod;
import com.github.yukulab.blockhideandseekmod.config.ModConfig;
import com.github.yukulab.blockhideandseekmod.game.GameController;
import com.github.yukulab.blockhideandseekmod.util.extention.ServerPlayerEntityKt;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Fertilizable;
import net.minecraft.block.SlabBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Stream;

public class HideController {

    private static final Map<UUID, BlockState> selectingBlocks = Maps.newHashMap();

    /**
     * 隠れようとしているプレイヤー
     */
    private static final Map<UUID, BlockPos> tryingPlayers = Maps.newHashMap();
    private static final Map<UUID, Integer> tryingTimes = Maps.newHashMap();

    /**
     * 隠れているプレイヤー
     */
    private static final Map<UUID, BlockPos> hidingPlayers = HashBiMap.create();
    private static final Map<UUID, Entity> ridingTarget = Maps.newHashMap();
    private static final Map<BlockPos, BlockState> hidingBlocks = Maps.newHashMap();

    public static final String SELECTED_BLOCK = "selectedBlock";
    private static final String HIDING_MESSAGE = "hidingmessage";
    private static final String HIDE_PROGRESS = "hidingprogress";

    public static void clearSelectors() {
        selectingBlocks.keySet().forEach(uuid -> HudDisplay.removeActionbarText(uuid, SELECTED_BLOCK));
        selectingBlocks.clear();
    }

    public static void updateSelectedBlock(PlayerEntity player, BlockState blockState) {
        var uuid = player.getUuid();
        var text = new LiteralText("対象ブロック: ").append(new TranslatableText(blockState.getBlock().getTranslationKey()));
        HudDisplay.setActionBarText(uuid, SELECTED_BLOCK, text);
        selectingBlocks.put(uuid, blockState);
    }

    public static boolean isHideableBlock(BlockState blockState) {
        var block = blockState.getBlock();
        return blockState.getMaterial().isSolid() && !(block instanceof SlabBlock) && !(block instanceof Fertilizable);
    }

    public static void removeSelectedBlock(PlayerEntity player) {
        var uuid = player.getUuid();
        HudDisplay.removeActionbarText(uuid, SELECTED_BLOCK);
        selectingBlocks.remove(uuid);
    }

    public static boolean isHiding(PlayerEntity player) {
        var uuid = player.getUuid();
        return hidingPlayers.containsKey(uuid) || tryingPlayers.containsKey(uuid);
    }

    public static @Nullable BlockState getHidingBlock(BlockPos pos) {
        return hidingBlocks.get(pos);
    }

    public static Set<Map.Entry<BlockPos, BlockState>> getHidingBlocks() {
        return hidingBlocks.entrySet();
    }

    public static BiMap<UUID, BlockPos> getHidingPlayerMaps() {
        return HashBiMap.create(hidingPlayers);
    }

    public static Set<UUID> getHidingPlayers() {
        return hidingPlayers.keySet();
    }

    public static void tryHide(PlayerEntity player) {
        if (!canHide(player)) {
            return;
        }
        tryingPlayers.put(player.getUuid(), player.getBlockPos());
    }

    public static void cancelHiding(ServerPlayerEntity player) {
        var uuid = player.getUuid();
        if (tryingPlayers.containsKey(uuid)) {
            tryingPlayers.remove(uuid);
            tryingTimes.remove(uuid);
            HudDisplay.removeActionbarText(uuid, HIDE_PROGRESS);
        } else if (hidingPlayers.containsKey(uuid)) {
            hidingPlayers.remove(uuid);
            hidingBlocks.remove(player.getBlockPos());
            var riding = ridingTarget.get(uuid);
            if (riding != null) {
                HudDisplay.removeActionbarText(uuid, HIDING_MESSAGE);
                var destroyPacket = new EntitiesDestroyS2CPacket(riding.getId());
                player.networkHandler.sendPacket(destroyPacket);
                player.removeStatusEffect(StatusEffects.INVISIBILITY);
                player.setInvulnerable(false);

                var blockPacket = new BlockUpdateS2CPacket(player.getBlockPos(), Blocks.AIR.getDefaultState());

                var playerTracker = ServerPlayerEntityKt.getPlayerTracker(player);
                BlockHideAndSeekMod.SERVER
                        .getPlayerManager()
                        .getPlayerList()
                        .stream()
                        .peek(p -> p.networkHandler.sendPacket(blockPacket))
                        .filter(p -> p.getUuid() != uuid)
                        .forEach(playerTracker::updateTrackedStatus);

                BlockHighlighting.removeHighlight(player.getBlockPos());
            }
        }
    }

    public static void showHidingBlockHighlight(ServerPlayerEntity player) {
        getHidingPlayerMaps().values().forEach(pos -> BlockHighlighting.addPlayer(pos, player));
    }

    private static boolean canHide(PlayerEntity player) {
        var uuid = player.getUuid();
        var scoreboard = BlockHideAndSeekMod.SERVER.getScoreboard();
        var playerteam = scoreboard.getPlayerTeam(player.getEntityName());

        if (!GameController.isGameRunning()) {
            return false;
        }

        if (TeamCreateAndDelete.getHiders() != playerteam || playerteam == null) {
            return false;
        }

        if (player.hasStatusEffect(StatusEffects.INVISIBILITY)) {
            var redText = new LiteralText("").setStyle(Style.EMPTY.withColor(Formatting.RED));
            HudDisplay.setActionBarText(uuid, HIDE_PROGRESS, redText.append(Text.of("透明化中は擬態できません")), 30L);
            return false;
        }

        var blockPos = player.getBlockPos();
        var world = player.world;
        var standingBlock = world.getBlockState(blockPos);
        var floorBlock = world.getBlockState(blockPos.down());

        var redText = new LiteralText("").setStyle(Style.EMPTY.withColor(Formatting.RED));
        var showTextTime = 30L;
        if (floorBlock.getMaterial().isLiquid() || standingBlock.getMaterial().isLiquid()) {
            HudDisplay.setActionBarText(uuid, HIDE_PROGRESS, redText.append(Text.of("水中では擬態できません")), showTextTime);
            return false;
        }
        if (!standingBlock.isAir()) {
            HudDisplay.setActionBarText(uuid, HIDE_PROGRESS, redText.append(Text.of("ブロックに重なった状態では擬態できません")), showTextTime);
            return false;
        }
        if (floorBlock.isAir()) {
            HudDisplay.setActionBarText(uuid, HIDE_PROGRESS, redText.append(Text.of("空中では擬態できません")), showTextTime);
            return false;
        }
        if (!selectingBlocks.containsKey(uuid) && !isHideableBlock(floorBlock)) {
            var text = new LiteralText("このブロックには擬態できません!").setStyle(Style.EMPTY.withColor(Formatting.RED));
            HudDisplay.setActionBarText(player.getUuid(), HIDE_PROGRESS, text, 30L);
            return false;
        }
        return true;
    }

    private static void setHide(UUID uuid) {
        var playerManager = BlockHideAndSeekMod.SERVER.getPlayerManager();
        var player = playerManager.getPlayer(uuid);
        if (player != null) {
            hidingPlayers.put(uuid, player.getBlockPos());
            var riding = EntityType.SILVERFISH.create(player.world);
            var playerPos = player.getBlockPos();
            var pos = new Vec3d(playerPos.getX() + 0.5, playerPos.getY() + 0.5, playerPos.getZ() + 0.5);
            if (riding != null) {
                riding.setPosition(pos);
                riding.setInvisible(true);

                riding.passengerList = ImmutableList.of(player);
                var networkHandler = player.networkHandler;
                networkHandler.sendPacket(new EntitySpawnS2CPacket(riding));
                networkHandler.sendPacket(new EntityTrackerUpdateS2CPacket(riding.getId(), riding.getDataTracker(), false));
                networkHandler.sendPacket(new EntityPassengersSetS2CPacket(riding));

                ridingTarget.put(uuid, riding);
                player.setStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, Integer.MAX_VALUE, 1), null);
                player.setInvulnerable(true);

                var defaultBlock = player.world.getBlockState(player.getBlockPos().down());
                var block = selectingBlocks.getOrDefault(uuid, defaultBlock);
                hidingBlocks.put(playerPos, block);
                var blockPacket = new BlockUpdateS2CPacket(playerPos, block);
                var playerTracker = ServerPlayerEntityKt.getPlayerTracker(player);
                playerManager.getPlayerList()
                        .stream()
                        .peek(p -> p.networkHandler.sendPacket(blockPacket))
                        .filter(p -> p.getUuid() != uuid)
                        .forEach(playerTracker::stopTracking);

                var text = new LiteralText("擬態中:左クリックで解除");
                HudDisplay.setActionBarText(player.getUuid(), HIDING_MESSAGE, text);
                player.playSound(SoundEvents.ENTITY_EVOKER_CAST_SPELL, SoundCategory.PLAYERS, 1.0f, 2.0f);

                var hiderTeam = TeamCreateAndDelete.getHiders();
                Collection<String> hiders = hiderTeam != null ? hiderTeam.getPlayerList() : Set.of();
                var observerTeam = TeamCreateAndDelete.getObservers();
                Collection<String> observers = observerTeam != null ? observerTeam.getPlayerList() : Set.of();

                var targetPlayers = Stream.concat(hiders.stream(), observers.stream())
                        .map(playerManager::getPlayer)
                        .filter(Objects::nonNull)
                        .toList();
                BlockHighlighting.setHighlight(playerPos, targetPlayers, entity -> {
                    entity.setCustomName(player.getName());
                    entity.setCustomNameVisible(true);
                });
            }
        }
    }

    private static boolean isEqual(BlockPos posOne, BlockPos posTwo) {
        if (posOne.getX() != posTwo.getX()) {
            return false;
        } else if (posOne.getY() != posTwo.getY()) {
            return false;
        } else return posOne.getZ() == posTwo.getZ();
    }

    static {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            var playerManager = server.getPlayerManager();

            var tryingMap = Maps.newHashMap(tryingPlayers);
            tryingMap.forEach(((uuid, blockPos) -> {
                tryingPlayers.remove(uuid);
                var player = playerManager.getPlayer(uuid);
                if (player != null && player.isSneaking() && isEqual(player.getBlockPos(), blockPos)) {
                    tryingPlayers.put(uuid, player.getBlockPos());
                    var time = tryingTimes.getOrDefault(uuid, 0) + 1;

                    var waitTime = (float) ModConfig.SystemConfig.Times.hideWaitTime;
                    if (time > waitTime) {
                        tryingTimes.remove(uuid);
                        tryingPlayers.remove(uuid);
                        HudDisplay.removeActionbarText(uuid, HIDE_PROGRESS);
                        setHide(uuid);
                    } else {
                        var progress = ((int) Math.floor(time / waitTime * 100));
                        HudDisplay.setActionBarText(uuid, HIDE_PROGRESS, HudDisplay.createProgressBar(progress));
                        tryingTimes.put(uuid, time);
                    }
                } else {
                    HudDisplay.removeActionbarText(uuid, HIDE_PROGRESS);
                    tryingTimes.remove(uuid);
                }
            }));
        });
    }
}
