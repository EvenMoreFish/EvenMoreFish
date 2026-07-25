package com.oheers.fish.competition.strategies;


import com.oheers.fish.competition.Competition;
import com.oheers.fish.competition.CompetitionEntry;
import com.oheers.fish.competition.CompetitionStrategy;
import com.oheers.fish.competition.leaderboard.Leaderboard;
import com.oheers.fish.fishing.items.Fish;
import com.oheers.fish.messages.ConfigMessage;
import com.oheers.fish.messages.abstracted.EMFMessage;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

public class ShortestTotalStrategy implements CompetitionStrategy {

    @Override
    public boolean randomInit(@NonNull Competition competition) {
        return true;
    }

    @Override
    public void applyToLeaderboard(Fish fish, Player fisher, Leaderboard leaderboard, Competition competition) {
        CompetitionEntry entry = leaderboard.getEntry(fisher.getUniqueId());

        if (entry != null) {
            leaderboard.trackFish(entry, fish);
        } else {
            leaderboard.addEntry(new CompetitionEntry(fisher.getUniqueId(), fish, competition.getCompetitionType()));
        }
    }

    @Override
    public EMFMessage getSingleConsoleLeaderboardMessage(@NonNull CompetitionEntry entry) {
        EMFMessage message = ConfigMessage.LEADERBOARD_SHORTEST_TOTAL.getMessage();
        message.setAmount(getDecimalFormat().format(entry.getValue()));
        return message;
    }

    @Override
    public EMFMessage getSinglePlayerLeaderboard(@NonNull CompetitionEntry entry) {
        EMFMessage message = ConfigMessage.LEADERBOARD_SHORTEST_TOTAL.getMessage();
        message.setAmount(getDecimalFormat().format(entry.getValue()));
        return message;
    }

    @Override
    public boolean shouldUseFishLength() {
        return true;
    }

    @Override
    public boolean isSingleReward() {
        return false;
    }

}
