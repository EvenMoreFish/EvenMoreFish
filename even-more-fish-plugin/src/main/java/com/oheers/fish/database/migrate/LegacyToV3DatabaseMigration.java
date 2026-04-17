package com.oheers.fish.database.migrate;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.database.Database;
import com.oheers.fish.database.connection.ConnectionFactory;
import com.oheers.fish.database.connection.MigrationManager;
import com.oheers.fish.messages.EMFSingleMessage;
import com.oheers.fish.messages.PrefixType;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jdbi.v3.core.Jdbi;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.sql.Types;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;

public class LegacyToV3DatabaseMigration {
    private final Database database;
    private final MigrationManager migrationManager;
    private final Jdbi jdbi;

    public LegacyToV3DatabaseMigration(final Database database, final MigrationManager migrationManager, final ConnectionFactory connectionFactory) {
        this.database = database;
        this.migrationManager = migrationManager;
        this.jdbi = Jdbi.create((org.jdbi.v3.core.ConnectionFactory) connectionFactory::getConnection);
    }

    private void translateFishDataV2() {
        if (migrationManager.queryTableExistence(prefixed("fish"))) {
            return;
        }

        if (migrationManager.queryTableExistence("Fish2")) {
            execute("alter table Fish2 rename to " + prefixed("fish"));
            return;
        }
        migrationManager.legacyInitVersion();
    }

    private void translateFishReportsV2(final UUID uuid, final @NotNull List<LegacyFishReport> reports) {
        String firstFishID = "";
        long epochFirst = Long.MAX_VALUE;
        String largestFishID = "";
        float largestSize = 0f;
        int totalFish = 0;
        int userId = database.getUserId(uuid);

        for (LegacyFishReport report : reports) {
            if (report.getTimeEpoch() < epochFirst) {
                epochFirst = report.getTimeEpoch();
                firstFishID = report.getRarity() + ":" + report.getName();
            }
            if (report.getLargestLength() > largestSize) {
                largestSize = report.getLargestLength();
                largestFishID = report.getRarity() + ":" + report.getName();
            }

            totalFish += report.getNumCaught();

            jdbi.useHandle(handle -> handle.createUpdate(
                            "insert into " + prefixed("fish_log") +
                                    " (id, rarity, fish, quantity, first_catch_time, largest_length) " +
                                    "values (:id, :rarity, :fish, :quantity, :first_catch_time, :largest_length)"
                    )
                    .bind("id", userId)
                    .bind("rarity", report.getRarity())
                    .bind("fish", report.getName())
                    .bind("quantity", report.getNumCaught())
                    .bind("first_catch_time", report.getTimeEpoch())
                    .bind("largest_length", report.getLargestLength())
                    .execute());
        }

        createFieldForFishFirstTimeFished(uuid, firstFishID, largestFishID, totalFish, largestSize);
    }

    private void createFieldForFishFirstTimeFished(final UUID uuid, final String firstFishID, final String largestFishID, int totalFish, float largestSize) {
        jdbi.useHandle(handle -> handle.createUpdate(
                        "update " + prefixed("users") +
                                " set first_fish = :first_fish, largest_fish = :largest_fish, num_fish_caught = :num_fish_caught, largest_length = :largest_length " +
                                "where uuid = :uuid"
                )
                .bind("first_fish", firstFishID)
                .bind("largest_fish", largestFishID)
                .bind("num_fish_caught", totalFish)
                .bind("largest_length", largestSize)
                .bind("uuid", uuid.toString())
                .execute());
    }

