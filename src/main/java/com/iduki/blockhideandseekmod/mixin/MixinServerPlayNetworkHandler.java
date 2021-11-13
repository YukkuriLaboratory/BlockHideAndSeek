package com.iduki.blockhideandseekmod.mixin;

import com.iduki.blockhideandseekmod.game.HideController;
import com.iduki.blockhideandseekmod.item.ServerSideItem;
import com.iduki.blockhideandseekmod.util.UUIDHolder;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.item.ItemStack;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
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
        var blockPacket = ((BlockUpdateS2CPacket) packet);
        var pos = blockPacket.getPos();
        var fakeBlock = HideController.getHidingBlock(pos);
        if (fakeBlock != null) {
            instance.sendPacket(new BlockUpdateS2CPacket(pos, fakeBlock));
        } else {
            instance.sendPacket(packet);
        }
    }

    @Inject(
            method = "onDisconnected",
            at = @At("HEAD")
    )
    private void cancelHiding(Text reason, CallbackInfo ci) {
        HideController.cancelHiding(player);
    }

    @Inject(
            method = "sendPacket(Lnet/minecraft/network/Packet;Lio/netty/util/concurrent/GenericFutureListener;)V",
            at = @At("HEAD")
    )
    private void setUUID(Packet<?> packet, GenericFutureListener<? extends Future<? super Void>> listener, CallbackInfo ci) {
        ((UUIDHolder) packet).setUUID(((UUIDHolder) connection).getUUID());
    }

    @Redirect(
            method = "onCreativeInventoryAction",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/packet/c2s/play/CreativeInventoryActionC2SPacket;getItemStack()Lnet/minecraft/item/ItemStack;"
            )
    )
    private ItemStack swapStack(CreativeInventoryActionC2SPacket instance) {
        var stack = instance.getItemStack();
        var tag = stack.getNbt();
        if (tag != null && tag.contains(ServerSideItem.TAG_KEY)) {
            return convertItemStack(stack);
        }
        return stack;
    }

    @SuppressWarnings("deprecation")
    private ItemStack convertItemStack(ItemStack itemStack) {
        var copy = itemStack.copy();
        var id = new Identifier(copy.getOrCreateNbt().getString(ServerSideItem.TAG_KEY));
        var item = Registry.ITEM.get(id);
        copy.item = item;
        var tag = copy.getNbt();
        if (tag != null) {
            tag.remove(ServerSideItem.TAG_KEY);
            var defaultVisualStack = ((ServerSideItem) item).createVisualStack(item.getDefaultStack());
            var displayTagFirst = copy.getSubNbt("display");
            var displayTagSecond = defaultVisualStack.getSubNbt("display");
            if (displayTagFirst != null && displayTagSecond != null) {
                var nameTagFirst = displayTagFirst.get("Name");
                if (nameTagFirst != null && nameTagFirst.equals(displayTagSecond.get("Name"))) {
                    displayTagFirst.remove("Name");
                }
                if (displayTagFirst.isEmpty()) {
                    tag.remove("display");
                }
            }
            if (tag.isEmpty()) {
                copy.setNbt(null);
            }
        }
        return copy;
    }
}
