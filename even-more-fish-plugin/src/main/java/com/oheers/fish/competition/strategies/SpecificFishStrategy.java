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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SpecificFishStrategy implements CompetitionStrategy {

    @Override
    public boolean begin(Competition competition) {
        return chooseFish(competition);
    }

    @Override
    public void applyToLeaderboard(Fish fish, Player fisher, Leaderboard leaderboard, Competition competition) {
        Fish selected = competition.getSelectedFish();
        if (selected != null) {
            if (!fish.getName().equalsIgnoreCase(selected.getName()) ||
                    fish.getRarity() != selected.getRarity()) {
                return;
            }
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

    private boolean chooseFish(Competition competition) {
        List<Rarity> configRarities = competition.getCompetitionFile().getAllowedRarities();
        final Logger logger = EvenMoreFish.getInstance().getLogger();
        if (configRarities.isEmpty()) {
            logger.severe(() -> "No allowed-rarities list found in the " + competition.getCompetitionFile().getFileName() + " competition config file.");
            return false;
        }

        List<Fish> fish = new ArrayList<>();
        List<Rarity> allowedRarities = new ArrayList<>();
        double totalWeight = 0;

        for (Rarity rarity : configRarities) {
            fish.addAll(rarity.getOriginalFishList());
            allowedRarities.add(rarity);
            totalWeight += rarity.getWeight();
        }

        int idx = 0;
        for (double r = Math.random() * totalWeight; idx < allowedRarities.size() - 1; ++idx) {
            r -= allowedRarities.get(idx).getWeight();
            if (r <= 0.0) {
                break;
            }
        }

        if (competition.getNumberNeeded() == 0) {
            competition.setNumberNeeded(competition.getCompetitionFile().getNumberNeeded());
        }

        try {
            Fish selectedFish = FishManager.getInstance().getFish(allowedRarities.get(idx), null, null, 1.0d, null, false);
            if (selectedFish == null) {
                // For the catch block to catch.
                throw new IllegalArgumentException();
            }
            competition.setSelectedFish(selectedFish);
            return true;
        } catch (IllegalArgumentException | IndexOutOfBoundsException exception) {
            logger.severe(() -> "Could not load: %s because a random fish could not be chosen. %nIf you need support, please provide the following information:".formatted(competition.getCompetitionName()));
            logger.severe(() -> "fish.size(): %s".formatted(fish.size()));
            logger.severe(() -> "allowedRarities.size(): %s".formatted(allowedRarities.size()));
            // Also log the exception
            logger.log(Level.SEVERE, exception.getMessage(), exception);
            return false;
        }
    }

}
