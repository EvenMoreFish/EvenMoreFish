package com.oheers.fish.competition.types;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.fishing.items.IFish;
import com.oheers.fish.competition.Competition;
import com.oheers.fish.competition.CompetitionEntry;
import com.oheers.fish.competition.leaderboard.Leaderboard;
import com.oheers.fish.competition.CompetitionType;
import com.oheers.fish.competition.CompetitionTypeRegistry;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Stream;

public class RandomCompetitionType implements CompetitionType {

    @Override
    public @NonNull Component getTypeVariable() {
        throw new UnsupportedOperationException("RANDOM CompetitionType should not be directly used.");
    }

    @Override
    public @NonNull Component getBossbarPrefix() {
        throw new UnsupportedOperationException("RANDOM CompetitionType should not be directly used.");
    }

    @Override
    public boolean shouldReverseLeaderboard() {
        throw new UnsupportedOperationException("RANDOM CompetitionType should not be directly used.");
    }

    @Override
    public boolean isUsable(@NonNull Competition competition) {
        throw new UnsupportedOperationException("RANDOM CompetitionType should not be directly used.");
    }

    @Override
    public void applyToLeaderboard(@NonNull IFish fish, @NonNull UUID fisher, @NonNull Leaderboard leaderboard, @NonNull Competition competition) {
        throw new UnsupportedOperationException("RANDOM CompetitionType should not be directly used.");
    }

    @Override
    public @NonNull Component formatLeaderboardEntry(@NonNull CompetitionEntry entry) {
        throw new UnsupportedOperationException("RANDOM CompetitionType should not be directly used.");
    }

    @Override
    public boolean useFishLength() {
        throw new UnsupportedOperationException("RANDOM CompetitionType should not be directly used.");
    }

    @Override
    public boolean isSingleReward() {
        throw new UnsupportedOperationException("RANDOM CompetitionType should not be directly used.");
    }

    public CompetitionType getRandomType(@NonNull Competition competition) {
        List<CompetitionType> types = getValidTypes(competition)
            .filter(type -> {
                if (type instanceof RandomCompetitionType) {
                    return false;
                }
                try {
                    return type.isUsable(competition);
                } catch (Exception exception) {
                    EvenMoreFish.getInstance().getLogger().log(Level.SEVERE, exception.getMessage(), exception);
                    return false;
                }
            })
            .toList();

        if (types.isEmpty()) {
            EvenMoreFish.getInstance().getLogger().warning("No competition types available for random strategy. Defaulting to LARGEST_FISH.");
            return CompetitionType.DEFAULT;
        }

        int type = EvenMoreFish.RANDOM.nextInt(types.size());
        return types.get(type);
    }

    private Stream<CompetitionType> getValidTypes(@NonNull Competition competition) {
        List<String> types = competition.getCompetitionFile().getConfig().getStringList("random-selection");
        if (types == null || types.isEmpty()) {
            return CompetitionTypeRegistry.getInstance().getRegistry().values().stream();
        }
        return types.stream()
            .map(CompetitionTypeRegistry.getInstance()::get)
            .filter(Objects::nonNull);
    }

    @Override
    public @NonNull String getKey() {
        return "random";
    }

}