    public void migrate(CommandSender initiator) {
        if (!migrationManager.usingV2()) {
            EMFSingleMessage msg = EMFSingleMessage.fromString("EvenMoreFish is already using the latest V3 database engine.");
            msg.prependMessage(PrefixType.ERROR.getPrefix());
            msg.send(initiator);
            return;
        }

        EvenMoreFish.getInstance().getLogger().info(() -> initiator.getName() + " has begun the migration to EMF database V3 from V2.");
        EMFSingleMessage msg = EMFSingleMessage.fromString("Beginning conversion to V3 database engine.");
        msg.prependMessage(PrefixType.ADMIN.getPrefix());
        msg.send(initiator);

        File oldDataFolder = new File(EvenMoreFish.getInstance().getDataFolder(), "data");
        File dataFolder = new File(EvenMoreFish.getInstance().getDataFolder(), "data-archived");

        if (oldDataFolder.renameTo(dataFolder)) {
            EMFSingleMessage message = EMFSingleMessage.fromString("Archived /data/ folder.");
            message.prependMessage(PrefixType.ADMIN.getPrefix());
            message.send(initiator);
        } else {
            EMFSingleMessage message = EMFSingleMessage.fromString("Failed to archive /data/ folder. Cancelling migration. [No further information]");
            message.prependMessage(PrefixType.ADMIN.getPrefix());
            message.send(initiator);
            return;
        }

        EMFSingleMessage fishReportMSG = EMFSingleMessage.fromString("Beginning FishReport migrations. This may take a while.");
        fishReportMSG.prependMessage(PrefixType.ADMIN.getPrefix());
        fishReportMSG.send(initiator);

        try {
            translateFishDataV2();
            this.migrationManager.legacyFlywayBaseline();

            for (File file : Objects.requireNonNull(dataFolder.listFiles())) {
                Type fishReportList = new TypeToken<List<LegacyFishReport>>() {
                }.getType();

                Gson gson = new Gson();
                List<LegacyFishReport> reports;
                try (FileReader reader = new FileReader(file)) {
                    reports = gson.fromJson(reader, fishReportList);
                }

                UUID playerUUID = UUID.fromString(file.getName().substring(0, file.getName().lastIndexOf(".")));
                createEmptyUserReport(playerUUID);
                translateFishReportsV2(playerUUID, reports);

                EMFSingleMessage migratedMSG = EMFSingleMessage.fromString("Migrated " + reports.size() + " fish for: " + playerUUID);
                migratedMSG.prependMessage(PrefixType.ADMIN.getPrefix());
                migratedMSG.send(initiator);
            }

        } catch (NullPointerException | FileNotFoundException exception) {
            EMFSingleMessage message = EMFSingleMessage.fromString("Fatal error whilst upgrading to V3 database engine.");
            message.prependMessage(PrefixType.ERROR.getPrefix());
            message.send(initiator);

            EvenMoreFish.getInstance().getLogger().log(Level.SEVERE, "Critical SQL/interruption error whilst upgrading to v3 engine.", exception);
        } catch (IOException e) {
            EvenMoreFish.getInstance().getLogger().log(Level.SEVERE, e.getMessage(), e);
            throw new RuntimeException(e);
        }

        EMFSingleMessage migratedMSG = EMFSingleMessage.fromString("Migration completed. Your database is now using the V3 database engine: to complete the migration, it is recommended to restart your server.");
        migratedMSG.prependMessage(PrefixType.ADMIN.getPrefix());
        migratedMSG.send(initiator);

        EMFSingleMessage thankyou = EMFSingleMessage.fromString("Now that migration is complete, you will be able to use functionality in upcoming updates such as quests, deliveries and a fish log. - Oheers");
        thankyou.prependMessage(PrefixType.ERROR.getPrefix());
        thankyou.send(initiator);

        migrationManager.migrateFromV5ToLatest();
    }

    public void createEmptyUserReport(@NotNull UUID uuid) {
        jdbi.useHandle(handle -> {
            var update = handle.createUpdate(
                    "insert into " + prefixed("users") +
                            " (uuid, first_fish, last_fish, largest_fish, largest_length, num_fish_caught, total_fish_length, competitions_won, competitions_joined) " +
                            "values (:uuid, :first_fish, :last_fish, :largest_fish, :largest_length, :num_fish_caught, :total_fish_length, :competitions_won, :competitions_joined)"
            );
            update.bind("uuid", uuid.toString())
                    .bind("first_fish", "None")
                    .bind("last_fish", "None")
                    .bind("largest_fish", "None")
                    .bind("largest_length", 0F)
                    .bind("num_fish_caught", 0)
                    .bind("total_fish_length", 0F)
                    .bind("competitions_won", 0)
                    .bind("competitions_joined", 0);
            update.execute();
        });
    }

    private void execute(@NotNull String sql) {
        jdbi.useHandle(handle -> handle.execute(sql));
    }

    private @NotNull String prefixed(@NotNull String tableName) {
        return MainConfig.getInstance().getPrefix() + tableName;
    }
}
