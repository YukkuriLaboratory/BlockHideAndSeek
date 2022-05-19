package com.github.yukulab.blockhideandseekmod.mixin;

import com.github.yukulab.blockhideandseekmod.game.GameController;
import com.github.yukulab.blockhideandseekmod.game.Prepare;
import com.github.yukulab.blockhideandseekmod.item.ItemFakeSummonerJava;
import com.github.yukulab.blockhideandseekmod.util.FlyController;
import com.github.yukulab.blockhideandseekmod.util.HideController;
import com.github.yukulab.blockhideandseekmod.util.TeamCreateAndDelete;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdatePlayerAbilitiesC2SPacket;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public class MixinServerPlayNetworkHandler {

    @Shadow
    @Final
    public ClientConnection connection;

    @Shadow
    public ServerPlayerEntity player;

    @Inject(
            method = "onHandSwing",
            at = @At("RETURN")
    )
    private void callCancelHiding(HandSwingC2SPacket packet, CallbackInfo ci) {
        HideController.cancelHiding(player);
    }

    @Redirect(
            method = "onPlayerInteractBlock",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;sendPacket(Lnet/minecraft/network/Packet;)V"
            )
    )
    private void cancelHidingBlockUpdate(ServerPlayNetworkHandler instance, Packet<?> packet) {
        if (GameController.getCurrent() instanceof Prepare prepare && player.getScoreboardTeam() == TeamCreateAndDelete.getSeekers()) {
            prepare.lockPlayerMovement(player);
        }

        var blockPacket = ((BlockUpdateS2CPacket) packet);
        var pos = blockPacket.getPos();
        var fakeBlock = HideController.getHidingBlock(pos);
        if (fakeBlock != null) {
            instance.sendPacket(new BlockUpdateS2CPacket(pos, fakeBlock));
            return;
        }

        var decoyBlock = ItemFakeSummonerJava.getDecoyState(pos);
        if (decoyBlock != null) {
            instance.sendPacket(new BlockUpdateS2CPacket(pos, decoyBlock));
            return;
        }

        instance.sendPacket(packet);
    }

    @Inject(
            method = "onDisconnected",
            at = @At("HEAD")
    )
    private void cancelHiding(Text reason, CallbackInfo ci) {
        HideController.cancelHiding(player);
    }

    @Redirect(
            method = "onUpdatePlayerAbilities",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/packet/c2s/play/UpdatePlayerAbilitiesC2SPacket;isFlying()Z"
            )
    )
    private boolean checkFlyAble(UpdatePlayerAbilitiesC2SPacket instance) {
        if (instance.isFlying()) {
            return !player.interactionManager.getGameMode().isSurvivalLike() || FlyController.canFly(player);
        }
        return false;
    }

    @Inject(
            method = "onUpdatePlayerAbilities",
            at = @At("TAIL")
    )
    private void updateFlying(UpdatePlayerAbilitiesC2SPacket packet, CallbackInfo ci) {
        player.sendAbilitiesUpdate();
    }
}
