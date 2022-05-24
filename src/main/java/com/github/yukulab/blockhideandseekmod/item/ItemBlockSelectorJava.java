package com.github.yukulab.blockhideandseekmod.item;

import com.github.yukulab.blockhideandseekmod.util.HideController;
import com.github.yukulab.blockhideandseekmod.util.HudDisplay;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.CooldownUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ItemBlockSelectorJava extends LoreItem implements JavaServerSideItem {

    private static final String SELECT_BLOCK_ERROR = "selectBlockError";

    private static final Settings SETTINGS = new Settings();

    public ItemBlockSelectorJava() {
        super(SETTINGS);
    }

    @Override
    public Text getName() {
        return new LiteralText("セレクター");
    }

    @Override
    public List<Text> getLore() {
        return List.of(
                LoreText.clickText(ACTION.RCLICK, "擬態するブロックを変更します"),
                LoreText.clickText(ACTION.SRCLICK, "擬態するブロックをリセットします")
        );
    }

    @Override
    public @NotNull Item getVisualItem() {
        return Items.CLOCK;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        var player = context.getPlayer();
        if (player != null) {
            HideController.cancelHiding(((ServerPlayerEntity) player));
            if (resetBlock(player)) {
                return ActionResult.PASS;
            }
            var block = context.getWorld().getBlockState(context.getBlockPos());
            float pitch;
            if (HideController.isHideableBlock(block)) {
                HideController.updateSelectedBlock(player, block);
                pitch = 1.5f;
            } else {
                var text = new LiteralText("このブロックには擬態できません!").setStyle(Style.EMPTY.withColor(Formatting.RED));
                HudDisplay.setActionBarText(player.getUuid(), SELECT_BLOCK_ERROR, text, 30L);
                pitch = 0.1f;
            }
            player.playSound(SoundEvents.ENTITY_ENDER_EYE_DEATH, SoundCategory.PLAYERS, 75, pitch);
            //連打防止用
            var coolTime = 10;
            player.getItemCooldownManager().set(this, coolTime);
            ((ServerPlayerEntity) player).networkHandler.sendPacket(new CooldownUpdateS2CPacket(getVisualItem(), coolTime));
            return ActionResult.PASS;
        }
        return ActionResult.FAIL;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        HideController.cancelHiding(((ServerPlayerEntity) user));
        resetBlock(user);
        return TypedActionResult.pass(user.getStackInHand(hand));
    }

    private boolean resetBlock(PlayerEntity player) {
        if (player.isSneaking()) {
            HideController.removeSelectedBlock(player);
            return true;
        }
        return false;
    }
}
