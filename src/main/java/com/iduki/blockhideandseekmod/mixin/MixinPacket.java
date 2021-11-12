package com.iduki.blockhideandseekmod.mixin;

import com.iduki.blockhideandseekmod.item.ServerSideItem;
import com.iduki.blockhideandseekmod.util.UUIDHolder;
import net.minecraft.network.Packet;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

import java.util.UUID;

@Mixin(Packet.class)
public interface MixinPacket extends UUIDHolder {
    @Override
    default void setUUID(@Nullable UUID uuid) {
        ServerSideItem.uuids.put(this, uuid);
    }

    @Override
    default @Nullable UUID getUUID() {
        return ServerSideItem.uuids.get(this);
    }
}
