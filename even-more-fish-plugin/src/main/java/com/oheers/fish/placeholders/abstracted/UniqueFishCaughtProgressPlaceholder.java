package com.oheers.fish.placeholders.abstracted;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.Logging;
import com.oheers.fish.database.DatabaseUtil;
import com.oheers.fish.database.data.UserFishRarityKey;
import com.oheers.fish.database.data.manager.DataManager;
import com.oheers.fish.database.data.manager.UserManager;
import com.oheers.fish.database.model.user.UserFishStats;
import com.oheers.fish.fishing.items.Fish;
import com.oheers.fish.fishing.items.FishManager;
import com.oheers.fish.fishing.items.Rarity;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;
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

    protected @NotNull String formatPercent(int caught, int total) {
        if (total <= 0) {
            return "0%";
        }

        double percent = caught * 100.0D / total;
        if (percent == Math.rint(percent)) {
            return (int) percent + "%";
        }
        return String.format(Locale.ROOT, "%.1f%%", percent);
    }

    protected int remainingFish(@NotNull UUID uuid, @NotNull List<Fish> fishList) {
        return Math.max(0, fishList.size() - countCaughtFish(uuid, fishList));
    }

    protected int totalCaughtQuantity(@NotNull UUID uuid, @NotNull List<Fish> fishList) {
        int userId = resolveUserId(uuid);
        if (userId == 0) {
            return 0;
        }

        DataManager<UserFishStats> userFishStatsDataManager = getUserFishStatsDataManager();
        if (userFishStatsDataManager == null) {
            return 0;
        }

        int total = 0;
        for (Fish fish : fishList) {
            UserFishStats stats = userFishStatsDataManager.get(key(userId, fish));
            if (stats != null) {
                total += stats.getQuantity();
            }
        }
        return total;
    }

    protected boolean hasCaughtFish(@NotNull UUID uuid, @NotNull Fish fish) {
        return timesCaught(uuid, fish) > 0;
    }

    protected int timesCaught(@NotNull UUID uuid, @NotNull Fish fish) {
        int userId = resolveUserId(uuid);
        if (userId == 0) {
            return 0;
        }

        DataManager<UserFishStats> userFishStatsDataManager = getUserFishStatsDataManager();
        if (userFishStatsDataManager == null) {
            return 0;
        }

        UserFishStats stats = userFishStatsDataManager.get(key(userId, fish));
        return stats == null ? 0 : stats.getQuantity();
    }

    protected @Nullable Fish firstUncaughtFish(@NotNull UUID uuid, @NotNull List<Fish> fishList) {
        for (Fish fish : fishList) {
            if (!hasCaughtFish(uuid, fish)) {
                return fish;
            }
        }
        return null;
    }

    protected @NotNull List<Fish> getAllFish() {
        return FishManager.getInstance().getRarityMap().values().stream()
            .map(Rarity::getOriginalFishList)
            .flatMap(List::stream)
            .toList();
    }

    protected @Nullable Rarity resolveRarity(@NotNull String rarityId) {
        return FishManager.getInstance().getRarity(rarityId);
    }

    protected @Nullable Fish resolveFish(@NotNull String rarityId, @NotNull String fishName) {
        return FishManager.getInstance().getFish(rarityId, fishName);
    }

    protected int countCaughtFish(@NotNull UUID uuid, @NotNull List<Fish> fishList) {
        int userId = resolveUserId(uuid);
        if (userId == 0) {
            return 0;
        }

        DataManager<UserFishStats> userFishStatsDataManager = getUserFishStatsDataManager();
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

    protected @Nullable ParsedRarityTarget parseRarityTarget(@Nullable OfflinePlayer player, @NotNull String identifier) {
        String payload = identifier.substring(getPrefixLength());
        int splitIndex = payload.lastIndexOf('_');
        if (splitIndex <= 0 || splitIndex == payload.length() - 1) {
            debug("Placeholder %s received an invalid rarity/target payload for identifier: %s".formatted(getClass().getSimpleName(), identifier));
            return null;
        }

        String rarityId = payload.substring(0, splitIndex);
        String target = payload.substring(splitIndex + 1);
        UUID uuid = resolveTarget(player, target, identifier);
        if (uuid == null) {
            return null;
        }

        Rarity rarity = resolveRarity(rarityId);
        if (rarity == null) {
            debug("Placeholder %s received an invalid rarity '%s' for identifier: %s".formatted(getClass().getSimpleName(), rarityId, identifier));
            return null;
        }

        return new ParsedRarityTarget(uuid, rarityId, rarity, rarity.getOriginalFishList());
    }

    protected @Nullable ParsedFishTarget parseFishTarget(@Nullable OfflinePlayer player, @NotNull String identifier) {
        String payload = identifier.substring(getPrefixLength());
        int targetSplitIndex = payload.lastIndexOf('_');
        if (targetSplitIndex <= 0 || targetSplitIndex == payload.length() - 1) {
            debug("Placeholder %s received an invalid fish/target payload for identifier: %s".formatted(getClass().getSimpleName(), identifier));
            return null;
        }

        String fishKey = payload.substring(0, targetSplitIndex);
        String target = payload.substring(targetSplitIndex + 1);
        int fishSplitIndex = fishKey.indexOf(':');
        if (fishSplitIndex <= 0 || fishSplitIndex == fishKey.length() - 1) {
            debug("Placeholder %s received an invalid rarity:fish key '%s' for identifier: %s".formatted(getClass().getSimpleName(), fishKey, identifier));
            return null;
        }

        String rarityId = fishKey.substring(0, fishSplitIndex);
        String fishName = fishKey.substring(fishSplitIndex + 1);
        UUID uuid = resolveTarget(player, target, identifier);
        if (uuid == null) {
            return null;
        }

        Fish fish = resolveFish(rarityId, fishName);
        if (fish == null) {
            debug("Placeholder %s received an invalid fish '%s' in rarity '%s' for identifier: %s".formatted(getClass().getSimpleName(), fishName, rarityId, identifier));
            return null;
        }

        return new ParsedFishTarget(uuid, rarityId, fishName, fish);
    }

    private int resolveUserId(@NotNull UUID uuid) {
        if (DatabaseUtil.isDatabaseOffline()) {
            return 0;
        }

        EvenMoreFish plugin = EvenMoreFish.getInstance();
        UserManager userManager = plugin.getPluginDataManager().getUserManager();
        if (userManager == null) {
            return 0;
        }

        return userManager.getUserId(uuid);
    }

    private @Nullable DataManager<UserFishStats> getUserFishStatsDataManager() {
        if (DatabaseUtil.isDatabaseOffline()) {
            return null;
        }
        return EvenMoreFish.getInstance().getPluginDataManager().getUserFishStatsDataManager();
    }

    private @NotNull String key(int userId, @NotNull Fish fish) {
        return UserFishRarityKey.of(userId, fish).toString();
    }

    protected void debug(@NotNull String message) {
        try {
            Logging.debug(message);
        } catch (RuntimeException ignored) {
            // Unit tests may not have a plugin singleton available for debug logging.
        }
    }

    protected record ParsedRarityTarget(@NotNull UUID uuid, @NotNull String rarityId, @NotNull Rarity rarity, @NotNull List<Fish> fishList) {
    }

    protected record ParsedFishTarget(@NotNull UUID uuid, @NotNull String rarityId, @NotNull String fishName, @NotNull Fish fish) {
    }
}
