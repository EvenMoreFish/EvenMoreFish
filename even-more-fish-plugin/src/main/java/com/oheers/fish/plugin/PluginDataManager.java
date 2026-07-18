package com.oheers.fish.plugin;


import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.database.Database;
import com.oheers.fish.database.DatabaseUtil;
import com.oheers.fish.database.data.FishLogKey;
import com.oheers.fish.database.data.FishRarityKey;
import com.oheers.fish.database.data.UserFishRarityKey;
import com.oheers.fish.database.execute.DatabaseWorker;
import com.oheers.fish.database.data.manager.DataManager;
import com.oheers.fish.database.data.manager.UserManager;
import com.oheers.fish.database.data.strategy.impl.CompetitionSavingStrategy;
import com.oheers.fish.database.data.strategy.impl.FishLogSavingStrategy;
import com.oheers.fish.database.data.strategy.impl.FishStatsSavingStrategy;
import com.oheers.fish.database.data.strategy.impl.UserFishStatsSavingStrategy;
import com.oheers.fish.database.data.strategy.impl.UserReportsSavingStrategy;
import com.oheers.fish.database.model.CompetitionReport;
import com.oheers.fish.database.model.fish.FishLog;
import com.oheers.fish.database.model.fish.FishStats;
import com.oheers.fish.database.model.user.UserFishStats;
import com.oheers.fish.database.model.user.UserReport;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class PluginDataManager {
    private final EvenMoreFish plugin;
    private Database database;
    private UserManager userManager;
    private DatabaseWorker databaseWorker;

    // Users whose fish stats rows have been fully preloaded into the cache,
    // so a cache miss on the server thread means "no row exists" and no
    // blocking existence query is needed.
    private final Set<Integer> preloadedUserFishStats = ConcurrentHashMap.newKeySet();
    private volatile boolean fishStatsPreloaded;

    // Data Managers
    private DataManager<Collection<FishLog>> fishLogDataManager;
    private DataManager<FishStats> fishStatsDataManager;
    private DataManager<UserFishStats> userFishStatsDataManager;
    private DataManager<UserReport> userReportDataManager;
    private DataManager<CompetitionReport> competitionDataManager;

    public PluginDataManager(EvenMoreFish plugin) {
        this.plugin = plugin;

        init();
    }

    public void init() {
        if (!MainConfig.getInstance().databaseEnabled()) {
            plugin.getLogger().info("Database is disabled in config");
            return;
        }

        this.database = new Database();
        this.databaseWorker = new DatabaseWorker();
        initDataManagers();
        databaseWorker.execute(this::preloadFishStats);
    }

    public void initDataManagers() {
        this.userManager = new UserManager(database, databaseWorker, this::preloadUserData);
        this.fishLogDataManager = new DataManager<>(new FishLogSavingStrategy(databaseWorker), key -> {
            FishLogKey logKey = FishLogKey.from(key);
            return Collections.singleton(database.getFishLog(logKey.getUserId(), logKey.getFishName(), logKey.getFishRarity(), logKey.getDateTime()));
        });
        this.fishStatsDataManager = new DataManager<>(new FishStatsSavingStrategy(databaseWorker), key -> {
            final FishRarityKey fishRarityKey = FishRarityKey.from(key);
            return database.getFishStats(fishRarityKey.fishName(),fishRarityKey.fishRarity());
        });

        this.userFishStatsDataManager = new DataManager<UserFishStats>(
            new UserFishStatsSavingStrategy(databaseWorker),
            key -> {
                final UserFishRarityKey userFishRarityKey = UserFishRarityKey.from(key);
                return database.getUserFishStats(userFishRarityKey.userId(), userFishRarityKey.fishName(), userFishRarityKey.fishRarity());
            },
            Long.valueOf(MainConfig.getInstance().getUserFishStatsSaveInterval()),
            TimeUnit.valueOf(MainConfig.getInstance().getSaveIntervalUnit())
        );

        this.userReportDataManager = new DataManager<>(new UserReportsSavingStrategy(databaseWorker), uuid -> database.getUserReport(UUID.fromString(uuid)));
        this.competitionDataManager = new DataManager<CompetitionReport>(
            new CompetitionSavingStrategy(databaseWorker),
            key -> database.getCompetitionReport(Integer.parseInt(key)),
            Long.valueOf(MainConfig.getInstance().getCompetitionSaveInterval()),
            TimeUnit.valueOf(MainConfig.getInstance().getSaveIntervalUnit())
        );
    }

    public void shutdown() {
        if (database == null || !DatabaseUtil.isDatabaseOnline()) {
            return;
        }

        try {
            // Flush all pending data. The managers enqueue their remaining
            // writes onto the database worker.
            userReportDataManager.shutdown();
            userFishStatsDataManager.shutdown();
            fishStatsDataManager.shutdown();
            fishLogDataManager.shutdown();
            competitionDataManager.shutdown();

            // Drain the database worker before closing the pool so no queued
            // database tasks are lost. Blocking here (onDisable) is acceptable.
            if (databaseWorker != null) {
                databaseWorker.shutdown(60, TimeUnit.SECONDS);
            }

            // Close database connection
            database.shutdown();
            plugin.getLogger().info("Database connections closed successfully");
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Error while shutting down database", e);
        }
    }

    public UserManager getUserManager() {
        return userManager;
    }

    public DataManager<Collection<FishLog>> getFishLogDataManager() {
        return fishLogDataManager;
    }

    public DataManager<FishStats> getFishStatsDataManager() {
        return fishStatsDataManager;
    }

    public DataManager<UserFishStats> getUserFishStatsDataManager() {
        return userFishStatsDataManager;
    }

    public DataManager<UserReport> getUserReportDataManager() {
        return userReportDataManager;
    }

    public DataManager<CompetitionReport> getCompetitionDataManager() {
        return competitionDataManager;
    }

    public Database getDatabase() {
        return database;
    }

    /**
     * The worker all database I/O is dispatched to.
     */
    public DatabaseWorker getDatabaseWorker() {
        return databaseWorker;
    }

    /**
     * True once every row of the global fish stats table has been loaded
     * into the cache, meaning a cache miss can be treated as "row does not
     * exist" without a blocking query.
     */
    public boolean isFishStatsPreloaded() {
        return fishStatsPreloaded;
    }

    /**
     * True once all of the user's fish stats rows have been loaded into the
     * cache, meaning a cache miss for that user can be treated as "row does
     * not exist" without a blocking query.
     */
    public boolean isUserFishStatsPreloaded(int userId) {
        return preloadedUserFishStats.contains(userId);
    }

    public CompletableFuture<Integer> preloadUserDataAsync(UUID uuid) {
        if (databaseWorker == null) {
            return CompletableFuture.completedFuture(0);
        }
        plugin.debug("Queueing user data preload for %s.".formatted(uuid));
        return databaseWorker.query(() -> {
            plugin.debug("Preloading user data for %s.".formatted(uuid));
            int userId = userManager.getUserId(uuid);
            if (userId != 0 && !isUserFishStatsPreloaded(userId)) {
                preloadUserData(uuid, userId);
            }
            plugin.debug("Finished user data preload for %s with user id %d.".formatted(uuid, userId));
            return userId;
        });
    }

    /**
     * Runs on the database worker after the user's row is guaranteed to exist:
     * warms the user report and user fish stats caches so the catch path on
     * the server thread never needs a blocking load.
     */
    private void preloadUserData(UUID uuid, int userId) {
        UserReport report = database.getUserReport(uuid);
        if (report != null) {
            userReportDataManager.cacheLoadedValue(uuid.toString(), report);
        }

        List<UserFishStats> statsRows = database.loadAllUserFishStats(userId);
        if (statsRows != null) {
            for (UserFishStats stats : statsRows) {
                userFishStatsDataManager.cacheLoadedValue(
                    UserFishRarityKey.of(userId, stats.getFishName(), stats.getFishRarity()).toString(),
                    stats
                );
            }
            preloadedUserFishStats.add(userId);
        }
    }

    private void preloadFishStats() {
        plugin.debug("Preloading fish stats.");
        List<FishStats> rows = database.loadAllFishStats();
        if (rows == null) {
            plugin.getLogger().warning("Could not preload fish stats; falling back to on-demand loads.");
            return;
        }

        for (FishStats stats : rows) {
            fishStatsDataManager.cacheLoadedValue(FishRarityKey.of(stats.getFishName(), stats.getFishRarity()).toString(), stats);
        }
        fishStatsPreloaded = true;
        plugin.getLogger().info("Preloaded %d fish stats entries.".formatted(rows.size()));
    }


}
