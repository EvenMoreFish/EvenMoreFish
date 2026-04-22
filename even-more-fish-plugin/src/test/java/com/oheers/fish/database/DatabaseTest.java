package com.oheers.fish.database;

import com.oheers.fish.competition.Competition;
import com.oheers.fish.competition.CompetitionType;
import com.oheers.fish.competition.leaderboard.Leaderboard;
import com.oheers.fish.database.data.FishRarityKey;
import com.oheers.fish.database.model.user.UserReport;
import com.oheers.fish.EvenMoreFish;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.HandleConsumer;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.Update;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Answers;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.io.File;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DatabaseTest {

    @Test
    void bindUserReportInsertPersistsRecentFishAsLastFish() throws Exception {
        Database database = allocateDatabase();
        setField(database, "usersTable", "emf_users");

        Handle handle = mock(Handle.class);
        Update update = mock(Update.class, Answers.RETURNS_SELF);
        when(handle.createUpdate(anyString())).thenReturn(update);

        UserReport report = createUserReport();

        invokePrivate(database, "bindUserReportInsert", new Class[]{Handle.class, UserReport.class}, handle, report);

        verify(update).bind("last_fish", report.getRecentFish().toString());
        verify(update, never()).bind("last_fish", report.getLargestFish().toString());
    }

    @Test
    void bindUserReportUpdatePersistsRecentFishAsLastFish() throws Exception {
        Database database = allocateDatabase();
        setField(database, "usersTable", "emf_users");

        Handle handle = mock(Handle.class);
        Update update = mock(Update.class, Answers.RETURNS_SELF);
        when(handle.createUpdate(anyString())).thenReturn(update);

        UserReport report = createUserReport();

        invokePrivate(database, "bindUserReportUpdate", new Class[]{Handle.class, UserReport.class, int.class}, handle, report, 7);

        verify(update).bind("last_fish", report.getRecentFish().toString());
        verify(update, never()).bind("last_fish", report.getLargestFish().toString());
    }

    @Test
    void createCompetitionReportUsesCompetitionStartTimeForStartTimestamp() throws Exception {
        Database database = allocateDatabase();
        setField(database, "competitionsTable", "emf_competitions");
        initializePluginSingleton();

        Jdbi jdbi = mock(Jdbi.class);
        Handle handle = mock(Handle.class);
        Update update = mock(Update.class, Answers.RETURNS_SELF);
        when(handle.createUpdate(anyString())).thenReturn(update);
        doAnswer(invocation -> {
            HandleConsumer<?> consumer = invocation.getArgument(0);
            consumer.useHandle(handle);
            return null;
        }).when(jdbi).useHandle(any(HandleConsumer.class));
        setField(database, "jdbi", jdbi);

        LocalDateTime startTime = LocalDateTime.of(2026, 4, 17, 9, 30, 0);
        Competition competition = new TestCompetition("spring-classic", startTime);

        LocalDateTime before = LocalDateTime.now();
        database.createCompetitionReport(competition);
        LocalDateTime after = LocalDateTime.now();

        verify(update).bind("start_time", startTime);

        ArgumentCaptor<LocalDateTime> endTimeCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(update).bind(eq("end_time"), endTimeCaptor.capture());

        LocalDateTime endTime = endTimeCaptor.getValue();
        assertFalse(endTime.isBefore(before));
        assertFalse(endTime.isAfter(after));
        assertTrue(endTime.isAfter(startTime));
    }

    private static UserReport createUserReport() {
        return new UserReport(
            UUID.fromString("11111111-1111-1111-1111-111111111111"),
            FishRarityKey.of("anchovy", "common"),
            FishRarityKey.of("bass", "rare"),
            FishRarityKey.of("marlin", "legendary"),
            FishRarityKey.of("minnow", "common"),
            12,
            2,
            4,
            42.5f,
            1.5f,
            130.0f,
            3,
            64.0
        );
    }

    private static Database allocateDatabase() throws Exception {
        return (Database) getUnsafe().allocateInstance(Database.class);
    }

    private static void initializePluginSingleton() throws Exception {
        Field instanceField = EvenMoreFish.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        if (instanceField.get(null) == null) {
            instanceField.set(null, getUnsafe().allocateInstance(TestPlugin.class));
        }
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = Database.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private static Object invokePrivate(Object target, String methodName, Class<?>[] parameterTypes, Object... args) throws Exception {
        Method method = Database.class.getDeclaredMethod(methodName, parameterTypes);
        method.setAccessible(true);
        return method.invoke(target, args);
    }

    private static sun.misc.Unsafe getUnsafe() throws Exception {
        Field field = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        return (sun.misc.Unsafe) field.get(null);
    }

    private static final class TestCompetition extends Competition {
        private final Leaderboard leaderboard = new Leaderboard(CompetitionType.LARGEST_FISH);
        private final String competitionName;
        private final LocalDateTime startTime;

        private TestCompetition(String competitionName, LocalDateTime startTime) {
            super(60L, CompetitionType.LARGEST_FISH);
            this.competitionName = competitionName;
            this.startTime = startTime;
        }

        @Override
        public Leaderboard getLeaderboard() {
            return leaderboard;
        }

        @Override
        public String getCompetitionName() {
            return competitionName;
        }

        @Override
        public LocalDateTime getStartTime() {
            return startTime;
        }
    }

    public static final class TestPlugin extends EvenMoreFish {
        @Override
        public void loadCommands() {
        }

        @Override
        public void enableCommands() {
        }

        @Override
        public void registerCommands() {
        }

        @Override
        public void resendCommands() {
        }

        @Override
        public void disableCommands() {
        }

        @Override
        public ItemStack getSkullFromUUID(UUID uuid) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ItemStack getSkullFromBase64(String base64) {
            throw new UnsupportedOperationException();
        }
    }
}
