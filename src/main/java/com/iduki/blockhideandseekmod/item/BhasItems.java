package com.iduki.blockhideandseekmod.item;

import com.iduki.blockhideandseekmod.BlockHideAndSeekMod;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.Objects;
import java.util.Set;

public class BhasItems {
    // 共通
    public static ItemFlyer FLYER = register("flyer", new ItemFlyer());

    // 鬼用
    public static ItemDetector DETECTOR = register("detector", new ItemDetector());
    public static ItemScanner SCANNER = register("scanner", new ItemScanner());
    public static ItemHidingBlockViewer BLOCK_VIEWER = register("block_viewer", new ItemHidingBlockViewer());

    // ミミック用
    public static ItemBlockSelector SELECTOR = register("selector", new ItemBlockSelector());
    public static ItemBlink BLINK = register("brink", new ItemBlink());

    private static <T extends Item> T register(String id, T item) {
        return Registry.register(Registry.ITEM, new Identifier(BlockHideAndSeekMod.MOD_ID, id), item);
    }

    public static final Set<Item> seekerItems = Set.of(DETECTOR, SCANNER, BLOCK_VIEWER, FLYER);

    public static final Set<Item> hiderItems = Set.of(SELECTOR, BLINK, FLYER);

    public static boolean isModItem(Item item) {
        return Objects.equals(Registry.ITEM.getId(item).getNamespace(), BlockHideAndSeekMod.MOD_ID);
    }

    /**
     * このクラス自体をロードして上のアイテム達を登録させてやるためだけに生まれた虚空の戦士．
     */
    public static void init() {
    }
}
