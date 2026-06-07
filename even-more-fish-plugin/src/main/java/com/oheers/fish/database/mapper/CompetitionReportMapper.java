package com.oheers.fish.database.mapper;

import com.oheers.fish.database.model.CompetitionReport;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.UUID;

public class CompetitionReportMapper implements RowMapper<CompetitionReport> {

    private static final UUID NIL_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");
    private static final Field WINNER_UUID_FIELD = getField("winnerUuid");
    private static final Field CONTESTANTS_FIELD = getField("contestants");
    private final String tableName;
    private final boolean normalizeLegacyText;

    public CompetitionReportMapper(String tableName, boolean normalizeLegacyText) {
        this.tableName = tableName;
        this.normalizeLegacyText = normalizeLegacyText;
    }

    @Override
    public CompetitionReport map(ResultSet rs, StatementContext ctx) throws SQLException {
        String winnerUuid = rs.getString("winner_uuid");
        String contestants = rs.getString("contestants");

        boolean noWinner = winnerUuid == null || winnerUuid.isBlank() || "None".equalsIgnoreCase(winnerUuid);
        boolean noContestants = contestants == null || contestants.isBlank() || "None".equalsIgnoreCase(contestants);

        CompetitionReport report = new CompetitionReport(
                rs.getString("competition_name"),
                rs.getString("winner_fish"),
                noWinner ? NIL_UUID.toString() : winnerUuid.trim(),
                rs.getFloat("winner_score"),
                noContestants ? NIL_UUID.toString() : normalizeContestants(contestants),
                CompatibleLocalDateTimeReader.read(rs, tableName, "start_time", identityFor(rs), normalizeLegacyText),
                CompatibleLocalDateTimeReader.read(rs, tableName, "end_time", identityFor(rs), normalizeLegacyText)
        );

        if (noWinner) {
            setField(report, WINNER_UUID_FIELD, null);
        }

        if (noContestants) {
            setField(report, CONTESTANTS_FIELD, Collections.emptyList());
        }

        return report;
    }

    private static String normalizeContestants(String contestants) {
        String[] split = contestants.split(",");
        for (int index = 0; index < split.length; index++) {
            split[index] = split[index].trim();
        }
        return String.join(",", split);
    }

    private static Field getField(String name) {
        try {
            Field field = CompetitionReport.class.getDeclaredField(name);
            field.setAccessible(true);
            return field;
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Unable to access CompetitionReport." + name, exception);
        }
    }

    private static void setField(CompetitionReport report, Field field, Object value) {
        try {
            field.set(report, value);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("Unable to update CompetitionReport mapper state", exception);
        }
    }

    private LinkedHashMap<String, Object> identityFor(ResultSet rs) throws SQLException {
        LinkedHashMap<String, Object> identity = new LinkedHashMap<>();
        identity.put("id", rs.getInt("id"));
        return identity;
    }
}
