package com.iduki.blockhideandseekmod.item;

import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

//TODO 基本機能の実装

/**
 * 鬼が持つ近くの隠れているBlockを指し示すアイテム
 */
public class ItemScanner extends Item implements ServerSideItem {

    public ItemScanner(Settings settings) {
        super(settings);
    }

    @Override
    public Text getName() {
        return new LiteralText("スキャナー").setStyle(Style.EMPTY.withColor(Formatting.GOLD));
    }

    @Override
    public Item getVisualItem() {
        return Items.CLOCK;
    }

}
