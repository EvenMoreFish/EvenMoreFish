package com.oheers.fish.database;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.annotations.NeedsTesting;
import com.oheers.fish.api.annotations.TestType;
import com.oheers.fish.competition.Competition;
import com.oheers.fish.competition.CompetitionEntry;
import com.oheers.fish.competition.leaderboard.Leaderboard;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.database.connection.ConnectionFactory;
import com.oheers.fish.database.connection.H2ConnectionFactory;
import com.oheers.fish.database.connection.MigrationManager;
import com.oheers.fish.database.connection.MySqlConnectionFactory;
import com.oheers.fish.database.connection.SqliteConnectionFactory;
import com.oheers.fish.database.data.FishRarityKey;
import com.oheers.fish.database.execute.ExecuteQuery;
import com.oheers.fish.database.execute.ExecuteUpdate;
import com.oheers.fish.database.generated.mysql.Tables;
import com.oheers.fish.database.generated.mysql.tables.records.CompetitionsRecord;
import com.oheers.fish.database.generated.mysql.tables.records.FishLogRecord;
import com.oheers.fish.database.generated.mysql.tables.records.UserFishStatsRecord;
import com.oheers.fish.database.model.CompetitionReport;
import com.oheers.fish.database.model.fish.FishLog;
import com.oheers.fish.database.model.fish.FishStats;
import com.oheers.fish.database.model.user.UserFishStats;
import com.oheers.fish.database.model.user.UserReport;
import com.oheers.fish.database.strategies.DatabaseStrategyFactory;
import com.oheers.fish.fishing.items.Fish;
import com.oheers.fish.fishing.items.Rarity;
import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.HumanEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.*;
import org.jooq.Record;
import org.jooq.conf.MappedSchema;
import org.jooq.conf.MappedTable;
import org.jooq.conf.RenderMapping;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;

import java.sql.Connection;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Level;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@NeedsTesting(
        reason = "Requires full testing, unit where possible, integration eventually.",
        testType = {TestType.MANUAL, TestType.UNIT, TestType.INTEGRATION}
)
public class Database implements DatabaseAPI {

    private String version;
    private final ConnectionFactory connectionFactory;
    private final MigrationManager migrationManager;

    private Settings settings;

    public Database() {
        setJooqStartupProperties();
        this.settings = new Settings();
        this.connectionFactory = getConnectionFactory(MainConfig.getInstance().getDatabaseType().toLowerCase());
        this.connectionFactory.init();

        this.migrationManager = new MigrationManager(connectionFactory);
        if (migrationManager.usingV2()) {
            this.version = "2";
            return;
        }

        this.version = this.migrationManager.getDatabaseVersion().getVersion();
        migrateFromDatabaseVersionToLatest();

        initSettings(MainConfig.getInstance().getPrefix(), MainConfig.getInstance().getDatabase());
    }

    private void setJooqStartupProperties() {
        if (MainConfig.getInstance().isDisableJooqStartupCommments()) {
            System.setProperty("org.jooq.no-logo", "true");
            System.setProperty("org.jooq.no-tips", "true");
        }
    }

    public void migrateFromDatabaseVersionToLatest() {
        switch (version) {
            case "5":
                this.migrationManager.migrateFromV5ToLatest();
                break;
            case "6.0":
                this.migrationManager.migrateFromV6ToLatest();
                break;
            default:
                this.migrationManager.migrateFromVersion(version, true);
                break;
        }

        this.version = this.migrationManager.getDatabaseVersion().getVersion();
    }


    public MigrationManager getMigrationManager() {
        return migrationManager;
    }

    private @NotNull ConnectionFactory getConnectionFactory(final @NotNull String type) {
        return switch (type) {
            case "mysql":
                yield new MySqlConnectionFactory();
            case "sqlite":
                yield new SqliteConnectionFactory();
            default:
                yield new H2ConnectionFactory();
        };
    }

    public void initSettings(final String tablePrefix, final String dbName) {
        settings.setExecuteLogging(true);
        settings.withRenderFormatted(true);
        settings.withRenderMapping(
                new RenderMapping().withSchemata(
                        new MappedSchema()
                                .withInput("") // Leave this if no schema is used
                                .withOutput(dbName)
                                .withTables(
                                        new MappedTable()
                                                .withInputExpression(Pattern.compile("\\$\\{table\\.prefix}(.*)")) // Correct escaped regex
                                                .withOutput(tablePrefix + "$1")
                                )
                )
        );

        this.settings = DatabaseStrategyFactory.getStrategy(connectionFactory).applySettings(settings, tablePrefix, dbName);
    }

