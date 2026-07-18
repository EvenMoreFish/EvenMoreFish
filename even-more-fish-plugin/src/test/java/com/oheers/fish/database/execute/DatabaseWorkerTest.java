package com.oheers.fish.database.execute;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DatabaseWorkerTest {

    @Test
    void executesTasksOffTheCallerThreadInSubmissionOrder() throws Exception {
        DatabaseWorker worker = new DatabaseWorker("emf-db-test", 1_000);
        List<Integer> order = new CopyOnWriteArrayList<>();
        AtomicReference<Thread> workerThread = new AtomicReference<>();
        CountDownLatch done = new CountDownLatch(1);

        IntStream.range(0, 100).forEach(i -> worker.execute(() -> order.add(i)));
        worker.execute(() -> {
            workerThread.set(Thread.currentThread());
            done.countDown();
        });

        assertTrue(done.await(5, TimeUnit.SECONDS));
        assertEquals(IntStream.range(0, 100).boxed().toList(), order);
        assertNotEquals(Thread.currentThread(), workerThread.get());
        assertTrue(workerThread.get().getName().startsWith("emf-db-test-"));
        assertTrue(worker.shutdown(5, TimeUnit.SECONDS));
    }

    @Test
    void queryCompletesFutureWithWorkerResult() throws Exception {
        DatabaseWorker worker = new DatabaseWorker("emf-db-test", 1_000);
        AtomicReference<Thread> workerThread = new AtomicReference<>();

        CompletableFuture<String> result = worker.query(() -> {
            workerThread.set(Thread.currentThread());
            return "loaded";
        });

        assertEquals("loaded", result.get(5, TimeUnit.SECONDS));
        assertNotEquals(Thread.currentThread(), workerThread.get());
        assertTrue(worker.shutdown(5, TimeUnit.SECONDS));
    }

    @Test
    void writeResultPreservesSubmissionOrderWithWrites() throws Exception {
        DatabaseWorker worker = new DatabaseWorker("emf-db-test", 1_000);
        List<String> order = new CopyOnWriteArrayList<>();

        CompletableFuture<Void> first = worker.write(() -> order.add("first"));
        CompletableFuture<String> second = worker.writeResult(() -> {
            order.add("second");
            return "result";
        });
        CompletableFuture<Void> third = worker.write(() -> order.add("third"));

        first.get(5, TimeUnit.SECONDS);
        assertEquals("result", second.get(5, TimeUnit.SECONDS));
        third.get(5, TimeUnit.SECONDS);

        assertEquals(List.of("first", "second", "third"), order);
        assertTrue(worker.shutdown(5, TimeUnit.SECONDS));
    }

    @Test
    void queryCompletesFutureExceptionallyWhenWorkFails() throws Exception {
        DatabaseWorker worker = new DatabaseWorker("emf-db-test", 1_000);

        CompletableFuture<String> result = worker.query(() -> {
            throw new IllegalStateException("boom");
        });

        ExecutionException exception = org.junit.jupiter.api.Assertions.assertThrows(
            ExecutionException.class,
            () -> result.get(5, TimeUnit.SECONDS)
        );
        assertInstanceOf(IllegalStateException.class, exception.getCause());
        assertEquals("boom", exception.getCause().getMessage());
        assertTrue(worker.shutdown(5, TimeUnit.SECONDS));
    }

    @Test
    void writeSubmittedAfterShutdownRunsInlineInsteadOfBeingLost() {
        DatabaseWorker worker = new DatabaseWorker("emf-db-test", 1_000);
        assertTrue(worker.shutdown(5, TimeUnit.SECONDS));

        AtomicReference<Thread> executionThread = new AtomicReference<>();
        worker.execute(() -> executionThread.set(Thread.currentThread()));

        assertEquals(Thread.currentThread(), executionThread.get());
    }

    @Test
    void pendingTaskCountTracksBacklog() throws Exception {
        DatabaseWorker worker = new DatabaseWorker("emf-db-test", 1_000);
        CountDownLatch blockerStarted = new CountDownLatch(1);
        CountDownLatch releaseBlocker = new CountDownLatch(1);

        worker.execute(() -> {
            blockerStarted.countDown();
            try {
                releaseBlocker.await(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        assertTrue(blockerStarted.await(5, TimeUnit.SECONDS));

        worker.execute(() -> { });
        assertTrue(worker.getPendingTasks() >= 1);

        releaseBlocker.countDown();
        assertTrue(worker.shutdown(5, TimeUnit.SECONDS));
        assertEquals(0, worker.getPendingTasks());
    }
}
