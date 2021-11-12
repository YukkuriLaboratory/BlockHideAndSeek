package com.iduki.blockhideandseekmod.mixin;

import com.iduki.blockhideandseekmod.item.ServerSideItem;
import com.iduki.blockhideandseekmod.util.UUIDHolder;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.UUID;

@Mixin(PacketByteBuf.class)
public abstract class MixinPacketByteBuf implements UUIDHolder {
    private UUID bhas$uuid;

    @ModifyVariable(
            method = "writeItemStack",
            at = @At("HEAD"),
            ordinal = 0
    )
    private ItemStack swapItemStack(ItemStack itemStack) {
        var item = itemStack.getItem();
        if (item instanceof ServerSideItem serverSideItem && bhas$uuid != null) {
            return serverSideItem.createVisualStack(itemStack);
        }
        return itemStack;
    }

    @Override
    public void setUUID(@Nullable UUID uuid) {
        bhas$uuid = uuid;
    }

    @Override
    public @Nullable UUID getUUID() {
        return bhas$uuid;
    }
}
