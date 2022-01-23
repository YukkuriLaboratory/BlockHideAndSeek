package com.github.yukulab.blockhideandseekmod.mixin;

import com.github.yukulab.blockhideandseekmod.game.GameController;
import com.github.yukulab.blockhideandseekmod.game.SelectTeam;
import com.github.yukulab.blockhideandseekmod.util.TeamCreateAndDelete;
import net.minecraft.entity.damage.DamageSource;
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
        var current = GameController.getCurrent();
        if (!GameController.isGameRunning() || current instanceof SelectTeam) {
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

        } else if (team == TeamCreateAndDelete.getSeekers()) {
            player.changeGameMode(GameMode.ADVENTURE);
            player.interactionManager.changeGameMode(GameMode.SURVIVAL);
            player.getAbilities().allowFlying = true;
            player.sendAbilitiesUpdate();
        }
    }
}
