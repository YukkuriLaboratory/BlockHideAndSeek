package com.github.yukulab.blockhideandseekmod.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

/**
 * デフォルトでLoreを持つItem向けのクラス
 */
public abstract class LoreItem extends Item {
    /**
     * デフォルトのLoreを定義する用
     *
     * @return Listの各要素がそのまま行として表現されます
     */
    public abstract List<Text> getLore();

    @Override
    public ItemStack getDefaultStack() {
        var itemStack = super.getDefaultStack();
        var nbt = itemStack.getOrCreateNbt();

        var compound = new NbtCompound();
        var lore = new NbtList();
        lore.addAll(getLore().stream().map(this::toNbtData).toList());
        compound.put(ItemStack.LORE_KEY, lore);
        nbt.put(ItemStack.DISPLAY_KEY, compound);

        return itemStack;
    }

    public LoreItem(Settings settings) {
        super(settings);
    }

    private NbtString toNbtData(Text text) {
        return NbtString.of(Text.Serializer.toJson(text));
    }

    protected static class LoreText {
        public static Text clickText(ACTION action, String description) {
            return new LiteralText("")
                    .append(new LiteralText(action.name).setStyle(Style.EMPTY.withColor(Formatting.GOLD)))
                    .append(new LiteralText(": ").setStyle(Style.EMPTY.withColor(Formatting.WHITE)))
                    .append(new LiteralText(description).setStyle(Style.EMPTY.withColor(Formatting.GREEN)));
        }

        public static Text unitText(String name, int time, UNIT unit) {
            return new LiteralText("")
                    .append(new LiteralText(name).setStyle(Style.EMPTY.withColor(Formatting.AQUA)))
                    .append(new LiteralText(": ").setStyle(Style.EMPTY.withColor(Formatting.WHITE)))
                    .append(new LiteralText(String.format("%s%s", time, unit.name)).setStyle(Style.EMPTY.withColor(Formatting.WHITE)));
        }
    }

    protected enum ACTION {
        RCLICK("右クリック"), SRCLICK("シフト右クリック");

        final String name;

        ACTION(String name) {
            this.name = name;
        }
    }

    protected enum UNIT {
        TICK("ティック"), SECONDS("秒"), BLOCK("ブロック");
        final String name;

        UNIT(String name) {
            this.name = name;
        }
    }
}
