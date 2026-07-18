package com.oheers.fish.database.data.manager;

import com.oheers.fish.database.Database;
import com.oheers.fish.database.execute.DatabaseWorker;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
}