    @NotNull
    public DSLContext getContext(Connection connection) {
        return DSL.using(connection, DatabaseUtil.getSQLDialect(this.connectionFactory.getType()), this.settings);
    }

    @Override
    public boolean hasUser(@NotNull UUID uuid) {
        return new ExecuteQuery<Boolean>(connectionFactory, settings) {
            @Override
            protected Boolean onRunQuery(DSLContext dslContext) throws Exception {
                return dslContext.select()
                        .from(Tables.USERS)
                        .where(Tables.USERS.UUID.eq(uuid.toString()))
                        .fetch()
                        .isNotEmpty();
            }

            @Override
            protected Boolean empty() {
                return false;
            }
        }.prepareAndRunQuery();
    }

    public boolean hasFishLog(int userId) {
        if (userId == 0) {
            return false;
        }

        return new ExecuteQuery<Boolean>(connectionFactory, settings) {
            @Override
            protected Boolean onRunQuery(DSLContext dslContext) throws Exception {
                return dslContext.fetchExists(Tables.FISH_LOG
                        .where(Tables.FISH_LOG.USER_ID.eq(userId))
                );
            }

            @Override
            protected Boolean empty() {
                return false;
            }
        }.prepareAndRunQuery();
    }

    @Override
    public int getUserId(@NotNull UUID uuid) {
        return new ExecuteQuery<Integer>(connectionFactory, settings) {
            @Override
            protected Integer onRunQuery(DSLContext dslContext) {
                Integer id = dslContext.select()
                        .from(Tables.USERS)
                        .where(Tables.USERS.UUID.eq(uuid.toString()))
                        .fetchOne(Tables.USERS.ID);

                if (id == null) {
                    return empty();
                }

                return id;
            }

            @Override
            protected Integer empty() {
                return 0;
            }
        }.prepareAndRunQuery();
    }

    @Override
    public UserReport getUserReport(@NotNull UUID uuid) {
        return new ExecuteQuery<UserReport>(connectionFactory, settings) {
            @Override
            protected UserReport onRunQuery(DSLContext dslContext) throws Exception {
                org.jooq.Record tableRecord = dslContext.select()
                        .from(Tables.USERS)
                        .where(Tables.USERS.UUID.eq(uuid.toString()))
                        .fetchOne();
                if (tableRecord == null) {
                    return empty();
                }

                DatabaseUtil.writeDbVerbose("Read user report for user (%s)".formatted(uuid.toString()));
                final int id = tableRecord.getValue(Tables.USERS.ID);
                final int numFishCaught = tableRecord.getValue(Tables.USERS.NUM_FISH_CAUGHT);
                final int competitionsWon = tableRecord.getValue(Tables.USERS.COMPETITIONS_WON);
                final int competitionsJoined = tableRecord.getValue(Tables.USERS.COMPETITIONS_JOINED);
                final FishRarityKey firstFish = FishRarityKey.from(tableRecord.getValue(Tables.USERS.FIRST_FISH));
                final FishRarityKey recentFish = FishRarityKey.from(tableRecord.getValue(Tables.USERS.LAST_FISH));
                final FishRarityKey largestFish = FishRarityKey.from(tableRecord.getValue(Tables.USERS.LARGEST_FISH));
                final float totalFishLength = tableRecord.getValue(Tables.USERS.TOTAL_FISH_LENGTH);
                final float largestLength = tableRecord.getValue(Tables.USERS.LARGEST_LENGTH);
                final String uuid = tableRecord.getValue(Tables.USERS.UUID);
                final int fishSold = tableRecord.getValue(Tables.USERS.FISH_SOLD);
                final double moneyEarned = tableRecord.getValue(Tables.USERS.MONEY_EARNED);

                final FishRarityKey shortestFish = FishRarityKey.from(tableRecord.getValue(Tables.USERS.SHORTEST_FISH));
                final float shortestLength = tableRecord.getValue(Tables.USERS.SHORTEST_LENGTH);

                return new UserReport(
                        id,
                        UUID.fromString(uuid),
                        firstFish,
                        recentFish,
                        largestFish,
                        shortestFish,
                        numFishCaught,
                        competitionsWon,
                        competitionsJoined,
                        largestLength,
                        shortestLength,
                        totalFishLength,
                        fishSold,
                        moneyEarned
                );
            }

            @Override
            protected @Nullable UserReport empty() {
                DatabaseUtil.writeDbVerbose("User report for (%s) does not exist in the database.".formatted(uuid));
                return null;
            }
        }.prepareAndRunQuery();
    }

