package com.oheers.fish.competition.strategies;


import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.competition.Competition;
import com.oheers.fish.competition.CompetitionEntry;
import com.oheers.fish.competition.CompetitionStrategy;
import com.oheers.fish.competition.CompetitionType;
import com.oheers.fish.competition.leaderboard.Leaderboard;
import com.oheers.fish.fishing.items.Fish;
import com.oheers.fish.fishing.items.FishManager;
import com.oheers.fish.fishing.items.Rarity;
import com.oheers.fish.messages.ConfigMessage;
import com.oheers.fish.messages.abstracted.EMFMessage;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;
import java.util.logging.Level;

public class SpecificRarityStrategy implements CompetitionStrategy {

    @Override
    public boolean begin(Competition competition) {
        return chooseRarity(competition);
    }

    @Override
    public void applyToLeaderboard(Fish fish, Player fisher, Leaderboard leaderboard, Competition competition) {
        Rarity compRarity = competition.getSelectedRarity();
        if (compRarity != null && !fish.getRarity().getId().equals(compRarity.getId())) {
            return; // Fish doesn't match the required rarity
        }

        CompetitionEntry entry = leaderboard.getEntry(fisher.getUniqueId());
        float increaseAmount = 1.0f;

        if (entry != null) {
            entry.incrementValue(increaseAmount);
            leaderboard.updateEntry(entry);
        } else {
            leaderboard.addEntry(new CompetitionEntry(fisher.getUniqueId(), fish, competition.getCompetitionType()));
        }

        if (entry != null && entry.getValue() >= competition.getNumberNeeded()) {
            competition.singleReward(fisher);
            competition.end(false);
        }
    }

    @Override
    public EMFMessage getBeginMessage(@NotNull Competition competition, CompetitionType type) {
        return getTypeFormat(competition, ConfigMessage.COMPETITION_START);
    }

    @Override
    public @NotNull EMFMessage getTypeFormat(@NotNull Competition competition, ConfigMessage configMessage) {
        final EMFMessage message = CompetitionStrategy.super.getTypeFormat(competition, configMessage);
        message.setAmount(Integer.toString(competition.getNumberNeeded()));
        Rarity selectedRarity = competition.getSelectedRarity();
        if (selectedRarity == null) {
            EvenMoreFish.getInstance().getLogger().warning("Null rarity found. Please check your config files.");
            return message;
        }
        message.setRarity(selectedRarity.getDisplayName());

        return message;
    }

    private boolean chooseRarity(Competition competition) {
        List<Rarity> configRarities = competition.getCompetitionFile().getAllowedRarities();

        if (configRarities.isEmpty()) {
            EvenMoreFish.getInstance().getLogger().severe("No allowed-rarities list found in the " + competition.getCompetitionFile().getFileName() + " competition config file.");
            return false;
        }

        competition.setNumberNeeded(competition.getCompetitionFile().getNumberNeeded());

        try {
            Rarity rarity = configRarities.get(EvenMoreFish.getInstance().getRandom().nextInt(configRarities.size()));
            if (rarity != null) {
                competition.setSelectedRarity(rarity);
                return true;
            }
            competition.setSelectedRarity(FishManager.getInstance().getRandomWeightedRarity(null, 0, null, Set.copyOf(FishManager.getInstance().getRarityMap().values())));
            return true;
        } catch (IllegalArgumentException exception) {
            EvenMoreFish.getInstance()
                    .getLogger()
                    .severe("Could not load: " + competition.getCompetitionName() + " because a random rarity could not be chosen. \nIf you need support, please provide the following information:");
            EvenMoreFish.getInstance().getLogger().severe("rarities.size(): " + FishManager.getInstance().getRarityMap().size());
            EvenMoreFish.getInstance().getLogger().severe("configRarities.size(): " + configRarities.size());
            // Also log the exception
            EvenMoreFish.getInstance().getLogger().log(Level.SEVERE, exception.getMessage(), exception);
            return false;
        }
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
        EMFMessage message = ConfigMessage.LEADERBOARD_LARGEST_FISH.getMessage();
        message.setLength(getDecimalFormat().format(entry.getValue()));
        return message;
    }

}
