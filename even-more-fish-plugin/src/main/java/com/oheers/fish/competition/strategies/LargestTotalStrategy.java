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

public class LargestTotalStrategy implements CompetitionStrategy {

    @Override
    public void applyToLeaderboard(Fish fish, Player fisher, Leaderboard leaderboard, Competition competition) {
        CompetitionEntry entry = leaderboard.getEntry(fisher.getUniqueId());
        float increaseAmount = fish.getLength();

        if (entry != null) {
            entry.incrementValue(increaseAmount);
            leaderboard.updateEntry(entry);
        } else {
            CompetitionEntry newEntry = new CompetitionEntry(fisher.getUniqueId(), fish, competition.getCompetitionType());
            newEntry.incrementValue(increaseAmount - 1); // Adjust for new entry
            leaderboard.addEntry(newEntry);
        }
    }

    @Override
    public EMFMessage getSingleConsoleLeaderboardMessage(@NotNull CompetitionEntry entry) {
        EMFMessage message = ConfigMessage.LEADERBOARD_LARGEST_TOTAL.getMessage();
        message.setAmount(getDecimalFormat().format(entry.getValue()));
        return message;
    }

    @Override
    public EMFMessage getSinglePlayerLeaderboard(@NotNull CompetitionEntry entry) {
        EMFMessage message = ConfigMessage.LEADERBOARD_LARGEST_TOTAL.getMessage();
        message.setAmount(getDecimalFormat().format(entry.getValue()));
        return message;
    }
}
