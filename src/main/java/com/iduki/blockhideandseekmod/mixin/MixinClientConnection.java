package com.iduki.blockhideandseekmod.mixin;

import com.iduki.blockhideandseekmod.mixin.interfaces.UUIDHolder;
import net.minecraft.network.ClientConnection;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

import java.util.UUID;

@Mixin(ClientConnection.class)
public class MixinClientConnection implements UUIDHolder {

    private UUID bhas$uuid;


    @Override
    public void setUUID(@Nullable UUID uuid) {
        bhas$uuid = uuid;
    }

    @Override
    public @Nullable UUID getUUID() {
        return bhas$uuid;
    }
}
