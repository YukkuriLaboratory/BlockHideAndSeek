package com.iduki.blockhideandseekmod.item;

import com.iduki.blockhideandseekmod.BlockHideAndSeekMod;
import com.iduki.blockhideandseekmod.config.ModConfig;
import com.iduki.blockhideandseekmod.game.HudDisplay;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.network.packet.s2c.play.CooldownUpdateS2CPacket;
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

/**
 * 近くの隠れているBlockを指し示すアイテム
 */
public class ItemScanner extends Item implements ServerSideItem {

    private final static String SCAN_RESULT = "scanResult";
    private final static String SCAN_NOTIFY = "scanNotify";

    public ItemScanner(Settings settings) {
        super(settings);
    }

    @Override
    public Text getName() {
        return new LiteralText("スキャナー").setStyle(Style.EMPTY.withColor(Formatting.GOLD));
    }

    @Override
    public Item getVisualItem() {
        return Items.COMPASS;
    }

    @Override
    public void postProcessNbt(NbtCompound nbt) {
        var compound = new NbtCompound();
        var lore = new NbtList();
        lore.add(toNbtData(new LiteralText("右クリック: 近くのミミックの人数を表示します")));
        lore.add(toNbtData(new LiteralText("捜索範囲: " + getScanLength() + "ブロック")));
        compound.put(ItemStack.LORE_KEY, lore);

        nbt.put(ItemStack.DISPLAY_KEY, compound);
    }

    private NbtString toNbtData(Text text) {
        return NbtString.of(Text.Serializer.toJson(text));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        var stack = player.getStackInHand(hand);
        var nearestPlayer = player.world
                .getPlayers()
                .stream()
                .filter(p -> p.isTeamPlayer(BlockHideAndSeekMod.SERVER.getScoreboard().getTeam("Hiders")))
                .filter(p -> p.distanceTo(player) < getScanLength())
                .toList();

        Text message;
        if (!nearestPlayer.isEmpty()) {
            message = new LiteralText(nearestPlayer.size() + "体のミミックを検出しました").setStyle(Style.EMPTY.withColor(Formatting.GREEN));
        } else {
            message = new LiteralText("範囲内にミミックが存在しません").setStyle(Style.EMPTY.withColor(Formatting.GREEN));
        }
        HudDisplay.setActionBarText(player.getUuid(), SCAN_RESULT, message, 30L);

        var notify = new LiteralText("スキャナーを検知しました").setStyle(Style.EMPTY.withColor(Formatting.RED));
        nearestPlayer.forEach(p -> {
            HudDisplay.setActionBarText(p.getUuid(), SCAN_NOTIFY, notify, 30L);
            p.playSound(SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 70, 2);
        });


        player.getItemCooldownManager().set(this, getCoolTime());
        //クライアントサイドではコンパスに見えてるのでコンパスにクールダウンを表示する
        ((ServerPlayerEntity) player).networkHandler.sendPacket(new CooldownUpdateS2CPacket(Items.COMPASS, getCoolTime()));

        return TypedActionResult.success(stack);
    }

    private static double getScanLength() {
        return ModConfig.ItemConfig.ItemScanner.scanLength;
    }

    private static int getCoolTime() {
        return ModConfig.ItemConfig.ItemScanner.coolTime;
    }
}
