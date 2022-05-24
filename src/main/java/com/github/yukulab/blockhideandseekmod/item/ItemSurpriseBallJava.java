package com.github.yukulab.blockhideandseekmod.item;

import com.github.yukulab.blockhideandseekmod.config.Config;
import com.github.yukulab.blockhideandseekmod.projectileentity.SurpriseBallEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
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

import java.util.List;

public class ItemSurpriseBallJava extends LoreItem implements JavaServerSideItem {
    private static final Item.Settings SETTINGS = new Item.Settings();

    public ItemSurpriseBallJava() {
        super(SETTINGS);
    }

    @Override
    public Text getName() {
        return new LiteralText("サプライズ玉(ボール)");
    }

    @Override
    public List<Text> getLore() {
        return List.of(
                new LiteralText("敵に投げて驚かせよう！").setStyle(Style.EMPTY.withColor(Formatting.WHITE)),
                new LiteralText("当たると10%の確率で一瞬鈍足or数秒吐き気を付与します").setStyle(Style.EMPTY.withColor(Formatting.GREEN))
        );
    }

    @Override
    public @NotNull Item getVisualItem() {
        return Items.FIREWORK_STAR;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        var stack = user.getStackInHand(hand);
        user.playSound(SoundEvents.ENTITY_ENDER_PEARL_THROW, SoundCategory.PLAYERS,1.0f,1.0f);
        SurpriseBallEntity ball = new SurpriseBallEntity(world,user);
        ball.setItem(stack);
        ball.setVelocity(user, user.prevPitch, user.prevYaw, 0F, 3F, 0F);
        world.spawnEntity(ball);

        var coolTime = getCoolTime() + getDuration();
        user.getItemCooldownManager().set(this, coolTime);
        ((ServerPlayerEntity) user).networkHandler.sendPacket(new CooldownUpdateS2CPacket(getVisualItem(), coolTime));

        return TypedActionResult.success(stack, false);
    }

    private int getCoolTime() {
        return Config.Item.SurpriseBall.getCoolTime();
    }

    public static int getDuration() {
        return Config.Item.SurpriseBall.getDuration();
    }
}