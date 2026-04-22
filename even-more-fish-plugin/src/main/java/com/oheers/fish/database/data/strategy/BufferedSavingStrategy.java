package com.oheers.fish.database.data.strategy;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public class BufferedSavingStrategy<T> implements DataSavingStrategy<T> {
    private final Consumer<T> createSaveFunction;
    private final Consumer<Collection<T>> batchSaveFunction;
    private final boolean writeThroughOnCreate;

    public BufferedSavingStrategy(
        Consumer<T> createSaveFunction,
        Consumer<Collection<T>> batchSaveFunction,
        boolean writeThroughOnCreate
    ) {
        this.createSaveFunction = createSaveFunction;
        this.batchSaveFunction = batchSaveFunction;
        this.writeThroughOnCreate = writeThroughOnCreate;
    }

    @Override
    public void save(T data) {
        if (createSaveFunction != null) {
            createSaveFunction.accept(data);
            return;
        }

        batchSaveFunction.accept(List.of(data));
    }

    @Override
    public void saveAll(Collection<T> data) {
        if (data.isEmpty()) {
            return;
        }

        batchSaveFunction.accept(data);
    }

    @Override
    public boolean writesThrough() {
        return false;
    }

    @Override
    public boolean writesThroughOnCreate() {
        return writeThroughOnCreate;
    }
}
