package com.oheers.fish.database.mapper;

import com.oheers.fish.database.data.FishRarityKey;
import com.oheers.fish.database.model.user.UserReport;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class UserReportMapper implements RowMapper<UserReport> {

    @Override
    public UserReport map(ResultSet rs, StatementContext ctx) throws SQLException {
        return new UserReport(
                rs.getInt("id"),
                UUID.fromString(rs.getString("uuid")),
                FishRarityKey.from(rs.getString("first_fish")),
                FishRarityKey.from(rs.getString("last_fish")),
                FishRarityKey.from(rs.getString("largest_fish")),
                FishRarityKey.from(rs.getString("shortest_fish")),
                rs.getInt("num_fish_caught"),
                rs.getInt("competitions_won"),
                rs.getInt("competitions_joined"),
                rs.getFloat("largest_length"),
                rs.getFloat("shortest_length"),
                rs.getFloat("total_fish_length"),
                rs.getInt("fish_sold"),
                rs.getDouble("money_earned")
        );
    }
}
