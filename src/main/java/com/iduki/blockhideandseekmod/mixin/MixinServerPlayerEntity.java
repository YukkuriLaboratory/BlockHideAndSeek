package com.iduki.blockhideandseekmod.mixin;

import com.iduki.blockhideandseekmod.game.GameState;
import com.iduki.blockhideandseekmod.game.HideController;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.network.packet.s2c.play.PlayerSpawnS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameMode;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(ServerPlayerEntity.class)
public abstract class MixinServerPlayerEntity {
    @Shadow
    @Final
    public MinecraftServer server;

    @Shadow
    public ServerPlayNetworkHandler networkHandler;

    @Inject(
            method = "onDeath",
            at = @At("HEAD")
    )
    private void removeHiders(DamageSource source, CallbackInfo ci) {
        var currentState = GameState.getCurrentState();
        if (currentState == GameState.Phase.IDLE || currentState == GameState.Phase.SELECT_TEAM) {
            return;
        }
        var player = ((ServerPlayerEntity) (Object) this);
        var scoreboard = server.getScoreboard();
        var playerName = player.getEntityName();
        var team = scoreboard.getPlayerTeam(playerName);
        // Observerへのチーム変更やらをここで一気にやってます
        if (team != null && (team.getName().equals("Hiders") || team.getName().equals("Seekerss"))) {
            scoreboard.removePlayerFromTeam(playerName, team);
            var observerTeam = scoreboard.getTeam("Observers");
            if (observerTeam != null) {
                scoreboard.addPlayerToTeam(playerName, observerTeam);
            }
            player.changeGameMode(GameMode.SPECTATOR);


            var playerManager = server.getPlayerManager();
            HideController.getHidingPlayers()
                    .stream()
                    .map(playerManager::getPlayer)
                    .filter(Objects::nonNull)
                    .map(PlayerSpawnS2CPacket::new)
                    .forEach(networkHandler::sendPacket);
        }
    }
}
