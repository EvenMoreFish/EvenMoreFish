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

public class LargestFishStrategy implements CompetitionStrategy {

    @Override
    public boolean randomInit(@NotNull Competition competition) {
        return true;
    }

    @Override
    public void applyToLeaderboard(Fish fish, Player fisher, Leaderboard leaderboard, Competition competition) {
        if (fish.getLength() <= 0) return;

        CompetitionEntry entry = leaderboard.getEntry(fisher.getUniqueId());

        if (entry != null) {
            if (fish.getLength() > entry.getFish().getLength()) {
                leaderboard.removeEntry(entry);
                leaderboard.addEntry(new CompetitionEntry(fisher.getUniqueId(), fish, competition.getCompetitionType()));
            }
        } else {
            leaderboard.addEntry(new CompetitionEntry(fisher.getUniqueId(), fish, competition.getCompetitionType()));
        }
    }

    @Override
    public EMFMessage getSingleConsoleLeaderboardMessage(@NotNull CompetitionEntry entry) {
        EMFMessage message = ConfigMessage.LEADERBOARD_LARGEST_FISH.getMessage();
        message.setLength(getDecimalFormat().format(entry.getValue()));
        return message;
    }

    @Override
    public EMFMessage getSinglePlayerLeaderboard(@NotNull CompetitionEntry entry) {
        EMFMessage message = ConfigMessage.LEADERBOARD_LARGEST_FISH.getMessage();
        message.setLength(getDecimalFormat().format(entry.getValue()));
        return message;
    }
}
