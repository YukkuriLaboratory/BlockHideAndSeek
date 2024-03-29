package com.github.yukulab.blockhideandseekmod.item;

import com.github.yukulab.blockhideandseekmod.BlockHideAndSeekMod;
import com.github.yukulab.blockhideandseekmod.config.Config;
import com.github.yukulab.blockhideandseekmod.screen.HidersBlockScreen;
import com.github.yukulab.blockhideandseekmod.util.HideController;
import com.github.yukulab.blockhideandseekmod.util.HudDisplay;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
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
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class ItemHidingBlockViewerJava extends LoreItem implements JavaServerSideItem {

    private static final Settings SETTINGS = new Settings();

    public ItemHidingBlockViewerJava() {
        super(SETTINGS);
    }

    @Override
    public Text getName() {
        return new LiteralText("ブロックゲッター");
    }

    @Override
    public List<Text> getLore() {
        return List.of(
                LoreText.clickText(ACTION.RCLICK, "現在擬態しているブロックの名前を確認することができます")
        );
    }

    @Override
    public @NotNull Item getVisualItem() {
        return Items.ENCHANTED_BOOK;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        var itemStack = user.getStackInHand(hand);
        HidersBlockScreen.open(((ServerPlayerEntity) user));
        user.getInventory().removeOne(itemStack);

        Consumer<ServerPlayerEntity> notifyTask;
        var text = new LiteralText("隠れているブロックが通知されました").setStyle(Style.EMPTY.withColor(Formatting.RED));
        if (isSubTitle()) {
            var emptyTitlePacket = new TitleS2CPacket(Text.of(""));
            var packet = new SubtitleS2CPacket(text);
            notifyTask = player -> {
                player.networkHandler.sendPacket(emptyTitlePacket);
                player.networkHandler.sendPacket(packet);
            };
        } else {
            notifyTask = player -> HudDisplay.setActionBarText(player.getUuid(), "blockNotify", text, 50L);
        }
        HideController.getHidingPlayers()
                .stream()
                .map(BlockHideAndSeekMod.SERVER.getPlayerManager()::getPlayer)
                .filter(Objects::nonNull)
                .forEach(player -> {
                    player.playSound(SoundEvents.ENTITY_WOLF_HOWL, SoundCategory.PLAYERS, 0.3f, 2);
                    notifyTask.accept(player);
                });
        return TypedActionResult.pass(itemStack);
    }

    private static boolean isSubTitle() {
        return Config.Item.HidingBlockViewer.getNotifyOnTitle();
    }
}
