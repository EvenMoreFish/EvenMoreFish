package com.oheers.fish.database.execute;

import com.oheers.fish.EvenMoreFish;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Owns database task execution for the plugin.
 *
 * <p>The initial implementation intentionally uses one FIFO lane for both
 * reads and writes. This preserves existing write ordering and avoids
 * read-after-write races while higher-level call sites are migrated to async
 * APIs. A separate read pool can be added later behind this same abstraction.
 */
public class DatabaseWorker implements Executor, AutoCloseable {
    private static final AtomicInteger THREAD_COUNTER = new AtomicInteger(1);
    private static final int DEFAULT_BACKLOG_WARNING_THRESHOLD = 1_000;

    private final ExecutorService executor;
    private final AtomicInteger pendingTasks = new AtomicInteger();
    private final AtomicBoolean backlogWarningActive = new AtomicBoolean();
    private final int backlogWarningThreshold;

    public DatabaseWorker() {
        this("emf-db-worker", DEFAULT_BACKLOG_WARNING_THRESHOLD);
    }

    public DatabaseWorker(@NotNull String threadNamePrefix, int backlogWarningThreshold) {
        if (backlogWarningThreshold <= 0) {
            throw new IllegalArgumentException("backlogWarningThreshold must be > 0");
        }
        this.backlogWarningThreshold = backlogWarningThreshold;
        this.executor = Executors.newSingleThreadExecutor(runnable -> {
            Thread thread = new Thread(runnable);
            thread.setName(threadNamePrefix + "-" + THREAD_COUNTER.getAndIncrement());
            thread.setDaemon(true);
            return thread;
        });
    }

    @Override
    public void execute(@NotNull Runnable task) {
        Objects.requireNonNull(task, "task");
        submit(() -> runLogged(task));
    }

    public @NotNull CompletableFuture<Void> write(@NotNull Runnable work) {
        Objects.requireNonNull(work, "work");
        CompletableFuture<Void> future = new CompletableFuture<>();
        submit(() -> {
            try {
                work.run();
                future.complete(null);
            } catch (Exception e) {
                logger().log(Level.SEVERE, "Async database write failed.", e);
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    public <T> @NotNull CompletableFuture<T> writeResult(@NotNull Supplier<T> work) {
        return submitFuture(work, "Async database write failed.");
    }

    public <T> @NotNull CompletableFuture<T> query(@NotNull Supplier<T> work) {
        return submitFuture(work, "Async database query failed.");
    }

    public int getPendingTasks() {
        return pendingTasks.get();
    }

    /**
     * Stops accepting new tasks and blocks until every queued database task has
     * completed, or the timeout elapses.
     *
     * @return true if the queue drained fully, false if tasks were dropped
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
        logger().warning("Database worker did not drain in time; " + dropped.size() + " pending task(s) were dropped.");
        return false;
    }

    @Override
    public void close() {
        executor.shutdown();
    }

    private <T> CompletableFuture<T> submitFuture(Supplier<T> work, String failureMessage) {
        Objects.requireNonNull(work, "work");
        CompletableFuture<T> future = new CompletableFuture<>();
        submit(() -> {
            try {
                future.complete(work.get());
            } catch (Exception e) {
                logger().log(Level.SEVERE, failureMessage, e);
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    private void submit(Runnable task) {
        int backlog = pendingTasks.incrementAndGet();
        warnIfBacklogged(backlog);
        try {
            executor.execute(() -> {
                try {
                    task.run();
                } finally {
                    int remaining = pendingTasks.decrementAndGet();
                    if (remaining < backlogWarningThreshold) {
                        backlogWarningActive.set(false);
                    }
                }
            });
        } catch (RejectedExecutionException e) {
            pendingTasks.decrementAndGet();
            // Queue already shut down (plugin disable in progress). Run inline
            // so writes submitted during disable are not silently lost.
            task.run();
        }
    }

    private void warnIfBacklogged(int backlog) {
        if (backlog >= backlogWarningThreshold && backlogWarningActive.compareAndSet(false, true)) {
            logger().warning("Database worker backlog has reached " + backlog + " pending task(s).");
        }
    }

    private void runLogged(Runnable task) {
        try {
            task.run();
        } catch (Exception e) {
            logger().log(Level.SEVERE, "Async database task failed.", e);
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
