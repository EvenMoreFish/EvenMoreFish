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

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.stream.Stream;

public class RandomStrategy implements CompetitionStrategy {

    private CompetitionType randomType;

    @Override
    public boolean randomInit(@NotNull Competition competition) {
        // This has to return false
        return false;
    }

    @Override
    public boolean begin(Competition competition) {
        competition.setCompetitionType(getRandomType(competition));
        this.randomType = competition.getCompetitionType();
        competition.setOriginallyRandom(true);
        return true;
    }

    @Override
    public void applyToLeaderboard(Fish fish, Player fisher, Leaderboard leaderboard, Competition competition) {
        randomType.getStrategy().applyToLeaderboard(fish,fisher,leaderboard,competition);
    }

    @Override
    public EMFMessage getBeginMessage(Competition competition, CompetitionType type) {
        return randomType.getStrategy().getBeginMessage(competition, type);
    }

    @Override
    public EMFMessage getSingleConsoleLeaderboardMessage(@NotNull CompetitionEntry entry) {
        return randomType.getStrategy().getSingleConsoleLeaderboardMessage(entry);
    }

    /**
     * Gets the single player leaderboard message.
     *
     * @param entry The competition entry to get the leaderboard information from.
     * @return The single player leaderboard message.
     */
    @Override
    public EMFMessage getSinglePlayerLeaderboard(@NotNull CompetitionEntry entry) {
        return randomType.getStrategy().getSinglePlayerLeaderboard(entry);
    }

    @Override
    public @NotNull EMFMessage getTypeFormat(@NotNull Competition competition, ConfigMessage configMessage) {
        return randomType.getStrategy().getTypeFormat(competition, configMessage);
    }

    public CompetitionType getRandomType(@NotNull Competition competition) {
        List<CompetitionType> types = getValidTypes(competition)
            .filter(type -> {
                try {
                    return type.getStrategy().randomInit(competition);
                } catch (Exception exception) {
                    EvenMoreFish.getInstance().getLogger().log(Level.SEVERE, exception.getMessage(), exception);
                    return false;
                }
            })
            .toList();

        if (types.isEmpty()) {
            EvenMoreFish.getInstance().getLogger().warning("No competition types available for random strategy. Defaulting to LARGEST_FISH.");
            return CompetitionType.LARGEST_FISH;
        }

        int type = EvenMoreFish.getInstance().getRandom().nextInt(types.size());
        return types.get(type);
    }

    private Stream<CompetitionType> getValidTypes(@NotNull Competition competition) {
        List<String> types = competition.getCompetitionFile().getConfig().getStringList("random-selection");
        if (types == null || types.isEmpty()) {
            return Arrays.stream(CompetitionType.values());
        }
        return types.stream().map(CompetitionType::getType).filter(Objects::nonNull);
    }

    @Override
    public boolean shouldUseFishLength() {
        return randomType.getStrategy().shouldUseFishLength();
    }

    @Override
    public DecimalFormat getDecimalFormat() {
        return randomType.getStrategy().getDecimalFormat();
    }

    @Override
    public boolean isSingleReward() {
        return randomType.getStrategy().isSingleReward();
    }

}
