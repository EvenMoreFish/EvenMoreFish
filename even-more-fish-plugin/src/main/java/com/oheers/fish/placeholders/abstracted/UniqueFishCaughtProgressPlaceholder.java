package com.oheers.fish.placeholders.abstracted;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.Logging;
import com.oheers.fish.database.DatabaseUtil;
import com.oheers.fish.database.data.UserFishRarityKey;
import com.oheers.fish.database.data.manager.DataManager;
import com.oheers.fish.database.data.manager.UserManager;
import com.oheers.fish.database.model.user.UserFishStats;
import com.oheers.fish.fishing.items.Fish;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

@ApiStatus.Internal
public abstract class UniqueFishCaughtProgressPlaceholder implements EMFPlaceholder {

    private final String prefix;
    private final int prefixLength;

    protected UniqueFishCaughtProgressPlaceholder(@NotNull String prefix) {
        this.prefix = prefix;
        this.prefixLength = prefix.length();
    }

    @Override
    public boolean shouldProcess(@NotNull String identifier) {
        return identifier.startsWith(prefix);
    }

    protected int getPrefixLength() {
        return prefixLength;
    }

    protected @Nullable UUID resolveTarget(@Nullable OfflinePlayer player, @NotNull String target, @NotNull String identifier) {
        UUID uuid = fetchPlayerOrUUIDString(player, target);
        if (uuid == null) {
            if (target.equalsIgnoreCase("player")) {
                debug("Placeholder %s could not resolve player context for identifier: %s".formatted(getClass().getSimpleName(), identifier));
            } else {
                debug("Placeholder %s received an invalid UUID target '%s' for identifier: %s".formatted(getClass().getSimpleName(), target, identifier));
            }
        }
        return uuid;
    }

    protected @NotNull String formatProgress(int caught, int total) {
        return caught + "/" + total;
    }

    protected int countCaughtFish(@NotNull UUID uuid, @NotNull List<Fish> fishList) {
        if (DatabaseUtil.isDatabaseOffline()) {
            return 0;
        }

        EvenMoreFish plugin = EvenMoreFish.getInstance();
        UserManager userManager = plugin.getPluginDataManager().getUserManager();
        if (userManager == null) {
            return 0;
        }

        int userId = userManager.getUserId(uuid);
        if (userId == 0) {
            return 0;
        }

        DataManager<UserFishStats> userFishStatsDataManager = plugin.getPluginDataManager().getUserFishStatsDataManager();
        if (userFishStatsDataManager == null) {
            return 0;
        }

        int caught = 0;
        for (Fish fish : fishList) {
            UserFishStats stats = userFishStatsDataManager.get(UserFishRarityKey.of(userId, fish).toString());
            if (stats != null && stats.getQuantity() > 0) {
                caught++;
            }
        }
        return caught;
    }

    protected void debug(@NotNull String message) {
        try {
            Logging.debug(message);
        } catch (RuntimeException ignored) {
            // Unit tests may not have a plugin singleton available for debug logging.
        }
    }
}
