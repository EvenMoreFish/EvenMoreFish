package com.oheers.fish.competition.strategies;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.competition.Competition;
import com.oheers.fish.competition.CompetitionEntry;
import com.oheers.fish.competition.CompetitionStrategy;
import com.oheers.fish.competition.CompetitionType;
import com.oheers.fish.competition.leaderboard.Leaderboard;
import com.oheers.fish.fishing.items.Fish;
import com.oheers.fish.messages.ConfigMessage;
import com.oheers.fish.messages.abstracted.EMFMessage;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SpecificFishStrategy implements CompetitionStrategy {

    @Override
    public boolean randomInit(@NotNull Competition competition) {
        return competition.getNumberNeeded() > 0 && competition.chooseFish();
    }

    @Override
    public boolean begin(Competition competition) {
        // Check if the competition fish is valid
        if (!competition.chooseFish()) {
            EvenMoreFish.getInstance().getLogger().warning(
                "Failed to select a fish for " + competition.getCompetitionFile().getId() + " competition."
            );
            return false;
        }
        // Set number-needed to 1 if it's not configured
        if (competition.getNumberNeeded() == 0) {
            EvenMoreFish.getInstance().getLogger().warning(
                competition.getCompetitionFile().getId() + " competition does not have number-needed set. Defaulting to 1."
            );
            competition.setNumberNeeded(1);
        }
        if (competition.getSelectedFish() == null) {
            EvenMoreFish.getInstance().getLogger().warning(
                "Failed to select a fish for " + competition.getCompetitionFile().getId() + " competition."
            );
            return false;
        }
        return true;
    }

    @Override
    public void applyToLeaderboard(Fish fish, Player fisher, Leaderboard leaderboard, Competition competition) {
        Fish selected = competition.getSelectedFish();
        if (selected != null && !fish.equals(selected)) {
            return;
        }

        CompetitionEntry entry = leaderboard.getEntry(fisher.getUniqueId());
        float increaseAmount = 1.0f;

        if (entry != null) {
            entry.incrementValue(increaseAmount);
            leaderboard.updateEntry(entry);
        } else {
            entry = new CompetitionEntry(fisher.getUniqueId(), fish, competition.getCompetitionType());
            leaderboard.addEntry(entry);
        }

        if (entry.getValue() >= competition.getNumberNeeded()) {
            competition.singleReward(fisher);
            competition.end(false);
        }
    }

    @Override
    public @NotNull EMFMessage getTypeFormat(@NotNull Competition competition, ConfigMessage configMessage) {
        Fish selectedFish = competition.getSelectedFish();
        EMFMessage message = CompetitionStrategy.super.getTypeFormat(competition, configMessage);
        message.setAmount(Integer.toString(competition.getNumberNeeded()));
        if (selectedFish != null) {
            message.setRarity(selectedFish.getRarity().getDisplayName());
            message.setFishCaught(selectedFish.getDisplayName());
        }
        return message;
    }

    @Override
    public EMFMessage getBeginMessage(@NotNull Competition competition, CompetitionType type) {
        return getTypeFormat(competition, ConfigMessage.COMPETITION_START);
    }

    /**
     * Gets the single console leaderboard message.
     *
     * @param entry The competition entry to get the leaderboard information from.
     * @return The single console leaderboard message.
     */
    @Override
    public EMFMessage getSingleConsoleLeaderboardMessage(@NotNull CompetitionEntry entry) {
        EMFMessage message = ConfigMessage.LEADERBOARD_LARGEST_FISH.getMessage();
        message.setLength(getDecimalFormat().format(entry.getValue()));
        return message;
    }

    /**
     * Gets the single player leaderboard message.
     *
     * @param entry The competition entry to get the leaderboard information from.
     * @return The single player leaderboard message.
     */
    @Override
    public EMFMessage getSinglePlayerLeaderboard(@NotNull CompetitionEntry entry) {
        Fish fish = entry.getFish();

        EMFMessage message = ConfigMessage.LEADERBOARD_LARGEST_FISH.getMessage();
        message.setLength(getDecimalFormat().format(entry.getValue()));
        message.setRarity(fish.getRarity().getDisplayName());
        message.setFishCaught(fish.getDisplayName());
        return message;
    }

    @Override
    public boolean shouldUseFishLength() {
        return true;
    }

}