    @Override
    public boolean hasFishStats(@NotNull Fish fish) {
        return new ExecuteQuery<Boolean>(connectionFactory, settings) {
            @Override
            protected Boolean onRunQuery(DSLContext dslContext) throws Exception {
                return dslContext.select()
                        .from(Tables.FISH)
                        .where(Tables.FISH.FISH_NAME.eq(fish.getName())
                                .and(Tables.FISH.FISH_RARITY.eq(fish.getRarity().getId())))
                        .limit(1)
                        .fetch()
                        .isNotEmpty();
            }

            @Override
            protected Boolean empty() {
                return false;
            }
        }.prepareAndRunQuery();
    }


    @Override
    public void incrementFish(@NotNull Fish fish) {
        new ExecuteUpdate(connectionFactory, settings) {
            @Override
            protected int onRunUpdate(DSLContext dslContext) {
                return dslContext.update(Tables.FISH)
                        .set(Tables.FISH.TOTAL_CAUGHT, Tables.FISH.TOTAL_CAUGHT.plus(1))
                        .where(Tables.FISH.FISH_RARITY.eq(fish.getRarity().getId())
                                .and(Tables.FISH.FISH_NAME.eq(fish.getName())))
                        .execute();
            }
        }.executeUpdate();
    }


    @Override
    public boolean userHasFish(@NotNull Fish fish, @NotNull HumanEntity user) {
        return userHasFish(fish.getRarity().getId(), fish.getName(), getUserId(user.getUniqueId()));
    }

    /**
     * Checks if a player has caught a specific rarity.
     *
     * @param rarity The rarity to check
     * @param user   The player to check
     * @return true if the player has caught the rarity, false otherwise
     */
    @Override
    public boolean userHasRarity(@NotNull Rarity rarity, @NotNull HumanEntity user) {
        return userHasRarity(rarity.getId(), getUserId(user.getUniqueId()));
    }

    @Override
    public void createCompetitionReport(@NotNull Competition competition) {
        new ExecuteUpdate(connectionFactory, settings) {
            @Override
            protected int onRunUpdate(DSLContext dslContext) {
                final Leaderboard leaderboard = competition.getLeaderboard();
                InsertSetMoreStep<CompetitionsRecord> common = dslContext.insertInto(Tables.COMPETITIONS)
                        .set(Tables.COMPETITIONS.COMPETITION_NAME, competition.getCompetitionName());

                if (leaderboard.getSize() <= 0) {
                    return common.set(Tables.COMPETITIONS.WINNER_UUID, leaderboard.getTopEntry().getPlayer().toString())
                            .set(Tables.COMPETITIONS.WINNER_FISH, prepareRarityFishString(leaderboard.getEntry(0).getFish()))
                            .set(Tables.COMPETITIONS.WINNER_SCORE, leaderboard.getTopEntry().getValue())
                            .set(Tables.COMPETITIONS.CONTESTANTS, prepareContestantsString(leaderboard.getEntries()))
                            .execute();
                }
                //add start and end times

                return common.set(Tables.COMPETITIONS.WINNER_UUID, "None")
                        .set(Tables.COMPETITIONS.WINNER_FISH, "None")
                        .set(Tables.COMPETITIONS.WINNER_SCORE, 0f)
                        .set(Tables.COMPETITIONS.CONTESTANTS, "None")
                        .execute();
            }
        }.executeUpdate();
    }

    private String prepareContestantsString(@NotNull List<CompetitionEntry> entries) {
        return StringUtils.join(entries.stream().map(CompetitionEntry::getPlayer).toList(), ",");
    }

    private @NotNull String prepareRarityFishString(final @NotNull Fish fish) {
        return fish.getRarity().getId() + ":" + fish.getName();
    }

    @Override
    public void createSale(@NotNull String transactionId, @NotNull String fishName, @NotNull String fishRarity, int fishAmount, double fishLength, double priceSold) {
        new ExecuteUpdate(connectionFactory, settings) {
            @Override
            protected int onRunUpdate(DSLContext dslContext) {
                return dslContext.insertInto(Tables.USERS_SALES)
                        .set(Tables.USERS_SALES.TRANSACTION_ID, transactionId)
                        .set(Tables.USERS_SALES.FISH_NAME, fishName)
                        .set(Tables.USERS_SALES.FISH_RARITY, fishRarity)
                        .set(Tables.USERS_SALES.FISH_AMOUNT, fishAmount)
                        .set(Tables.USERS_SALES.FISH_LENGTH, fishLength)
                        .set(Tables.USERS_SALES.PRICE_SOLD, priceSold)
                        .execute();
            }
        }.executeUpdate();

    }

