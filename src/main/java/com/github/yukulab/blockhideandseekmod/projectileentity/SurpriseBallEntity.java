package com.github.yukulab.blockhideandseekmod.projectileentity;

import com.github.yukulab.blockhideandseekmod.item.BhasItems;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;

import java.util.Random;

public class SurpriseBallEntity extends SnowballEntity {
    public SurpriseBallEntity(EntityType<? extends SnowballEntity> entityType, World world) {
        super(entityType, world);
    }

    public SurpriseBallEntity(World world, LivingEntity owner) {
        super(world,owner);
    }

    public SurpriseBallEntity(World world, double x, double y, double z) {
        super(world,x,y,z);
    }

    @Override
    protected Item getDefaultItem() {
        return BhasItems.SURPRISEBALL;
    }

    protected void onEntityHit(EntityHitResult entityHitResult) {
        super.onEntityHit(entityHitResult);
        Entity entity = entityHitResult.getEntity();

        if (entity instanceof ServerPlayerEntity player) {
            ItemStack itemStack = new ItemStack(BhasItems.SURPRISEPUMPKIN);
            itemStack.addEnchantment(Enchantments.BINDING_CURSE, 5);
            player.getInventory().insertStack(39, itemStack);
            player.playSound(SoundEvents.BLOCK_SLIME_BLOCK_BREAK, 2.0F, 1.0F);

            Random rand = new Random();
            int num = rand.nextInt(100);
            if (num < 20) {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.LEVITATION, 60, 1, false, false, false));
            }
            if (num >= 20 && num < 40) {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 60, 3, false, false, false));
            }
            if (num >= 40 && num < 60) {
                var message = new LiteralText("").setStyle(Style.EMPTY.withColor(Formatting.GREEN));
                var subMessage = new LiteralText("酔っちゃったらごめんなさい");
                var packet = new TitleS2CPacket(message);
                var subPacket = new SubtitleS2CPacket(subMessage);
                player.networkHandler.sendPacket(packet);
                player.networkHandler.sendPacket(subPacket);
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 160, 1, false, false, false));
            }
        }
    }

    protected void onCollision(HitResult hitResult) {
        super.onCollision(hitResult);
        if (!this.world.isClient) {
            this.world.sendEntityStatus(this, (byte)3);
            this.discard();
        }

    }
}
