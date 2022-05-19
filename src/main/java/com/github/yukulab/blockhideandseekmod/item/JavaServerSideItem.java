package com.github.yukulab.blockhideandseekmod.item;

import dev.uten2c.strobo.serversideitem.RenderType;
import dev.uten2c.strobo.serversideitem.ServerSideItem;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

public interface JavaServerSideItem extends ServerSideItem {
    @NotNull
    @Override
    default ItemStack createVisualStack(@NotNull ItemStack itemStack, @NotNull ServerPlayerEntity serverPlayerEntity, @NotNull RenderType renderType) {
        return ServerSideItem.DefaultImpls.createVisualStack(this, itemStack, serverPlayerEntity, renderType);
    }
}