    @Override
    public void createTransaction(@NotNull String transactionId, int userId, @NotNull Timestamp timestamp) {
        new ExecuteUpdate(connectionFactory, settings) {
            @Override
            protected int onRunUpdate(DSLContext dslContext) {
                return dslContext.insertInto(Tables.TRANSACTIONS)
                        .set(Tables.TRANSACTIONS.ID, transactionId)
                        .set(Tables.TRANSACTIONS.USER_ID, userId)
                        .set(Tables.TRANSACTIONS.TIMESTAMP, timestamp.toLocalDateTime())
                        .execute();
            }
        }.executeUpdate();
    }

    //todo should be cached and fetched only when a player asks for it?
    @Override
    public FishLog getFishLog(int userId, String fishName, String fishRarity, LocalDateTime time) {
        return new ExecuteQuery<FishLog>(connectionFactory, settings) {

            @Override
            protected FishLog onRunQuery(DSLContext dslContext) throws Exception {
                Record result = dslContext.select()
                        .from(Tables.FISH_LOG)
                        .where(Tables.FISH_LOG.USER_ID.eq(userId)
                                .and(Tables.FISH_LOG.FISH_NAME.eq(fishName))
                                .and(Tables.FISH_LOG.FISH_RARITY.eq(fishName))
                                .and(Tables.FISH_LOG.CATCH_TIME.eq(time))
                        ).fetchOne();

                if (result == null) {
                    return empty();
                }


                final float length = result.getValue(Tables.FISH_LOG.FISH_LENGTH);
                final String competitionId = result.getValue(Tables.FISH_LOG.COMPETITION_ID);
                return new FishLog(userId, fishName, fishRarity, time, length, competitionId);
            }

            @Override
            protected FishLog empty() {
                return null;
            }
        }.prepareAndRunQuery();
    }

    public void shutdown() {
        try {
            this.connectionFactory.shutdown();
        } catch (Exception e) {
            EvenMoreFish.getInstance().getLogger().log(Level.SEVERE, e.getMessage(), e);
        }
    }

    public String getDatabaseVersion() {
        if (!MainConfig.getInstance().databaseEnabled()) {
            return "Disabled";
        }

        if (!MainConfig.getInstance().isDatabaseOnline()) {
            return "Offline";
        }

        return "V" + this.version;
    }

    public String getType() {
        if (!MainConfig.getInstance().databaseEnabled()) {
            return "Disabled";
        }

        if (!MainConfig.getInstance().isDatabaseOnline()) {
            return "Offline";
        }
        return connectionFactory.getType();
    }

    @Override
    public UserFishStats getUserFishStats(int userId, String fishName, String fishRarity) {
        return new ExecuteQuery<UserFishStats>(connectionFactory, settings) {
            @Override
            protected UserFishStats onRunQuery(DSLContext dslContext) throws Exception {
                Optional<Record> optionalRecord = dslContext.select()
                        .from(Tables.USER_FISH_STATS)
                        .where(Tables.USER_FISH_STATS.USER_ID.eq(userId)
                                .and(Tables.USER_FISH_STATS.FISH_NAME.eq(fishName)))
                        .and(Tables.USER_FISH_STATS.FISH_RARITY.eq(fishRarity))
                        .fetchOptional();

                if (optionalRecord.isEmpty()) {
                    return empty();
                }

                final LocalDateTime firstCatchTime = optionalRecord.get().getValue(Tables.USER_FISH_STATS.FIRST_CATCH_TIME);
                final float shortestLength = optionalRecord.get().getValue(Tables.USER_FISH_STATS.SHORTEST_LENGTH);
                final float longestLength = optionalRecord.get().getValue(Tables.USER_FISH_STATS.LONGEST_LENGTH);
                final int quantity = optionalRecord.get().getValue(Tables.USER_FISH_STATS.QUANTITY);
                return new UserFishStats(userId, fishName, fishRarity, firstCatchTime, shortestLength, longestLength, quantity);
            }

            @Override
            protected UserFishStats empty() {
                //todo
                return null;
            }
        }.prepareAndRunQuery();
    }

