package com.iduki.blockhideandseekmod.util;

import com.google.common.collect.Maps;
import com.iduki.blockhideandseekmod.BlockHideAndSeekMod;
import com.iduki.blockhideandseekmod.config.ModConfig;
import com.iduki.blockhideandseekmod.game.GameState;
import net.minecraft.entity.player.PlayerEntity;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class FlyController {
    private final static Map<UUID, Boolean> lastFlyState = Maps.newConcurrentMap();
    private final static Map<UUID, Duration> flyableTime = Maps.newConcurrentMap();
    private final static Map<UUID, Duration> regenCoolTime = Maps.newConcurrentMap();
    private final static Map<UUID, Duration> useCoolTime = Maps.newConcurrentMap();

    private static Instant lastChecked = Instant.now();

    private final static ThreadPoolExecutor EXECUTOR = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);

    static {
        executeLoop();
    }

    public static void registerPlayer(PlayerEntity player) {
        var uuid = player.getUuid();
        lastFlyState.put(uuid, false);
        flyableTime.put(uuid, Duration.ZERO);
        regenCoolTime.put(uuid, maxRegenCoolTime);
        useCoolTime.put(uuid, maxUseCoolTime);
        player.getAbilities().allowFlying = true;
        player.sendAbilitiesUpdate();
    }

    public static void removePlayer(PlayerEntity player) {
        var uuid = player.getUuid();
        lastFlyState.remove(uuid);
        flyableTime.remove(uuid);
        regenCoolTime.remove(uuid);
        useCoolTime.remove(uuid);
        player.getAbilities().allowFlying = false;
        player.sendAbilitiesUpdate();
    }

    public static boolean canFly(PlayerEntity player) {
        var state = GameState.getCurrentState();
        if (state == GameState.Phase.IDLE || state == GameState.Phase.SELECT_TEAM) {
            return false;
        }

        var uuid = player.getUuid();
        var reuseTime = useCoolTime.get(uuid);
        if (!reuseTime.isZero() && !reuseTime.isNegative()) {
            return false;
        }
        var remainsTime = flyableTime.get(uuid);
        return !remainsTime.isZero() && !remainsTime.isNegative();
    }

    public static Duration getFlyAbleTime(PlayerEntity player) {
        return flyableTime.get(player.getUuid());
    }

    public static Duration getUseCoolTime(PlayerEntity player) {
        return useCoolTime.get(player.getUuid());
    }

    private static void update() {
        var server = BlockHideAndSeekMod.SERVER;
        var playerManager = server.getPlayerManager();
        var nowTime = Instant.now();

        flyableTime.keySet()
                .stream()
                .map(playerManager::getPlayer)
                .filter(Objects::nonNull)
                .forEach(player -> {
                    var uuid = player.getUuid();
                    var abilities = player.getAbilities();
                    if (abilities.flying && player.interactionManager.getGameMode().isSurvivalLike()) {
                        regenCoolTime.put(uuid, maxRegenCoolTime);

                        var currentFlyTime = flyableTime.get(uuid).minus(Duration.between(lastChecked, nowTime));
                        currentFlyTime = currentFlyTime.isNegative() ? Duration.ZERO : currentFlyTime;
                        flyableTime.put(uuid, currentFlyTime);
                        if (currentFlyTime.isZero()) {
                            useCoolTime.put(uuid, maxUseCoolTime);
                            server.submitAndJoin(() -> {
                                abilities.flying = false;
                                player.sendAbilitiesUpdate();
                            });
                        }
                    } else {
                        if (lastFlyState.get(uuid)) {
                            useCoolTime.put(uuid, maxUseCoolTime);
                        }

                        var remainsUseCoolTime = useCoolTime.get(uuid).minus(Duration.between(lastChecked, nowTime));
                        remainsUseCoolTime = remainsUseCoolTime.isNegative() ? Duration.ZERO : remainsUseCoolTime;
                        useCoolTime.put(uuid, remainsUseCoolTime);

                        var regenTime = regenCoolTime.get(uuid).minus(Duration.between(lastChecked, nowTime));
                        regenTime = regenTime.isNegative() ? Duration.ZERO : regenTime;
                        regenCoolTime.put(uuid, regenTime);

                        if (regenTime.isZero()) {
                            var flyTime = flyableTime.get(uuid).plus(Duration.between(lastChecked, nowTime));
                            var maxTime = getMaxTime();
                            flyTime = maxTime.minus(flyTime).isNegative() ? maxTime : flyTime;
                            flyableTime.put(uuid, flyTime);
                        }
                    }
                    lastFlyState.put(uuid, abilities.flying);
                });

        lastChecked = nowTime;
    }

    @SuppressWarnings({"BusyWait", "InfiniteLoopStatement"})
    private static void executeLoop() {
        EXECUTOR.execute(() -> {
            while (true) {
                var startTime = Instant.now();
                update();
                try {
                    var sleepTime = Duration.ofMillis(50).minus(Duration.between(startTime, Instant.now()));
                    sleepTime = sleepTime.isNegative() ? Duration.ZERO : sleepTime;
                    Thread.sleep(sleepTime.toMillis());
                } catch (InterruptedException e) {
                    BlockHideAndSeekMod.LOGGER.throwing(e);
                }
            }
        });
    }

    public static Duration getMaxTime() {
        return Duration.ofSeconds(ModConfig.ItemConfig.ItemFlyer.flyTime);
    }

    private final static Duration maxRegenCoolTime = Duration.ofSeconds(1);

    public final static Duration maxUseCoolTime = Duration.ofMillis(500);

}
