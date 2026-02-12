package com.oheers.fish.database.connection;


import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.config.MainConfig;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jetbrains.annotations.NotNull;
import org.jooq.SQLDialect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/*
 * We can add additional factories to allow for multiple database support in the future.
 */
public abstract class ConnectionFactory {
    private final static int DEFAULT_PORT = 3306;
    protected HikariDataSource dataSource;
    private final Logger logger = LoggerFactory.getLogger(ConnectionFactory.class);
    private Boolean supportsTransaction;
    private SQLDialect sqlDialect;


    /**
     * This may be different with every database type.
     *
     * @param config       hikari config
     * @param address      address
     * @param port         port
     * @param databaseName databaseName
     * @param username     username
     * @param password     password
     */
    protected abstract void configureDatabase(HikariConfig config, String address, int port, String databaseName, String username, String password);

    private String getDatabaseAddress() {
        return MainConfig.getInstance().getAddress().split(":")[0];
    }

    private int getDatabasePort() {
        if (!MainConfig.getInstance().getAddress().contains(":")) {
            return DEFAULT_PORT;
        }
        try {
            return Integer.parseInt(MainConfig.getInstance().getAddress().split(":")[1]);
        } catch (NumberFormatException e) {
            return DEFAULT_PORT;
        }
    }

    public void init() {
        HikariConfig config = new HikariConfig();
        config.setPoolName("evenmorefish-hikari");

        configureDatabase(config, getDatabaseAddress(), getDatabasePort(), MainConfig.getInstance().getDatabase(), MainConfig.getInstance().getUsername(), MainConfig.getInstance().getPassword());
        config.setInitializationFailTimeout(-1);
        config.setValidationTimeout(5000);
        config.addDataSourceProperty("validateBorrowedConnections", true);
        config.setLeakDetectionThreshold(30000);


        Map<String, String> properties = new HashMap<>();

        overrideProperties(properties);
        setProperties(config, properties);

        initDriver();
        this.dataSource = new HikariDataSource(config);
        logger.info("Connected to database!");
    }

    //LP
    protected void overrideProperties(@NotNull Map<String, String> properties) {
        properties.putIfAbsent("socketTimeout", String.valueOf(TimeUnit.SECONDS.toMillis(30)));
    }

    //LP
    protected void setProperties(HikariConfig config, @NotNull Map<String, String> properties) {
        for (Map.Entry<String, String> property : properties.entrySet()) {
            config.addDataSourceProperty(property.getKey(), property.getValue());
        }
    }

    public void shutdown() {
        if (this.dataSource != null) {
            this.dataSource.close();
        }
    }

    public abstract String getType();

    public Connection getConnection() throws SQLException {
        if (this.dataSource == null) {
            throw new SQLException("Null data source");
        }

        Connection connection = this.dataSource.getConnection();
        if (connection == null) {
            throw new SQLException("Null connection");
        }

        return connection;
    }

    public String getDriverClass() {
        return "";
    }

    /**
     * Sometimes it may be necessary to init a driver ahead of time, use this in that case
     */
    protected void initDriver() {
        if (getDriverClass().isEmpty())
            return;

        try {
            Class.forName(getDriverClass());
        } catch (ClassNotFoundException e) {
            EvenMoreFish.getInstance().getLogger().severe("Tried to init driver: %s, but could not find it.".formatted(getDriverClass()));
        }
    }

    public boolean supportsTransactions() {
        if (supportsTransaction != null) {
            return supportsTransaction;
        }

        try (Connection connection = getConnection()) {
            this.supportsTransaction = connection.getMetaData().supportsTransactions();
        } catch (SQLException e) {
            EvenMoreFish.getInstance().getLogger().log(Level.WARNING,
                    "Failed to check transaction support, assuming false", e);
            this.supportsTransaction = false;
        }

        return this.supportsTransaction;
    }

    public @NotNull SQLDialect getSQLDialect(@NotNull Connection connection) {
        if (this.sqlDialect != null) {
            return this.sqlDialect;
        }

        SQLDialect detectedDialect = getDefaultDialect();
        try {
            DatabaseMetaData metaData = connection.getMetaData();
            String productName = toLowerCase(metaData.getDatabaseProductName());
            String productVersion = toLowerCase(metaData.getDatabaseProductVersion());
            String url = toLowerCase(metaData.getURL());

            if (productName.contains("mariadb")
                    || productVersion.contains("mariadb")
                    || url.contains("mariadb")) {
                detectedDialect = SQLDialect.MARIADB;
            } else if (productName.contains("mysql") || url.contains("mysql")) {
                detectedDialect = SQLDialect.MYSQL;
            }
        } catch (SQLException e) {
            logger.debug("Could not detect SQL dialect from JDBC metadata, using configured type", e);
        }

        this.sqlDialect = detectedDialect;
        return detectedDialect;
    }

    private @NotNull SQLDialect getDefaultDialect() {
        return switch (getType().toUpperCase(Locale.ROOT)) {
            case "MYSQL" -> SQLDialect.MYSQL;
            case "SQLITE" -> SQLDialect.SQLITE;
            case "H2" -> SQLDialect.H2;
            default -> SQLDialect.DEFAULT;
        };
    }

    private @NotNull String toLowerCase(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }

}
