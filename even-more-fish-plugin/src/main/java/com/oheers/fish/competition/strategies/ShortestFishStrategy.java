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

import java.util.UUID;

public class ShortestFishStrategy implements CompetitionStrategy {

    @Override
    public boolean randomInit(@NonNull Competition competition) {
        return true;
    }

    @Override
    public void applyToLeaderboard(Fish fish, UUID fisher, Leaderboard leaderboard, Competition competition) {
        if (fish.getLength() <= 0) return;

        CompetitionEntry entry = leaderboard.getEntry(fisher);

        if (entry != null) {
            if (fish.getLength() < entry.getFish().getLength()) {
                // These HAVE to be in this order, otherwise players are spammed with new first place messages
                leaderboard.addEntry(new CompetitionEntry(fisher, fish, competition.getCompetitionType()));
                leaderboard.removeEntry(entry);
            }
        } else {
            leaderboard.addEntry(new CompetitionEntry(fisher, fish, competition.getCompetitionType()));
        }
    }

    @Override
    public EMFMessage getSingleConsoleLeaderboardMessage(@NonNull CompetitionEntry entry) {
        EMFMessage message = ConfigMessage.LEADERBOARD_SHORTEST_FISH.getMessage();
        message.setLength("%.1f".formatted(entry.getValue()));
        return message;
    }

    @Override
    public EMFMessage getSinglePlayerLeaderboard(@NonNull CompetitionEntry entry) {
        EMFMessage message = ConfigMessage.LEADERBOARD_SHORTEST_FISH.getMessage();
        message.setLength("%.1f".formatted(entry.getValue()));
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
