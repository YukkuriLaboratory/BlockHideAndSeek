package com.iduki.blockhideandseekmod.item;

import com.iduki.blockhideandseekmod.BlockHideAndSeekMod;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class BhasItems {

    public static ItemFlyer FLYER = register("flyer", new ItemFlyer());

    public static ItemDetector DETECTOR = register("detector", new ItemDetector());
    public static ItemScanner SCANNER = register("scanner", new ItemScanner());

    public static ItemBlockSelector SELECTOR = register("selector", new ItemBlockSelector());

    private static <T extends Item> T register(String id, T item) {
        return Registry.register(Registry.ITEM, new Identifier(BlockHideAndSeekMod.MOD_ID, id), item);
    }

    /**
     * このクラス自体をロードして上のアイテム達を登録させてやるためだけに生まれた虚空の戦士．
     */
    public static void init() {
    }
}
