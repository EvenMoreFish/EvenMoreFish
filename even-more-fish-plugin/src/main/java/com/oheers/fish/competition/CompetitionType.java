package com.oheers.fish.competition;

import com.oheers.fish.api.fishing.items.IFish;
import com.oheers.fish.api.registry.EMFRegistry;
import com.oheers.fish.api.registry.RegistryItem;
import com.oheers.fish.competition.leaderboard.Leaderboard;
import com.oheers.fish.competition.types.LargestFishCompetitionType;
import com.oheers.fish.competition.types.RandomCompetitionType;
import com.oheers.fish.messages.ConfigMessage;
import com.oheers.fish.messages.abstracted.EMFMessage;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.NonNull;

import java.util.UUID;

public interface CompetitionType extends RegistryItem {

    LargestFishCompetitionType DEFAULT = new LargestFishCompetitionType();
    RandomCompetitionType RANDOM = new RandomCompetitionType();

    @NonNull Component getTypeVariable();

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

}
