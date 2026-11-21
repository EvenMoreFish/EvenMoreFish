package com.oheers.fish.competition;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.competition.configs.CompetitionFile;
import com.oheers.fish.utils.TimeCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;

import java.nio.file.Path;
import java.time.DayOfWeek;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CompetitionScheduleConflictTest {

    private static final TimeCode MONDAY_NOON = TimeCode.exact(DayOfWeek.MONDAY, 12, 0);

    @TempDir
    Path directory;

    @Test
    void specificDayConflictKeepsFirstIdInStableOrderAndLogsBothIds() {
        Logger logger = mock(Logger.class);
        try (MockedStatic<EvenMoreFish> plugin = mockPlugin(logger)) {
            CompetitionManager manager = manager();
            CompetitionFile beta = specific("beta", DayOfWeek.MONDAY, "12:00");
            CompetitionFile alpha = specific("alpha", DayOfWeek.MONDAY, "12:00");
            manager.getItemMap().put(beta.getId(), beta);
            manager.getItemMap().put(alpha.getId(), alpha);

            manager.getItemMap().values().forEach(manager::loadSpecificDayTimes);

            assertSame(alpha, manager.getCompetitions().get(MONDAY_NOON));
            assertEquals(1, manager.getSize());
            verify(logger).severe(org.mockito.ArgumentMatchers.<String>argThat(message -> message.contains("MONDAY-12-0")
                && message.contains("beta' conflicts with 'alpha")));
            clear(manager);
        }
    }

    @Test
    void repeatedTimesInOneFileAreHarmlessButAnotherFileConflicts() {
        Logger logger = mock(Logger.class);
        try (MockedStatic<EvenMoreFish> plugin = mockPlugin(logger)) {
            CompetitionManager manager = manager();
            CompetitionFile alpha = repeated("alpha", List.of("12:00", "12:00"));
            CompetitionFile beta = repeated("beta", List.of("12:00"));

            manager.loadRepeatedTiming(alpha);
            manager.loadRepeatedTiming(beta);

            assertSame(alpha, manager.getCompetitions().get(MONDAY_NOON));
            assertEquals(1, manager.getSize());
            verify(logger).severe(org.mockito.ArgumentMatchers.<String>argThat(message -> message.contains("MONDAY-12-0")
                && message.contains("beta' conflicts with 'alpha")));
            clear(manager);
        }
    }

    @Test
    void crossFormatConflictDoesNotReplaceTheFirstOrUnrelatedTime() {
        Logger logger = mock(Logger.class);
        try (MockedStatic<EvenMoreFish> plugin = mockPlugin(logger)) {
            CompetitionManager manager = manager();
            CompetitionFile alpha = specific("alpha", DayOfWeek.MONDAY, "12:00");
            CompetitionFile beta = repeated("beta", List.of("12:00"));
            CompetitionFile gamma = specific("gamma", DayOfWeek.TUESDAY, "13:00");

            manager.loadSpecificDayTimes(alpha);
            manager.loadRepeatedTiming(beta);
            manager.loadSpecificDayTimes(gamma);

            assertSame(alpha, manager.getCompetitions().get(MONDAY_NOON));
            assertSame(gamma, manager.getCompetitions().get(TimeCode.exact(DayOfWeek.TUESDAY, 13, 0)));
            assertEquals(2, manager.getSize());
            verify(logger).severe(org.mockito.ArgumentMatchers.<String>argThat(message -> message.contains("MONDAY-12-0")
                && message.contains("beta' conflicts with 'alpha")));
            clear(manager);
        }
    }

    @Test
    void nonconflictingSchedulesProduceNoError() {
        Logger logger = mock(Logger.class);
        try (MockedStatic<EvenMoreFish> plugin = mockPlugin(logger)) {
            CompetitionManager manager = manager();
            manager.loadSpecificDayTimes(specific("alpha", DayOfWeek.MONDAY, "12:00"));
            manager.loadSpecificDayTimes(specific("beta", DayOfWeek.MONDAY, "13:00"));

            assertEquals(2, manager.getSize());
            verify(logger, never()).severe(org.mockito.ArgumentMatchers.anyString());
            clear(manager);
        }
    }

    private MockedStatic<EvenMoreFish> mockPlugin(Logger logger) {
        EvenMoreFish instance = mock(EvenMoreFish.class);
        when(instance.getDataFolder()).thenReturn(directory.toFile());
        when(instance.getLogger()).thenReturn(logger);
        MockedStatic<EvenMoreFish> plugin = mockStatic(EvenMoreFish.class);
        plugin.when(EvenMoreFish::getInstance).thenReturn(instance);
        return plugin;
    }

    private CompetitionManager manager() {
        CompetitionManager manager = CompetitionManager.getInstance();
        manager.getCompetitions().clear();
        manager.getItemMap().clear();
        return manager;
    }

    private void clear(CompetitionManager manager) {
        manager.getCompetitions().clear();
        manager.getItemMap().clear();
    }

    private CompetitionFile specific(String id, DayOfWeek day, String time) {
        CompetitionFile file = mock(CompetitionFile.class);
        when(file.getId()).thenReturn(id);
        when(file.getScheduledDays()).thenReturn(Map.of(day, List.of(time)));
        return file;
    }

    private CompetitionFile repeated(String id, List<String> times) {
        CompetitionFile file = mock(CompetitionFile.class);
        when(file.getId()).thenReturn(id);
        when(file.getTimes()).thenReturn(times);
        when(file.getBlacklistedDays()).thenReturn(Arrays.stream(DayOfWeek.values())
            .filter(day -> day != DayOfWeek.MONDAY).toList());
        return file;
    }
}
