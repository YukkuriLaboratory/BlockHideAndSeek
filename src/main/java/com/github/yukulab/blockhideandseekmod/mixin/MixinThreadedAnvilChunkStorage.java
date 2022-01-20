package com.github.yukulab.blockhideandseekmod.mixin;

import com.github.yukulab.blockhideandseekmod.entity.DecoyEntity;
import com.github.yukulab.blockhideandseekmod.item.ItemFakeSummoner;
import com.github.yukulab.blockhideandseekmod.util.HideController;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ThreadedAnvilChunkStorage.class)
public class MixinThreadedAnvilChunkStorage {
    /**
     * チャンクが再読み込みされるタイミングで擬態ブロックの情報を再送信します.
     */
    @Redirect(
            method = "sendChunkDataPackets",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/server/world/ThreadedAnvilChunkStorage$EntityTracker;entity:Lnet/minecraft/entity/Entity;"
            )
    )
    private Entity sendHidingBlock(ThreadedAnvilChunkStorage.EntityTracker instance, ServerPlayerEntity player) {
        var entity = instance.entity;
        if (entity instanceof ServerPlayerEntity mimic && HideController.isHiding(mimic)) {
            var hidingPos = HideController.getHidingPlayerMaps().get(mimic.getUuid());
            if (hidingPos != null) {
                player.networkHandler.sendPacket(new BlockUpdateS2CPacket(hidingPos, HideController.getHidingBlock(hidingPos)));
            }
        } else if (entity instanceof DecoyEntity) {
            var pos = entity.getBlockPos();
            player.networkHandler.sendPacket(new BlockUpdateS2CPacket(pos, ItemFakeSummoner.getDecoyState(pos)));
        }
        return entity;
    }
}
