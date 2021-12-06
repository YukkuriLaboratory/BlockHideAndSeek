package com.github.yukulab.blockhideandseekmod.mixin;

import com.github.yukulab.blockhideandseekmod.game.HideController;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class MixinEntity {

    @Inject(
            method = "setSneaking",
            at = @At("HEAD")
    )
    private void setPlayerHidingState(boolean sneaking, CallbackInfo ci) {
        var entity = ((Entity) (Object) this);
        if (entity instanceof ServerPlayerEntity player && sneaking) {
            HideController.tryHide(player);
        }
    }
}
