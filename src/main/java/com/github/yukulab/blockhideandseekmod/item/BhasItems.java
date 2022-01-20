package com.github.yukulab.blockhideandseekmod.item;

import com.github.yukulab.blockhideandseekmod.BlockHideAndSeekMod;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.List;
import java.util.Objects;

public class BhasItems {
    // 共通
    public static ItemFlyer FLYER = register("flyer", new ItemFlyer());
    public static ItemRuleBook RuleBook = register("rulebook", new ItemRuleBook());

    // 鬼用
    public static ItemDetector DETECTOR = register("detector", new ItemDetector());
    public static ItemScanner SCANNER = register("scanner", new ItemScanner());
    public static ItemHidingBlockViewer BLOCK_VIEWER = register("block_viewer", new ItemHidingBlockViewer());

    // ミミック用
    public static ItemBlockSelector SELECTOR = register("selector", new ItemBlockSelector());
    public static ItemBlink BLINK = register("blink", new ItemBlink());
    public static ItemJammer JAMMER = register("jammer", new ItemJammer());
    public static ItemSurpriseBall SURPRISEBALL = register("surpriseball", new ItemSurpriseBall());
    public static ItemFakeSummoner FAKESUMMONER = register("fakesummoner", new ItemFakeSummoner());

    //使用不可
    public static ItemSurprisePumpkin SURPRISEPUMPKIN = register("surprisepumpkin", new ItemSurprisePumpkin());

    private static <T extends Item> T register(String id, T item) {
        return Registry.register(Registry.ITEM, new Identifier(BlockHideAndSeekMod.MOD_ID, id), item);
    }

    public static final List<LoreItem> seekerItems = List.of(DETECTOR, SCANNER, BLOCK_VIEWER, FLYER);

    public static final List<LoreItem> hiderItems = List.of(SELECTOR, BLINK, JAMMER, FAKESUMMONER, SURPRISEBALL, FLYER);

    public static boolean isModItem(Item item) {
        return Objects.equals(Registry.ITEM.getId(item).getNamespace(), BlockHideAndSeekMod.MOD_ID);
    }

    /**
     * このクラス自体をロードして上のアイテム達を登録させてやるためだけに生まれた虚空の戦士．
     */
    public static void init() {
    }
}
