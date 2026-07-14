package com.oheers.fish.database.data.strategy;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AsyncSavingStrategyTest {

    /**
     * Records submitted tasks without running them, so tests can assert
     * that nothing executed on the caller thread and then run the backlog.
     */
    private static final class DeferredExecutor implements Executor {
        private final List<Runnable> tasks = new ArrayList<>();

        @Override
        public void execute(Runnable command) {
            tasks.add(command);
        }

        void runAll() {
            tasks.forEach(Runnable::run);
            tasks.clear();
        }
    }

    @Test
    void immediateStrategyDispatchesSavesToExecutor() {
        DeferredExecutor executor = new DeferredExecutor();
        List<String> saved = new ArrayList<>();
        ImmediateSavingStrategy<String> strategy = new ImmediateSavingStrategy<>(saved::add, executor);

        strategy.save("a");
        strategy.saveAll(List.of("b", "c"));

        assertTrue(saved.isEmpty(), "saves must not run on the caller thread");
        executor.runAll();
        assertEquals(List.of("a", "b", "c"), saved);
    }

    @Test
    void immediateStrategyWithoutExecutorSavesInline() {
        List<String> saved = new ArrayList<>();
        ImmediateSavingStrategy<String> strategy = new ImmediateSavingStrategy<>(saved::add);

        strategy.save("a");

        assertEquals(List.of("a"), saved);
    }

    @Test
    void bufferedStrategyDispatchesCreateAndBatchWritesToExecutor() {
        DeferredExecutor executor = new DeferredExecutor();
        List<String> createWrites = new ArrayList<>();
        List<List<String>> batchWrites = new ArrayList<>();
        BufferedSavingStrategy<String> strategy = new BufferedSavingStrategy<>(
            createWrites::add,
            batch -> batchWrites.add(new ArrayList<>(batch)),
            true,
            executor
        );

        strategy.save("created");
        strategy.saveAll(List.of("x", "y"));

        assertTrue(createWrites.isEmpty());
        assertTrue(batchWrites.isEmpty());
        executor.runAll();
        assertEquals(List.of("created"), createWrites);
        assertEquals(List.of(List.of("x", "y")), batchWrites);
    }

    @Test
    void bufferedStrategySnapshotsBatchesBeforeHandoff() {
        DeferredExecutor executor = new DeferredExecutor();
        List<Collection<String>> batchWrites = new ArrayList<>();
        BufferedSavingStrategy<String> strategy = new BufferedSavingStrategy<>(
            null,
            batchWrites::add,
            false,
            executor
        );

        List<String> source = new ArrayList<>(List.of("x", "y"));
        strategy.saveAll(source);
        source.clear();
        executor.runAll();

        assertEquals(1, batchWrites.size());
        assertEquals(List.of("x", "y"), List.copyOf(batchWrites.getFirst()));
    }
}
