package com.iduki.blockhideandseekmod.item;

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

public class ItemHidingBlockViewer extends LoreItem implements ServerSideItem {

    private static final Settings SETTINGS = new Settings();

    public ItemHidingBlockViewer() {
        super(SETTINGS);
    }

    @Override
    public Text getName() {
        return new LiteralText("ブロックゲッター");
    }

    @Override
    List<Text> getLore() {
        return List.of(
                new LiteralText("右クリック: 現在擬態しているブロックの名前を確認することができます")
        );
    }

    @Override
    public Item getVisualItem() {
        return Items.ENCHANTED_BOOK;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        var itemStack = user.getStackInHand(hand);
        //TODO インベントリーにハイドしているミミックのブロックを入れる
        user.getInventory().removeOne(itemStack);
        return TypedActionResult.pass(itemStack);
    }
}
