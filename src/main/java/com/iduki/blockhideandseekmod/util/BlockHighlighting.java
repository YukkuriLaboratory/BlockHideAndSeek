package com.iduki.blockhideandseekmod.util;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.iduki.blockhideandseekmod.BlockHideAndSeekMod;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.ShulkerEntity;
import net.minecraft.network.packet.s2c.play.EntitiesDestroyS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.server.ServerTask;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

public class BlockHighlighting {

    private static final Multimap<BlockPos, UUID> showingPlayers = HashMultimap.create();
    private static final Map<BlockPos, ShulkerEntity> fakeEntities = Maps.newHashMap();

    /**
     * 対象の位置に偽のハイライトされたブロックの枠を追加します
     * ↓こういうやつ
     * https://www.spigotmc.org/attachments/upload_2020-1-5_16-46-16-png.478792/
     *
     * @param pos     対象の位置
     * @param players 表示するプレイヤー
     */
    public static void setHighlight(BlockPos pos, List<ServerPlayerEntity> players, Consumer<ShulkerEntity> entityEditConsumer) {
        if (players.isEmpty()) {
            return;
        }

        showingPlayers.putAll(pos, players.stream().map(Entity::getUuid).toList());
        var entity = EntityType.SHULKER.create(players.get(0).world);
        if (entity == null) {
            BlockHideAndSeekMod.LOGGER.error("cannot get shulker entity!!");
            return;
        }
        entityEditConsumer.accept(entity);
        entity.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        entity.setInvisible(true);
        entity.setGlowing(true);
        fakeEntities.put(pos, entity);

        var spawnPacket = new EntitySpawnS2CPacket(entity);
        var dataPacket = new EntityTrackerUpdateS2CPacket(entity.getId(), entity.getDataTracker(), false);
        players.forEach(player -> {
            var networkHandler = player.networkHandler;
            networkHandler.sendPacket(spawnPacket);
            networkHandler.sendPacket(dataPacket);
        });
    }

    /**
     * @param pos      対象の位置
     * @param showTick 表示時間
     * @param players  表示するプレイヤー
     * @see BlockHighlighting#setHighlight(BlockPos, List, Consumer)
     * 上記メソッドに時間制限を加えたもの
     */
    public static void setHighlight(BlockPos pos, int showTick, List<ServerPlayerEntity> players, Consumer<ShulkerEntity> entityEditConsumer) {
        setHighlight(pos, players, entityEditConsumer);
        var server = BlockHideAndSeekMod.SERVER;
        server.send(new ServerTask(server.getTicks() - 3 + showTick, () -> removeHighlight(pos)));
    }

    /**
     * ハイライトを削除します
     */
    public static void removeHighlight(BlockPos pos) {
        var entity = fakeEntities.remove(pos);
        if (entity == null) {
            showingPlayers.removeAll(pos);
            return;
        }
        var deletePacket = new EntitiesDestroyS2CPacket(entity.getId());
        showingPlayers.removeAll(pos)
                .stream()
                .map(BlockHideAndSeekMod.SERVER.getPlayerManager()::getPlayer)
                .filter(Objects::nonNull)
                .forEach(player -> player.networkHandler.sendPacket(deletePacket));
    }

    /**
     * 既存のハイライトにプレイヤーを追加します
     */
    public static void addPlayer(BlockPos pos, ServerPlayerEntity player) {
        if (showingPlayers.containsKey(pos)) {
            showingPlayers.put(pos, player.getUuid());
            resendHighlightData(player);
        }
    }

    public static void resendHighlightData(ServerPlayerEntity player) {
        showingPlayers.entries()
                .stream()
                .filter(entry -> entry.getValue() == player.getUuid())
                .map(Map.Entry::getKey)
                .map(fakeEntities::get)
                .forEach(entity -> {
                    var spawnPacket = new EntitySpawnS2CPacket(entity);
                    var dataPacket = new EntityTrackerUpdateS2CPacket(entity.getId(), entity.getDataTracker(), true);
                    var networkHandler = player.networkHandler;
                    networkHandler.sendPacket(spawnPacket);
                    networkHandler.sendPacket(dataPacket);
                });
    }
}
