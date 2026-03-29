package com.oheers.fish.placeholders;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.competition.Competition;
import com.oheers.fish.competition.CompetitionEntry;
import com.oheers.fish.database.DatabaseUtil;
import com.oheers.fish.database.model.user.UserReport;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@ApiStatus.Internal
public interface EMFPlaceholder {

    boolean shouldProcess(@NotNull String identifier);

    @Nullable String parsePAPI(@Nullable Player player, @NotNull String identifier);

    default @Nullable CompetitionEntry fetchEntry(@NotNull Competition active, @NotNull String identifier, int prefixLength) {
        Integer place = FishUtils.getInteger(identifier.substring(prefixLength));
        if (place == null || place <= 0) {
            return null;
        }
        return active.getLeaderboard().getEntry(place);
    }

    default @Nullable UUID fetchPlayerOrUUIDString(@Nullable Player player, @NotNull String substring) {
        if (substring.equalsIgnoreCase("player")) {
            return player == null ? null : player.getUniqueId();
        }
        try {
            return UUID.fromString(substring);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    default @Nullable UserReport fetchUserReport(@NotNull UUID uuid) {
        // Should remove all NPEs if the database is offline.
        // Any NPEs that show up will be bugs.
        if (DatabaseUtil.isDatabaseOffline()) {
            return null;
        }
        return EvenMoreFish.getInstance().getPluginDataManager().getUserReportDataManager().get(uuid.toString());
    }

}