    public void upsertUserFishStats(UserFishStats userFishStats) {
        new ExecuteUpdate(connectionFactory, settings) {
            @Override
            protected int onRunUpdate(DSLContext dslContext) {
                return dslContext.insertInto(Tables.USER_FISH_STATS)
                        .set(Tables.USER_FISH_STATS.USER_ID, userFishStats.getUserId())
                        .set(Tables.USER_FISH_STATS.FISH_NAME, userFishStats.getFishName())
                        .set(Tables.USER_FISH_STATS.FISH_RARITY, userFishStats.getFishRarity())
                        .set(Tables.USER_FISH_STATS.FIRST_CATCH_TIME, userFishStats.getFirstCatchTime())
                        .set(Tables.USER_FISH_STATS.SHORTEST_LENGTH, userFishStats.getShortestLength())
                        .set(Tables.USER_FISH_STATS.LONGEST_LENGTH, userFishStats.getLongestLength())
                        .set(Tables.USER_FISH_STATS.QUANTITY, userFishStats.getQuantity())
                        .onDuplicateKeyUpdate()
                        .set(Tables.USER_FISH_STATS.SHORTEST_LENGTH, userFishStats.getShortestLength())
                        .set(Tables.USER_FISH_STATS.LONGEST_LENGTH, userFishStats.getLongestLength())
                        .set(Tables.USER_FISH_STATS.QUANTITY, userFishStats.getQuantity())
                        .execute();
            }
        }.executeUpdate();
    }


    @Override
    public Set<FishLog> getFishLogEntries(int userId, String fishName, String fishRarity) {
        return new ExecuteQuery<Set<FishLog>>(connectionFactory, settings) {
            @Override
            protected Set<FishLog> onRunQuery(DSLContext dslContext) throws Exception {
                Result<Record> result = dslContext.select()
                        .from(Tables.FISH_LOG)
                        .where(Tables.FISH_LOG.USER_ID.eq(userId))
                        .and(Tables.FISH_LOG.FISH_NAME.eq(fishName))
                        .and(Tables.FISH_LOG.FISH_RARITY.eq(fishName))
                        .fetch();
                if (result.isEmpty()) {
                    return empty();
                }

                final Set<FishLog> fishLogs = new HashSet<>();
                for (Record recordResult : result) {
                    final LocalDateTime catchTime = recordResult.getValue(Tables.FISH_LOG.CATCH_TIME);
                    final float length = recordResult.getValue(Tables.FISH_LOG.FISH_LENGTH);
                    final String competitionId = recordResult.getValue(Tables.FISH_LOG.COMPETITION_ID);

                    fishLogs.add(new FishLog(userId, fishName, fishRarity, catchTime, length, competitionId));
                }
                return fishLogs;
            }

            @Override
            protected Set<FishLog> empty() {
                return Set.of();
            }
        }.prepareAndRunQuery();
    }

    @Override
    public void setFishLogEntry(FishLog fishLogEntry) {
        new ExecuteUpdate(connectionFactory, settings) {
            @Override
            protected int onRunUpdate(DSLContext dslContext) {
                return dslContext.insertInto(Tables.FISH_LOG)
                        .set(Tables.FISH_LOG.USER_ID, fishLogEntry.getUserId())
                        .set(Tables.FISH_LOG.FISH_NAME, fishLogEntry.getFishName())
                        .set(Tables.FISH_LOG.FISH_RARITY, fishLogEntry.getFishRarity())
                        .set(Tables.FISH_LOG.FISH_LENGTH, fishLogEntry.getLength())
                        .set(Tables.FISH_LOG.CATCH_TIME, fishLogEntry.getCatchTime())
                        .set(Tables.FISH_LOG.COMPETITION_ID, fishLogEntry.getCompetitionId())
                        .execute();
            }
        }.executeUpdate();
    }


    @Override
    public FishStats getFishStats(String fishName, String fishRarity) {
        return new ExecuteQuery<FishStats>(connectionFactory, settings) {
            @Override
            protected FishStats onRunQuery(DSLContext dslContext) throws Exception {
                Optional<Record> optionalRecord = dslContext.select()
                        .from(Tables.FISH)
                        .where(Tables.FISH.FISH_NAME.eq(fishName))
                        .and(Tables.FISH.FISH_RARITY.eq(fishName))
                        .fetchOptional();

                if (optionalRecord.isEmpty()) {
                    return empty();
                }

                final LocalDateTime firstCatchTime = optionalRecord.get().getValue(Tables.FISH.FIRST_CATCH_TIME);
                final UUID discoverer = UUID.fromString(optionalRecord.get().get(Tables.FISH.DISCOVERER));
                final float shortestLength = optionalRecord.get().getValue(Tables.FISH.SHORTEST_LENGTH);
                final UUID shortestFisher = UUID.fromString(optionalRecord.get().getValue(Tables.FISH.SHORTEST_FISHER));
                final float longestLength = optionalRecord.get().getValue(Tables.FISH.LARGEST_FISH);
                final UUID longestFisher = UUID.fromString(optionalRecord.get().getValue(Tables.FISH.LARGEST_FISHER));
                final int quantity = optionalRecord.get().getValue(Tables.FISH.TOTAL_CAUGHT);
                return new FishStats(fishName, fishRarity, firstCatchTime, discoverer, shortestLength, shortestFisher, longestLength, longestFisher, quantity);
            }

            @Override
            protected FishStats empty() {
                return null;
            }
        }.prepareAndRunQuery();
    }

