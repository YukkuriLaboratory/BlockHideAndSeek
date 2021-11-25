package com.iduki.blockhideandseekmod.item;

import com.iduki.blockhideandseekmod.BlockHideAndSeekMod;
import com.iduki.blockhideandseekmod.config.ModConfig;
import com.iduki.blockhideandseekmod.game.HideController;
import com.iduki.blockhideandseekmod.game.HudDisplay;
import com.iduki.blockhideandseekmod.util.SeekerDamageSource;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.CooldownUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.List;

public class ItemDetector extends LoreItem implements ServerSideItem {

    private final static String DETECT_MESSAGE = "detectMessage";

    private final static Settings SETTINGS = new Settings();

    public ItemDetector() {
        super(SETTINGS);
    }

    @Override
    public Text getName() {
        return new LiteralText("検知器").setStyle(Style.EMPTY.withColor(Formatting.GOLD));
    }

    @Override
    List<Text> getLore() {
        return List.of(
                new LiteralText("右クリック: ブロックに隠れるミミックを検出します"),
                new LiteralText("クールタイム: " + MathHelper.floor((getCoolTime() / 20.0) * 10) / 10 + "秒")
        );
    }

    @Override
    public Item getVisualItem() {
        return Items.CLOCK;
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, world, entity, slot, selected);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        var seeker = context.getPlayer();
        if (seeker == null) {
            return ActionResult.FAIL;
        }
        var pos = context.getBlockPos();
        var hidingMap = HideController.getHidingPlayerMaps().inverse();

        var uuid = hidingMap.get(pos);
        if (uuid != null) {
            var player = BlockHideAndSeekMod.SERVER.getPlayerManager().getPlayer(uuid);
            if (player != null) {
                var source = new SeekerDamageSource(seeker);
                HideController.cancelHiding(player);
                player.damage(source, getDamageAmount());

                var message = new LiteralText("").setStyle(Style.EMPTY.withColor(Formatting.GREEN))
                        .append(player.getDisplayName())
                        .append(Text.of("を発見しました"));
                HudDisplay.setActionBarText(seeker.getUuid(), DETECT_MESSAGE, message, 30L);
                seeker.playSound(SoundEvents.ENTITY_ARROW_HIT_PLAYER, SoundCategory.PLAYERS, 75, 1);
                return ActionResult.PASS;
            }
        }

        var message = new LiteralText("検出できません").setStyle(Style.EMPTY.withColor(Formatting.RED));
        HudDisplay.setActionBarText(seeker.getUuid(), DETECT_MESSAGE, message, 30L);
        seeker.playSound(SoundEvents.ENTITY_ARROW_HIT_PLAYER, SoundCategory.PLAYERS, 75, 2);
        seeker.getItemCooldownManager().set(this, getCoolTime());
        ((ServerPlayerEntity) seeker).networkHandler.sendPacket(new CooldownUpdateS2CPacket(getVisualItem(), getCoolTime()));
        return ActionResult.PASS;
    }

    private int getCoolTime() {
        return ModConfig.ItemConfig.ItemDetecter.coolTime;
    }

    private float getDamageAmount() {
        return ModConfig.ItemConfig.ItemDetecter.damageAmount;
    }
}
