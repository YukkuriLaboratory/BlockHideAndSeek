package com.github.yukulab.blockhideandseekmod.item;

import com.github.yukulab.blockhideandseekmod.config.ModConfig;
import com.github.yukulab.blockhideandseekmod.projectileentity.SurpriseBallEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.CooldownUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.List;

public class ItemSurpriseBall extends LoreItem implements ServerSideItem {
    private static final Item.Settings SETTINGS = new Item.Settings();

    public ItemSurpriseBall() {
        super(SETTINGS);
    }

    @Override
    public Text getName() {
        return new LiteralText("サプライズ玉(ボール)");
    }

    @Override
    public List<Text> getLore() {
        return List.of(
                new LiteralText("敵に投げて驚かせよう！(投げると擬態が解除されます)")
        );
    }

    @Override
    public Item getVisualItem() {
        return Items.FIREWORK_STAR;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        var stack = user.getStackInHand(hand);
        user.playSound(SoundEvents.ENTITY_ENDER_PEARL_THROW, SoundCategory.PLAYERS,1.0f,1.0f);
        SurpriseBallEntity ball = new SurpriseBallEntity(world,user);
        ball.setItem(stack);
        ball.setProperties(user, user.prevPitch, user.prevYaw, 0F, 3F, 0F);
        world.spawnEntity(ball);

        user.incrementStat(Stats.USED.getOrCreateStat(this));

        var coolTime = getCoolTime();
        user.getItemCooldownManager().set(this, coolTime);
        ((ServerPlayerEntity) user).networkHandler.sendPacket(new CooldownUpdateS2CPacket(getVisualItem(), coolTime));

        return TypedActionResult.success(stack);
    }

    private int getCoolTime() {
        return ModConfig.ItemConfig.ItemSurpriseBall.coolTime;
    }
}
