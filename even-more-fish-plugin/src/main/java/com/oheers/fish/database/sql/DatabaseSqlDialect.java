package com.oheers.fish.database.sql;

public interface DatabaseSqlDialect {

    String userFishStatsUpsert(String tableName);

    String fishStatsUpsert(String tableName);
}
