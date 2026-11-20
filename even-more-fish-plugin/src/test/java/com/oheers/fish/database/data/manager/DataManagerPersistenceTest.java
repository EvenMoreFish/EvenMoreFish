package com.oheers.fish.database.data.manager;

import com.oheers.fish.database.data.strategy.BufferedSavingStrategy;
import com.oheers.fish.database.model.user.UserFishStats;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DataManagerPersistenceTest {

    @Test
    void queuedWriteDoesNotClearDirtyKeyBeforeCompletion() {
        DeferredExecutor executor = new DeferredExecutor();
        List<List<String>> saved = new ArrayList<>();
        DataManager<String> manager = new DataManager<>(
            new BufferedSavingStrategy<>(null, values -> saved.add(List.copyOf(values)), false, executor),
            key -> null
        );

        manager.update("fish", "first");
        manager.flush();
        manager.flush();
        assertEquals(1, executor.pending());
        assertEquals(1, manager.getPendingPersistenceCount());
        assertEquals(List.of(), saved);

        executor.runNext();
        manager.flush();
        assertEquals(0, executor.pending());
        assertEquals(0, manager.getPendingPersistenceCount());
        assertEquals(List.of(List.of("first")), saved);
    }

    @Test
    void failedWriteRemainsDirtyAndCanBeRetried() {
        DeferredExecutor executor = new DeferredExecutor();
        AtomicInteger attempts = new AtomicInteger();
        List<List<String>> saved = new ArrayList<>();
        DataManager<String> manager = new DataManager<>(
            new BufferedSavingStrategy<>(null, values -> {
                if (attempts.incrementAndGet() == 1) {
                    throw new IllegalStateException("database unavailable");
                }
                saved.add(List.copyOf(values));
            }, false, executor),
            key -> null
        );

        manager.update("fish", "first");
        manager.flush();
        executor.runNext();
        assertEquals(1, manager.getPendingPersistenceCount());
        manager.flush();
        executor.runNext();

        assertEquals(2, attempts.get());
        assertEquals(List.of(List.of("first")), saved);
        assertEquals(0, manager.getPendingPersistenceCount());
        manager.flush();
        assertEquals(0, executor.pending());
    }

    @Test
    void oldAcknowledgementDoesNotClearNewerUpdate() {
        DeferredExecutor executor = new DeferredExecutor();
        List<List<String>> saved = new ArrayList<>();
        DataManager<String> manager = new DataManager<>(
            new BufferedSavingStrategy<>(null, values -> saved.add(List.copyOf(values)), false, executor),
            key -> null
        );

        manager.update("fish", "first");
        manager.flush();
        manager.update("fish", "second");
        executor.runNext();
        assertEquals(1, manager.getPendingPersistenceCount());
        manager.flush();
        assertEquals(1, executor.pending());
        executor.runNext();

        assertEquals(List.of(List.of("first"), List.of("second")), saved);
        assertEquals(0, manager.getPendingPersistenceCount());
        manager.flush();
        assertEquals(0, executor.pending());
    }

    @Test
    void mutableStatsAreCapturedBeforeWorkerExecution() {
        DeferredExecutor executor = new DeferredExecutor();
        List<Integer> savedQuantities = new ArrayList<>();
        BufferedSavingStrategy<UserFishStats> strategy = new BufferedSavingStrategy<>(
            null,
            values -> savedQuantities.add(values.iterator().next().getQuantity()),
            false,
            executor
        ) {
            @Override
            public UserFishStats snapshot(UserFishStats stats) {
                return stats.snapshot();
            }
        };
        DataManager<UserFishStats> manager = new DataManager<>(strategy, key -> null);
        UserFishStats stats = new UserFishStats(1, "cod", "common", LocalDateTime.of(2026, 1, 1, 12, 0), 1F, 1F, 1);

        manager.update("fish", stats);
        manager.flush();
        stats.incrementQuantity();
        executor.runNext();

        assertEquals(List.of(1), savedQuantities);
    }

    @Test
    void failedCreateWriteRetriesThroughTheBufferedBatch() {
        DeferredExecutor executor = new DeferredExecutor();
        List<String> saved = new ArrayList<>();
        DataManager<String> manager = new DataManager<>(
            new BufferedSavingStrategy<>(
                value -> { throw new IllegalStateException("create failed"); },
                saved::addAll,
                true,
                executor
            ),
            key -> null
        );

        assertEquals("created", manager.getOrCreate("fish", key -> null, () -> "created"));
        assertEquals(1, manager.getPendingPersistenceCount());
        executor.runNext();
        assertEquals(1, manager.getPendingPersistenceCount());

        manager.flush();
        executor.runNext();
        assertEquals(List.of("created"), saved);
        assertEquals(0, manager.getPendingPersistenceCount());
    }

    private static final class DeferredExecutor implements Executor {
        private final Queue<Runnable> tasks = new ArrayDeque<>();

        @Override
        public void execute(Runnable task) {
            tasks.add(task);
        }

        int pending() {
            return tasks.size();
        }

        void runNext() {
            tasks.remove().run();
        }
    }
}
