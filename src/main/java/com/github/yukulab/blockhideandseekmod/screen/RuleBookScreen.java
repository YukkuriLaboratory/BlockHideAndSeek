package com.github.yukulab.blockhideandseekmod.screen;

import com.github.yukulab.blockhideandseekmod.BlockHideAndSeekMod;
import com.github.yukulab.blockhideandseekmod.item.BhasItems;
import com.github.yukulab.blockhideandseekmod.item.LoreItem;
import net.minecraft.item.Items;
import net.minecraft.item.WrittenBookItem;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.network.packet.s2c.play.OpenWrittenBookS2CPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.server.ServerTask;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;

import java.util.List;

public class RuleBookScreen {
    private final NbtList pageNbt = new NbtList();

    public RuleBookScreen() {
        List<Text> pages = List.of(
                new LiteralText("")
                        .append(new LiteralText("基本ルール\n"))
                        .append("\n")
                        .append(new LiteralText("")
                                .append(new LiteralText("鬼側").setStyle(Style.EMPTY.withColor(Formatting.RED)))
                                .append("と")
                                .append(new LiteralText("ミミック(隠れる)側").setStyle(Style.EMPTY.withColor(Formatting.DARK_GREEN)))
                                .append("に分かれてかくれんぼを行います.")
                                .append("\n"))
                        .append(new LiteralText("\n"))
                        .append(new LiteralText("- 勝利条件 -\n"))
                        .append(new LiteralText("鬼側:\n").setStyle(Style.EMPTY.withColor(Formatting.RED)))
                        .append(new LiteralText("ミミック全員を制限時間内に見つけて倒す\n"))
                        .append(new LiteralText("ミミック側:\n").setStyle(Style.EMPTY.withColor(Formatting.DARK_GREEN)))
                        .append(new LiteralText("最後まで生き残る\n")),
                new LiteralText("")
                        .append(new LiteralText("- 鬼 -\n").setStyle(Style.EMPTY.withColor(Formatting.RED)))
                        .append("\n")
                        .append("使用可能アイテム:\n")
                        .append(parseItemList(BhasItems.seekerItems)),
                new LiteralText("")
                        .append(new LiteralText("- ミミック -\n").setStyle(Style.EMPTY.withColor(Formatting.DARK_GREEN)))
                        .append("使用可能アイテム:\n")
                        .append(parseItemList(BhasItems.hiderItems).append("\n"))
                        .append("\n")
                        .append("特殊スキル:「擬態」\n")
                        .append("操作: シフト\n")
                        .append("効果: その場でブロックとなって隠れます")
        );
        pageNbt.addAll(pages.stream().map(page -> NbtString.of(Text.Serializer.toJson(page))).toList());
    }

    public static void open(ServerPlayerEntity player) {
        var item = Items.WRITTEN_BOOK.getDefaultStack();
        var nbt = item.getOrCreateNbt();
        var screen = new RuleBookScreen();
        nbt.put(WrittenBookItem.PAGES_KEY, screen.pageNbt);
        nbt.put(WrittenBookItem.TITLE_KEY, NbtString.of("gamerules"));
        nbt.put(WrittenBookItem.AUTHOR_KEY, NbtString.of("bhas"));
        nbt.putBoolean(WrittenBookItem.RESOLVED_KEY, true);

        var networkHandler = player.networkHandler;
        networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(ScreenHandlerSlotUpdateS2CPacket.UPDATE_PLAYER_INVENTORY_SYNC_ID, player.playerScreenHandler.nextRevision(), 40, item));
        networkHandler.sendPacket(new OpenWrittenBookS2CPacket(Hand.OFF_HAND));

        var server = BlockHideAndSeekMod.SERVER;
        server.send(new ServerTask(server.getTicks(), () ->
                networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(ScreenHandlerSlotUpdateS2CPacket.UPDATE_PLAYER_INVENTORY_SYNC_ID, player.playerScreenHandler.nextRevision(), 40, player.getInventory().offHand.get(0))))
        );
    }

    private static LiteralText parseItemList(List<LoreItem> items) {
        var text = new LiteralText("");
        var count = 0;
        for (LoreItem item : items) {
            var lore = new LiteralText("");
            var loreCount = 0;
            var loreTexts = item.getLore();
            for (Text loreLine : loreTexts) {
                lore.append(loreLine);
                if (++loreCount < loreTexts.size()) {
                    lore.append("\n");
                }
            }
            text.append(((LiteralText) item.getName()).setStyle(Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, lore))));
            if (++count < items.size()) {
                text.append("/");
            }
        }
        return text;
    }
}