    public void upsertFishStats(@NotNull FishStats fishStats) {
        new ExecuteUpdate(connectionFactory, settings) {
            @Override
            protected int onRunUpdate(DSLContext dslContext) {
                return dslContext.insertInto(Tables.FISH)
                        .set(Tables.FISH.FISH_NAME, fishStats.getFishName())
                        .set(Tables.FISH.FISH_RARITY, fishStats.getFishRarity())
                        .set(Tables.FISH.FIRST_FISHER, fishStats.getDiscoverer().toString())
                        .set(Tables.FISH.DISCOVERER, fishStats.getDiscoverer().toString())
                        .set(Tables.FISH.TOTAL_CAUGHT, fishStats.getQuantity())
                        .set(Tables.FISH.LARGEST_FISH, fishStats.getLongestLength())
                        .set(Tables.FISH.LARGEST_FISHER, fishStats.getLongestFisher().toString())
                        .set(Tables.FISH.SHORTEST_LENGTH, fishStats.getShortestLength())
                        .set(Tables.FISH.SHORTEST_FISHER, fishStats.getShortestFisher().toString())
                        .set(Tables.FISH.FIRST_CATCH_TIME, fishStats.getFirstCatchTime())
                        .onDuplicateKeyUpdate()
                        .set(Tables.FISH.TOTAL_CAUGHT, fishStats.getQuantity())
                        .set(
                                Tables.FISH.LARGEST_FISH,
                                DSL.when(
                                                Tables.FISH.LARGEST_FISH.lt(fishStats.getLongestLength()),
                                                fishStats.getLongestLength()
                                        )
                                        .otherwise(Tables.FISH.LARGEST_FISH)
                        )
                        .set(
                                Tables.FISH.LARGEST_FISHER,
                                DSL.when(
                                                Tables.FISH.LARGEST_FISH.lt(fishStats.getLongestLength()),
                                                fishStats.getLongestFisher().toString()
                                        )
                                        .otherwise(Tables.FISH.LARGEST_FISHER)
                        )
                        .set(
                                Tables.FISH.SHORTEST_LENGTH,
                                DSL.when(
                                                Tables.FISH.SHORTEST_LENGTH.gt(fishStats.getShortestLength())
                                                        .or(Tables.FISH.SHORTEST_LENGTH.isNull()),
                                                fishStats.getShortestLength()
                                        )
                                        .otherwise(Tables.FISH.SHORTEST_LENGTH)
                        )
                        .set(
                                Tables.FISH.SHORTEST_FISHER,
                                DSL.when(
                                                Tables.FISH.SHORTEST_LENGTH.gt(fishStats.getShortestLength())
                                                        .or(Tables.FISH.SHORTEST_LENGTH.isNull()),
                                                fishStats.getShortestFisher().toString()
                                        )
                                        .otherwise(Tables.FISH.SHORTEST_FISHER)
                        )
                        .execute();
            }
        }.executeInTransaction();
    }

    /**
     * Checks if a user has caught a specific fish with a given rarity.
     *
     * @param rarity The rarity of the fish.
     * @param fish   The name of the fish.
     * @param id     The unique identifier of the user.
     * @return {@code true} if the user has caught the fish, {@code false} otherwise.
     */
    @Override
    public boolean userHasFish(@NotNull String rarity, @NotNull String fish, int id) {
        return new ExecuteQuery<Boolean>(connectionFactory, settings) {
            @Override
            protected Boolean onRunQuery(DSLContext dslContext) throws Exception {
                return dslContext.fetchExists(
                        Tables.USER_FISH_STATS,
                        Tables.USER_FISH_STATS.USER_ID.eq(id)
                                .and(Tables.USER_FISH_STATS.FISH_RARITY.eq(rarity))
                                .and(Tables.USER_FISH_STATS.FISH_NAME.eq(fish))
                );
            }

            @Override
            protected Boolean empty() {
                return false;
            }
        }.prepareAndRunQuery();

    }

