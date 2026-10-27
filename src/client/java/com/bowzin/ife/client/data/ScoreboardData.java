package com.bowzin.ife.client.data;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerScoreEntry;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;

import java.util.ArrayList;
import java.util.EnumMap;

import static com.bowzin.ife.client.events.IFEEvents.IMPROPER_DISMOUNT_EVENT;
import static com.bowzin.ife.client.events.IFEEvents.SCOREBOARD_UPDATE_EVENT;

public class ScoreboardData {

    private boolean isRiding = false;

    private EnumMap<Stat,String> stats = new EnumMap<>(Stat.class);

    public ScoreboardData() {
        SCOREBOARD_UPDATE_EVENT.register(this::extract);
    }

    private void extract(Objective objective) {
        EnumMap<Stat,String> newStats = new EnumMap<>(Stat.class);
        for (String infoLine: getInfoLines(objective)) {
            for(Stat stat : Stat.values()) {
                if(infoLine.startsWith(stat.prefix)) {
                    newStats.put(stat,infoLine.substring(stat.prefix.length()));
                    break;
                }
            }
        }
        if(newStats.containsKey(Stat.CURRENT_RIDE)&&!isRiding) {isRiding=true;}
        if(newStats.containsKey(Stat.NEAREST_WARP)&&isRiding) {isRiding=false;IMPROPER_DISMOUNT_EVENT.invoker().onDismount();}
        stats = newStats;
    }

    private ArrayList<String> getInfoLines(Objective objective) {
        Scoreboard scoreboard = objective.getScoreboard();
        ArrayList<String> retVal = new ArrayList<>();
        for (PlayerScoreEntry score : scoreboard.listPlayerScores(objective)) {
            PlayerTeam team = scoreboard.getPlayersTeam(score.owner());
            Component ownerName = score.ownerName();
            Component name = PlayerTeam.formatNameForTeam(team, ownerName);
            String infoLine = ChatFormatting.stripFormatting(name.getString()).trim();
            if(infoLine.length() > 2) {
                retVal.add(infoLine.substring(2));
            }
        }
        return retVal;
    }


    public String getStat(Stat stat) {

        String retval = stats.get(stat);
        if(retval==null) {
            return "-";
        }
        return retval;
    }

    public enum Stat {
        COINS("Coins: "), MAGIC_KEYS("Magic Keys: "), PLAYERS("Players: "),
        MULTIPLIER("Multiplier: "), CURRENT_RIDE("Current Ride"), NEAREST_WARP("Nearest Warp");

        final String prefix;
        Stat(String prefix) { this.prefix = prefix; }
    }
}
