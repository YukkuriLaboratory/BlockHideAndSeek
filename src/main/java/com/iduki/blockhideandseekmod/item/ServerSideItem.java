package com.iduki.blockhideandseekmod.item;

import com.google.common.collect.Maps;
import com.iduki.blockhideandseekmod.util.UUIDHolder;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.registry.Registry;

import java.util.Map;
import java.util.UUID;

public interface ServerSideItem {
    String TAG_KEY = "0cf10e31-a339-43ca-9785-a01beb08e008";
    Map<UUIDHolder, UUID> uuids = Maps.newHashMap();

    Item getVisualItem();

    @SuppressWarnings("deprecation")
    default ItemStack createVisualStack(ItemStack original) {
        var stack = original.copy();
        var item = stack.getItem();
        var id = Registry.ITEM.getId(item);

        if (!stack.hasCustomName()) {
            stack.setCustomName(stack.getItem().getName());
        }

        stack.item = getVisualItem();
        var tag = stack.getOrCreateNbt();
        tag.putString(TAG_KEY, id.toString());
        return stack;
    }
}
