package com.iduki.blockhideandseekmod.game;

import com.iduki.blockhideandseekmod.BlockHideAndSeekMod;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.network.ServerPlayerEntity;

import static net.minecraft.util.Formatting.*;

public class TeamCreateandDelete {

    //これ別に引数いらないけどまぁいっか
    public static void addSeeker(ServerPlayerEntity serverPlayerEntity) {
        var scoreboard = BlockHideAndSeekMod.SERVER.getScoreboard();
        Team team = scoreboard.getTeam("Seekers");
        if (team == null) {
            team = scoreboard.addTeam("Seekers");
            team.setColor(RED);
            team.setFriendlyFireAllowed(false);
        }
    }

    public static void addHider(ServerPlayerEntity serverPlayerEntity) {
        var scoreboard = BlockHideAndSeekMod.SERVER.getScoreboard();
        if (serverPlayerEntity.getName() == null) return;
        Team team = scoreboard.getTeam("Hiders");
        if (team == null) {
            team = scoreboard.addTeam("Hiders");
            team.setColor(BLUE);
            team.setFriendlyFireAllowed(false);
        }
    }

    public static void addObserver(ServerPlayerEntity serverPlayerEntity) {
        var scoreboard = BlockHideAndSeekMod.SERVER.getScoreboard();
        if (serverPlayerEntity.getName() == null) return;
        Team team = scoreboard.getTeam("Observers");
        if (team == null) {
            team = scoreboard.addTeam("Observers");
            team.setColor(DARK_GRAY);
            team.setFriendlyFireAllowed(false);
        }
    }

    public static void deleteTeam() {
        var scoreboard = BlockHideAndSeekMod.SERVER.getScoreboard();
        scoreboard.removeTeam(scoreboard.getTeam("Seekers"));
        scoreboard.removeTeam(scoreboard.getTeam("Hiders"));
        scoreboard.removeTeam(scoreboard.getTeam("Observers"));
    }


}

