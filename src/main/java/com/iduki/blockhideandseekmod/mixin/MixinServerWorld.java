package com.iduki.blockhideandseekmod.mixin;

import com.iduki.blockhideandseekmod.game.HideController;
import com.iduki.blockhideandseekmod.game.PreparationTime;
import com.iduki.blockhideandseekmod.game.TeamSelector;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitiesDestroyS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
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
    public abstract void playSound(@Nullable PlayerEntity except, double x, double y, double z, SoundEvent sound, SoundCategory category, float volume, float pitch);

    @Shadow
    public abstract void playSoundFromEntity(@Nullable PlayerEntity except, Entity entity, SoundEvent sound, SoundCategory category, float volume, float pitch);

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
    private void addBossBarTarget(ServerPlayerEntity player, CallbackInfo ci) {
        TeamSelector.addBossBarTarget(player);
        PreparationTime.addBossBarTarget(player);
    }

    @Inject(
            method = "removePlayer",
            at = @At("TAIL")
    )
    private void removeBossBarTarget(ServerPlayerEntity player, Entity.RemovalReason reason, CallbackInfo ci) {
        TeamSelector.removeBossBarTarget(player);
        PreparationTime.removeBossBarTarget(player);
    }
}
