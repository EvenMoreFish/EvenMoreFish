package com.oheers.fish.database.data;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DatabaseWriteQueueTest {

    @Test
    void executesTasksOffTheCallerThreadInSubmissionOrder() throws Exception {
        DatabaseWriteQueue queue = new DatabaseWriteQueue();
        List<Integer> order = new CopyOnWriteArrayList<>();
        AtomicReference<Thread> workerThread = new AtomicReference<>();
        CountDownLatch done = new CountDownLatch(1);

        IntStream.range(0, 100).forEach(i -> queue.execute(() -> order.add(i)));
        queue.execute(() -> {
            workerThread.set(Thread.currentThread());
            done.countDown();
        });

        assertTrue(done.await(5, TimeUnit.SECONDS));
        assertEquals(IntStream.range(0, 100).boxed().toList(), order);
        assertNotEquals(Thread.currentThread(), workerThread.get());
        assertTrue(workerThread.get().getName().startsWith("emf-db-writer-"));
        assertTrue(queue.shutdown(5, TimeUnit.SECONDS));
    }

    @Test
    void shutdownDrainsAllQueuedWrites() {
        DatabaseWriteQueue queue = new DatabaseWriteQueue();
        List<Integer> executed = new CopyOnWriteArrayList<>();

        IntStream.range(0, 50).forEach(i -> queue.execute(() -> {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            executed.add(i);
        }));

        assertTrue(queue.shutdown(10, TimeUnit.SECONDS));
        assertEquals(50, executed.size());
    }

    @Test
    void writeSubmittedAfterShutdownRunsInlineInsteadOfBeingLost() {
        DatabaseWriteQueue queue = new DatabaseWriteQueue();
        assertTrue(queue.shutdown(5, TimeUnit.SECONDS));

        AtomicReference<Thread> executionThread = new AtomicReference<>();
        queue.execute(() -> executionThread.set(Thread.currentThread()));

        assertEquals(Thread.currentThread(), executionThread.get());
    }

    @Test
    void failingWriteDoesNotStopSubsequentWrites() {
        DatabaseWriteQueue queue = new DatabaseWriteQueue();
        AtomicBoolean secondRan = new AtomicBoolean(false);

        queue.execute(() -> {
            throw new IllegalStateException("simulated write failure");
        });
        queue.execute(() -> secondRan.set(true));

        assertTrue(queue.shutdown(5, TimeUnit.SECONDS));
        assertTrue(secondRan.get());
    }
}
