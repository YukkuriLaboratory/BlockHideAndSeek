package com.github.yukulab.blockhideandseekmod.mixin;

import com.github.yukulab.blockhideandseekmod.game.GameController;
import com.github.yukulab.blockhideandseekmod.game.MainGame;
import com.github.yukulab.blockhideandseekmod.game.Prepare;
import com.github.yukulab.blockhideandseekmod.item.BhasItems;
import com.github.yukulab.blockhideandseekmod.util.BlockHighlighting;
import com.github.yukulab.blockhideandseekmod.util.FlyController;
import com.github.yukulab.blockhideandseekmod.util.HideController;
import com.github.yukulab.blockhideandseekmod.util.TeamCreateAndDelete;
import com.github.yukulab.blockhideandseekmod.util.extention.ServerPlayerEntityKt;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.GameMode;
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
        HideController.getHidingPlayers()
                .stream()
                .map(uuid -> server.getPlayerManager().getPlayer(uuid))
                .filter(Objects::nonNull)
                .map(ServerPlayerEntityKt::getPlayerTracker)
                .forEach(tracker -> tracker.stopTracking(player));
    }

    @Inject(
            method = "addPlayer",
            at = @At("TAIL")
    )
    private void onPlayerJoinWorld(ServerPlayerEntity player, CallbackInfo ci) {
        GameController.addBossBarTarget(player);
        FlyController.registerPlayer(player);
        BlockHighlighting.resendHighlightData(player);

        if (!GameController.isGameRunning()) {
            player.getInventory().remove(
                    itemStack -> BhasItems.isModItem(itemStack.getItem()),
                    64,
                    player.getInventory()
            );
        }

        var currentTeam = player.getScoreboardTeam();
        var seekersTeam = TeamCreateAndDelete.getSeekers();
        var current = GameController.getCurrent();
        if (current instanceof Prepare prepare && currentTeam == seekersTeam) {
            prepare.lockPlayerMovement(player);
        }

        if ((current instanceof Prepare || current instanceof MainGame) && currentTeam != seekersTeam) {
            HideController.showHidingBlockHighlight(player);
        }

        if ((current instanceof Prepare || current instanceof MainGame) && currentTeam == null) {
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
        GameController.removeBossBarTarget(player);
        FlyController.removePlayer(player);
    }
}
