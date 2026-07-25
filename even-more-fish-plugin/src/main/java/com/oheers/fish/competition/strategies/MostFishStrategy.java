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

import java.text.DecimalFormat;

public class MostFishStrategy implements CompetitionStrategy {

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

    /**
     * Gets the single console leaderboard message.
     *
     * @param entry The competition entry to get the leaderboard information from.
     * @return The single console leaderboard message.
     */
    @Override
    public EMFMessage getSingleConsoleLeaderboardMessage(@NonNull CompetitionEntry entry) {
        EMFMessage message = ConfigMessage.LEADERBOARD_MOST_FISH.getMessage();
        message.setAmount((int) entry.getValue());
        return message;
    }

    /**
     * Gets the single player leaderboard message.
     *
     * @param entry The competition entry to get the leaderboard information from.
     * @return The single player leaderboard message.
     */
    @Override
    public EMFMessage getSinglePlayerLeaderboard(@NonNull CompetitionEntry entry) {
        EMFMessage message = ConfigMessage.LEADERBOARD_MOST_FISH.getMessage();
        message.setAmount((int) entry.getValue());
        return message;
    }

    @Override
    public boolean shouldUseFishLength() {
        return false;
    }

    @Override
    public DecimalFormat getDecimalFormat() {
        return new DecimalFormat();
    }

    @Override
    public boolean isSingleReward() {
        return false;
    }

}
