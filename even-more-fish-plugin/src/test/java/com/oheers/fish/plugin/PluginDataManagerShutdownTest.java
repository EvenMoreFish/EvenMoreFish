package com.oheers.fish.plugin;

import com.oheers.fish.database.execute.DatabaseWorker;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PluginDataManagerShutdownTest {

    @Test
    void timedOutWorkerKeepsPoolOpenUntilRunningWriteStops() throws Exception {
        DatabaseWorker worker = new DatabaseWorker("emf-shutdown-test", 1_000);
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        CountDownLatch poolClosed = new CountDownLatch(1);
        AtomicBoolean poolOpenDuringWrite = new AtomicBoolean();

        CompletableFuture<Void> write = worker.write(() -> {
            started.countDown();
            while (true) {
                try {
                    release.await();
                    break;
                } catch (InterruptedException ignored) {
                    // Model a database operation that does not stop immediately.
                }
            }
            poolOpenDuringWrite.set(poolClosed.getCount() == 1);
        });
        assertTrue(started.await(5, TimeUnit.SECONDS));
        try {
            assertFalse(worker.shutdown(10, TimeUnit.MILLISECONDS));
            PluginDataManager.closeAfterWorkerStops(worker, poolClosed::countDown, Logger.getLogger("test"));
            assertEquals(1, poolClosed.getCount());
        } finally {
            release.countDown();
        }

        write.get(5, TimeUnit.SECONDS);
        assertTrue(poolClosed.await(5, TimeUnit.SECONDS));
        assertTrue(poolOpenDuringWrite.get());
    }
}
