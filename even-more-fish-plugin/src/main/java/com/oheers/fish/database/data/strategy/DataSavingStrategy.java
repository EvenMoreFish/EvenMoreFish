package com.oheers.fish.database.data.strategy;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public interface DataSavingStrategy<T> {
    void save(T data);

    void saveAll(Collection<T> data);

    /** Completes only after the submitted write has finished. */
    default CompletionStage<Void> saveAcknowledged(T data) {
        save(data);
        return CompletableFuture.completedFuture(null);
    }

    /** Completes only after the submitted batch has finished. */
    default CompletionStage<Void> saveAllAcknowledged(Collection<T> data) {
        saveAll(data);
        return CompletableFuture.completedFuture(null);
    }

    /** Capture mutable values before they are handed to an asynchronous writer. */
    default T snapshot(T data) {
        return data;
    }

    default boolean writesThrough() {
        return true;
    }

    default boolean writesThroughOnCreate() {
        return writesThrough();
    }
}
