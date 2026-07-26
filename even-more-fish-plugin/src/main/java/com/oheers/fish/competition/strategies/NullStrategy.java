package com.oheers.fish.competition.strategies;

import com.oheers.fish.competition.Competition;
import com.oheers.fish.competition.CompetitionEntry;
import com.oheers.fish.competition.CompetitionStrategy;
import com.oheers.fish.competition.leaderboard.Leaderboard;
import com.oheers.fish.fishing.items.Fish;
import com.oheers.fish.messages.EMFSingleMessage;
import com.oheers.fish.messages.abstracted.EMFMessage;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

import java.util.UUID;


public class NullStrategy implements CompetitionStrategy {

    @Override
    public boolean randomInit(@NonNull Competition competition) {
        return true;
    }

    @Override
    public void applyToLeaderboard(Fish fish, UUID fisher, Leaderboard leaderboard, Competition competition) {
        //do nothing duh
    }

    /**
     * Gets the single console leaderboard message.
     *
     * @param entry The competition entry to get the leaderboard information from.
     * @return The single console leaderboard message.
     */
    @Override
    public EMFMessage getSingleConsoleLeaderboardMessage(@NonNull CompetitionEntry entry) {
        return EMFSingleMessage.empty();
    }

    /**
     * Gets the single player leaderboard message.
     *
     * @param entry The competition entry to get the leaderboard information from.
     * @return The single player leaderboard message.
     */
    @Override
    public EMFMessage getSinglePlayerLeaderboard(@NonNull CompetitionEntry entry) {
        return EMFSingleMessage.empty();
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
