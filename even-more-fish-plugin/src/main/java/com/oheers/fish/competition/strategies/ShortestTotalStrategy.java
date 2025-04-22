package com.oheers.fish.competition.strategies;


import com.oheers.fish.competition.Competition;
import com.oheers.fish.competition.CompetitionEntry;
import com.oheers.fish.competition.CompetitionStrategy;
import com.oheers.fish.competition.leaderboard.Leaderboard;
import com.oheers.fish.fishing.items.Fish;
import com.oheers.fish.messages.ConfigMessage;
import com.oheers.fish.messages.abstracted.EMFMessage;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ShortestTotalStrategy implements CompetitionStrategy {

    @Override
    public boolean randomInit(@NotNull Competition competition) {
        return true;
    }

    @Override
    public void applyToLeaderboard(Fish fish, Player fisher, Leaderboard leaderboard, Competition competition) {
        CompetitionEntry entry = leaderboard.getEntry(fisher.getUniqueId());
        float increaseAmount = fish.getLength();

        if (entry != null) {
            entry.incrementValue(increaseAmount);
            leaderboard.updateEntry(entry);
        } else {
            CompetitionEntry newEntry = new CompetitionEntry(fisher.getUniqueId(), fish, competition.getCompetitionType());
            newEntry.incrementValue(increaseAmount - 1); // Adjust for the first entry
            leaderboard.addEntry(newEntry);
        }
    }

    @Override
    public EMFMessage getSingleConsoleLeaderboardMessage(@NotNull CompetitionEntry entry) {
        EMFMessage message = ConfigMessage.LEADERBOARD_SHORTEST_TOTAL.getMessage();
        message.setAmount(getDecimalFormat().format(entry.getValue()));
        return message;
    }

    @Override
    public EMFMessage getSinglePlayerLeaderboard(@NotNull CompetitionEntry entry) {
        EMFMessage message = ConfigMessage.LEADERBOARD_SHORTEST_TOTAL.getMessage();
        message.setAmount(getDecimalFormat().format(entry.getValue()));
        return message;
    }

    @Override
    public boolean shouldUseFishLength() {
        return true;
    }

}
