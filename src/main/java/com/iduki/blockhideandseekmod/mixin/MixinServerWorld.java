package com.iduki.blockhideandseekmod.mixin;

import com.iduki.blockhideandseekmod.game.HideController;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitiesDestroyS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
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
}
