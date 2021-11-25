package com.iduki.blockhideandseekmod.game;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import com.iduki.blockhideandseekmod.BlockHideAndSeekMod;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;

import java.util.ArrayList;
import java.util.UUID;

public class HudDisplay {

    private static final Table<UUID, String, Text> actionBarTable = HashBasedTable.create();
    private static final Table<UUID, String, Long> showTimeTable = HashBasedTable.create();

    /**
     * アクションバーに表示する内容を設定します
     * 注意: このシステムでは各システムごとにidを用いてメッセージを設定し，出力時にidのアルファベット順に整列されます
     *
     * @param playerUuid 対象のplayerのUUID
     * @param id         設定するメッセージの識別id
     * @param message    出力するメッセージ
     */
    public static void setActionBarText(UUID playerUuid, String id, Text message) {
        actionBarTable.put(playerUuid, id, message);
        showTimeTable.remove(playerUuid, id);
    }

    /**
     * アクションバーに表示する内容を設定します
     * 注意: このシステムでは各システムごとにidを用いてメッセージを設定し，出力時にidのアルファベット順に整列されます
     *
     * @param playerUuid 対象のplayerのUUID
     * @param id         設定するメッセージの識別id
     * @param message    出力するメッセージ
     * @param showTick   出力する時間(Tick)
     */
    public static void setActionBarText(UUID playerUuid, String id, Text message, Long showTick) {
        actionBarTable.put(playerUuid, id, message);
        showTimeTable.put(playerUuid, id, showTick);
    }

    /**
     * メッセージを削除します
     *
     * @param playerUuid 対象のplayerのUUID
     * @param id         設定するメッセージの識別id
     */
    public static void removeActionbarText(UUID playerUuid, String id) {
        actionBarTable.remove(playerUuid, id);
        var player = BlockHideAndSeekMod.SERVER.getPlayerManager().getPlayer(playerUuid);
        if (player != null) {
            player.sendMessage(LiteralText.EMPTY, true);
        }
    }

    /**
     * プログレスバーみたいなTextを生成します
     *
     * @param percent 進行の割合. Range: 0 to 100
     * @return プログレスバーもどき
     */
    public static Text createProgressBar(int percent) {
        var text = new LiteralText("")
                .append(new LiteralText("["));

        int TILES = 20;
        var activeTiles = ((int) Math.floor(TILES * (percent / 100.0)));
        text.append(new LiteralText("|".repeat(activeTiles)).setStyle(Style.EMPTY.withColor(Formatting.GREEN)))
                .append(new LiteralText("|".repeat(TILES - activeTiles)).setStyle(Style.EMPTY.withColor(Formatting.RED)))
                .append(new LiteralText("]"));

        return text;
    }

    private static Text appendBlank(Text text) {
        if (text instanceof MutableText mutableText) {
            return mutableText.append(Text.of(" "));
        }
        return text;
    }

    static {
        ServerTickEvents.START_SERVER_TICK.register(server -> {
            var playerManager = server.getPlayerManager();
            ArrayList<Pair<UUID, String>> removeTargets = Lists.newArrayList();
            actionBarTable.rowMap().forEach(((uuid, stringTextMap) -> {
                var player = playerManager.getPlayer(uuid);
                if (player != null) {
                    var message = new LiteralText("");
                    stringTextMap.keySet()
                            .stream()
                            .filter(key -> {
                                var time = showTimeTable.get(uuid, key);
                                if (time == null) {
                                    return true;
                                }
                                if (--time >= 0) {
                                    showTimeTable.put(uuid, key, time);
                                    return true;
                                } else {
                                    showTimeTable.remove(uuid, key);
                                    removeTargets.add(new Pair<>(uuid, key));
                                    return false;
                                }
                            })
                            .sorted().map(stringTextMap::get).map(HudDisplay::appendBlank).forEach(message::append);
                    player.sendMessage(message, true);
                }
            }));
            for (Pair<UUID, String> removeTarget : removeTargets) {
                actionBarTable.remove(removeTarget.getLeft(), removeTarget.getRight());
            }
        });
    }
}
