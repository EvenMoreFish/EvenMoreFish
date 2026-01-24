package com.oheers.fish.database.execute;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public final class DbExecutor implements AutoCloseable {
    private final ExecutorService executor;

    public DbExecutor(int threads, String threadNamePrefix) {
        if (threads <= 0) {
            throw new IllegalArgumentException("threads must be > 0");
        }
        this.executor = Executors.newFixedThreadPool(threads, namedThreadFactory(threadNamePrefix));
    }

    public <T> CompletableFuture<T> queryAsync(Supplier<T> work) {
        Objects.requireNonNull(work, "work");
        return CompletableFuture.supplyAsync(work, executor);
    }

    public CompletableFuture<Integer> updateAsync(Supplier<Integer> work) {
        Objects.requireNonNull(work, "work");
        return CompletableFuture.supplyAsync(work, executor);
    }

    public CompletableFuture<Void> runAsync(Runnable work) {
        Objects.requireNonNull(work, "work");
        return CompletableFuture.runAsync(work, executor);
    }

    public ExecutorService getExecutor() {
        return executor;
    }

    @Override
    public void close() {
        executor.shutdown();
    }

    private static ThreadFactory namedThreadFactory(String prefix) {
        String safePrefix = (prefix == null || prefix.isBlank()) ? "emf-db" : prefix.trim();
        AtomicInteger counter = new AtomicInteger(1);
        return runnable -> {
            Thread thread = new Thread(runnable);
            thread.setName(safePrefix + "-" + counter.getAndIncrement());
            thread.setDaemon(true);
            return thread;
        };
    }
}
