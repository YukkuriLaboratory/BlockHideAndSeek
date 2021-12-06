package com.github.yukulab.blockhideandseekmod.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.text.Text;

import java.util.List;

/**
 * デフォルトでLoreを持つItem向けのクラス
 */
public abstract class LoreItem extends Item {
    /**
     * デフォルトのLoreを定義する用
     *
     * @return Listの各要素がそのまま行として表現されます
     */
    public abstract List<Text> getLore();

    @Override
    public ItemStack getDefaultStack() {
        var itemStack = super.getDefaultStack();
        var nbt = itemStack.getOrCreateNbt();

        var compound = new NbtCompound();
        var lore = new NbtList();
        lore.addAll(getLore().stream().map(this::toNbtData).toList());
        compound.put(ItemStack.LORE_KEY, lore);
        nbt.put(ItemStack.DISPLAY_KEY, compound);

        return itemStack;
    }

    public LoreItem(Settings settings) {
        super(settings);
    }

    private NbtString toNbtData(Text text) {
        return NbtString.of(Text.Serializer.toJson(text));
    }
}
