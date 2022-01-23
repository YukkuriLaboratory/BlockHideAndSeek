package com.github.yukulab.blockhideandseekmod.item;

import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.world.World;

import java.util.List;

public class ItemSurprisePumpkin extends LoreItem implements ServerSideItem {
    private static final Item.Settings SETTINGS = new Item.Settings();

    public ItemSurprisePumpkin() {
        super(SETTINGS);
    }

    private int time = 0;

    @Override
    public Text getName() {
        return new LiteralText("呪いのパンプキン");
    }

    @Override
    public List<Text> getLore() {
        return List.of(
                new LiteralText("")
        );
    }

    @Override
    public Item getVisualItem() {
        return Items.CARVED_PUMPKIN;
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (entity instanceof ServerPlayerEntity player) {
            time = time + 1;
            if (time >= ItemSurpriseBall.getDuration()) {
                player.getInventory().removeOne(new ItemStack(BhasItems.SURPRISEPUMPKIN));
                player.getInventory().removeStack(39);
                time = 0;
            }
        }
    }


}
