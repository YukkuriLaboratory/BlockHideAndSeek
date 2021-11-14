package com.iduki.blockhideandseekmod.game;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.iduki.blockhideandseekmod.BlockHideAndSeekMod;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

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
    private static final Map<UUID, BlockPos> hidingPlayers = Maps.newHashMap();
    private static final Map<UUID, Entity> ridingTarget = Maps.newHashMap();
    private static final Map<BlockPos, BlockState> hidingBlocks = Maps.newHashMap();

    //TODO call when game stopped
    public static void clearSelecters() {
        selectingBlocks.clear();
    }

    //TODO call when player selected Blocks
    public static void updateSelectedBlock(PlayerEntity player, BlockState blockState) {
        selectingBlocks.put(player.getUuid(), blockState);
    }

    public static @Nullable BlockState getHidingBlock(BlockPos pos) {
        return hidingBlocks.get(pos);
    }

    public static Set<Map.Entry<BlockPos, BlockState>> getHidingBlocks() {
        return hidingBlocks.entrySet();
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
        if (hidingPlayers.containsKey(uuid)) {
            hidingPlayers.remove(uuid);
            hidingBlocks.remove(player.getBlockPos());
            var riding = ridingTarget.get(uuid);
            if (riding != null) {
                HudDisplay.removeActionbarText(uuid, HIDING_MESSAGE);
                var destroyPacket = new EntitiesDestroyS2CPacket(riding.getId());
                player.networkHandler.sendPacket(destroyPacket);
                player.setInvisible(false);
                player.setInvulnerable(false);
                var playerDataPacket = new EntityTrackerUpdateS2CPacket(player.getId(), player.getDataTracker(), true);

                var blockPacket = new BlockUpdateS2CPacket(player.getBlockPos(), Blocks.AIR.getDefaultState());
                var showPlayerPacket = new EntitySpawnS2CPacket(player);
                var playerList = BlockHideAndSeekMod.SERVER
                        .getPlayerManager()
                        .getPlayerList();

                playerList.stream()
                        .peek(p -> p.networkHandler.sendPacket(blockPacket))
                        .filter(p -> p.getUuid() != uuid)
                        .map(p -> p.networkHandler)
                        .forEach(handler -> {
                            handler.sendPacket(showPlayerPacket);
                            handler.sendPacket(playerDataPacket);
                        });
            }
        }
    }

    private static boolean canHide(PlayerEntity player) {
        //TODO check player joined hiding team and game started
        var uuid = player.getUuid();
        return !hidingPlayers.containsKey(uuid) && !tryingPlayers.containsKey(uuid);
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
                player.setInvisible(true);
                player.setInvulnerable(true);
                var hidePacket = new EntityTrackerUpdateS2CPacket(player.getId(), player.getDataTracker(), false);
                player.networkHandler.sendPacket(hidePacket);

                var defaultBlock = player.world.getBlockState(player.getBlockPos().down());
                var block = selectingBlocks.getOrDefault(uuid, defaultBlock);
                hidingBlocks.put(playerPos, block);
                var blockPacket = new BlockUpdateS2CPacket(playerPos, block);
                var hidePlayerPacket = new EntitiesDestroyS2CPacket(player.getId());
                playerManager.getPlayerList()
                        .stream()
                        .peek(p -> p.networkHandler.sendPacket(blockPacket))
                        .filter(p -> p.getUuid() != uuid)
                        .map(p -> p.networkHandler)
                        .forEach(handler -> handler.sendPacket(hidePlayerPacket));

                var text = new LiteralText("擬態中:左クリックで解除");
                HudDisplay.setActionBarText(player.getUuid(), HIDING_MESSAGE, text);
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

    private static final String HIDING_MESSAGE = "hidingmessage";
    private static final String HIDE_PROGRESS = "hidingprogress";

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

                    var waitTime = 60.0;
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