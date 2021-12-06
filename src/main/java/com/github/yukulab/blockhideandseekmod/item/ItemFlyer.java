package com.github.yukulab.blockhideandseekmod.item;

import com.github.yukulab.blockhideandseekmod.util.FlyController;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.CooldownUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.List;

/**
 * 実はこのアイテムを持っているかどうかは飛行可能であるかに関係が無い
 * あくまでも状態の表示用アイテムである
 */
public class ItemFlyer extends LoreItem implements ServerSideItem {

    private final static Settings SETTINGS = new Settings().maxDamage(Items.ELYTRA.getMaxDamage());

    public ItemFlyer() {
        super(SETTINGS);
    }

    @Override
    public Text getName() {
        return new LiteralText("フライヤー").setStyle(Style.EMPTY.withColor(Formatting.AQUA));
    }

    @Override
    public List<Text> getLore() {
        return List.of(
                Text.of("一定時間の飛行を可能にします"),
                Text.of(""),
                new LiteralText("装備不可").setStyle(Style.EMPTY.withColor(Formatting.BLUE))
        );
    }

    @Override
    public Item getVisualItem() {
        return Items.ELYTRA;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        user.playerScreenHandler.syncState();
        return super.use(world, user, hand);
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (entity instanceof ServerPlayerEntity player) {
            stack.setDamage(MathHelper.floor(getMaxDamage() - getMaxDamage() * ((FlyController.getFlyAbleTime(player).toMillis()) / (float) FlyController.getMaxTime().toMillis())));

            var coolTimeTick = MathHelper.floor(FlyController.getUseCoolTime(player).toMillis() / 50f);
            player.getItemCooldownManager().set(this, coolTimeTick);
            player.networkHandler.sendPacket(new CooldownUpdateS2CPacket(getVisualItem(), coolTimeTick));
        }
    }
}
