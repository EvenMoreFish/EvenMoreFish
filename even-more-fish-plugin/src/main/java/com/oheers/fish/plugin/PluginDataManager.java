package com.oheers.fish.plugin;


import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.database.Database;
import com.oheers.fish.database.DatabaseUtil;
import com.oheers.fish.database.data.DatabaseWriteQueue;
import com.oheers.fish.database.data.FishLogKey;
import com.oheers.fish.database.data.FishRarityKey;
import com.oheers.fish.database.data.UserFishRarityKey;
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
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class PluginDataManager {
    private final EvenMoreFish plugin;
    private Database database;
    private UserManager userManager;
    private DatabaseWriteQueue writeQueue;

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
        this.writeQueue = new DatabaseWriteQueue();
        initDataManagers();
    }

    public void initDataManagers() {
        this.userManager = new UserManager(database, writeQueue, null);
        this.fishLogDataManager = new DataManager<>(new FishLogSavingStrategy(writeQueue), key -> {
            FishLogKey logKey = FishLogKey.from(key);
            return Collections.singleton(database.getFishLog(logKey.getUserId(), logKey.getFishName(), logKey.getFishRarity(), logKey.getDateTime()));
        });
        this.fishStatsDataManager = new DataManager<>(new FishStatsSavingStrategy(writeQueue), key -> {
            final FishRarityKey fishRarityKey = FishRarityKey.from(key);
            return database.getFishStats(fishRarityKey.fishName(),fishRarityKey.fishRarity());
        });

        this.userFishStatsDataManager = new DataManager<UserFishStats>(
            new UserFishStatsSavingStrategy(writeQueue),
            key -> {
                final UserFishRarityKey userFishRarityKey = UserFishRarityKey.from(key);
                return database.getUserFishStats(userFishRarityKey.userId(), userFishRarityKey.fishName(), userFishRarityKey.fishRarity());
            },
            Long.valueOf(MainConfig.getInstance().getUserFishStatsSaveInterval()),
            TimeUnit.valueOf(MainConfig.getInstance().getSaveIntervalUnit())
        );

        this.userReportDataManager = new DataManager<>(new UserReportsSavingStrategy(writeQueue), uuid -> database.getUserReport(UUID.fromString(uuid)));
        this.competitionDataManager = new DataManager<CompetitionReport>(
            new CompetitionSavingStrategy(writeQueue),
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
            // writes onto the write queue.
            userReportDataManager.shutdown();
            userFishStatsDataManager.shutdown();
            fishStatsDataManager.shutdown();
            fishLogDataManager.shutdown();
            competitionDataManager.shutdown();

            // Drain the write queue before closing the pool so no queued
            // writes are lost. Blocking here (onDisable) is acceptable.
            if (writeQueue != null) {
                writeQueue.shutdown(60, TimeUnit.SECONDS);
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
     * The single-threaded queue all database writes are dispatched to.
     */
    public DatabaseWriteQueue getWriteQueue() {
        return writeQueue;
    }


}
