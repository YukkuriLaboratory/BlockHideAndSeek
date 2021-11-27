package com.iduki.blockhideandseekmod.mixin;

import com.iduki.blockhideandseekmod.BlockHideAndSeekMod;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class MixinServerPlayerEntity {
    @Inject(
            method = "onDeath",
            at = @At("HEAD")
    )
    private void removeHiders(DamageSource source, CallbackInfo ci) {
        var entity = ((ServerPlayerEntity) (Object) this);
        var scoreboard = BlockHideAndSeekMod.SERVER.getScoreboard();
        var playerName = entity.getEntityName();
        var playerTeam = scoreboard.getPlayerTeam(playerName);
        scoreboard.removePlayerFromTeam(playerName, playerTeam);
    }
}
