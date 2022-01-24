package com.github.yukulab.blockhideandseekmod.entity;

import com.github.yukulab.blockhideandseekmod.config.Config;
import com.github.yukulab.blockhideandseekmod.item.ItemFakeSummoner;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.ShulkerEntity;
import net.minecraft.world.World;

public class DecoyEntity extends BlockHighlightEntity {
    public DecoyEntity(EntityType<ShulkerEntity> entityType, World world) {
        super(entityType, world);
    }

    private int time = 0;

    @Override
    public void tick() {
        time = time + 1;
        if (time >= getDeleteTime() * 20) {
            ItemFakeSummoner.removeHighlight(this.getBlockPos());
            time = 0;
        }
    }

    private static int getDeleteTime() {
        return Config.Item.FakeSummoner.getDeleteTime();
    }
}
