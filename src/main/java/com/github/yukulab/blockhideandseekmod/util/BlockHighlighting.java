package com.github.yukulab.blockhideandseekmod.util;

import com.github.yukulab.blockhideandseekmod.BlockHideAndSeekMod;
import com.github.yukulab.blockhideandseekmod.entity.BhasEntityTypes;
import com.google.common.collect.Maps;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.mob.ShulkerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class BlockHighlighting {

    private static final Map<BlockPos, ShulkerEntity> fakeEntities = Maps.newHashMap();

    private static final Map<BlockPos, Long> willRemove = Maps.newHashMap();

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

        var world = players.get(0).world;
        var entity = BhasEntityTypes.BLOCKHIGHLIGHT.create(world);
        if (entity == null) {
            BlockHideAndSeekMod.LOGGER.error("cannot get BlockHighlightEntity!!");
            return;
        }
        entityEditConsumer.accept(entity);
        entity.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        world.spawnEntity(entity);
        fakeEntities.put(pos, entity);
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
        willRemove.put(pos, ((long) showTick));
    }

    /**
     * ハイライトを削除します
     */
    public static void removeHighlight(BlockPos pos) {
        var entity = fakeEntities.remove(pos);
        if (entity == null) {
            return;
        }
        entity.discard();
    }

    public static void clearHighlight() {
        fakeEntities.keySet().forEach(BlockHighlighting::removeHighlight);
    }

    static {
        ServerTickEvents.START_SERVER_TICK.register(server -> willRemove.entrySet().stream().toList().forEach(entry -> {
            var currentTick = entry.getValue() - 1;
            if (currentTick <= 0) {
                removeHighlight(entry.getKey());
            } else {
                willRemove.put(entry.getKey(), currentTick);
            }
        }));
    }
}
