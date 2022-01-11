package com.github.yukulab.blockhideandseekmod.projectileentity;

import com.github.yukulab.blockhideandseekmod.item.BhasItems;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;

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
            itemStack.addEnchantment(Enchantments.BINDING_CURSE,2);
            player.getInventory().insertStack(39,itemStack);
            player.playSound(SoundEvents.BLOCK_SLIME_BLOCK_BREAK, 2.0F, 1.0F);
        }
    }

    protected void onCollision(HitResult hitResult) {
        super.onCollision(hitResult);
        if (!this.world.isClient) {
            this.world.sendEntityStatus(this, (byte)3);
            this.kill();
        }

    }
}
