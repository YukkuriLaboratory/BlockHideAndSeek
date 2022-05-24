package com.github.yukulab.blockhideandseekmod.item;

import com.github.yukulab.blockhideandseekmod.BlockHideAndSeekMod;
import com.github.yukulab.blockhideandseekmod.config.Config;
import com.github.yukulab.blockhideandseekmod.util.HideController;
import com.github.yukulab.blockhideandseekmod.util.HudDisplay;
import com.github.yukulab.blockhideandseekmod.util.extention.ServerPlayerEntityKt;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
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
import org.spongepowered.include.com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ItemBlink extends LoreItem implements JavaServerSideItem {

    private static final Map<UUID, Long> currentTime = Maps.newHashMap();

    private static final Item.Settings SETTINGS = new Item.Settings();

    private static final String TICK_ID = "tick";

    public ItemBlink() {
        super(SETTINGS);
    }

    @Override
    public Text getName() {
        return new LiteralText("セパレータ");
    }

    @Override
    public List<Text> getLore() {
        return List.of(
                LoreText.clickText(ACTION.RCLICK, "一定時間透明になります"),
                LoreText.unitText("効果時間", getDuration(), UNIT.TICK),
                LoreText.unitText("クールタイム", getCoolTime(), UNIT.TICK)
        );
    }

    @Override
    public @NotNull Item getVisualItem() {
        return Items.DRAGON_BREATH;
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (entity instanceof ServerPlayerEntity player) {
            var nbt = stack.getOrCreateNbt();
            var tickId = getTickId(nbt);
            var tick = currentTime.getOrDefault(tickId, 0L) - 1;
            currentTime.put(tickId, tick);
            if (tick == 0) {
                player.removeStatusEffect(StatusEffects.INVISIBILITY);
                player.playSound(SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS,1.0f,1.0f);
                var playerTracker = ServerPlayerEntityKt.getPlayerTracker(player);
                BlockHideAndSeekMod.SERVER
                        .getPlayerManager()
                        .getPlayerList()
                        .stream()
                        .filter(p -> p.getUuid() != player.getUuid())
                        .forEach(playerTracker::updateTrackedStatus);
            }
        }
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        var player = ((ServerPlayerEntity) user);
        var stack = user.getStackInHand(hand);
        if (!HideController.isHiding(player)) {
            var nbt = stack.getOrCreateNbt();
            var coolTime = getCoolTime() + getDuration();
            var tickId = getTickId(nbt);
            currentTime.put(tickId, (long) getDuration());
            player.getItemCooldownManager().set(this, coolTime);
            player.setStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, Integer.MAX_VALUE, 1), null);
            player.playSound(SoundEvents.ENTITY_WANDERING_TRADER_DRINK_POTION, SoundCategory.PLAYERS,1.0f,1.0f);
            var playerTracker = ServerPlayerEntityKt.getPlayerTracker(player);
            BlockHideAndSeekMod.SERVER
                    .getPlayerManager()
                    .getPlayerList()
                    .stream()
                    .filter(p -> p.getUuid() != player.getUuid())
                    .forEach(playerTracker::stopTracking);


            (player).networkHandler.sendPacket(new CooldownUpdateS2CPacket(getVisualItem(), coolTime));
        } else {
            HudDisplay.setActionBarText(player.getUuid(), "Blink Error", new LiteralText("擬態中は透明になれません。").setStyle(Style.EMPTY.withColor(Formatting.RED)), 60L);
        }
        return TypedActionResult.pass(stack);
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

    private static int getDuration() {
        return Config.Item.Blink.getDuration();
    }

    private static int getCoolTime() {
        return Config.Item.Blink.getCoolTime();
    }
}
