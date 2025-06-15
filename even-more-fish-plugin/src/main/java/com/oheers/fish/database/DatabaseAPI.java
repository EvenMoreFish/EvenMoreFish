package com.oheers.fish.database;

import com.oheers.fish.competition.Competition;
import com.oheers.fish.database.model.CompetitionReport;
import com.oheers.fish.database.model.fish.FishLog;
import com.oheers.fish.database.model.fish.FishStats;
import com.oheers.fish.database.model.user.UserFishStats;
import com.oheers.fish.database.model.user.UserReport;
import com.oheers.fish.fishing.items.Fish;
import com.oheers.fish.fishing.items.Rarity;
import org.bukkit.entity.HumanEntity;
import org.jetbrains.annotations.NotNull;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;

/**
 * Interface defining all database operations related to the fishing system.
 * Provides methods for managing user data, fish statistics, competitions, and transactions.
 */
public interface DatabaseAPI {

    /**
     * Checks if a user exists in the database.
     *
     * @param uuid The UUID of the player to check
     * @return true if the user exists, false otherwise
     */
    boolean hasUser(@NotNull UUID uuid);


    /**
     * Retrieves the internal database ID for a player.
     *
     * @param uuid The UUID of the player
     * @return The database ID of the player, or 0 if not found
     */
    int getUserId(@NotNull UUID uuid);


    /**
     * Retrieves a user's fishing report from the database.
     *
     * @param uuid The UUID of the player
     * @return The UserReport containing all fishing statistics, or null if not found
     */
    UserReport getUserReport(@NotNull UUID uuid);

    /**
     * Checks if fish statistics exist in the database for a specific fish.
     *
     * @param fish The fish to check
     * @return true if statistics exist, false otherwise
     */
    boolean hasFishStats(@NotNull Fish fish);


    /**
     * Increments the catch count for a specific fish in the database.
     *
     * @param fish The fish to update
     */
    void incrementFish(@NotNull Fish fish);

    /**
     * Checks if a player has caught a specific fish.
     *
     * @param fish The fish to check
     * @param user The player to check
     * @return true if the player has caught the fish, false otherwise
     */
    boolean userHasFish(@NotNull Fish fish, @NotNull HumanEntity user);

    /**
     * Checks if a player has caught a specific rarity.
     *
     * @param rarity The rarity to check
     * @param user The player to check
     * @return true if the player has caught the rarity, false otherwise
     */
    boolean userHasRarity(@NotNull Rarity rarity, @NotNull HumanEntity user);

    /**
     * Checks if a player has caught a specific fish by rarity and name.
     *
     * @param rarity The rarity of the fish
     * @param fish The name of the fish
     * @param id The database ID of the player
     * @return true if the player has caught the fish, false otherwise
     */
    boolean userHasFish(@NotNull String rarity, @NotNull String fish, int id);

    /**
     * Checks if a player has caught a specific rarity by its name.
     *
     * @param rarity The rarity's name
     * @param id The database ID of the player
     * @return true if the player has caught the rarity, false otherwise
     */
    boolean userHasRarity(@NotNull String rarity, int id);

    /**
     * Creates a competition report in the database.
     *
     * @param competition The competition to record
     */
    void createCompetitionReport(@NotNull Competition competition);

    /**
     * Records a fish sale transaction in the database.
     *
     * @param transactionId Unique ID for the transaction
     * @param fishName Name of the fish sold
     * @param fishRarity Rarity of the fish sold
     * @param fishAmount Number of fish sold
     * @param fishLength Length of the fish sold
     * @param priceSold Price the fish was sold for
     */
    void createSale(
            @NotNull String transactionId,
            @NotNull String fishName,
            @NotNull String fishRarity,
            int fishAmount,
            double fishLength,
            double priceSold
    );

    /**
     * Creates a transaction record in the database.
     *
     * @param transactionId Unique ID for the transaction
     * @param userId Database ID of the user involved
     * @param timestamp When the transaction occurred
     */
    void createTransaction(
            @NotNull String transactionId,
            int userId,
            @NotNull Timestamp timestamp
    );

    /**
     * Retrieves statistics for a specific fish caught by a specific player.
     *
     * @param userId Database ID of the player
     * @param fishName Name of the fish
     * @param fishRarity Rarity of the fish
     * @return UserFishStats containing the statistics, or null if not found
     */
    UserFishStats getUserFishStats(final int userId, final String fishName, final String fishRarity);


    /**
     * Retrieves a specific fish log entry from the database.
     *
     * @param userId Database ID of the player
     * @param fishName Name of the fish
     * @param fishRarity Rarity of the fish
     * @param time When the fish was caught
     * @return FishLog containing the log details, or null if not found
     */
    FishLog getFishLog(final int userId, final String fishName, final String fishRarity, final LocalDateTime time);

    /**
     * Retrieves all log entries for a specific fish caught by a player.
     *
     * @param userId Database ID of the player
     * @param fishName Name of the fish
     * @param fishRarity Rarity of the fish
     * @return Set of FishLog entries, empty set if none found
     */
    Set<FishLog> getFishLogEntries(final int userId, final String fishName, final String fishRarity);

    /**
     * Creates a new fish log entry in the database.
     *
     * @param fishLogEntry The log entry to create
     */
    void setFishLogEntry(final FishLog fishLogEntry);

    /**
     * Retrieves global statistics for a specific fish.
     *
     * @param fishName Name of the fish
     * @param fishRarity Rarity of the fish
     * @return FishStats containing the statistics, or null if not found
     */
    FishStats getFishStats(final String fishName, final String fishRarity);

    /**
     * Updates global statistics for a specific fish in the database.
     *
     * @param fishStats The updated statistics
     */
    void upsertFishStats(final FishStats fishStats);

    /**
     * Batch inserts multiple fish log entries into the database.
     *
     * @param logs Collection of FishLog entries to insert
     */
    void batchInsertFishLogs(Collection<FishLog> logs);

    /**
     * Creates or updates a user's fishing report in the database.
     * Performs an upsert operation (insert if not exists, update if exists).
     *
     * @param report The UserReport containing all fishing statistics
     */
    Integer upsertUserReport(UserReport report);

    /**
     * Batch updates multiple user fish statistics records in the database.
     * Performs upsert operations (insert if not exists, update if exists).
     *
     * @param userFishStats Collection of UserFishStats to update
     */
    void batchUpdateUserFishStats(Collection<UserFishStats> userFishStats);


    /**
     * Updates or creates a competition report in the database.
     *
     * @param competition The competition report to update
     */
    void updateCompetition(CompetitionReport competition);

    /**
     * Retrieves a competition report from the database by its ID.
     *
     * @param id The database ID of the competition
     * @return CompetitionReport containing the competition details, or null if not found
     */
    CompetitionReport getCompetitionReport(int id);

    /**
     * Batch inserts multiple competition reports into the database.
     *
     * @param competitions Collection of CompetitionReports to insert
     */
    void batchUpdateCompetitions(Collection<CompetitionReport> competitions);
}