package com.oheers.fish.database.data.strategy;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Persists each value as soon as it is saved. When an executor is supplied,
 * the write itself is dispatched to that executor so the caller (typically
 * the server thread) never blocks on database I/O; writes still happen in
 * submission order.
 */
public class ImmediateSavingStrategy<T> implements DataSavingStrategy<T> {
    private static final Logger LOGGER = Logger.getLogger(ImmediateSavingStrategy.class.getName());
    private final Consumer<T> saveFunction;
    private final Executor executor;

    public ImmediateSavingStrategy(Consumer<T> saveFunction) {
        this(saveFunction, null);
    }

    public ImmediateSavingStrategy(Consumer<T> saveFunction, Executor executor) {
        this.saveFunction = saveFunction;
        this.executor = executor;
    }

    @Override
    public void save(T data) {
        saveAcknowledged(data).whenComplete((unused, failure) -> {
            if (failure != null) {
                LOGGER.log(Level.SEVERE, "Immediate database save failed.", failure);
            }
        });
    }

    @Override
    public CompletionStage<Void> saveAcknowledged(T data) {
        if (executor != null) {
            return CompletableFuture.runAsync(() -> saveFunction.accept(data), executor);
        }
        saveFunction.accept(data);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void saveAll(Collection<T> data) {
        data.forEach(this::save);
    }

    @Override
    public CompletionStage<Void> saveAllAcknowledged(Collection<T> data) {
        CompletableFuture<?>[] writes = data.stream()
            .map(value -> saveAcknowledged(value).toCompletableFuture())
            .toArray(CompletableFuture[]::new);
        return CompletableFuture.allOf(writes);
    }
}
