package com.github.yukulab.blockhideandseekmod.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ThreadedAnvilChunkStorage.EntityTracker.class)
public class MixinEntityTracker {
    @Shadow
    @Final
    public Entity entity;

    /**
     * Invisibleがついているプレイヤーの情報を他プレイヤーに送信しないようにします.
     */
    @Inject(
            method = "updateTrackedStatus(Lnet/minecraft/server/network/ServerPlayerEntity;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void preventHidingPlayerDataSending(ServerPlayerEntity player, CallbackInfo ci) {
        if (entity instanceof ServerPlayerEntity mimic && mimic.hasStatusEffect(StatusEffects.INVISIBILITY)) {
            ci.cancel();
        }
    }
}
