package com.oheers.fish.database.data.manager;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.database.Database;
import com.oheers.fish.database.execute.DatabaseWorker;
import com.oheers.fish.database.model.user.EmptyUserReport;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Field;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserManagerTest {

    @Test
    void getUserIdAsyncResolvesCacheMissOnDatabaseWorker() throws Exception {
        UUID uuid = UUID.randomUUID();
        AtomicReference<Thread> loaderThread = new AtomicReference<>();
        Database database = mock(Database.class);
        when(database.getUserId(uuid)).thenAnswer(invocation -> {
            loaderThread.set(Thread.currentThread());
            return 7;
        });
        DatabaseWorker worker = new DatabaseWorker("emf-user-manager-test", 1_000);
        UserManager manager = new UserManager(database, worker, null);

        CompletableFuture<Integer> result = manager.getUserIdAsync(uuid);

        assertEquals(7, result.get(5, TimeUnit.SECONDS));
        assertNotEquals(Thread.currentThread(), loaderThread.get());
        assertTrue(worker.shutdown(5, TimeUnit.SECONDS));
    }

    @Test
    void getUserIdAsyncReturnsCachedValueWithoutSecondDatabaseLookup() throws Exception {
        UUID uuid = UUID.randomUUID();
        Database database = mock(Database.class);
        when(database.getUserId(uuid)).thenReturn(7);
        DatabaseWorker worker = new DatabaseWorker("emf-user-manager-test", 1_000);
        UserManager manager = new UserManager(database, worker, null);

        assertEquals(7, manager.getUserIdAsync(uuid).get(5, TimeUnit.SECONDS));
        CompletableFuture<Integer> cachedResult = manager.getUserIdAsync(uuid);

        assertTrue(cachedResult.isDone());
        assertEquals(7, cachedResult.get(5, TimeUnit.SECONDS));
        verify(database, times(1)).getUserId(uuid);
        assertTrue(worker.shutdown(5, TimeUnit.SECONDS));
    }

    @Test
    void getCachedUserIdDoesNotQueryDatabaseOnMiss() {
        UUID uuid = UUID.randomUUID();
        Database database = mock(Database.class);
        DatabaseWorker worker = new DatabaseWorker("emf-user-manager-test", 1_000);
        UserManager manager = new UserManager(database, worker, null);

        assertEquals(0, manager.getCachedUserId(uuid));
        verify(database, times(0)).getUserId(uuid);
        assertTrue(worker.shutdown(5, TimeUnit.SECONDS));
    }

    @Test
    void joinPreloadCreatesMissingUserOnDatabaseWorker() throws Exception {
        EvenMoreFish previousPlugin = installMockPlugin();
        try {
            UUID uuid = UUID.randomUUID();
            AtomicReference<Thread> upsertThread = new AtomicReference<>();
            CountDownLatch preloadDone = new CountDownLatch(1);
            AtomicInteger preloadedId = new AtomicInteger();
            AtomicReference<UUID> preloadedUuid = new AtomicReference<>();
            Database database = mock(Database.class);
            when(database.getUserId(uuid)).thenReturn(0);
            when(database.upsertUserReport(any(EmptyUserReport.class))).thenAnswer(invocation -> {
                upsertThread.set(Thread.currentThread());
                return 11;
            });
            DatabaseWorker worker = new DatabaseWorker("emf-user-manager-test", 1_000);
            UserManager manager = new UserManager(database, worker, (joinedUuid, id) -> {
                preloadedUuid.set(joinedUuid);
                preloadedId.set(id);
                preloadDone.countDown();
            });
            Player player = mock(Player.class);
            when(player.getUniqueId()).thenReturn(uuid);

            manager.onJoin(new PlayerJoinEvent(player, ""));

            assertTrue(preloadDone.await(5, TimeUnit.SECONDS));
            assertEquals(uuid, preloadedUuid.get());
            assertEquals(11, preloadedId.get());
            assertEquals(11, manager.getCachedUserId(uuid));
            assertNotEquals(Thread.currentThread(), upsertThread.get());
            ArgumentCaptor<EmptyUserReport> reportCaptor = ArgumentCaptor.forClass(EmptyUserReport.class);
            verify(database).upsertUserReport(reportCaptor.capture());
            assertEquals(uuid, reportCaptor.getValue().getUuid());
            assertTrue(worker.shutdown(5, TimeUnit.SECONDS));
        } finally {
            setPlugin(previousPlugin);
        }
    }

    private static EvenMoreFish installMockPlugin() throws Exception {
        EvenMoreFish previousPlugin = getPlugin();
        setPlugin(mock(EvenMoreFish.class));
        return previousPlugin;
    }

    private static EvenMoreFish getPlugin() throws Exception {
        Field field = EvenMoreFish.class.getDeclaredField("instance");
        field.setAccessible(true);
        return (EvenMoreFish) field.get(null);
    }

    private static void setPlugin(EvenMoreFish plugin) throws Exception {
        Field field = EvenMoreFish.class.getDeclaredField("instance");
        field.setAccessible(true);
        field.set(null, plugin);
    }
}
