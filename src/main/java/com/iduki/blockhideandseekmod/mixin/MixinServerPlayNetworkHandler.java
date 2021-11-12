package com.iduki.blockhideandseekmod.mixin;

import com.iduki.blockhideandseekmod.item.ServerSideItem;
import com.iduki.blockhideandseekmod.mixin.interfaces.UUIDHolder;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.item.ItemStack;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
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
