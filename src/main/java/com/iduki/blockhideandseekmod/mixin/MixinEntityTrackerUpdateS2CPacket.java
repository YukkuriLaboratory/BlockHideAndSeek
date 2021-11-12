package com.iduki.blockhideandseekmod.mixin;

import com.iduki.blockhideandseekmod.item.ServerSideItem;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

@Mixin(EntityTrackerUpdateS2CPacket.class)
public class MixinEntityTrackerUpdateS2CPacket {

    @SuppressWarnings("unchecked")
    @Redirect(
            method = "write",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/data/DataTracker;entriesToPacket(Ljava/util/List;Lnet/minecraft/network/PacketByteBuf;)V"
            )
    )
    private <T> void write(List<DataTracker.Entry<T>> list, PacketByteBuf packetByteBuf) {
        for (DataTracker.Entry<T> entry : list) {
            var value = entry.get();
            if (value instanceof ItemStack itemStack) {
                var item = itemStack.getItem();
                if (item instanceof ServerSideItem serverSideItem) {
                    itemStack = serverSideItem.createVisualStack(itemStack);
                    itemStack.removeSubNbt("Display");
                }
                entry.set((T) itemStack);
            }
        }
        DataTracker.entriesToPacket((List<DataTracker.Entry<?>>) (Object) list, packetByteBuf);
    }
}
