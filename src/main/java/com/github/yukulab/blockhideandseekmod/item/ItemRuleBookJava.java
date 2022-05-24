package com.github.yukulab.blockhideandseekmod.item;

import com.github.yukulab.blockhideandseekmod.screen.RuleBookScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ItemRuleBookJava extends LoreItem implements JavaServerSideItem {

    private static final Item.Settings SETTINGS = new Item.Settings();

    public ItemRuleBookJava() {
        super(SETTINGS);
    }

    @Override
    public Text getName() {
        return new LiteralText("ルールブック");
    }

    @Override
    public List<Text> getLore() {
        return List.of(
                new LiteralText("BlockHideAndSeekのゲームルールブック").setStyle(Style.EMPTY.withColor(Formatting.WHITE))
        );
    }

    @Override
    public @NotNull Item getVisualItem() {
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
