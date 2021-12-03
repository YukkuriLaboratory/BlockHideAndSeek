package com.iduki.blockhideandseekmod.item;

import com.iduki.blockhideandseekmod.BlockHideAndSeekMod;
import com.iduki.blockhideandseekmod.config.ModConfig;
import com.iduki.blockhideandseekmod.game.HideController;
import com.iduki.blockhideandseekmod.game.HudDisplay;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
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

import java.util.List;

public class ItemBlink extends LoreItem implements ServerSideItem {

    private static final Item.Settings SETTINGS = new Item.Settings();

    private static final String TICK = "tick";

    public ItemBlink() {
        super(SETTINGS);
    }

    @Override
    public Text getName() {
        return new LiteralText("セパレータ");
    }

    @Override
    List<Text> getLore() {
        return List.of(
                new LiteralText("右クリック: 一定時間透明になります。（クールタイムあり）")
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
            var nowTick = nbt.getInt(TICK);
            nbt.putInt(TICK, nowTick - 1);
            if (nowTick - 1 == 0) {
                player.setInvisible(true);
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
            nbt.putInt(TICK, ModConfig.ItemConfig.ItemBlink.duration);
            player.getItemCooldownManager().set(this, coolTime);
            player.setInvisible(false);
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
}
