package com.github.yukulab.blockhideandseekmod.item;

import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public class ItemJammer extends LoreItem implements ServerSideItem{
    private final static Settings SETTINGS = new Settings();

    public ItemJammer() {
        super(SETTINGS);
    }

    @Override
    public Text getName() {
        return new LiteralText("妨害装置").setStyle(Style.EMPTY.withColor(Formatting.GOLD));
    }

    @Override
    public List<Text> getLore() {
        return List.of(
                new LiteralText("右クリック: 一定時間鬼のスキャナーを弱体化します")
        );
    }

    @Override
    public Item getVisualItem() {
        return Items.SOUL_TORCH;
    }

}
