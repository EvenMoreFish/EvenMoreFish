package com.oheers.fish.database;

import java.sql.SQLException;

public class UnreadableTimestampException extends SQLException {
    public UnreadableTimestampException(String tableName, String columnName, String rawValue, Throwable cause) {
        super("Unable to parse timestamp value '%s' from %s.%s".formatted(rawValue, tableName, columnName), cause);
    }
}
