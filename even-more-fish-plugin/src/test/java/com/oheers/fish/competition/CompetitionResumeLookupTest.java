package com.oheers.fish.competition;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.competition.configs.CompetitionFile;
import com.oheers.fish.utils.TimeCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;

import java.nio.file.Path;
import java.time.DayOfWeek;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class CompetitionResumeLookupTest {

    @TempDir
    Path directory;

    @Test
    void findsLoadedManualCompetitionWithoutScheduleAndUsesCaseInsensitiveId() {
        EvenMoreFish pluginInstance = mock(EvenMoreFish.class);
        when(pluginInstance.getDataFolder()).thenReturn(directory.toFile());
        try (MockedStatic<EvenMoreFish> plugin = mockStatic(EvenMoreFish.class)) {
            plugin.when(EvenMoreFish::getInstance).thenReturn(pluginInstance);
            CompetitionManager manager = CompetitionManager.getInstance();
            manager.getItemMap().clear();
            manager.getCompetitions().clear();
            CompetitionFile manual = mock(CompetitionFile.class);
            when(manual.getId()).thenReturn("manual");
            manager.getItemMap().put(manual.getId(), manual);

            assertSame(manual, manager.getFileFromId("MANUAL"));
            assertNull(manager.getFileFromId("missing"));
            assertNull(manager.getFileFromId(null));

            manager.getCompetitions().put(TimeCode.exact(DayOfWeek.MONDAY, 12, 0), manual);
            assertSame(manual, manager.getFileFromId("manual"));
            manager.getItemMap().clear();
            assertNull(manager.getFileFromId("manual"));
            manager.getCompetitions().clear();
        }
    }
}
