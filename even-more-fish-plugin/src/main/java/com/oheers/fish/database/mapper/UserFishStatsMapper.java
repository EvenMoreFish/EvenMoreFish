package com.oheers.fish.database.mapper;

import com.oheers.fish.database.model.user.UserFishStats;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;

public class UserFishStatsMapper implements RowMapper<UserFishStats> {
    private final String tableName;
    private final boolean normalizeLegacyText;

    public UserFishStatsMapper(String tableName, boolean normalizeLegacyText) {
        this.tableName = tableName;
        this.normalizeLegacyText = normalizeLegacyText;
    }

    @Override
    public UserFishStats map(ResultSet rs, StatementContext ctx) throws SQLException {
        return new UserFishStats(
                rs.getInt("user_id"),
                rs.getString("fish_name"),
                rs.getString("fish_rarity"),
                CompatibleLocalDateTimeReader.read(rs, tableName, "first_catch_time", identityFor(rs), normalizeLegacyText),
                rs.getFloat("shortest_length"),
                rs.getFloat("longest_length"),
                rs.getInt("quantity")
        );
    }

    private LinkedHashMap<String, Object> identityFor(ResultSet rs) throws SQLException {
        LinkedHashMap<String, Object> identity = new LinkedHashMap<>();
        identity.put("user_id", rs.getInt("user_id"));
        identity.put("fish_name", rs.getString("fish_name"));
        identity.put("fish_rarity", rs.getString("fish_rarity"));
        return identity;
    }
}
