package com.iduki.blockhideandseekmod.item;

import com.iduki.blockhideandseekmod.game.HideController;
import com.iduki.blockhideandseekmod.game.HudDisplay;
import net.minecraft.block.SlabBlock;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.CooldownUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;

import java.util.List;

public class ItemBlockSelector extends LoreItem implements ServerSideItem {

    private static final String SELECT_BLOCK_ERROR = "selectBlockError";

    private static final Settings SETTINGS = new Settings();

    public ItemBlockSelector() {
        super(SETTINGS);
    }

    @Override
    public Text getName() {
        return new LiteralText("セレクター");
    }

    @Override
    List<Text> getLore() {
        return List.of(
                new LiteralText("右クリック: 擬態するブロックを変更します"),
                new LiteralText("シフト右クリック: 擬態するブロックをリセットします")
        );
    }

    @Override
    public Item getVisualItem() {
        return Items.CLOCK;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        var player = context.getPlayer();
        if (player != null) {
            if (player.isSneaking()) {
                HideController.removeSelectedBlock(player);
                return ActionResult.PASS;
            }
            var block = context.getWorld().getBlockState(context.getBlockPos());
            if (block.getMaterial().isSolid() && !(block.getBlock() instanceof SlabBlock)) {
                HideController.updateSelectedBlock(player, block);
            } else {
                var text = new LiteralText("このブロックには擬態できません!").setStyle(Style.EMPTY.withColor(Formatting.RED));
                HudDisplay.setActionBarText(player.getUuid(), SELECT_BLOCK_ERROR, text, 30L);
            }
            //連打防止用
            var coolTime = 10;
            player.getItemCooldownManager().set(this, coolTime);
            ((ServerPlayerEntity) player).networkHandler.sendPacket(new CooldownUpdateS2CPacket(getVisualItem(), coolTime));
            return ActionResult.PASS;
        }
        return ActionResult.FAIL;
    }
}
