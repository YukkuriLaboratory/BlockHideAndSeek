package com.github.yukulab.blockhideandseekmod.item;

import com.github.yukulab.blockhideandseekmod.BlockHideAndSeekMod;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.List;
import java.util.Objects;

public class BhasItems {
    // 共通
    public static ItemFlyerJava FLYER = register("flyer", new ItemFlyerJava());
    public static ItemRuleBookJava RuleBook = register("rulebook", new ItemRuleBookJava());

    // 鬼用
    public static ItemDetectorJava DETECTOR = register("detector", new ItemDetectorJava());
    public static ItemScannerJava SCANNER = register("scanner", new ItemScannerJava());
    public static ItemHidingBlockViewerJava BLOCK_VIEWER = register("block_viewer", new ItemHidingBlockViewerJava());

    // ミミック用
    public static ItemBlockSelectorJava SELECTOR = register("selector", new ItemBlockSelectorJava());
    public static ItemBlink BLINK = register("blink", new ItemBlink());
    public static ItemJammerJava JAMMER = register("jammer", new ItemJammerJava());
    public static ItemSurpriseBallJava SURPRISEBALL = register("surpriseball", new ItemSurpriseBallJava());
    public static ItemFakeSummonerJava FAKESUMMONER = register("fakesummoner", new ItemFakeSummonerJava());

    //使用不可
    public static ItemSurprisePumpkinJava SURPRISEPUMPKIN = register("surprisepumpkin", new ItemSurprisePumpkinJava());

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
