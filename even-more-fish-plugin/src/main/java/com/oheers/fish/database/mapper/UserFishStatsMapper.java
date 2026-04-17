package com.oheers.fish.database.mapper;

import com.oheers.fish.database.model.user.UserFishStats;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class UserFishStatsMapper implements RowMapper<UserFishStats> {

    @Override
    public UserFishStats map(ResultSet rs, StatementContext ctx) throws SQLException {
        return new UserFishStats(
                rs.getInt("user_id"),
                rs.getString("fish_name"),
                rs.getString("fish_rarity"),
                getLocalDateTime(rs, "first_catch_time"),
                rs.getFloat("shortest_length"),
                rs.getFloat("longest_length"),
                rs.getInt("quantity")
        );
    }

    private LocalDateTime getLocalDateTime(ResultSet rs, String column) throws SQLException {
        Timestamp timestamp = rs.getTimestamp(column);
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }
}