    /**
     * Checks if a player has caught a specific rarity by its name.
     *
     * @param rarity The rarity's name
     * @param id     The database ID of the player
     * @return true if the player has caught the rarity, false otherwise
     */
    @Override
    public boolean userHasRarity(@NotNull String rarity, int id) {
        return new ExecuteQuery<Boolean>(connectionFactory, settings) {
            @Override
            protected Boolean onRunQuery(DSLContext dslContext) throws Exception {
                return dslContext.fetchExists(
                        Tables.USER_FISH_STATS,
                        Tables.USER_FISH_STATS.USER_ID.eq(id)
                                .and(Tables.USER_FISH_STATS.FISH_RARITY.eq(rarity))
                );
            }

            @Override
            protected Boolean empty() {
                return false;
            }
        }.prepareAndRunQuery();
    }

    public void batchInsertFishLogs(Collection<FishLog> logs) {
        new ExecuteUpdate(connectionFactory, settings) {
            @Override
            protected int onRunUpdate(DSLContext dslContext) {
                List<FishLogRecord> records = logs.stream()
                        .filter(Objects::nonNull)
                        .map(log -> {
                            FishLogRecord logRecord = new FishLogRecord();
                            logRecord.setUserId(log.getUserId());
                            logRecord.setFishLength(log.getLength());
                            logRecord.setCompetitionId(log.getCompetitionId());
                            logRecord.setCatchTime(log.getCatchTime());
                            logRecord.setFishName(log.getFishName());
                            logRecord.setFishRarity(log.getFishRarity());
                            return logRecord;
                        })
                        .toList();

                if (records.isEmpty()) {
                    return 0;
                }

                dslContext.batchStore(records).execute();

                return records.size();
            }
        }.executeUpdate();
    }

    public Integer upsertUserReport(UserReport report) {
        return new ExecuteUpdate(connectionFactory, settings) {
            @Override
            protected int onRunUpdate(DSLContext dslContext) {
                return dslContext.insertInto(Tables.USERS)
                        .set(Tables.USERS.UUID, report.getUuid().toString())
                        .set(Tables.USERS.COMPETITIONS_JOINED, report.getCompetitionsJoined())
                        .set(Tables.USERS.COMPETITIONS_WON, report.getCompetitionsWon())
                        .set(Tables.USERS.TOTAL_FISH_LENGTH, report.getTotalFishLength())
                        .set(Tables.USERS.FIRST_FISH, report.getFirstFish().toString())
                        .set(Tables.USERS.MONEY_EARNED, report.getMoneyEarned())
                        .set(Tables.USERS.FISH_SOLD, report.getFishSold())
                        .set(Tables.USERS.NUM_FISH_CAUGHT, report.getNumFishCaught())
                        .set(Tables.USERS.LARGEST_FISH, report.getLargestFish().toString())
                        .set(Tables.USERS.LARGEST_LENGTH, report.getLargestLength())
                        .set(Tables.USERS.LAST_FISH, report.getLargestFish().toString())
                        .set(Tables.USERS.SHORTEST_FISH, report.getShortestFish().toString())
                        .set(Tables.USERS.SHORTEST_LENGTH, report.getShortestLength())
                        .onDuplicateKeyUpdate()
                        .set(Tables.USERS.COMPETITIONS_JOINED, report.getCompetitionsJoined())
                        .set(Tables.USERS.COMPETITIONS_WON, report.getCompetitionsWon())
                        .set(Tables.USERS.TOTAL_FISH_LENGTH, report.getTotalFishLength())
                        .set(Tables.USERS.FIRST_FISH, report.getFirstFish().toString())
                        .set(Tables.USERS.MONEY_EARNED, report.getMoneyEarned())
                        .set(Tables.USERS.FISH_SOLD, report.getFishSold())
                        .set(Tables.USERS.NUM_FISH_CAUGHT, report.getNumFishCaught())
                        .set(Tables.USERS.LARGEST_FISH, report.getLargestFish().toString())
                        .set(Tables.USERS.LARGEST_LENGTH, report.getLargestLength())
                        .set(Tables.USERS.LAST_FISH, report.getLargestFish().toString())
                        .set(Tables.USERS.SHORTEST_FISH, report.getShortestFish().toString())
                        .set(Tables.USERS.SHORTEST_LENGTH, report.getShortestLength())
                        .execute();
            }
        }.executeUpdate();
    }


