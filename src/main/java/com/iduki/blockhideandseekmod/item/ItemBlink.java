package com.iduki.blockhideandseekmod.item;

import com.iduki.blockhideandseekmod.BlockHideAndSeekMod;
import com.iduki.blockhideandseekmod.config.ModConfig;
import com.iduki.blockhideandseekmod.game.HideController;
import com.iduki.blockhideandseekmod.util.HudDisplay;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.CooldownUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitiesDestroyS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerSpawnS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.spongepowered.include.com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ItemBlink extends LoreItem implements ServerSideItem {

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
                new LiteralText("右クリック: 一定時間透明になります"),
                Text.of("効果時間: " + ModConfig.ItemConfig.ItemBlink.duration),
                Text.of("クールタイム: " + ModConfig.ItemConfig.ItemBlink.coolTime)
        );
    }

    @Override
    public Item getVisualItem() {
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
                var spawnPacket = new PlayerSpawnS2CPacket(player);
                BlockHideAndSeekMod.SERVER
                        .getPlayerManager()
                        .getPlayerList()
                        .stream()
                        .filter(p -> p.getUuid() != player.getUuid())
                        .forEach(p -> p.networkHandler.sendPacket(spawnPacket));
            }
        }
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        var player = ((ServerPlayerEntity) user);
        var stack = user.getStackInHand(hand);
        if (!HideController.isHiding(player)) {
            var nbt = stack.getOrCreateNbt();
            var coolTime = ModConfig.ItemConfig.ItemBlink.coolTime + ModConfig.ItemConfig.ItemBlink.duration;
            var tickId = getTickId(nbt);
            currentTime.put(tickId, (long) ModConfig.ItemConfig.ItemBlink.duration);
            player.getItemCooldownManager().set(this, coolTime);
            player.setStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, Integer.MAX_VALUE, 1), null);
            var destroyPacket = new EntitiesDestroyS2CPacket(player.getId());
            BlockHideAndSeekMod.SERVER
                    .getPlayerManager()
                    .getPlayerList()
                    .stream()
                    .filter(p -> p.getUuid() != player.getUuid())
                    .forEach(p -> p.networkHandler.sendPacket(destroyPacket));

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
}
