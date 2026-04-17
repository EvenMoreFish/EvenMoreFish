package com.oheers.fish.database.mapper;

import com.oheers.fish.database.model.fish.FishLog;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class FishLogMapper implements RowMapper<FishLog> {

    @Override
    public FishLog map(ResultSet rs, StatementContext ctx) throws SQLException {
        return new FishLog(
                rs.getInt("user_id"),
                rs.getString("fish_name"),
                rs.getString("fish_rarity"),
                getLocalDateTime(rs, "catch_time"),
                rs.getFloat("fish_length"),
                rs.getString("competition_id")
        );
    }

    private LocalDateTime getLocalDateTime(ResultSet rs, String column) throws SQLException {
        Timestamp timestamp = rs.getTimestamp(column);
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }
}
