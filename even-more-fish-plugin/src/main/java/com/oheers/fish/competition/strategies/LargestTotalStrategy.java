package com.oheers.fish.competition.strategies;

import com.oheers.fish.api.fishing.items.IFish;
import com.oheers.fish.competition.Competition;
import com.oheers.fish.competition.CompetitionEntry;
import com.oheers.fish.competition.CompetitionStrategy;
import com.oheers.fish.competition.leaderboard.Leaderboard;
import com.oheers.fish.messages.ConfigMessage;
import com.oheers.fish.messages.abstracted.EMFMessage;
import org.jspecify.annotations.NonNull;

import java.util.UUID;

public class LargestTotalStrategy implements CompetitionStrategy {

    @Override
    public boolean randomInit(@NonNull Competition competition) {
        return true;
    }

    @Override
    public void applyToLeaderboard(IFish fish, UUID fisher, Leaderboard leaderboard, Competition competition) {
        if (fish.isLengthless()) {
            return;
        }
        CompetitionEntry entry = leaderboard.getEntry(fisher);

        if (entry != null) {
            leaderboard.trackFish(entry, fish);
        } else {
            leaderboard.addEntry(new CompetitionEntry(fisher, fish, competition.getCompetitionType()));
        }
    }

    @Override
    public EMFMessage getSingleConsoleLeaderboardMessage(@NonNull CompetitionEntry entry) {
        EMFMessage message = ConfigMessage.LEADERBOARD_LARGEST_TOTAL.getMessage();
        message.setAmount(getDecimalFormat().format(entry.getValue()));
        return message;
    }

    @Override
    public EMFMessage getSinglePlayerLeaderboard(@NonNull CompetitionEntry entry) {
        EMFMessage message = ConfigMessage.LEADERBOARD_LARGEST_TOTAL.getMessage();
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
