package com.github.yukulab.blockhideandseekmod.item;

import com.github.yukulab.blockhideandseekmod.BlockHideAndSeekMod;
import com.github.yukulab.blockhideandseekmod.config.Config;
import com.github.yukulab.blockhideandseekmod.util.HudDisplay;
import com.github.yukulab.blockhideandseekmod.util.TeamCreateAndDelete;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
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
import org.jetbrains.annotations.NotNull;
import org.spongepowered.include.com.google.common.collect.Lists;
import org.spongepowered.include.com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ItemJammerJava extends LoreItem implements JavaServerSideItem {

    private final static String JAMMING_RESULT = "jammingResult";
    private final static String JAMMING_END_RESULT = "jammingEndResult";

    private static final Map<UUID, Long> currentTime = Maps.newHashMap();

    private static final String TICK_ID = "tick";

    private static final Settings SETTINGS = new Settings();

    private static final List<UUID> activatedPlayers = Lists.newArrayList();

    public ItemJammerJava() {
        super(SETTINGS);
    }

    @Override
    public Text getName() {
        return new LiteralText("ジャマー").setStyle(Style.EMPTY.withColor(Formatting.GOLD));
    }

    @Override
    public List<Text> getLore() {
        return List.of(
                LoreText.clickText(ACTION.RCLICK, "一定時間鬼のスキャナーを無効化します")
        );
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (entity instanceof ServerPlayerEntity player) {
            var nbt = stack.getOrCreateNbt();
            var tickId = getTickId(nbt);
            var tick = currentTime.getOrDefault(tickId, 0L) - 1;
            currentTime.put(tickId, tick);
            if (tick == 0) {
                activatedPlayers.remove(entity.getUuid());
                Text message;
                message = new LiteralText("ジャミングの効果が切れました").setStyle(Style.EMPTY.withColor(Formatting.GREEN));
                player.playSound(SoundEvents.BLOCK_CONDUIT_DEACTIVATE, SoundCategory.PLAYERS, 1.0f, 2.0f);
                HudDisplay.setActionBarText(entity.getUuid(), JAMMING_END_RESULT, message, 30L);
            }
        }
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        var stack = user.getStackInHand(hand);
        var nbt = stack.getOrCreateNbt();
        var coolTime = getCoolTime() + getDuration();
        var tickId = getTickId(nbt);
        activatedPlayers.add(user.getUuid());
        currentTime.put(tickId, (long) getDuration());
        user.getItemCooldownManager().set(this, coolTime);
        ((ServerPlayerEntity) user).networkHandler.sendPacket(new CooldownUpdateS2CPacket(getVisualItem(), coolTime));
        var message = new LiteralText("ジャミングを使用しました").setStyle(Style.EMPTY.withColor(Formatting.GREEN));

        user.playSound(SoundEvents.BLOCK_DISPENSER_DISPENSE, SoundCategory.PLAYERS, 10f, 1.2f);
        HudDisplay.setActionBarText(user.getUuid(), JAMMING_RESULT, message, 30L);

        var hiders = BlockHideAndSeekMod.SERVER.getPlayerManager().getPlayerList()
                .stream()
                .filter(p -> p.isTeamPlayer(TeamCreateAndDelete.getHiders()))
                .filter(p -> p != user)
                .filter(p -> user.getBlockPos().isWithinDistance(p.getBlockPos(), ItemScannerJava.getScanLength()))
                .toList();

        var otherMessage = new LiteralText("")
                .append(new LiteralText("").append(user.getDisplayName()).setStyle(Style.EMPTY.withColor(Formatting.GOLD)))
                .append(new LiteralText("がジャミングを使用しました").setStyle(Style.EMPTY.withColor(Formatting.GREEN)));
        hiders.forEach(p -> {
            p.playSound(SoundEvents.ITEM_SPYGLASS_USE, SoundCategory.PLAYERS, 10f, 0.8f);
            HudDisplay.setActionBarText(p.getUuid(), JAMMING_RESULT, otherMessage, 30L);
        });


        return TypedActionResult.pass(stack);
    }

    @Override
    public @NotNull Item getVisualItem() {
        return Items.SOUL_TORCH;
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

    private static int getCoolTime() {
        return Config.Item.Jammer.getCoolTime();
    }

    private static int getDuration() {
        return Config.Item.Jammer.getDuration();
    }

    public static boolean isActivated(UUID uuid) {
        return activatedPlayers.contains(uuid);
    }

    public static void clearJamming() {
        activatedPlayers.clear();
    }
}
