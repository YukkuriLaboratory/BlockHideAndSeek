package com.iduki.blockhideandseekmod.item;

import com.iduki.blockhideandseekmod.BlockHideAndSeekMod;
import com.iduki.blockhideandseekmod.game.HideController;
import com.iduki.blockhideandseekmod.screen.HidersBlockScreen;
import com.iduki.blockhideandseekmod.util.HudDisplay;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.List;
import java.util.Objects;

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
    public List<Text> getLore() {
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
        HidersBlockScreen.open(((ServerPlayerEntity) user));
        user.getInventory().removeOne(itemStack);

        var text = new LiteralText("隠れているブロックが通知されました").setStyle(Style.EMPTY.withColor(Formatting.RED));
        var packet = new SubtitleS2CPacket(text);
        HideController.getHidingPlayers()
                .stream()
                .peek(uuid -> HudDisplay.setActionBarText(uuid, "blockNotify", text, 50L))
                .map(BlockHideAndSeekMod.SERVER.getPlayerManager()::getPlayer)
                .filter(Objects::nonNull)
                .forEach(player -> {
                    player.playSound(SoundEvents.ENTITY_WOLF_HOWL, SoundCategory.PLAYERS, 0.3f, 2);
                    player.networkHandler.sendPacket(packet);
                });
        return TypedActionResult.pass(itemStack);
    }
}
