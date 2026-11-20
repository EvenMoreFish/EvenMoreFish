package com.oheers.fish.database.execute;

import com.oheers.fish.EvenMoreFish;
import org.jspecify.annotations.NonNull;

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
import java.util.function.Consumer;
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

    public DatabaseWorker(@NonNull String threadNamePrefix, int backlogWarningThreshold) {
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
        debug("Database worker started with thread prefix '%s'.".formatted(threadNamePrefix));
    }

    @Override
    public void execute(@NonNull Runnable task) {
        Objects.requireNonNull(task, "task");
        submit(() -> runLogged(task), ignored -> { });
    }

    public @NonNull CompletableFuture<Void> write(@NonNull Runnable work) {
        Objects.requireNonNull(work, "work");
        CompletableFuture<Void> future = new CompletableFuture<>();
        submitFutureTask(() -> {
            try {
                work.run();
                future.complete(null);
            } catch (Throwable e) {
                logger().log(Level.SEVERE, "Async database write failed.", e);
                future.completeExceptionally(e);
            }
        }, future);
        return future;
    }

    public <T> @NonNull CompletableFuture<T> writeResult(@NonNull Supplier<T> work) {
        return submitFuture(work, "Async database write failed.");
    }

    public <T> @NonNull CompletableFuture<T> query(@NonNull Supplier<T> work) {
        return submitFuture(work, "Async database query failed.");
    }

    public int getPendingTasks() {
        return pendingTasks.get();
    }

    /**
     * Stops accepting new tasks and blocks until every queued database task has
     * completed, or the timeout elapses.
     *
     * @return true if the queue drained fully, false if the timeout or interruption forced cancellation
     */
    public boolean shutdown(long timeout, @NonNull TimeUnit unit) {
        executor.shutdown();
        try {
            if (executor.awaitTermination(timeout, unit)) {
                return true;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        List<Runnable> dropped = executor.shutdownNow();
        RejectedExecutionException failure = new RejectedExecutionException("Database worker shutdown timed out");
        dropped.forEach(task -> ((ManagedTask) task).cancel(failure));
        logger().warning("Database worker did not drain in time; " + dropped.size() + " pending task(s) were dropped.");
        return false;
    }

    public boolean awaitTermination(long timeout, @NonNull TimeUnit unit) throws InterruptedException {
        return executor.awaitTermination(timeout, unit);
    }

    @Override
    public void close() {
        executor.shutdown();
    }

    private <T> CompletableFuture<T> submitFuture(Supplier<T> work, String failureMessage) {
        Objects.requireNonNull(work, "work");
        CompletableFuture<T> future = new CompletableFuture<>();
        submitFutureTask(() -> {
            try {
                future.complete(work.get());
            } catch (Throwable e) {
                logger().log(Level.SEVERE, failureMessage, e);
                future.completeExceptionally(e);
            }
        }, future);
        return future;
    }

    private void submitFutureTask(Runnable task, CompletableFuture<?> future) {
        try {
            submit(task, future::completeExceptionally);
        } catch (RejectedExecutionException ignored) {
            // The future already carries the rejection.
        }
    }

    private void submit(Runnable task, Consumer<Throwable> onDropped) {
        int backlog = pendingTasks.incrementAndGet();
        warnIfBacklogged(backlog);
        ManagedTask managedTask = new ManagedTask(task, onDropped);
        try {
            executor.execute(managedTask);
        } catch (RejectedExecutionException e) {
            managedTask.cancel(e);
            throw e;
        }
    }

    private void taskCompleted() {
        int remaining = pendingTasks.decrementAndGet();
        if (remaining < backlogWarningThreshold) {
            backlogWarningActive.set(false);
        }
    }

    private final class ManagedTask implements Runnable {
        private final Runnable task;
        private final Consumer<Throwable> onDropped;
        private final AtomicBoolean startedOrCancelled = new AtomicBoolean();

        private ManagedTask(Runnable task, Consumer<Throwable> onDropped) {
            this.task = task;
            this.onDropped = onDropped;
        }

        @Override
        public void run() {
            if (!startedOrCancelled.compareAndSet(false, true)) {
                return;
            }
            try {
                task.run();
            } finally {
                taskCompleted();
            }
        }

        private void cancel(Throwable failure) {
            if (!startedOrCancelled.compareAndSet(false, true)) {
                return;
            }
            try {
                onDropped.accept(failure);
            } finally {
                taskCompleted();
            }
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
            Logger pluginLogger = EvenMoreFish.getInstance().getLogger();
            return pluginLogger == null ? fallbackLogger() : pluginLogger;
        } catch (RuntimeException ignored) {
            // Unit tests and early bootstrap paths may not have an initialized plugin singleton.
            return fallbackLogger();
        }
    }

    private Logger fallbackLogger() {
        return Logger.getLogger("EvenMoreFish");
    }

    private void debug(@NonNull String message) {
        try {
            EvenMoreFish.getInstance().debug(message);
        } catch (RuntimeException ignored) {
            // Unit tests and early bootstrap paths may not have an initialized plugin singleton.
        }
    }
}
