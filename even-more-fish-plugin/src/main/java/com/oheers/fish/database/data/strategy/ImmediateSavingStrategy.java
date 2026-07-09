package com.oheers.fish.database.data.strategy;

import java.util.Collection;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

/**
 * Persists each value as soon as it is saved. When an executor is supplied,
 * the write itself is dispatched to that executor so the caller (typically
 * the server thread) never blocks on database I/O; writes still happen in
 * submission order.
 */
public class ImmediateSavingStrategy<T> implements DataSavingStrategy<T> {
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
        if (executor != null) {
            executor.execute(() -> saveFunction.accept(data));
            return;
        }
        saveFunction.accept(data);
    }

    @Override
    public void saveAll(Collection<T> data) {
        data.forEach(this::save);
    }
}
