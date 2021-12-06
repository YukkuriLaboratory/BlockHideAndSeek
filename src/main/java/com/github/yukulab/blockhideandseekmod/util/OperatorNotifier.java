package com.github.yukulab.blockhideandseekmod.util;

import com.google.common.collect.Sets;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

import java.util.Set;

public class OperatorNotifier {

    private static final Set<Text> notifyList = Sets.newHashSet();

    public static void addNotify(Text text) {
        notifyList.add(new LiteralText("[BHAS] ").append(text));
    }

    public static void sendNotify(PlayerEntity player) {
        var server = player.getServer();
        if (server != null && player.hasPermissionLevel(server.getOpPermissionLevel())) {
            notifyList.forEach(text -> player.sendMessage(text, false));
        }
    }

}
