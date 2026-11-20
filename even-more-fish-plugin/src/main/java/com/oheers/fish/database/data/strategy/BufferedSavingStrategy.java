package com.oheers.fish.database.data.strategy;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Buffers updates for periodic batch flushing. When an executor is supplied,
 * both the write-through-on-create path and batch flushes are dispatched to
 * it so callers (the server thread flushing on player quit, or the periodic
 * flush scheduler) never block on database I/O.
 */
public class BufferedSavingStrategy<T> implements DataSavingStrategy<T> {
    private static final Logger LOGGER = Logger.getLogger(BufferedSavingStrategy.class.getName());
    private final Consumer<T> createSaveFunction;
    private final Consumer<Collection<T>> batchSaveFunction;
    private final boolean writeThroughOnCreate;
    private final Executor executor;

    public BufferedSavingStrategy(
        Consumer<T> createSaveFunction,
        Consumer<Collection<T>> batchSaveFunction,
        boolean writeThroughOnCreate
    ) {
        this(createSaveFunction, batchSaveFunction, writeThroughOnCreate, null);
    }

    public BufferedSavingStrategy(
        Consumer<T> createSaveFunction,
        Consumer<Collection<T>> batchSaveFunction,
        boolean writeThroughOnCreate,
        Executor executor
    ) {
        this.createSaveFunction = createSaveFunction;
        this.batchSaveFunction = batchSaveFunction;
        this.writeThroughOnCreate = writeThroughOnCreate;
        this.executor = executor;
    }

    @Override
    public void save(T data) {
        saveAcknowledged(data).whenComplete((unused, failure) -> {
            if (failure != null) {
                LOGGER.log(Level.SEVERE, "Buffered database save failed.", failure);
            }
        });
    }

    @Override
    public CompletionStage<Void> saveAcknowledged(T data) {
        T captured = snapshot(data);
        if (createSaveFunction != null) {
            return dispatch(() -> createSaveFunction.accept(captured));
        }

        return dispatch(() -> batchSaveFunction.accept(List.of(captured)));
    }

    @Override
    public void saveAll(Collection<T> data) {
        saveAllAcknowledged(data).whenComplete((unused, failure) -> {
            if (failure != null) {
                LOGGER.log(Level.SEVERE, "Buffered database batch save failed.", failure);
            }
        });
    }

    @Override
    public CompletionStage<Void> saveAllAcknowledged(Collection<T> data) {
        if (data.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }

        // Snapshot before handing off so the batch is stable even if the
        // caller's collection is mutated after this call returns.
        List<T> snapshot = data.stream().map(this::snapshot).toList();
        return dispatch(() -> batchSaveFunction.accept(snapshot));
    }

    @Override
    public boolean writesThrough() {
        return false;
    }

    @Override
    public boolean writesThroughOnCreate() {
        return writeThroughOnCreate;
    }

    private CompletionStage<Void> dispatch(Runnable write) {
        if (executor != null) {
            return CompletableFuture.runAsync(write, executor);
        }
        write.run();
        return CompletableFuture.completedFuture(null);
    }
}
