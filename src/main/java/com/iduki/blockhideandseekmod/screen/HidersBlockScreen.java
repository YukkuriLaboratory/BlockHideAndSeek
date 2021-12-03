package com.iduki.blockhideandseekmod.screen;

import com.iduki.blockhideandseekmod.BlockHideAndSeekMod;
import com.iduki.blockhideandseekmod.config.ModConfig;
import com.iduki.blockhideandseekmod.game.HideController;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

public class HidersBlockScreen extends ScreenHandler {

    SimpleInventory blankInventory;

    protected HidersBlockScreen(PlayerInventory playerInventory, int row, int syncId) {
        super(getFromRow(row), syncId);
        var size = row * 9;
        blankInventory = new SimpleInventory(size <= 0 ? 9 : size);

        var playerManager = BlockHideAndSeekMod.SERVER.getPlayerManager();
        var hidingMaps = HideController.getHidingPlayerMaps();
        var playerUuidList = hidingMaps.keySet().stream().toList();
        Collections.shuffle(playerUuidList);
        AtomicInteger playerSize = new AtomicInteger(playerUuidList.size());
        // set slots
        IntStream.range(0, row).forEach(y ->
                IntStream.range(0, 9).forEach(x -> {
                            var index = x + y * 9;
                            addSlot(getLockedSlot(blankInventory, index));
                            //アイテムの追加
                            if (playerSize.decrementAndGet() < 0) {
                                return;
                            }

                            var uuid = playerUuidList.get(playerSize.get());
                            var player = playerManager.getPlayer(uuid);
                            var hidingPos = hidingMaps.get(uuid);
                            ItemStack itemStack;
                            if (player != null && hidingPos != null) {
                                var block = HideController.getHidingBlock(hidingPos);
                                if (block != null) {
                                    itemStack = block.getBlock().asItem().getDefaultStack();
                                    var name = new LiteralText("")
                                            .append(player.getName())
                                            .setStyle(Style.EMPTY.withColor(Formatting.GOLD));
                                    itemStack.setCustomName(name);
                                } else {
                                    itemStack = getErrorItem();
                                }
                            } else {
                                itemStack = getErrorItem();
                            }

                            setStackInSlot(index, nextRevision(), itemStack);
                        }
                )
        );

        // lock player inventory
        IntStream.range(0, 3).forEach(y ->
                IntStream.range(0, 9).forEach(x ->
                        addSlot(getLockedSlot(playerInventory, x + y * 9 + 9))
                )
        );
        IntStream.range(0, 9).forEach(x -> addSlot(getLockedSlot(playerInventory, x)));
    }

    @Override
    public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
        setCursorStack(ItemStack.EMPTY);
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }

    public static void open(ServerPlayerEntity serverPlayer) {
        var title = Text.of("結果");
        var factory = new SimpleNamedScreenHandlerFactory((syncId, inv, ignore) -> new HidersBlockScreen(inv, getScreenRow(), syncId), title);
        serverPlayer.openHandledScreen(factory);
    }

    private static int getScreenRow() {
        return ModConfig.ItemConfig.ItemHidingBlockViewer.screenRow;
    }

    private static ItemStack getErrorItem() {
        var item = Items.BARRIER.getDefaultStack();
        item.setCustomName(new LiteralText("取得できませんでした").setStyle(Style.EMPTY.withColor(Formatting.RED)));
        return item;
    }

    private static Slot getLockedSlot(Inventory inventory, int index) {
        return new Slot(inventory, index, 0, 0) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return false;
            }

            @Override
            public boolean canTakeItems(PlayerEntity playerEntity) {
                return false;
            }
        };
    }

    private static ScreenHandlerType<GenericContainerScreenHandler> getFromRow(int row) {
        return switch (row) {
            case 1 -> ScreenHandlerType.GENERIC_9X1;
            case 2 -> ScreenHandlerType.GENERIC_9X2;
            case 3 -> ScreenHandlerType.GENERIC_9X3;
            case 4 -> ScreenHandlerType.GENERIC_9X4;
            case 5 -> ScreenHandlerType.GENERIC_9X5;
            default -> ScreenHandlerType.GENERIC_9X6;
        };
    }
}
