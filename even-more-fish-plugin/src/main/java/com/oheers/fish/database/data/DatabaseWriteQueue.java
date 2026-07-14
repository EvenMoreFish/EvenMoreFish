package com.oheers.fish.database.data;

import com.oheers.fish.EvenMoreFish;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Serialises all database writes onto a single background thread so blocking
 * JDBC calls never run on the server thread. Tasks execute strictly in
 * submission order, which preserves the relative ordering of dependent writes
 * (e.g. a user row is created before that user's fish logs are inserted).
 */
public class DatabaseWriteQueue implements Executor {
    private static final AtomicInteger THREAD_COUNTER = new AtomicInteger(1);

    private final ExecutorService executor = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable);
        thread.setName("emf-db-writer-" + THREAD_COUNTER.getAndIncrement());
        thread.setDaemon(true);
        return thread;
    });

    @Override
    public void execute(@NotNull Runnable task) {
        try {
            executor.execute(() -> runLogged(task));
        } catch (RejectedExecutionException e) {
            // Queue already shut down (plugin disable in progress). Run inline
            // so the write is not lost; blocking the caller is fine here.
            runLogged(task);
        }
    }

    /**
     * Stops accepting new tasks and blocks until every queued write has
     * completed, or the timeout elapses. Call on plugin disable, before the
     * connection pool is closed.
     *
     * @return true if the queue drained fully, false if writes were dropped
     */
    public boolean shutdown(long timeout, @NotNull TimeUnit unit) {
        executor.shutdown();
        try {
            if (executor.awaitTermination(timeout, unit)) {
                return true;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        List<Runnable> dropped = executor.shutdownNow();
        logger().warning("Database write queue did not drain in time; " + dropped.size() + " pending write(s) were dropped.");
        return false;
    }

    private void runLogged(Runnable task) {
        try {
            task.run();
        } catch (Exception e) {
            logger().log(Level.SEVERE, "Async database write failed.", e);
        }
    }

    private Logger logger() {
        try {
            return EvenMoreFish.getInstance().getLogger();
        } catch (RuntimeException ignored) {
            // Unit tests and early bootstrap paths may not have an initialized plugin singleton.
            return Logger.getLogger("EvenMoreFish");
        }
    }
}
