package com.oheers.fish.database.data.manager;

import com.oheers.fish.database.data.strategy.BufferedSavingStrategy;
import com.oheers.fish.database.data.strategy.DataSavingStrategy;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DataManagerTest {

    @Test
    void getDoesNotPersistLoadedValues() {
        AtomicInteger saveCalls = new AtomicInteger();
        AtomicInteger saveAllCalls = new AtomicInteger();
        DataManager<String> dataManager = new DataManager<>(new RecordingImmediateStrategy(saveCalls, saveAllCalls), key -> "loaded");

        String value = dataManager.get("fish");

        assertEquals("loaded", value);
        assertEquals(0, saveCalls.get());
        assertEquals(0, saveAllCalls.get());
    }

    @Test
    void bufferedStrategyWritesThroughOnCreateAndBuffersUpdatesUntilFlush() {
        List<String> createWrites = new ArrayList<>();
        List<List<String>> batchWrites = new ArrayList<>();
        DataManager<String> dataManager = new DataManager<>(
            new BufferedSavingStrategy<>(createWrites::add, data -> batchWrites.add(new ArrayList<>(data)), true),
            key -> null
        );

        String created = dataManager.getOrCreate("fish", key -> null, () -> "first");
        dataManager.update("fish", "second");

        assertEquals("first", created);
        assertEquals(List.of("first"), createWrites);
        assertEquals(0, batchWrites.size());

        dataManager.flush();

        assertEquals(1, batchWrites.size());
        assertEquals(List.of("second"), batchWrites.getFirst());
    }

    @Test
    void shutdownFlushesBufferedValues() {
        List<List<String>> batchWrites = new ArrayList<>();
        DataManager<String> dataManager = new DataManager<>(
            new BufferedSavingStrategy<>(null, data -> batchWrites.add(new ArrayList<>(data)), false),
            key -> null
        );

        assertNull(dataManager.get("fish"));

        dataManager.update("fish", "value");
        dataManager.shutdown();

        assertEquals(1, batchWrites.size());
        assertEquals(List.of("value"), batchWrites.getFirst());
    }

    @Test
    void getAsyncReturnsCachedValuesInline() throws Exception {
        DataManager<String> dataManager = new DataManager<>(new RecordingImmediateStrategy(new AtomicInteger(), new AtomicInteger()), key -> "loaded");
        dataManager.cacheLoadedValue("fish", "cached");
        DatabaseWorkerStub worker = new DatabaseWorkerStub();

        CompletableFuture<String> result = dataManager.getAsync("fish", worker);

        assertTrue(result.isDone());
        assertEquals("cached", result.get(5, TimeUnit.SECONDS));
        assertEquals(0, worker.queryCalls.get());
        assertTrue(worker.shutdown(5, TimeUnit.SECONDS));
    }

    @Test
    void getAsyncLoadsCacheMissOnDatabaseWorker() throws Exception {
        DataManager<String> dataManager = new DataManager<>(new RecordingImmediateStrategy(new AtomicInteger(), new AtomicInteger()), key -> "loaded");
        DatabaseWorkerStub worker = new DatabaseWorkerStub();
        AtomicReference<Thread> loaderThread = new AtomicReference<>();

        CompletableFuture<String> result = dataManager.getAsync("fish", key -> {
            loaderThread.set(Thread.currentThread());
            return "loaded";
        }, worker);

        assertEquals("loaded", result.get(5, TimeUnit.SECONDS));
        assertEquals("loaded", dataManager.peek("fish"));
        assertNotEquals(Thread.currentThread(), loaderThread.get());
        assertEquals(1, worker.queryCalls.get());
        assertTrue(worker.shutdown(5, TimeUnit.SECONDS));
    }

    @Test
    void getOrCreateAsyncCreatesCacheMissOnDatabaseWorker() throws Exception {
        List<String> saved = new ArrayList<>();
        DataManager<String> dataManager = new DataManager<>(new RecordingImmediateStrategy(saved::add), key -> null);
        DatabaseWorkerStub worker = new DatabaseWorkerStub();

        CompletableFuture<String> result = dataManager.getOrCreateAsync("fish", key -> null, () -> "created", worker);

        assertEquals("created", result.get(5, TimeUnit.SECONDS));
        assertEquals("created", dataManager.peek("fish"));
        assertEquals(List.of("created"), saved);
        assertEquals(1, worker.queryCalls.get());
        assertTrue(worker.shutdown(5, TimeUnit.SECONDS));
    }

    private static final class RecordingImmediateStrategy implements DataSavingStrategy<String> {
        private final AtomicInteger saveCalls;
        private final AtomicInteger saveAllCalls;
        private final java.util.function.Consumer<String> saveConsumer;

        private RecordingImmediateStrategy(AtomicInteger saveCalls, AtomicInteger saveAllCalls) {
            this.saveCalls = saveCalls;
            this.saveAllCalls = saveAllCalls;
            this.saveConsumer = null;
        }

        private RecordingImmediateStrategy(java.util.function.Consumer<String> saveConsumer) {
            this.saveCalls = new AtomicInteger();
            this.saveAllCalls = new AtomicInteger();
            this.saveConsumer = saveConsumer;
        }

        @Override
        public void save(String data) {
            saveCalls.incrementAndGet();
            if (saveConsumer != null) {
                saveConsumer.accept(data);
            }
        }

        @Override
        public void saveAll(Collection<String> data) {
            saveAllCalls.incrementAndGet();
        }
    }

    private static final class DatabaseWorkerStub extends com.oheers.fish.database.execute.DatabaseWorker {
        private final AtomicInteger queryCalls = new AtomicInteger();

        private DatabaseWorkerStub() {
            super("emf-data-manager-test", 1_000);
        }

        @Override
        public <T> CompletableFuture<T> query(java.util.function.Supplier<T> work) {
            queryCalls.incrementAndGet();
            return super.query(work);
        }
    }
}
