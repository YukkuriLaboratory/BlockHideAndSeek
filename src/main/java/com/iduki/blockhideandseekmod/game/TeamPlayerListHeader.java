package com.iduki.blockhideandseekmod.game;

import com.iduki.blockhideandseekmod.BlockHideAndSeekMod;
import net.minecraft.network.packet.s2c.play.PlayerListHeaderS2CPacket;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;

public class TeamPlayerListHeader {

    public static void TeamList() {
        var playerManager = BlockHideAndSeekMod.SERVER.getPlayerManager();
        var scoreboard = BlockHideAndSeekMod.SERVER.getScoreboard();
        var emptyText = LiteralText.EMPTY;
        var hidersCount = String.valueOf(TeamCreateandDelete.getHidersTeam().getPlayerList().size());
        var seekersCount = String.valueOf(TeamCreateandDelete.getSeekersTeam().getPlayerList().size());
        var packet = new LiteralText("Seekers:").setStyle(Style.EMPTY.withColor(Formatting.RED))
                .append(new LiteralText(seekersCount).setStyle(Style.EMPTY.withColor(Formatting.WHITE)))
                .append(new LiteralText("/").setStyle(Style.EMPTY.withColor(Formatting.YELLOW)))
                .append(new LiteralText("Hiders:").setStyle(Style.EMPTY.withColor(Formatting.GREEN)))
                .append(new LiteralText(hidersCount).setStyle(Style.EMPTY.withColor(Formatting.WHITE)));
        PlayerListHeaderS2CPacket Packet = new PlayerListHeaderS2CPacket(emptyText, packet);
        playerManager.getPlayerList().forEach(player -> player.networkHandler.sendPacket(Packet));
    }

    public static void EmptyList() {
        var playerManager = BlockHideAndSeekMod.SERVER.getPlayerManager();
        var emptyText = LiteralText.EMPTY;
        PlayerListHeaderS2CPacket Packet = new PlayerListHeaderS2CPacket(emptyText, emptyText);
        playerManager.getPlayerList().forEach(player -> player.networkHandler.sendPacket(Packet));
    }
}
