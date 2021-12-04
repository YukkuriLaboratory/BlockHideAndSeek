package com.iduki.blockhideandseekmod.item;

import com.iduki.blockhideandseekmod.config.ModConfig;
import com.iduki.blockhideandseekmod.game.TeamCreateandDelete;
import com.iduki.blockhideandseekmod.util.HudDisplay;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtOps;
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
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Objects;

/**
 * 近くの隠れているBlockを指し示すアイテム
 */
public class ItemScanner extends LoreItem implements ServerSideItem {

    private final static String SCAN_RESULT = "scanResult";
    private final static String SCAN_NOTIFY = "scanNotify";

    private static final String TICK = "tick";

    public static final String LODESTONE_POS_KEY = "LodestonePos";
    public static final String LODESTONE_DIMENSION_KEY = "LodestoneDimension";
    public static final String LODESTONE_TRACKED_KEY = "LodestoneTracked";
    private static final Logger LOGGER = LogManager.getLogger();

    private final static Settings SETTINGS = new Settings();

    public ItemScanner() {
        super(SETTINGS);
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
    List<Text> getLore() {
        return List.of(
                new LiteralText("右クリック: 近くのミミックの人数を表示します"),
                new LiteralText("捜索範囲: " + getScanLength() + "ブロック"),
                new LiteralText("クールタイム: " + MathHelper.floor((getCoolTime() / 20.0) * 10) / 10 + "秒")
        );
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (entity instanceof ServerPlayerEntity) {
            var nbt = stack.getOrCreateNbt();
            var nowTick = nbt.getInt(TICK);
            nbt.putInt(TICK, nowTick - 1);
            if (nowTick - 1 == 0) {
                nbt.putBoolean(LODESTONE_TRACKED_KEY, false);
                nbt.remove(LODESTONE_DIMENSION_KEY);
                nbt.remove(LODESTONE_POS_KEY);
            }
        }
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        var stack = player.getStackInHand(hand);
        var nearestPlayer = player.world
                .getPlayers()
                .stream()
                .filter(p -> p.isTeamPlayer(TeamCreateandDelete.getHiders()))
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

        var nearplayer = player.world.getClosestPlayer(player.getX(), player.getY(), player.getZ(), getScanLength(), p -> p.isTeamPlayer(TeamCreateandDelete.getHiders()));

        var nbt = stack.getOrCreateNbt();

        if (nearplayer != null) {
            nbt.put(LODESTONE_POS_KEY, NbtHelper.fromBlockPos(nearplayer.getBlockPos()));
            var var10000 = World.CODEC.encodeStart(NbtOps.INSTANCE, player.world.getRegistryKey());
            Logger var10001 = LOGGER;
            Objects.requireNonNull(var10001);
            var10000.resultOrPartial(var10001::error).ifPresent((nbtElement) -> nbt.put(LODESTONE_DIMENSION_KEY, nbtElement));
            nbt.putBoolean(LODESTONE_TRACKED_KEY, true);

            var coolTime = getCoolTime() + getDuration();
            nbt.putInt(TICK, ModConfig.ItemConfig.ItemScanner.duration);
            player.getItemCooldownManager().set(this, coolTime);
            //クライアントサイドではコンパスに見えてるのでコンパスにクールダウンを表示する
            ((ServerPlayerEntity) player).networkHandler.sendPacket(new CooldownUpdateS2CPacket(getVisualItem(), getCoolTime()));
        }


        return TypedActionResult.success(stack);
    }


    //以下3つのメソッドは途中で設定が変わってもいいようにメソッド化して毎回元のフィールドを参照してる
    private static double getScanLength() {
        return ModConfig.ItemConfig.ItemScanner.scanLength;
    }

    private static int getCoolTime() {
        return ModConfig.ItemConfig.ItemScanner.coolTime;
    }

    private static int getDuration() {
        return ModConfig.ItemConfig.ItemScanner.duration;
    }
}
