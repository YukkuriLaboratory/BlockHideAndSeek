package com.github.yukulab.blockhideandseekmod.mixin;

import com.github.yukulab.blockhideandseekmod.game.GameStart;
import com.github.yukulab.blockhideandseekmod.game.GameState;
import com.github.yukulab.blockhideandseekmod.game.PreparationTime;
import com.github.yukulab.blockhideandseekmod.game.TeamSelector;
import com.github.yukulab.blockhideandseekmod.item.BhasItems;
import com.github.yukulab.blockhideandseekmod.util.BlockHighlighting;
import com.github.yukulab.blockhideandseekmod.util.FlyController;
import com.github.yukulab.blockhideandseekmod.util.HideController;
import com.github.yukulab.blockhideandseekmod.util.TeamCreateAndDelete;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitiesDestroyS2CPacket;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.world.GameMode;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(ServerWorld.class)
public abstract class MixinServerWorld {
    @Shadow
    @Final
    private MinecraftServer server;

    @Shadow
    public abstract boolean isFlat();

    @Shadow
    public abstract void playSound(@Nullable PlayerEntity except, double x, double y, double z, SoundEvent sound, SoundCategory category, float volume, float pitch);

    @Shadow
    public abstract ServerScoreboard getScoreboard();

    @Inject(
            method = "addPlayer",
            at = @At("TAIL")
    )
    private void sendFakeBlockPacket(ServerPlayerEntity player, CallbackInfo ci) {
        HideController.getHidingBlocks().forEach(entry -> {
            var packet = new BlockUpdateS2CPacket(entry.getKey(), entry.getValue());
            player.networkHandler.sendPacket(packet);
        });
        var hidingPlayers = HideController.getHidingPlayers()
                .stream()
                .map(uuid -> server.getPlayerManager().getPlayer(uuid))
                .filter(Objects::nonNull)
                .map(ServerPlayerEntity::getId)
                .toList();
        var packet = new EntitiesDestroyS2CPacket(new IntArrayList(hidingPlayers));
        player.networkHandler.sendPacket(packet);
    }

    @Inject(
            method = "addPlayer",
            at = @At("TAIL")
    )
    private void onPlayerJoinWorld(ServerPlayerEntity player, CallbackInfo ci) {
        TeamSelector.addBossBarTarget(player);
        PreparationTime.addBossBarTarget(player);
        GameStart.addBossBarTarget(player);
        FlyController.registerPlayer(player);
        BlockHighlighting.resendHighlightData(player);

        var currentState = GameState.getCurrentState();
        if (currentState == GameState.Phase.IDLE) {
            player.getInventory().remove(
                    itemStack -> BhasItems.isModItem(itemStack.getItem()),
                    64,
                    player.getInventory()
            );
        }

        var currentTeam = player.getScoreboardTeam();
        var seekersTeam = TeamCreateAndDelete.getSeekers();
        if (currentState == GameState.Phase.PREPARE && currentTeam == seekersTeam) {
            PreparationTime.lockPlayerMovement(player);
        }

        if ((currentState == GameState.Phase.PREPARE || currentState == GameState.Phase.RUNNING) && currentTeam != seekersTeam) {
            HideController.showHidingBlockHighlight(player);
        }

        if ((currentState == GameState.Phase.PREPARE || currentState == GameState.Phase.RUNNING) && currentTeam == null) {
            player.changeGameMode(GameMode.SPECTATOR);
            var observerTeam = TeamCreateAndDelete.getObservers();
            if (observerTeam != null) {
                getScoreboard().addPlayerToTeam(player.getEntityName(), observerTeam);
            }
        }
    }

    @Inject(
            method = "removePlayer",
            at = @At("TAIL")
    )
    private void onPlayerLeaveWorld(ServerPlayerEntity player, Entity.RemovalReason reason, CallbackInfo ci) {
        TeamSelector.removeBossBarTarget(player);
        PreparationTime.removeBossBarTarget(player);
        GameStart.removeBossBarTarget(player);
        FlyController.removePlayer(player);
    }
}
