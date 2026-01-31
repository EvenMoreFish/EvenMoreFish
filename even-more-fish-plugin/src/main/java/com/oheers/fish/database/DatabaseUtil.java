package com.oheers.fish.database;


import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.api.plugin.EMFPlugin;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.database.connection.ConnectionFactory;
import org.eclipse.sisu.launch.Main;
import org.jetbrains.annotations.NotNull;
import org.jooq.SQLDialect;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseUtil {

    private DatabaseUtil() {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the corresponding SQLDialect for the given string.
     *
     * @param dialectString the string representing the SQL dialect
     * @return the corresponding SQLDialect, or SQLDialect.DEFAULT if no match is found
     */
    public static SQLDialect getSQLDialect(String dialectString) {
        if (dialectString == null || dialectString.isBlank()) {
            return SQLDialect.DEFAULT;
        }
        SQLDialect dialect = FishUtils.getEnumValue(SQLDialect.class, dialectString);
        if (dialect == null) {
            return SQLDialect.DEFAULT;
        }
        return dialect;
    }

    public static void writeDbVerbose(final String message) {
        if (MainConfig.getInstance().doDBVerbose()) {
            EvenMoreFish.getInstance().getLogger().info(() -> message);
        }
    }

    public static boolean isDatabaseOffline() {
        final EvenMoreFish plugin = EvenMoreFish.getInstance();

        if (!MainConfig.getInstance().databaseEnabled()) {
            plugin.debug("Database is disabled in config.");
            return true;
        }

        final Database database = plugin.getPluginDataManager().getDatabase();
        if (database == null) {
            plugin.debug("Database instance is null.");
            return true;
        }

        boolean usingV2 = database.getMigrationManager().usingV2();
        if (usingV2) {
            plugin.debug("Database migration manager reports version 2.");
            return true;
        }

        plugin.debug("Database is online and usable.");
        return false;
    }

    public static boolean isDatabaseOnline() {
        return !isDatabaseOffline();
    }

    public static void debugDatabaseTypeVersion(@NotNull ConnectionFactory connectionFactory)  {
        try (Connection connection = connectionFactory.getConnection()) {
            final String version = connection.getMetaData().getDatabaseProductVersion();
            final String type = connection.getMetaData().getDatabaseProductName();
            EvenMoreFish.getInstance().debug("%s version %s".formatted(type, version));
        } catch (SQLException e) {
            //
        }
    }

}
