package com.oheers.fish.competition;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.fishing.items.IFish;
import com.oheers.fish.api.fishing.items.IRarity;
import com.oheers.fish.api.registry.RegistryItem;
import com.oheers.fish.competition.leaderboard.Leaderboard;
import com.oheers.fish.competition.types.LargestFishCompetitionType;
import com.oheers.fish.competition.types.RandomCompetitionType;
import net.kyori.adventure.text.Component;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public interface CompetitionType extends RegistryItem {

    LargestFishCompetitionType DEFAULT = new LargestFishCompetitionType();
    RandomCompetitionType RANDOM = new RandomCompetitionType();

    @NonNull Component getTypeVariable(@NonNull Competition competition);

    @NonNull Component getBossbarPrefix();

    boolean shouldReverseLeaderboard();

    boolean isUsable(@NonNull Competition competition);

    void applyToLeaderboard(@NonNull IFish fish, @NonNull UUID fisher, @NonNull Leaderboard leaderboard, @NonNull Competition competition);

    @NonNull Component formatLeaderboardEntry(@NonNull CompetitionEntry entry);

    boolean useFishLength();

    boolean isSingleReward();

    default boolean register() {
        return CompetitionTypeRegistry.getInstance().register(this);
    }

    default boolean unregister() {
        return CompetitionTypeRegistry.getInstance().unregister(this);
    }

    @NonNull String getAuthor();

    @NonNull Plugin getPlugin();

    default @Nullable List<@NonNull IRarity> getAllowedRaritiesOrLog(@NonNull Competition competition) {
        List<IRarity> configRarities = competition.getCompetitionFile().getAllowedRarities();
        if (configRarities.isEmpty()) {
            EvenMoreFish.getInstance().getLogger()
                .severe("No allowed-rarities list found in " + competition.getCompetitionFile().getFileName() + " competition config file.");
            return null;
        }
        return configRarities;
    }

    /**
     * Chooses a random CompetitionType.
     */
    abstract class Random implements CompetitionType {

        @Override
        public final @NonNull Component getTypeVariable(@NonNull Competition competition) {
            throw new UnsupportedOperationException("RANDOM CompetitionType should not be directly used.");
        }

        @Override
        public final @NonNull Component getBossbarPrefix() {
            throw new UnsupportedOperationException("RANDOM CompetitionType should not be directly used.");
        }

        @Override
        public final boolean shouldReverseLeaderboard() {
            throw new UnsupportedOperationException("RANDOM CompetitionType should not be directly used.");
        }

        @Override
        public final boolean isUsable(@NonNull Competition competition) {
            throw new UnsupportedOperationException("RANDOM CompetitionType should not be directly used.");
        }

        @Override
        public final void applyToLeaderboard(@NonNull IFish fish, @NonNull UUID fisher, @NonNull Leaderboard leaderboard, @NonNull Competition competition) {
            throw new UnsupportedOperationException("RANDOM CompetitionType should not be directly used.");
        }

        @Override
        public final @NonNull Component formatLeaderboardEntry(@NonNull CompetitionEntry entry) {
            throw new UnsupportedOperationException("RANDOM CompetitionType should not be directly used.");
        }

        @Override
        public final boolean useFishLength() {
            throw new UnsupportedOperationException("RANDOM CompetitionType should not be directly used.");
        }

        @Override
        public final boolean isSingleReward() {
            throw new UnsupportedOperationException("RANDOM CompetitionType should not be directly used.");
        }

        public abstract @NonNull CompetitionType getRandomType(@NonNull Competition competition);

    }

    /**
     * Forwards all methods to a randomly selected type.
     * <p>
     * Internal use only.
     */
    @ApiStatus.Internal
    class Forwarding implements CompetitionType {

        private final Random random;
        private final CompetitionType ref;

        /**
         * @param random The initial random competition type.
         * @param ref The type to forward to.
         * @throws UnsupportedOperationException if {@code ref} is a {@link Random}
         */
        public Forwarding(@NonNull Random random, @NonNull CompetitionType ref) {
            this.random = random;
            this.ref = ref;
            if (ref instanceof Random) {
                throw new UnsupportedOperationException("CompetitionType.Forwarding cannot forward to another Random type.");
            }
        }

        @Override
        public @NonNull Component getTypeVariable(@NonNull Competition competition) {
            return ref.getTypeVariable(competition);
        }

        @Override
        public @NonNull Component getBossbarPrefix() {
            return ref.getBossbarPrefix();
        }

        @Override
        public boolean shouldReverseLeaderboard() {
            return ref.shouldReverseLeaderboard();
        }

        @Override
        public boolean isUsable(@NonNull Competition competition) {
            return ref.isUsable(competition);
        }

        @Override
        public void applyToLeaderboard(@NonNull IFish fish, @NonNull UUID fisher, @NonNull Leaderboard leaderboard, @NonNull Competition competition) {
            ref.applyToLeaderboard(fish, fisher, leaderboard, competition);
        }

        @Override
        public @NonNull Component formatLeaderboardEntry(@NonNull CompetitionEntry entry) {
            return ref.formatLeaderboardEntry(entry);
        }

        @Override
        public boolean useFishLength() {
            return ref.useFishLength();
        }

        @Override
        public boolean isSingleReward() {
            return ref.isSingleReward();
        }

        @Override
        public @NonNull String getAuthor() {
            return ref.getAuthor();
        }

        @Override
        public @NonNull Plugin getPlugin() {
            return ref.getPlugin();
        }

        @Override
        public @NonNull String getKey() {
            return ref.getKey();
        }

        public @NonNull CompetitionType getRandom() {
            return this.random;
        }

    }

}
