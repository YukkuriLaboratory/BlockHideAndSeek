package com.github.yukulab.blockhideandseekmod.mixin;

import com.github.yukulab.blockhideandseekmod.util.HideController;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public class MixinPlayerEntity {
    @Inject(
            method = "damage",
            at = @At("RETURN")
    )
    private void cancelHiding(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        var player = ((PlayerEntity) (Object) this);
        var result = cir.getReturnValue();
        if (result && player instanceof ServerPlayerEntity serverPlayerEntity) {
            HideController.cancelHiding(serverPlayerEntity);
        }
    }
}
