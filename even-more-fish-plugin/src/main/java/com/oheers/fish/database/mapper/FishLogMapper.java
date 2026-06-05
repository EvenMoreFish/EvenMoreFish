package com.oheers.fish.database.mapper;

import com.oheers.fish.database.model.fish.FishLog;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;

public class FishLogMapper implements RowMapper<FishLog> {
    private final String tableName;
    private final boolean normalizeLegacyText;

    public FishLogMapper(String tableName, boolean normalizeLegacyText) {
        this.tableName = tableName;
        this.normalizeLegacyText = normalizeLegacyText;
    }

    @Override
    public FishLog map(ResultSet rs, StatementContext ctx) throws SQLException {
        return new FishLog(
                rs.getInt("user_id"),
                rs.getString("fish_name"),
                rs.getString("fish_rarity"),
                CompatibleLocalDateTimeReader.read(rs, tableName, "catch_time", identityFor(rs), normalizeLegacyText),
                rs.getFloat("fish_length"),
                rs.getString("competition_id")
        );
    }

    private LinkedHashMap<String, Object> identityFor(ResultSet rs) throws SQLException {
        LinkedHashMap<String, Object> identity = new LinkedHashMap<>();
        identity.put("id", rs.getInt("id"));
        return identity;
    }
}