    public void batchUpdateUserFishStats(Collection<UserFishStats> userFishStats) {
        new ExecuteUpdate(connectionFactory, settings) {
            @Override
            protected int onRunUpdate(DSLContext dslContext) {
                List<UserFishStatsRecord> records = userFishStats.stream()
                        .map(stats -> {
                            UserFishStatsRecord userFishStatsRecord = new UserFishStatsRecord();
                            userFishStatsRecord.setUserId(stats.getUserId());
                            userFishStatsRecord.setFishName(stats.getFishName());
                            userFishStatsRecord.setFishRarity(stats.getFishRarity());
                            userFishStatsRecord.setFirstCatchTime(stats.getFirstCatchTime());
                            userFishStatsRecord.setLongestLength(stats.getLongestLength());
                            userFishStatsRecord.setShortestLength(stats.getShortestLength());
                            userFishStatsRecord.setQuantity(stats.getQuantity());

                            return userFishStatsRecord;
                        })
                        .toList();

                //upsert
                dslContext.batchStore(records).execute();

                return records.size();
            }
        }.executeUpdate();

    }


    public void updateCompetition(CompetitionReport competition) {
        String winnerUuid = competition.getWinnerUuid() == null ? "None" : competition.getWinnerUuid().toString();

        new ExecuteUpdate(connectionFactory, settings) {
            @Override
            protected int onRunUpdate(DSLContext dslContext) {
                return dslContext.insertInto(Tables.COMPETITIONS)
                        .set(Tables.COMPETITIONS.COMPETITION_NAME, competition.getCompetitionConfigId())
                        .set(Tables.COMPETITIONS.WINNER_FISH, competition.getWinnerFish())
                        .set(Tables.COMPETITIONS.WINNER_UUID, winnerUuid)
                        .set(Tables.COMPETITIONS.WINNER_SCORE, competition.getWinnerScore())
                        .set(Tables.COMPETITIONS.CONTESTANTS, competition.getContestants().stream().map(UUID::toString).collect(Collectors.joining(", ")))
                        .set(Tables.COMPETITIONS.START_TIME, competition.getStartTime())
                        .set(Tables.COMPETITIONS.END_TIME, competition.getEndTime())
                        .execute();
            }
        }.executeUpdate();
    }

    public CompetitionReport getCompetitionReport(int id) {
        return new ExecuteQuery<CompetitionReport>(connectionFactory, settings) {
            @Override
            protected CompetitionReport onRunQuery(DSLContext dslContext) throws Exception {
                Record result = dslContext.select(Tables.COMPETITIONS)
                        .where(Tables.COMPETITIONS.ID.eq(id))
                        .fetchOne();
                if (result == null) {
                    return empty();
                }

                return new CompetitionReport(
                        result.getValue(Tables.COMPETITIONS.COMPETITION_NAME),
                        result.getValue(Tables.COMPETITIONS.WINNER_FISH),
                        result.getValue(Tables.COMPETITIONS.WINNER_UUID),
                        result.getValue(Tables.COMPETITIONS.WINNER_SCORE),
                        result.getValue(Tables.COMPETITIONS.CONTESTANTS),
                        result.getValue(Tables.COMPETITIONS.START_TIME),
                        result.getValue(Tables.COMPETITIONS.END_TIME)
                );
            }

            @Override
            protected CompetitionReport empty() {
                return null;
            }
        }.prepareAndRunQuery();
    }

    public void batchUpdateCompetitions(Collection<CompetitionReport> competitions) {
        new ExecuteUpdate(connectionFactory, settings) {
            @Override
            protected int onRunUpdate(DSLContext dslContext) {
                List<CompetitionsRecord> records = competitions.stream()
                        .map(competition -> {
                            CompetitionsRecord competitionsRecord = new CompetitionsRecord();
                            competitionsRecord.setCompetitionName(competition.getCompetitionConfigId());
                            competitionsRecord.setWinnerFish(competition.getWinnerFish());
                            competitionsRecord.setWinnerUuid(competition.getWinnerUuid().toString());
                            competitionsRecord.setWinnerScore(competition.getWinnerScore());
                            competitionsRecord.setContestants(competition.getContestants()
                                    .stream()
                                    .map(UUID::toString)
                                    .collect(Collectors.joining(", ")));
                            competitionsRecord.setStartTime(competition.getStartTime());
                            competitionsRecord.setEndTime(competition.getEndTime());

                            return competitionsRecord;
                        })
                        .toList();

                //insert, comp are never updated, just inserted
                dslContext.batchInsert(records).execute();

                return records.size();
            }
        }.executeUpdate();
    }
}
