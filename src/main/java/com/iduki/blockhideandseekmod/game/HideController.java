package com.iduki.blockhideandseekmod.game;

import com.google.common.collect.Maps;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.Map;
import java.util.UUID;

public class HideController {

    /**
     * 隠れようとしているプレイヤー
     */
    private static final Map<UUID, BlockPos> tryingPlayers = Maps.newHashMap();
    private static final Map<UUID, Integer> tryingTimes = Maps.newHashMap();

    /**
     * 隠れているプレイヤー
     */
    private static final Map<UUID, BlockPos> hidingPlayers = Maps.newHashMap();

    //TODO call on player was just sneaking
    public static void tryHide(PlayerEntity player) {
        if (!canHide(player)) {
            return;
        }
        tryingPlayers.put(player.getUuid(), player.getBlockPos());
    }

    private static boolean canHide(PlayerEntity player) {
        //TODO check player joined hiding team and game started
        var uuid = player.getUuid();
        return !hidingPlayers.containsKey(uuid) && !tryingPlayers.containsKey(uuid);
    }

    private static void setHide(UUID uuid) {
        //TODO ride fake silverfish and send invisible highlighted Shulker and send block packet to seeker
    }

    private static boolean isEqual(BlockPos posOne, BlockPos posTwo) {
        if (posOne.getX() != posTwo.getX()) {
            return false;
        } else if (posOne.getY() != posTwo.getY()) {
            return false;
        } else return posOne.getZ() == posTwo.getZ();
    }

    private static final String HIDE_PROGRESS = "hiding";

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
