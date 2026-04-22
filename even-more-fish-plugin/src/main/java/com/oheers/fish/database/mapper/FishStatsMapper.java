package com.oheers.fish.database.mapper;

import com.oheers.fish.database.model.fish.FishStats;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

public class FishStatsMapper implements RowMapper<FishStats> {

    @Override
    public FishStats map(ResultSet rs, StatementContext ctx) throws SQLException {
        return new FishStats(
                rs.getString("fish_name"),
                rs.getString("fish_rarity"),
                getLocalDateTime(rs, "first_catch_time"),
                UUID.fromString(rs.getString("discoverer")),
                rs.getFloat("shortest_length"),
                UUID.fromString(rs.getString("shortest_fisher")),
                rs.getFloat("largest_fish"),
                UUID.fromString(rs.getString("largest_fisher")),
                rs.getInt("total_caught")
        );
    }

    private LocalDateTime getLocalDateTime(ResultSet rs, String column) throws SQLException {
        Timestamp timestamp = rs.getTimestamp(column);
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }
}
