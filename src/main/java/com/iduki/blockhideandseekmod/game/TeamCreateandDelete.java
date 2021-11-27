package com.iduki.blockhideandseekmod.game;

import com.iduki.blockhideandseekmod.BlockHideAndSeekMod;
import net.minecraft.scoreboard.Team;

import java.util.Objects;
import java.util.stream.Stream;

import static net.minecraft.util.Formatting.*;

public class TeamCreateandDelete {

    public static void addSeeker() {
        var scoreboard = BlockHideAndSeekMod.SERVER.getScoreboard();
        Team team = scoreboard.getTeam("Seekers");
        if (team == null) {
            team = scoreboard.addTeam("Seekers");
            team.setColor(RED);
            team.setFriendlyFireAllowed(false);
        }
    }

    public static void addHider() {
        var scoreboard = BlockHideAndSeekMod.SERVER.getScoreboard();
        Team team = scoreboard.getTeam("Hiders");
        if (team == null) {
            team = scoreboard.addTeam("Hiders");
            team.setColor(GREEN);
            team.setFriendlyFireAllowed(false);
        }
    }

    public static void addObserver() {
        var scoreboard = BlockHideAndSeekMod.SERVER.getScoreboard();
        Team team = scoreboard.getTeam("Observers");
        if (team == null) {
            team = scoreboard.addTeam("Observers");
            team.setColor(DARK_GRAY);
            team.setFriendlyFireAllowed(false);
        }
    }

    public static void deleteTeam() {
        var scoreboard = BlockHideAndSeekMod.SERVER.getScoreboard();
        Stream.of(scoreboard.getTeam("Seekers"), scoreboard.getTeam("Hiders"), scoreboard.getTeam("Observers"))
                .filter(Objects::nonNull)
                .forEach(scoreboard::removeTeam);
    }

}

