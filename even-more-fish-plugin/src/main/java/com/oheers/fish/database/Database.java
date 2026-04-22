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
import com.oheers.fish.database.mapper.CompetitionReportMapper;
import com.oheers.fish.database.mapper.FishLogMapper;
import com.oheers.fish.database.mapper.FishStatsMapper;
import com.oheers.fish.database.mapper.UserFishStatsMapper;
import com.oheers.fish.database.mapper.UserReportMapper;
import com.oheers.fish.database.model.CompetitionReport;
import com.oheers.fish.database.model.fish.FishLog;
import com.oheers.fish.database.model.fish.FishStats;
import com.oheers.fish.database.model.user.UserFishStats;
import com.oheers.fish.database.model.user.UserReport;
import com.oheers.fish.database.sql.DatabaseSqlDialect;
import com.oheers.fish.database.sql.DatabaseSqlDialectFactory;
import com.oheers.fish.fishing.items.Fish;
import org.jetbrains.annotations.NotNull;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.PreparedBatch;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;

import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;

@NeedsTesting(reason = "Requires full testing, unit where possible, integration eventually.", testType = {TestType.MANUAL, TestType.UNIT, TestType.INTEGRATION})
public class Database implements DatabaseAPI {
    private String version;
    private final ConnectionFactory connectionFactory;
    private final MigrationManager migrationManager;
    private Jdbi jdbi;
    private String competitionsTable;
    private String fishTable;
    private String usersTable;
    private String fishLogTable;
    private String userFishStatsTable;
    private String transactionsTable;
    private String userSalesTable;
    private DatabaseSqlDialect sqlDialect;

