package com.oheers.fish.database.mapper;

import com.oheers.fish.database.model.CompetitionReport;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

public class CompetitionReportMapper implements RowMapper<CompetitionReport> {

    private static final UUID NIL_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");
    private static final Field WINNER_UUID_FIELD = getField("winnerUuid");
    private static final Field CONTESTANTS_FIELD = getField("contestants");

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
                getLocalDateTime(rs, "start_time"),
                getLocalDateTime(rs, "end_time")
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

    private LocalDateTime getLocalDateTime(ResultSet rs, String column) throws SQLException {
        Timestamp timestamp = rs.getTimestamp(column);
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }
}
