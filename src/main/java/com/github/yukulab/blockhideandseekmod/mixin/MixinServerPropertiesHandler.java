package com.github.yukulab.blockhideandseekmod.mixin;

import com.github.yukulab.blockhideandseekmod.util.OperatorNotifier;
import net.minecraft.server.dedicated.ServerPropertiesHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerPropertiesHandler.class)
public class MixinServerPropertiesHandler {
    @Redirect(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/dedicated/ServerPropertiesHandler;getInt(Ljava/lang/String;I)I",
                    ordinal = 3
            )
    )
    private int addNotify(ServerPropertiesHandler instance, String s, int i) {
        if (i > 0) {
            OperatorNotifier.addNotify(Text.of("Info:SpawnProtectionを0にしています"));
        }
        return 0;
    }
}
