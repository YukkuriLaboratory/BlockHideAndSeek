package com.iduki.blockhideandseekmod.game;

import com.iduki.blockhideandseekmod.BlockHideAndSeekMod;
import net.minecraft.scoreboard.Team;

import java.util.Objects;
import java.util.stream.Stream;

import static net.minecraft.scoreboard.AbstractTeam.VisibilityRule.HIDE_FOR_OTHER_TEAMS;
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
            team.setNameTagVisibilityRule(HIDE_FOR_OTHER_TEAMS);
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

    public static Team getSeekers() {
        return BlockHideAndSeekMod.SERVER.getScoreboard().getTeam("Seekers");
    }

    public static Team getHiders() {
        return BlockHideAndSeekMod.SERVER.getScoreboard().getTeam("Hiders");
    }

    public static Team getObservers() {
        return BlockHideAndSeekMod.SERVER.getScoreboard().getTeam("Observers");
    }

}

