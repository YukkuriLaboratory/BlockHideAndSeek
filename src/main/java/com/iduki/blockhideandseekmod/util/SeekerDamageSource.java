package com.iduki.blockhideandseekmod.util;

import com.iduki.blockhideandseekmod.game.HideController;
import com.iduki.blockhideandseekmod.game.HudDisplay;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.EntityDamageSource;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

/**
 * 鬼がミミックを発見した時に与えるダメージ用
 */
public class SeekerDamageSource extends EntityDamageSource {
    public SeekerDamageSource(Entity source) {
        super("player", source);
    }

    @Override
    public Text getDeathMessage(LivingEntity entity) {
        HudDisplay.removeActionbarText(entity.getUuid(), HideController.SELECTED_BLOCK);
        return new LiteralText("")
                .append(entity.getDisplayName())
                .append(new LiteralText("は"))
                .append(source.getDisplayName())
                .append(new LiteralText("に発見された"));
    }
}
