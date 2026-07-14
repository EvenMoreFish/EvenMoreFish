package com.oheers.fish.database.data.strategy;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

/**
 * Buffers updates for periodic batch flushing. When an executor is supplied,
 * both the write-through-on-create path and batch flushes are dispatched to
 * it so callers (the server thread flushing on player quit, or the periodic
 * flush scheduler) never block on database I/O.
 */
public class BufferedSavingStrategy<T> implements DataSavingStrategy<T> {
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
        if (createSaveFunction != null) {
            dispatch(() -> createSaveFunction.accept(data));
            return;
        }

        dispatch(() -> batchSaveFunction.accept(List.of(data)));
    }

    @Override
    public void saveAll(Collection<T> data) {
        if (data.isEmpty()) {
            return;
        }

        // Snapshot before handing off so the batch is stable even if the
        // caller's collection is mutated after this call returns.
        List<T> snapshot = List.copyOf(data);
        dispatch(() -> batchSaveFunction.accept(snapshot));
    }

    @Override
    public boolean writesThrough() {
        return false;
    }

    @Override
    public boolean writesThroughOnCreate() {
        return writeThroughOnCreate;
    }

    private void dispatch(Runnable write) {
        if (executor != null) {
            executor.execute(write);
            return;
        }
        write.run();
    }
}
