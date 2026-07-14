package com.oheers.fish.fishing;


import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.events.EMFFishCaughtEvent;
import com.oheers.fish.api.events.EMFFishHuntEvent;
import com.oheers.fish.competition.Competition;
import com.oheers.fish.database.DatabaseUtil;
import com.oheers.fish.database.data.FishRarityKey;
import com.oheers.fish.database.data.UserFishRarityKey;
import com.oheers.fish.database.data.manager.DataManager;
import com.oheers.fish.database.model.fish.FishLog;
import com.oheers.fish.database.model.fish.FishStats;
import com.oheers.fish.database.model.user.UserFishStats;
import com.oheers.fish.database.model.user.UserReport;
import com.oheers.fish.fishing.items.Fish;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

/*
Handles DB related logic.
 */
public class EMFFishListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEMFFishCatch(EMFFishCaughtEvent event) {
        handleFishEvent(event.getPlayer(), (Fish) event.getFish(), event.getCatchTime());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEMFFishHunt(EMFFishHuntEvent event) {
        handleFishEvent(event.getPlayer(), (Fish) event.getFish(), event.getHuntTime());
    }

    private void handleFishEvent(Player player, Fish fish, LocalDateTime catchTime) {
        if (!DatabaseUtil.isDatabaseOnline()) {
            return;
        }

        final int userId = EvenMoreFish.getInstance().getPluginDataManager().getUserManager().getUserId(player.getUniqueId());

        handleFishLog(userId, fish, catchTime);
        handleUserFishStats(userId, fish);
        handleFishStats(fish);
        handleUserReport(player.getUniqueId(), fish);
    }


    private void handleUserReport(final UUID uuid, Fish fish) {
        final DataManager<UserReport> userReportDataManager = EvenMoreFish.getInstance().getPluginDataManager().getUserReportDataManager();
        final UserReport userReport = userReportDataManager.get(String.valueOf(uuid), key -> EvenMoreFish.getInstance().getPluginDataManager().getDatabase().getUserReport(uuid));

        if (userReport.getShortestLength() == -1 || userReport.getShortestLength() > fish.getLength()) {
            userReport.setShortestLengthAndFish(fish);
        }

        if (userReport.getLargestLength() < fish.getLength()) {
            userReport.setLongestLengthAndFish(fish);
        }

        if (userReport.getFirstFish().toString().equals(".")) {
            userReport.setFirstFish(FishRarityKey.of(fish));
        }

        userReport.setRecentFish(FishRarityKey.of(fish));
        userReport.incrementFishCaught(1);
        userReport.incrementTotalLength(fish.getLength());

        userReportDataManager.update(String.valueOf(uuid), userReport);
        EvenMoreFish.getInstance().debug("Saving user report %s".formatted(userReport.toString()));
    }

    private void handleFishStats(final @NotNull Fish fish) {
        final DataManager<FishStats> fishStatsDataManager = EvenMoreFish.getInstance().getPluginDataManager().getFishStatsDataManager();
        final FishRarityKey fishRarityKey = FishRarityKey.of(fish);
        final String key = fishRarityKey.toString();
        FishStats stats = fishStatsDataManager.peek(key);
        if (stats == null) {
            if (EvenMoreFish.getInstance().getPluginDataManager().isFishStatsPreloaded()) {
                // Every stored row is already cached, so a miss means the
                // fish has never been caught — no blocking lookup needed.
                stats = FishStats.empty(fish, LocalDateTime.now());
            } else {
                final var loadResult = EvenMoreFish.getInstance().getPluginDataManager().getDatabase().loadFishStats(fish.getName(), fish.getRarity().getId());
                if (loadResult.isUnreadable()) {
                    EvenMoreFish.getInstance().getLogger().warning("Skipping fish stats update for " + key + " because the stored row could not be read.");
                    return;
                }
                if (loadResult.isFound()) {
                    stats = loadResult.getValue();
                    fishStatsDataManager.cacheLoadedValue(key, stats);
                } else {
                    stats = FishStats.empty(fish, LocalDateTime.now());
                }
            }
        }

        UUID fishermanUuid = fish.getFishermanUUID();
        float length = fish.getLength();
        if (fishermanUuid != null) {
            if (stats.getLongestLength() < length) {
                stats.setLongestLength(length);
                stats.setLongestFisher(fishermanUuid);
            }

            if (stats.getShortestLength() > length) {
                stats.setShortestLength(length);
                stats.setShortestFisher(fishermanUuid);
            }
        }

        stats.incrementQuantity();
        fishStatsDataManager.update(key, stats);
        EvenMoreFish.getInstance().debug("Fish Stats: %s".formatted( stats.toString()));
    }

    private void handleFishLog(final int userId, final Fish fish, final LocalDateTime catchTime) {
        final DataManager<Collection<FishLog>> fishLogDataManager = EvenMoreFish.getInstance().getPluginDataManager().getFishLogDataManager();
        final String competitionId = Competition.getCurrentlyActive() != null ? Competition.getCurrentlyActive().getCompetitionName() : null;
        final FishLog log = new FishLog(userId, fish, catchTime, competitionId);
        final String key = UserFishRarityKey.of(userId,fish).toString();
        fishLogDataManager.update(key, Collections.singletonList(log));
    }

    private void handleUserFishStats(final int userId, final @NotNull Fish fish) {
        final DataManager<UserFishStats> userFishStatsDataManager = EvenMoreFish.getInstance().getPluginDataManager().getUserFishStatsDataManager();
        final String key = UserFishRarityKey.of(userId,fish).toString();
        UserFishStats stats = userFishStatsDataManager.peek(key);
        if (stats == null) {
            if (EvenMoreFish.getInstance().getPluginDataManager().isUserFishStatsPreloaded(userId)) {
                // All of this user's rows are already cached, so a miss means
                // a first catch of this fish — no blocking lookup needed.
                stats = new UserFishStats(userId, fish, LocalDateTime.now());
            } else {
                final var loadResult = EvenMoreFish.getInstance().getPluginDataManager().getDatabase().loadUserFishStats(userId, fish.getName(), fish.getRarity().getId());
                if (loadResult.isUnreadable()) {
                    EvenMoreFish.getInstance().getLogger().warning("Skipping user fish stats update for " + key + " because the stored row could not be read.");
                    return;
                }
                if (loadResult.isFound()) {
                    stats = loadResult.getValue();
                    userFishStatsDataManager.cacheLoadedValue(key, stats);
                } else {
                    stats = new UserFishStats(userId, fish, LocalDateTime.now());
                }
            }
        }

        if (stats.getLongestLength() < fish.getLength()) {
            stats.setLongestLength(fish.getLength());
        }

        if (stats.getShortestLength() > fish.getLength()) {
            stats.setShortestLength(fish.getLength());
        }

        stats.incrementQuantity();
        userFishStatsDataManager.update(key, stats);
    }
}
