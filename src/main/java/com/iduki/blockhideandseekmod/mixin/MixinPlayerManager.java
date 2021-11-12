package com.iduki.blockhideandseekmod.mixin;

import com.iduki.blockhideandseekmod.mixin.interfaces.UUIDHolder;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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
}