    public Database() {
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


    public void migrateFromDatabaseVersionToLatest() {
        switch (version) {
            case "5" -> this.migrationManager.migrateFromV5ToLatest();
            case "6.0" -> this.migrationManager.migrateFromV6ToLatest();
            default -> this.migrationManager.migrateFromVersion(version, true);
        }
        this.version = this.migrationManager.getDatabaseVersion().getVersion();
    }

    public MigrationManager getMigrationManager() {
        return migrationManager;
    }

    private @NotNull ConnectionFactory getConnectionFactory(final @NotNull String type) {
        return switch (type) {
            case "mysql" -> new MySqlConnectionFactory();
            case "sqlite" -> new SqliteConnectionFactory();
            default -> new H2ConnectionFactory();
        };
    }

    public void initSettings(final String tablePrefix, final String dbName) {
        this.competitionsTable = tablePrefix + "competitions";
        this.fishTable = tablePrefix + "fish";
        this.usersTable = tablePrefix + "users";
        this.fishLogTable = tablePrefix + "fish_log";
        this.userFishStatsTable = tablePrefix + "user_fish_stats";
        this.transactionsTable = tablePrefix + "transactions";
        this.userSalesTable = tablePrefix + "users_sales";
        this.sqlDialect = DatabaseSqlDialectFactory.create(connectionFactory.getType());
        this.jdbi = createJdbi();
    }

    public Jdbi getJdbi() {
        return jdbi;
    }

    private Jdbi createJdbi() {
        Jdbi database = Jdbi.create(connectionFactory::getConnection);
        database.installPlugin(new SqlObjectPlugin());
        database.registerRowMapper(UserReport.class, new UserReportMapper());
        database.registerRowMapper(FishLog.class, new FishLogMapper());
        database.registerRowMapper(FishStats.class, new FishStatsMapper());
        database.registerRowMapper(UserFishStats.class, new UserFishStatsMapper());
        database.registerRowMapper(CompetitionReport.class, new CompetitionReportMapper());
        return database;
    }

    @Override
    public boolean hasUser(@NotNull UUID uuid) {
        return withHandle(handle -> handle.createQuery("select 1 from " + usersTable + " where uuid = :uuid limit 1").bind("uuid", uuid.toString()).mapTo(Integer.class).findOne().isPresent(), false);
    }

    public boolean hasFishLog(int userId) {
        if (userId == 0) {
            return false;
        }
        return withHandle(handle -> handle.createQuery("select 1 from " + fishLogTable + " where user_id = :user_id limit 1").bind("user_id", userId).mapTo(Integer.class).findOne().isPresent(), false);
    }

    @Override
    public int getUserId(@NotNull UUID uuid) {
        return withHandle(handle -> handle.createQuery("select id from " + usersTable + " where uuid = :uuid order by id asc limit 1").bind("uuid", uuid.toString()).mapTo(Integer.class).findOne().orElse(0), 0);
    }

    @Override
    public UserReport getUserReport(@NotNull UUID uuid) {
        UserReport report = withHandle(handle -> handle.createQuery("select id, uuid, first_fish, last_fish, largest_fish, shortest_fish, largest_length, shortest_length, num_fish_caught, total_fish_length, competitions_won, competitions_joined, fish_sold, money_earned from " + usersTable + " where uuid = :uuid order by id asc limit 1").bind("uuid", uuid.toString()).mapTo(UserReport.class).findOne().orElse(null), null);
        if (report == null) {
            DatabaseUtil.writeDbVerbose("User report for (%s) does not exist in the database.".formatted(uuid));
        } else {
            DatabaseUtil.writeDbVerbose("Read user report for user (%s)".formatted(uuid));
        }
        return report;
    }

    @Override
    public boolean hasFishStats(@NotNull Fish fish) {
        return withHandle(handle -> handle.createQuery("select 1 from " + fishTable + " where fish_name = :fish_name and fish_rarity = :fish_rarity limit 1").bind("fish_name", fish.getName()).bind("fish_rarity", fish.getRarity().getId()).mapTo(Integer.class).findOne().isPresent(), false);
    }

    @Override
    public void incrementFish(@NotNull Fish fish) {
        useHandle(handle -> handle.createUpdate("update " + fishTable + " set total_caught = total_caught + 1 where fish_rarity = :fish_rarity and fish_name = :fish_name").bind("fish_rarity", fish.getRarity().getId()).bind("fish_name", fish.getName()).execute());
    }

    @Override
    public void createCompetitionReport(@NotNull Competition competition) {
        Leaderboard leaderboard = competition.getLeaderboard();
        String winnerUuid;
        String winnerFish;
        float winnerScore;
        String contestants;
        if (leaderboard.getSize() > 0) {
            CompetitionEntry topEntry = leaderboard.getTopEntry();
            winnerUuid = topEntry.getPlayer().toString();
            winnerFish = prepareRarityFishString(topEntry.getFish());
            winnerScore = topEntry.getValue();
            contestants = prepareContestantsString(leaderboard.getEntries());
        } else {
            winnerUuid = "None";
            winnerFish = "None";
            winnerScore = 0f;
            contestants = "None";
        }
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = competition.getStartTime();
        useHandle(handle -> handle.createUpdate("insert into " + competitionsTable + " (competition_name, winner_uuid, winner_fish, winner_score, contestants, start_time, end_time) values (:competition_name, :winner_uuid, :winner_fish, :winner_score, :contestants, :start_time, :end_time)").bind("competition_name", competition.getCompetitionName()).bind("winner_uuid", winnerUuid).bind("winner_fish", winnerFish).bind("winner_score", winnerScore).bind("contestants", contestants).bind("start_time", startTime == null ? endTime : startTime).bind("end_time", endTime).execute());
    }

    private String prepareContestantsString(@NotNull List<CompetitionEntry> entries) {
        if (entries.isEmpty()) {
            return "None";
        }
        return entries.stream().map(CompetitionEntry::getPlayer).map(UUID::toString).collect(Collectors.joining(","));
    }

    private @NotNull String prepareRarityFishString(final @NotNull Fish fish) {
        return fish.getRarity().getId() + ":" + fish.getName();
    }

    @Override
    public void createSale(@NotNull String transactionId, @NotNull String fishName, @NotNull String fishRarity, int fishAmount, double fishLength, double priceSold) {
        useHandle(handle -> handle.createUpdate("insert into " + userSalesTable + " (transaction_id, fish_name, fish_rarity, fish_amount, fish_length, price_sold) values (:transaction_id, :fish_name, :fish_rarity, :fish_amount, :fish_length, :price_sold)").bind("transaction_id", transactionId).bind("fish_name", fishName).bind("fish_rarity", fishRarity).bind("fish_amount", fishAmount).bind("fish_length", fishLength).bind("price_sold", priceSold).execute());
    }

    @Override
    public void createTransaction(@NotNull String transactionId, int userId, @NotNull Timestamp timestamp) {
        useHandle(handle -> handle.createUpdate("insert into " + transactionsTable + " (id, user_id, timestamp) values (:id, :user_id, :timestamp)").bind("id", transactionId).bind("user_id", userId).bind("timestamp", timestamp.toLocalDateTime()).execute());
    }

    @Override
    public FishLog getFishLog(int userId, String fishName, String fishRarity, LocalDateTime time) {
        return withHandle(handle -> handle.createQuery("select user_id, fish_name, fish_rarity, fish_length, catch_time, competition_id from " + fishLogTable + " where user_id = :user_id and fish_name = :fish_name and fish_rarity = :fish_rarity and catch_time = :catch_time").bind("user_id", userId).bind("fish_name", fishName).bind("fish_rarity", fishRarity).bind("catch_time", time).mapTo(FishLog.class).findOne().orElse(null), null);
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
        if (!DatabaseUtil.isDatabaseOnline()) {
            return "Offline";
        }
        return "V" + this.version;
    }

    public String getType() {
        if (!MainConfig.getInstance().databaseEnabled()) {
            return "Disabled";
        }
        if (!DatabaseUtil.isDatabaseOnline()) {
            return "Offline";
        }
        return connectionFactory.getType();
    }

    @Override
    public UserFishStats getUserFishStats(int userId, String fishName, String fishRarity) {
        return withHandle(handle -> handle.createQuery("select user_id, fish_name, fish_rarity, first_catch_time, shortest_length, longest_length, quantity from " + userFishStatsTable + " where user_id = :user_id and fish_name = :fish_name and fish_rarity = :fish_rarity").bind("user_id", userId).bind("fish_name", fishName).bind("fish_rarity", fishRarity).mapTo(UserFishStats.class).findOne().orElse(null), null);
    }

    public void upsertUserFishStats(UserFishStats userFishStats) {
        useHandle(handle -> bindUserFishStatsUpdate(handle, userFishStatsUpsertSql(), userFishStats).execute());
    }

    @Override
    public Set<FishLog> getFishLogEntries(int userId, String fishName, String fishRarity) {
        return withHandle(handle -> new LinkedHashSet<>(handle.createQuery("select user_id, fish_name, fish_rarity, fish_length, catch_time, competition_id from " + fishLogTable + " where user_id = :user_id and fish_name = :fish_name and fish_rarity = :fish_rarity").bind("user_id", userId).bind("fish_name", fishName).bind("fish_rarity", fishRarity).mapTo(FishLog.class).list()), Set.of());
    }

    @Override
    public void setFishLogEntry(FishLog fishLogEntry) {
        useHandle(handle -> bindFishLogInsert(handle, fishLogEntry).execute());
    }

    @Override
    public FishStats getFishStats(String fishName, String fishRarity) {
        return withHandle(handle -> handle.createQuery("select fish_name, fish_rarity, first_catch_time, discoverer, shortest_length, shortest_fisher, largest_fish, largest_fisher, total_caught from " + fishTable + " where fish_name = :fish_name and fish_rarity = :fish_rarity").bind("fish_name", fishName).bind("fish_rarity", fishRarity).mapTo(FishStats.class).findOne().orElse(null), null);
    }

    public void upsertFishStats(@NotNull FishStats fishStats) {
        useTransaction(handle -> bindFishStatsUpdate(handle, fishStats).execute());
    }

    @Override
    public boolean userHasFish(@NotNull String rarity, @NotNull String fish, int id) {
        return withHandle(handle -> handle.createQuery("select 1 from " + userFishStatsTable + " where user_id = :user_id and fish_rarity = :fish_rarity and fish_name = :fish_name limit 1").bind("user_id", id).bind("fish_rarity", rarity).bind("fish_name", fish).mapTo(Integer.class).findOne().isPresent(), false);
    }

    @Override
    public boolean userHasRarity(@NotNull String rarity, int id) {
        return withHandle(handle -> handle.createQuery("select 1 from " + userFishStatsTable + " where user_id = :user_id and fish_rarity = :fish_rarity limit 1").bind("user_id", id).bind("fish_rarity", rarity).mapTo(Integer.class).findOne().isPresent(), false);
    }

    public void batchInsertFishLogs(Collection<FishLog> logs) {
        useHandle(handle -> {
            PreparedBatch batch = handle.prepareBatch("insert into " + fishLogTable + " (user_id, fish_name, fish_rarity, fish_length, catch_time, competition_id) values (:user_id, :fish_name, :fish_rarity, :fish_length, :catch_time, :competition_id)");
            int count = 0;
            for (FishLog log : logs) {
                if (log == null) {
                    continue;
                }
                bindFishLogBatch(batch, log);
                batch.add();
                count++;
            }
            if (count > 0) {
                batch.execute();
            }
        });
    }

    public Integer upsertUserReport(UserReport report) {
        return withHandle(handle -> {
            String userUuid = report.getUuid().toString();
            Integer id = handle.createQuery("select id from " + usersTable + " where uuid = :uuid order by id asc limit 1").bind("uuid", userUuid).mapTo(Integer.class).findOne().orElse(null);
            if (id != null) {
                bindUserReportUpdate(handle, report, id).execute();
                return id;
            }
            bindUserReportInsert(handle, report).execute();
            return handle.createQuery("select id from " + usersTable + " where uuid = :uuid order by id asc limit 1").bind("uuid", userUuid).mapTo(Integer.class).findOne().orElse(0);
        }, 0);
    }

    public void batchUpdateUserFishStats(Collection<UserFishStats> userFishStats) {
        useHandle(handle -> {
            PreparedBatch batch = handle.prepareBatch(userFishStatsUpsertSql());
            int count = 0;
            for (UserFishStats stats : userFishStats) {
                if (stats == null) {
                    continue;
                }
                bindUserFishStatsBatch(batch, stats);
                batch.add();
                count++;
            }
            if (count > 0) {
                batch.execute();
            }
        });
    }

    public void updateCompetition(CompetitionReport competition) {
        useHandle(handle -> bindCompetitionInsert(handle, competition).execute());
    }

    public CompetitionReport getCompetitionReport(int id) {
        return withHandle(handle -> handle.createQuery("select competition_name, winner_fish, winner_uuid, winner_score, contestants, start_time, end_time from " + competitionsTable + " where id = :id").bind("id", id).mapTo(CompetitionReport.class).findOne().orElse(null), null);
    }

    public void batchUpdateCompetitions(Collection<CompetitionReport> competitions) {
        useHandle(handle -> {
            PreparedBatch batch = handle.prepareBatch("insert into " + competitionsTable + " (competition_name, winner_fish, winner_uuid, winner_score, contestants, start_time, end_time) values (:competition_name, :winner_fish, :winner_uuid, :winner_score, :contestants, :start_time, :end_time)");
            int count = 0;
            for (CompetitionReport competition : competitions) {
                if (competition == null) {
                    continue;
                }
                bindCompetitionBatch(batch, competition);
                batch.add();
                count++;
            }
            if (count > 0) {
                batch.execute();
            }
        });
    }

    private String userFishStatsUpsertSql() {
        return sqlDialect.userFishStatsUpsert(userFishStatsTable);
    }

    private String fishStatsUpsertSql() {
        return sqlDialect.fishStatsUpsert(fishTable);
    }

    private <T> T withHandle(HandleCallback<T> callback, T fallback) {
        try {
            if (jdbi == null) {
                return fallback;
            }
            return jdbi.withHandle(callback::apply);
        } catch (Exception exception) {
            EvenMoreFish.getInstance().getLogger().log(Level.SEVERE, "Query execution failed", exception);
            return fallback;
        }
    }

    private void useHandle(HandleConsumer consumer) {
        try {
            if (jdbi == null) {
                return;
            }
            jdbi.useHandle(consumer::accept);
        } catch (Exception exception) {
            EvenMoreFish.getInstance().getLogger().log(Level.SEVERE, "Update execution failed", exception);
        }
    }

    private void useTransaction(HandleConsumer consumer) {
        try {
            if (jdbi == null) {
                return;
            }
            jdbi.useTransaction(consumer::accept);
        } catch (Exception exception) {
            EvenMoreFish.getInstance().getLogger().log(Level.SEVERE, "Transactional update execution failed", exception);
        }
    }

    private org.jdbi.v3.core.statement.Update bindUserFishStatsUpdate(Handle handle, String sql, UserFishStats stats) {
        return handle.createUpdate(sql).bind("user_id", stats.getUserId()).bind("fish_name", stats.getFishName()).bind("fish_rarity", stats.getFishRarity()).bind("first_catch_time", stats.getFirstCatchTime()).bind("shortest_length", stats.getShortestLength()).bind("longest_length", stats.getLongestLength()).bind("quantity", stats.getQuantity());
    }

    private void bindUserFishStatsBatch(PreparedBatch batch, UserFishStats stats) {
        batch.bind("user_id", stats.getUserId()).bind("fish_name", stats.getFishName()).bind("fish_rarity", stats.getFishRarity()).bind("first_catch_time", stats.getFirstCatchTime()).bind("shortest_length", stats.getShortestLength()).bind("longest_length", stats.getLongestLength()).bind("quantity", stats.getQuantity());
    }

    private org.jdbi.v3.core.statement.Update bindFishLogInsert(Handle handle, FishLog fishLogEntry) {
        org.jdbi.v3.core.statement.Update update = handle.createUpdate("insert into " + fishLogTable + " (user_id, fish_name, fish_rarity, fish_length, catch_time, competition_id) values (:user_id, :fish_name, :fish_rarity, :fish_length, :catch_time, :competition_id)").bind("user_id", fishLogEntry.getUserId()).bind("fish_name", fishLogEntry.getFishName()).bind("fish_rarity", fishLogEntry.getFishRarity()).bind("fish_length", fishLogEntry.getLength()).bind("catch_time", fishLogEntry.getCatchTime());
        if (fishLogEntry.getCompetitionId() == null) {
            update.bindNull("competition_id", Types.VARCHAR);
        } else {
            update.bind("competition_id", fishLogEntry.getCompetitionId());
        }
        return update;
    }

    private void bindFishLogBatch(PreparedBatch batch, FishLog fishLogEntry) {
        batch.bind("user_id", fishLogEntry.getUserId()).bind("fish_name", fishLogEntry.getFishName()).bind("fish_rarity", fishLogEntry.getFishRarity()).bind("fish_length", fishLogEntry.getLength()).bind("catch_time", fishLogEntry.getCatchTime());
        if (fishLogEntry.getCompetitionId() == null) {
            batch.bindNull("competition_id", Types.VARCHAR);
        } else {
            batch.bind("competition_id", fishLogEntry.getCompetitionId());
        }
    }

    private org.jdbi.v3.core.statement.Update bindFishStatsUpdate(Handle handle, FishStats fishStats) {
        return handle.createUpdate(fishStatsUpsertSql()).bind("fish_name", fishStats.getFishName()).bind("fish_rarity", fishStats.getFishRarity()).bind("first_fisher", fishStats.getDiscoverer().toString()).bind("discoverer", fishStats.getDiscoverer().toString()).bind("total_caught", fishStats.getQuantity()).bind("largest_fish", fishStats.getLongestLength()).bind("largest_fisher", fishStats.getLongestFisher().toString()).bind("shortest_length", fishStats.getShortestLength()).bind("shortest_fisher", fishStats.getShortestFisher().toString()).bind("first_catch_time", fishStats.getFirstCatchTime());
    }

    private org.jdbi.v3.core.statement.Update bindUserReportInsert(Handle handle, UserReport report) {
        return handle.createUpdate("insert into " + usersTable + " (uuid, competitions_joined, competitions_won, total_fish_length, first_fish, money_earned, fish_sold, num_fish_caught, largest_fish, largest_length, last_fish, shortest_fish, shortest_length) values (:uuid, :competitions_joined, :competitions_won, :total_fish_length, :first_fish, :money_earned, :fish_sold, :num_fish_caught, :largest_fish, :largest_length, :last_fish, :shortest_fish, :shortest_length)").bind("uuid", report.getUuid().toString()).bind("competitions_joined", report.getCompetitionsJoined()).bind("competitions_won", report.getCompetitionsWon()).bind("total_fish_length", report.getTotalFishLength()).bind("first_fish", report.getFirstFish().toString()).bind("money_earned", report.getMoneyEarned()).bind("fish_sold", report.getFishSold()).bind("num_fish_caught", report.getNumFishCaught()).bind("largest_fish", report.getLargestFish().toString()).bind("largest_length", report.getLargestLength()).bind("last_fish", report.getRecentFish().toString()).bind("shortest_fish", report.getShortestFish().toString()).bind("shortest_length", report.getShortestLength());
    }

    private org.jdbi.v3.core.statement.Update bindUserReportUpdate(Handle handle, UserReport report, int id) {
        return handle.createUpdate("update " + usersTable + " set competitions_joined = :competitions_joined, competitions_won = :competitions_won, total_fish_length = :total_fish_length, first_fish = :first_fish, money_earned = :money_earned, fish_sold = :fish_sold, num_fish_caught = :num_fish_caught, largest_fish = :largest_fish, largest_length = :largest_length, last_fish = :last_fish, shortest_fish = :shortest_fish, shortest_length = :shortest_length where id = :id").bind("id", id).bind("competitions_joined", report.getCompetitionsJoined()).bind("competitions_won", report.getCompetitionsWon()).bind("total_fish_length", report.getTotalFishLength()).bind("first_fish", report.getFirstFish().toString()).bind("money_earned", report.getMoneyEarned()).bind("fish_sold", report.getFishSold()).bind("num_fish_caught", report.getNumFishCaught()).bind("largest_fish", report.getLargestFish().toString()).bind("largest_length", report.getLargestLength()).bind("last_fish", report.getRecentFish().toString()).bind("shortest_fish", report.getShortestFish().toString()).bind("shortest_length", report.getShortestLength());
    }

    private org.jdbi.v3.core.statement.Update bindCompetitionInsert(Handle handle, CompetitionReport competition) {
        return handle.createUpdate("insert into " + competitionsTable + " (competition_name, winner_fish, winner_uuid, winner_score, contestants, start_time, end_time) values (:competition_name, :winner_fish, :winner_uuid, :winner_score, :contestants, :start_time, :end_time)").bind("competition_name", competition.getCompetitionConfigId()).bind("winner_fish", competition.getWinnerFish()).bind("winner_uuid", competition.getWinnerUuid() == null ? "None" : competition.getWinnerUuid().toString()).bind("winner_score", competition.getWinnerScore()).bind("contestants", competition.getContestants().isEmpty() ? "None" : competition.getContestants().stream().map(UUID::toString).collect(Collectors.joining(","))).bind("start_time", competition.getStartTime()).bind("end_time", competition.getEndTime());
    }

    private void bindCompetitionBatch(PreparedBatch batch, CompetitionReport competition) {
        batch.bind("competition_name", competition.getCompetitionConfigId()).bind("winner_fish", competition.getWinnerFish()).bind("winner_uuid", competition.getWinnerUuid() == null ? "None" : competition.getWinnerUuid().toString()).bind("winner_score", competition.getWinnerScore()).bind("contestants", competition.getContestants().isEmpty() ? "None" : competition.getContestants().stream().map(UUID::toString).collect(Collectors.joining(","))).bind("start_time", competition.getStartTime()).bind("end_time", competition.getEndTime());
    }

    @FunctionalInterface
    private interface HandleCallback<T> {
        T apply(Handle handle) throws Exception;
    }

    @FunctionalInterface
    private interface HandleConsumer {
        void accept(Handle handle) throws Exception;
    }
}
