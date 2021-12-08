package com.github.yukulab.blockhideandseekmod.mixin;

import com.github.yukulab.blockhideandseekmod.game.GameState;
import com.github.yukulab.blockhideandseekmod.util.HideController;
import com.github.yukulab.blockhideandseekmod.util.TeamCreateAndDelete;
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
        if (team == null) {
            return;
        }
        // Observerへのチーム変更やらをここで一気にやってます
        if (team == TeamCreateAndDelete.getHiders()) {
            scoreboard.removePlayerFromTeam(playerName, team);
            var observerTeam = TeamCreateAndDelete.getObservers();
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

            HideController.showHidingBlockHighlight(player);
        } else if (team == TeamCreateAndDelete.getSeekers()) {
            player.changeGameMode(GameMode.ADVENTURE);
            player.interactionManager.changeGameMode(GameMode.SURVIVAL);
            player.getAbilities().allowFlying = true;
            player.sendAbilitiesUpdate();
        }
    }
}
