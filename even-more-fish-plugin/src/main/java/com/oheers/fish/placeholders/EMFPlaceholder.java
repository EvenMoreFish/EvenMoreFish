package com.oheers.fish.placeholders;

import com.oheers.fish.FishUtils;
import com.oheers.fish.competition.Competition;
import com.oheers.fish.competition.CompetitionEntry;
import com.oheers.fish.messages.ConfigMessage;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Internal
public interface EMFPlaceholder {

    boolean shouldProcess(@NotNull String identifier);

    @Nullable String parsePAPI(@Nullable OfflinePlayer player, @NotNull String identifier);

    default @Nullable CompetitionEntry fetchEntry(@NotNull Competition active, @NotNull String identifier, int prefixLength, @NotNull ConfigMessage notFoundMessage) {
        Integer place = FishUtils.getInteger(identifier.substring(prefixLength));
        if (place == null || place <= 0) {
            return null;
        }
        return active.getLeaderboard().getEntry(place);
    }

}
