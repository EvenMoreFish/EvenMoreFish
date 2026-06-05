package com.oheers.fish.database.mapper;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.database.UnreadableTimestampException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.Map;

final class CompatibleLocalDateTimeReader {

    private CompatibleLocalDateTimeReader() {
        throw new UnsupportedOperationException();
    }

    static LocalDateTime read(
        ResultSet resultSet,
        String tableName,
        String columnName,
        LinkedHashMap<String, Object> rowIdentity,
        boolean normalizeLegacyText
    ) throws SQLException {
        try {
            Timestamp timestamp = resultSet.getTimestamp(columnName);
            if (timestamp != null) {
                return timestamp.toLocalDateTime();
            }
        } catch (SQLException exception) {
            LocalDateTime compatibleValue = parseCompatibleValue(resultSet, tableName, columnName, exception);
            warnAndNormalize(resultSet, tableName, columnName, rowIdentity, normalizeLegacyText, compatibleValue, exception);
            return compatibleValue;
        }

        String rawValue = resultSet.getString(columnName);
        if (rawValue == null || rawValue.isBlank()) {
            return null;
        }

        LocalDateTime compatibleValue = parseCompatibleValue(rawValue, tableName, columnName, null);
        warnAndNormalize(resultSet, tableName, columnName, rowIdentity, normalizeLegacyText, compatibleValue, null);
        return compatibleValue;
    }

    private static LocalDateTime parseCompatibleValue(
        ResultSet resultSet,
        String tableName,
        String columnName,
        SQLException originalException
    ) throws SQLException {
        String rawValue = resultSet.getString(columnName);
        return parseCompatibleValue(rawValue, tableName, columnName, originalException);
    }

    private static LocalDateTime parseCompatibleValue(
        String rawValue,
        String tableName,
        String columnName,
        SQLException originalException
    ) throws SQLException {
        if (rawValue == null || rawValue.isBlank()) {
            if (originalException != null) {
                throw originalException;
            }
            return null;
        }

        try {
            return Timestamp.valueOf(rawValue).toLocalDateTime();
        } catch (IllegalArgumentException ignored) {
            try {
                return LocalDateTime.parse(rawValue);
            } catch (DateTimeParseException parseException) {
                throw new UnreadableTimestampException(tableName, columnName, rawValue, originalException == null ? parseException : originalException);
            }
        }
    }

    private static void warnAndNormalize(
        ResultSet resultSet,
        String tableName,
        String columnName,
        LinkedHashMap<String, Object> rowIdentity,
        boolean normalizeLegacyText,
        LocalDateTime compatibleValue,
        SQLException originalException
    ) {
        String rowDescription = describeIdentity(rowIdentity);
        EvenMoreFish plugin = EvenMoreFish.getInstance();
        plugin.getLogger().warning(
            "Parsed legacy timestamp text for %s.%s (%s) using compatibility fallback."
                .formatted(tableName, columnName, rowDescription)
        );
        if (originalException != null) {
            plugin.debug("Legacy timestamp fallback used for %s.%s (%s).".formatted(tableName, columnName, rowDescription), originalException);
        }

        if (!normalizeLegacyText) {
            return;
        }

        try {
            normalizeTimestamp(resultSet, tableName, columnName, rowIdentity, compatibleValue);
        } catch (SQLException exception) {
            plugin.getLogger().warning(
                "Failed to normalize legacy timestamp text for %s.%s (%s)."
                    .formatted(tableName, columnName, rowDescription)
            );
            plugin.debug("Failed to normalize legacy timestamp text.", exception);
        }
    }

    private static void normalizeTimestamp(
        ResultSet resultSet,
        String tableName,
        String columnName,
        LinkedHashMap<String, Object> rowIdentity,
        LocalDateTime compatibleValue
    ) throws SQLException {
        if (rowIdentity.isEmpty()) {
            return;
        }
        PreparedStatement statement = null;
        try {
            StringBuilder sql = new StringBuilder("update ");
            sql.append(tableName).append(" set ").append(columnName).append(" = ? where ");

            int clauseIndex = 0;
            for (String key : rowIdentity.keySet()) {
                if (clauseIndex++ > 0) {
                    sql.append(" and ");
                }
                sql.append(key).append(" = ?");
            }

            statement = resultSet.getStatement().getConnection().prepareStatement(sql.toString());
            statement.setTimestamp(1, Timestamp.valueOf(compatibleValue));

            int parameterIndex = 2;
            for (Object value : rowIdentity.values()) {
                statement.setObject(parameterIndex++, value);
            }

            statement.executeUpdate();
        } finally {
            if (statement != null) {
                statement.close();
            }
        }
    }

    private static String describeIdentity(LinkedHashMap<String, Object> rowIdentity) {
        StringBuilder builder = new StringBuilder();
        int index = 0;
        for (Map.Entry<String, Object> entry : rowIdentity.entrySet()) {
            if (index++ > 0) {
                builder.append(", ");
            }
            builder.append(entry.getKey()).append('=').append(entry.getValue());
        }
        return builder.toString();
    }
}
