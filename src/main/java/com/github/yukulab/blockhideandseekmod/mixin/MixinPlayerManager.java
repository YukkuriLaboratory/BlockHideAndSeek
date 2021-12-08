package com.github.yukulab.blockhideandseekmod.mixin;

import com.github.yukulab.blockhideandseekmod.BlockHideAndSeekMod;
import com.github.yukulab.blockhideandseekmod.util.OperatorNotifier;
import com.github.yukulab.blockhideandseekmod.util.TeamCreateAndDelete;
import com.github.yukulab.blockhideandseekmod.util.UUIDHolder;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerManager.class)
public class MixinPlayerManager {
    @Inject(
            method = "onPlayerConnect",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;<init>(Lnet/minecraft/server/MinecraftServer;Lnet/minecraft/network/ClientConnection;Lnet/minecraft/server/network/ServerPlayerEntity;)V"
            )
    )
    private void setUUID(ClientConnection connection, ServerPlayerEntity player, CallbackInfo ci) {
        if (connection instanceof UUIDHolder holder) {
            holder.setUUID(player.getUuid());
        }
    }

    @Inject(
            method = "onPlayerConnect",
            at = @At("TAIL")
    )
    private void sendNotify(ClientConnection connection, ServerPlayerEntity player, CallbackInfo ci) {
        OperatorNotifier.sendNotify(player);
    }

    @Inject(
            method = "respawnPlayer",
            at = @At("RETURN")
    )
    private void checkSeekerRespawn(ServerPlayerEntity player, boolean alive, CallbackInfoReturnable<ServerPlayerEntity> cir) {
        var scoreboard = BlockHideAndSeekMod.SERVER.getScoreboard();
        var team = scoreboard.getPlayerTeam(player.getEntityName());
        if (team != null && team == TeamCreateAndDelete.getSeekers()) {
            player.changeGameMode(GameMode.ADVENTURE);
            player.interactionManager.changeGameMode(GameMode.SURVIVAL);
            player.getAbilities().allowFlying = true;
            player.sendAbilitiesUpdate();
        }
    }
}
