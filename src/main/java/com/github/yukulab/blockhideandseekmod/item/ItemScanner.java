package com.github.yukulab.blockhideandseekmod.item;

import com.github.yukulab.blockhideandseekmod.config.ModConfig;
import com.github.yukulab.blockhideandseekmod.util.HudDisplay;
import com.github.yukulab.blockhideandseekmod.util.TeamCreateandDelete;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
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
import org.spongepowered.include.com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * 近くの隠れているBlockを指し示すアイテム
 */
public class ItemScanner extends LoreItem implements ServerSideItem {

    private final static String SCAN_RESULT = "scanResult";
    private final static String SCAN_NOTIFY = "scanNotify";

    private static final Map<UUID, Long> currentTime = Maps.newHashMap();

    private static final String TICK_ID = "tick";

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
    public List<Text> getLore() {
        return List.of(
                new LiteralText("右クリック: 近くのミミックの人数を表示します"),
                new LiteralText("shift+右クリック: 右クリック時の半分の範囲でスキャンします"),
                new LiteralText("捜索範囲: " + getScanLength() + "ブロック"),
                new LiteralText("クールタイム: " + MathHelper.floor((getCoolTime() / 20.0) * 10) / 10 + "秒"),
                new LiteralText("(一番近くのミミックの場所をコンパスに一定時間表示します)")
        );
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (entity instanceof ServerPlayerEntity) {
            var nbt = stack.getOrCreateNbt();
            var tickId = getTickId(nbt);
            var tick = currentTime.getOrDefault(tickId, 0L) - 1;
            currentTime.put(tickId, tick);
            if (tick == 0) {
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
                .filter(p -> p.distanceTo(player) < isSneakingScanLength(player))
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

        var nearplayer = player.world.getClosestPlayer(player.getX(), player.getY(), player.getZ(), isSneakingScanLength(player), p -> p.isTeamPlayer(TeamCreateandDelete.getHiders()));

        var nbt = stack.getOrCreateNbt();

        if (nearplayer != null) {
            nbt.put(LODESTONE_POS_KEY, NbtHelper.fromBlockPos(nearplayer.getBlockPos()));
            var var10000 = World.CODEC.encodeStart(NbtOps.INSTANCE, player.world.getRegistryKey());
            Logger var10001 = LOGGER;
            Objects.requireNonNull(var10001);
            var10000.resultOrPartial(var10001::error).ifPresent((nbtElement) -> nbt.put(LODESTONE_DIMENSION_KEY, nbtElement));
            nbt.putBoolean(LODESTONE_TRACKED_KEY, true);
        }
        if (!player.isSneaking()) {
            player.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 1.0f, 0.5f);
        } else {
            player.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 1.0f, 3.0f);
        }

        var tickId = getTickId(nbt);
        currentTime.put(tickId, (long) ModConfig.ItemConfig.ItemScanner.duration);

        var coolTime = getCoolTime() + getDuration();
        player.getItemCooldownManager().set(this, coolTime);
        //クライアントサイドではコンパスに見えてるのでコンパスにクールダウンを表示する
        ((ServerPlayerEntity) player).networkHandler.sendPacket(new CooldownUpdateS2CPacket(getVisualItem(), coolTime));


        return TypedActionResult.success(stack);
    }

    private static UUID getTickId(NbtCompound nbt) {
        UUID tickId;
        if (nbt.contains(TICK_ID)) {
            tickId = nbt.getUuid(TICK_ID);
        } else {
            tickId = UUID.randomUUID();
            nbt.putUuid(TICK_ID, tickId);
        }
        return tickId;
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

    private static double isSneakingScanLength(PlayerEntity player) {
        if (!player.isSneaking()) {
            return ModConfig.ItemConfig.ItemScanner.scanLength;
        } else {
            return ModConfig.ItemConfig.ItemScanner.scanLength / 2;
        }
    }
}
