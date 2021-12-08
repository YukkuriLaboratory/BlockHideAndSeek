package com.github.yukulab.blockhideandseekmod.item;

import com.github.yukulab.blockhideandseekmod.screen.RuleBookScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.List;

public class ItemRuleBook extends LoreItem implements ServerSideItem {

    private static final Item.Settings SETTINGS = new Item.Settings();

    public ItemRuleBook() {
        super(SETTINGS);
    }

    @Override
    public Text getName() {
        return new LiteralText("ルールブック");
    }

    @Override
    public List<Text> getLore() {
        return List.of(
                new LiteralText("BlockHideAndSeekのゲームルールブック")
        );
    }

    @Override
    public Item getVisualItem() {
        return Items.KNOWLEDGE_BOOK;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        var player = ((ServerPlayerEntity) user);
        RuleBookScreen.open(player);
        user.playerScreenHandler.syncState();
        return TypedActionResult.pass(user.getStackInHand(hand));
    }

}
