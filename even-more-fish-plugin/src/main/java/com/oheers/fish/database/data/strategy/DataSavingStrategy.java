package com.oheers.fish.database.data.strategy;

import java.util.Collection;

public interface DataSavingStrategy<T> {
    void save(T data);

    void saveAll(Collection<T> data);

    default boolean writesThrough() {
        return true;
    }

    default boolean writesThroughOnCreate() {
        return writesThrough();
    }
}
