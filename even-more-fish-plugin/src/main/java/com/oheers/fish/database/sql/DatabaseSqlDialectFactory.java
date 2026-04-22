package com.oheers.fish.database.sql;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public final class DatabaseSqlDialectFactory {

    private DatabaseSqlDialectFactory() {
    }

    public static @NotNull DatabaseSqlDialect create(@NotNull String databaseType) {
        return switch (databaseType.toUpperCase(Locale.ROOT)) {
            case "SQLITE" -> new SqliteDatabaseSqlDialect();
            case "POSTGRESQL", "POSTGRES" -> new PostgresDatabaseSqlDialect();
            case "MYSQL", "MARIADB", "H2" -> new MysqlLikeDatabaseSqlDialect();
            default -> throw new IllegalArgumentException("Unsupported database type: " + databaseType);
        };
    }
}
