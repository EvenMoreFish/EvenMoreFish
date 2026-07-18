package com.oheers.fish.database.connection;

import com.zaxxer.hikari.HikariConfig;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class SqliteConnectionFactory extends ConnectionFactory {
    @Override
    protected void configureDatabase(@NotNull HikariConfig config, String address, int port, String databaseName, String username, String password) {
        config.setJdbcUrl("jdbc:sqlite:plugins/EvenMoreFish/" + databaseName + ".db?journal_mode=WAL");
        config.setMaximumPoolSize(1);
        config.setMinimumIdle(1);
    }

    @Override
    protected void overrideProperties(@NotNull Map<String, String> properties) {
        properties.putIfAbsent("cachePrepStmts", "true");
        properties.putIfAbsent("prepStmtCacheSize", "250");
        properties.putIfAbsent("prepStmtCacheSqlLimit", "2048");
        properties.putIfAbsent("foreign_keys", "true");
        properties.putIfAbsent("journal_mode", "WAL");
        properties.putIfAbsent("synchronous", "NORMAL");
        super.overrideProperties(properties);
    }

    @Override
    public String getType() {
        return "SQLITE";
    }

}
