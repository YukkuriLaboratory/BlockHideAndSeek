package com.iduki.blockhideandseekmod.mixin;

import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MinecraftDedicatedServer.class)
public class MixinMinecraftDedicatedServer {
    @Redirect(
            method = "setupServer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/dedicated/MinecraftDedicatedServer;setFlightEnabled(Z)V"
            )
    )
    private void allowFlight(MinecraftDedicatedServer instance, boolean b) {
        instance.setFlightEnabled(true);
    }
}
