package com.oheers.fish.database.data.strategy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class PeriodicSavingStrategy<T> implements DataSavingStrategy<T> {
    private final Consumer<Collection<T>> batchSaveFunction;
    private final Queue<T> pendingSaves = new ConcurrentLinkedQueue<>();
    private final ScheduledExecutorService scheduler;

    public PeriodicSavingStrategy(Consumer<Collection<T>> batchSaveFunction, long interval, TimeUnit unit) {
        this.batchSaveFunction = batchSaveFunction;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(namedDaemonThreadFactory("emf-db-save"));
        scheduler.scheduleAtFixedRate(this::flush, interval, interval, unit);
    }

    @Override
    public void save(T data) {
        pendingSaves.add(data); // Queue for batch save
    }

    @Override
    public void saveAll(Collection<T> data) {
        pendingSaves.addAll(data);
    }

    private void flush() {
        if (pendingSaves.isEmpty()) return;
        List<T> toSave = new ArrayList<>();
        while (!pendingSaves.isEmpty()) {
            toSave.add(pendingSaves.poll());
        }
        batchSaveFunction.accept(toSave); // Batch save
    }

    public void shutdown() {
        scheduler.shutdown();
        flush(); // Save remaining data
    }

    private static ThreadFactory namedDaemonThreadFactory(String prefix) {
        String safePrefix = (prefix == null || prefix.isBlank()) ? "emf-db-save" : prefix.trim();
        AtomicInteger counter = new AtomicInteger(1);
        return runnable -> {
            Thread thread = new Thread(runnable);
            thread.setName(safePrefix + "-" + counter.getAndIncrement());
            thread.setDaemon(true);
            return thread;
        };
    }
}
