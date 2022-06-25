package com.github.yukulab.blockhideandseekmod.item;

import com.google.common.collect.Maps;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ItemSurprisePumpkinJava extends LoreItem implements JavaServerSideItem {
    private static final Item.Settings SETTINGS = new Item.Settings();

    public ItemSurprisePumpkinJava() {
        super(SETTINGS);
    }

    private static final Map<UUID, Integer> timeMap = Maps.newHashMap();

    private static final Map<UUID, BlockPos> blockPos = Maps.newHashMap();

    @Override
    public Text getName() {
        return new LiteralText("呪いのパンプキン");
    }

    @Override
    public List<Text> getLore() {
        return List.of(
                new LiteralText("")
        );
    }

    @Override
    public @NotNull Item getVisualItem() {
        return Items.CARVED_PUMPKIN;
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (entity instanceof ServerPlayerEntity player) {
            var time = timeMap.getOrDefault(entity.getUuid(), 0) + 1;
            if (time == 1 && player.getActiveStatusEffects().containsKey(StatusEffects.LEVITATION)) {
                blockPos.put(player.getUuid(), player.getBlockPos());
                world.syncWorldEvent(null, WorldEvents.MUSIC_DISC_PLAYED, player.getBlockPos(), Item.getRawId(Items.MUSIC_DISC_PIGSTEP));
            }
            if (time >= ItemSurpriseBallJava.getDuration()) {
                player.getInventory().removeOne(new ItemStack(BhasItems.SURPRISEPUMPKIN));
                player.getInventory().removeStack(39);
                var pos = blockPos.remove(player.getUuid());
                if (pos != null) {
                    world.syncWorldEvent(WorldEvents.MUSIC_DISC_PLAYED, pos, 0);
                }
                time = 0;
            }
            timeMap.put(entity.getUuid(), time);
        }
    }


}
