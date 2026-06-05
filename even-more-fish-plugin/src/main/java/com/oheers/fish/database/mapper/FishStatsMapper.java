package com.oheers.fish.database.mapper;

import com.oheers.fish.database.model.fish.FishStats;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.UUID;

public class FishStatsMapper implements RowMapper<FishStats> {
    private final String tableName;
    private final boolean normalizeLegacyText;

    public FishStatsMapper(String tableName, boolean normalizeLegacyText) {
        this.tableName = tableName;
        this.normalizeLegacyText = normalizeLegacyText;
    }

    @Override
    public FishStats map(ResultSet rs, StatementContext ctx) throws SQLException {
        return new FishStats(
                rs.getString("fish_name"),
                rs.getString("fish_rarity"),
                CompatibleLocalDateTimeReader.read(rs, tableName, "first_catch_time", identityFor(rs), normalizeLegacyText),
                UUID.fromString(rs.getString("discoverer")),
                rs.getFloat("shortest_length"),
                UUID.fromString(rs.getString("shortest_fisher")),
                rs.getFloat("largest_fish"),
                UUID.fromString(rs.getString("largest_fisher")),
                rs.getInt("total_caught")
        );
    }

    private LinkedHashMap<String, Object> identityFor(ResultSet rs) throws SQLException {
        LinkedHashMap<String, Object> identity = new LinkedHashMap<>();
        identity.put("fish_name", rs.getString("fish_name"));
        identity.put("fish_rarity", rs.getString("fish_rarity"));
        return identity;
    }
}
