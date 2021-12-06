package com.github.yukulab.blockhideandseekmod.mixin;

import com.github.yukulab.blockhideandseekmod.util.OperatorNotifier;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MinecraftDedicatedServer.class)
public abstract class MixinMinecraftDedicatedServer {

    @Redirect(
            method = "setupServer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/dedicated/MinecraftDedicatedServer;setFlightEnabled(Z)V"
            )
    )
    private void allowFlight(MinecraftDedicatedServer instance, boolean b) {
        if (!b) {
            OperatorNotifier.addNotify(Text.of("Info:AllowFlightをtrueにしています"));
        }
        instance.setFlightEnabled(true);
    }

}
