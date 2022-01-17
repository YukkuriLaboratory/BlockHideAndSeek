package com.github.yukulab.blockhideandseekmod.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.List;


public class ItemTemplate extends LoreItem implements ServerSideItem {
    private static final Item.Settings SETTINGS = new Item.Settings();

    public ItemTemplate() {
        super(SETTINGS);
    }

    @Override
    public Text getName() {
        return new LiteralText("アイテム名");
    }

    @Override
    public List<Text> getLore() {
        return List.of(
                new LiteralText("説明や挙動など"),
                Text.of("効果時間など" + "ModConfig.ItemConfig.ItemTemplate.duration"),
                Text.of("クールタイムなど" + "ModConfig.ItemConfig.ItemTemplate.coolTime")
        );
    }

    @Override
    public Item getVisualItem() {
        return Items.GLASS;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        var stack = user.getStackInHand(hand);
        return TypedActionResult.success(stack);
    }
}
