package com.oheers.fish.database;

import com.oheers.fish.database.model.CompetitionReport;
import com.oheers.fish.database.mapper.CompetitionReportMapper;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;

class CompetitionReportPersistenceTest {

    @TempDir
    Path directory;

    @Test
    void sqliteMigrationKeepsRepeatedRunsAndMakesRetriesIdempotent() throws Exception {
        Jdbi jdbi = Jdbi.create("jdbc:sqlite:" + directory.resolve("reports.db"));
        jdbi.registerRowMapper(CompetitionReport.class, new CompetitionReportMapper("emf_competitions", false));
        jdbi.useHandle(handle -> handle.execute("""
            CREATE TABLE emf_competitions (
              id INTEGER PRIMARY KEY AUTOINCREMENT,
              competition_name VARCHAR(256) NOT NULL,
              winner_uuid VARCHAR(128) NOT NULL,
              winner_fish VARCHAR(256) NOT NULL,
              winner_score REAL NOT NULL,
              contestants TEXT NOT NULL,
              start_time DATETIME NOT NULL,
              end_time DATETIME NOT NULL
            )
            """));
        String migration;
        try (var resource = getClass().getResourceAsStream("/db/migrations/sqlite/V8_3__Add_competition_report_id.sql")) {
            migration = new String(resource.readAllBytes(), StandardCharsets.UTF_8).replace("${table.prefix}", "emf_");
        }
        jdbi.useHandle(handle -> {
            for (String statement : migration.split(";")) {
                if (!statement.isBlank()) {
                    handle.execute(statement);
                }
            }
        });

        Database database = mock(Database.class, CALLS_REAL_METHODS);
        setField(database, "jdbi", jdbi);
        setField(database, "competitionsTable", "emf_competitions");
        LocalDateTime start = LocalDateTime.of(2026, 1, 1, 12, 0);
        CompetitionReport first = new CompetitionReport("contest", List.of(), start, start.plusMinutes(5));
        CompetitionReport second = new CompetitionReport("contest", List.of(), start.plusHours(1), start.plusHours(1).plusMinutes(5));
        assertNotEquals(first.getRunId(), second.getRunId());

        database.batchUpdateCompetitions(List.of(first, second));
        database.batchUpdateCompetitions(List.of(first, second));

        int count = jdbi.withHandle(handle -> handle.createQuery("select count(*) from emf_competitions")
            .mapTo(Integer.class).one());
        assertEquals(2, count);
        assertEquals(first.getRunId(), database.getCompetitionReport(first.getRunId()).getRunId());
        assertEquals(second.getRunId(), database.getCompetitionReport(second.getRunId()).getRunId());

        jdbi.useHandle(handle -> handle.execute("DROP TABLE emf_competitions"));
        assertThrows(IllegalStateException.class, () -> database.batchUpdateCompetitions(List.of(first)));
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        Field field = Database.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}
