package com.oheers.fish.database.data.manager;


import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.database.data.strategy.DataSavingStrategy;
import com.oheers.fish.database.execute.DatabaseWorker;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DataManager<T> {
    private final Map<String, T> cache = new ConcurrentHashMap<>();
    private final Object dirtyLock = new Object();
    private final Map<String, Long> dirtyVersions = new LinkedHashMap<>();
    private final Map<String, Long> inFlightVersions = new LinkedHashMap<>();
    private long nextVersion;
    private boolean acceptingUpdates = true;
    private final DataSavingStrategy<T> savingStrategy;
    private final Function<String, T> defaultLoader;
    private final ScheduledExecutorService flushScheduler;

    public DataManager(DataSavingStrategy<T> savingStrategy, Function<String, T> defaultLoader) {
        this(savingStrategy, defaultLoader, null, null);
    }

    public DataManager(
        DataSavingStrategy<T> savingStrategy,
        Function<String, T> defaultLoader,
        Long flushInterval,
        TimeUnit flushUnit
    ) {
        this.savingStrategy = savingStrategy;
        this.defaultLoader = defaultLoader;
        this.flushScheduler = createFlushScheduler(flushInterval, flushUnit);
    }

    public T get(String key, Function<String, T> loader) {
        T cachedValue = cache.get(key);
        if (cachedValue != null) {
            return cachedValue;
        }

        T loadedValue = loader.apply(key);
        if (loadedValue == null) {
            return null;
        }

        T previousValue = cache.putIfAbsent(key, loadedValue);
        return previousValue == null ? loadedValue : previousValue;
    }

    public CompletableFuture<T> getAsync(String key, Function<String, T> loader, DatabaseWorker databaseWorker) {
        T cachedValue = cache.get(key);
        if (cachedValue != null) {
            return CompletableFuture.completedFuture(cachedValue);
        }
        return databaseWorker.query(() -> get(key, loader));
    }

    public T getOrCreate(String key, Function<String, T> loader, Supplier<T> fallbackValueSupplier) {
        T loadedValue = get(key, loader);
        if (loadedValue != null) {
            debug("Using loaded value.");
            return loadedValue;
        }

        T cachedValue = cache.get(key);
        if (cachedValue != null) {
            debug("Using cached value.");
            return cachedValue;
        }

        T fallbackValue = fallbackValueSupplier.get();
        if (fallbackValue == null) {
            return null;
        }
        long version;
        T savedValue = fallbackValue;
        synchronized (dirtyLock) {
            requireAcceptingUpdates();
            T existing = cache.putIfAbsent(key, fallbackValue);
            if (existing != null) {
                return existing;
            }
            version = markDirty(key);
            if (savingStrategy.writesThroughOnCreate()) {
                savedValue = savingStrategy.snapshot(fallbackValue);
                inFlightVersions.put(key, version);
                saveCreatedValue(key, version, savedValue);
            }
        }
        debug("Using fallback value.");
        return fallbackValue;
    }

    public CompletableFuture<T> getOrCreateAsync(
        String key,
        Function<String, T> loader,
        Supplier<T> fallbackValueSupplier,
        DatabaseWorker databaseWorker
    ) {
        T cachedValue = cache.get(key);
        if (cachedValue != null) {
            return CompletableFuture.completedFuture(cachedValue);
        }
        return databaseWorker.query(() -> getOrCreate(key, loader, fallbackValueSupplier));
    }

    public T get(String key) {
        if (defaultLoader == null) {
            throw new IllegalStateException("No default loader configured");
        }
        return get(key, defaultLoader);
    }

    public CompletableFuture<T> getAsync(String key, DatabaseWorker databaseWorker) {
        if (defaultLoader == null) {
            throw new IllegalStateException("No default loader configured");
        }
        return getAsync(key, defaultLoader, databaseWorker);
    }

    public T peek(String key) {
        return cache.get(key);
    }

    public void cacheLoadedValue(String key, T data) {
        if (data != null) {
            cache.putIfAbsent(key, data);
        }
    }

    public void update(String key, T data) {
        synchronized (dirtyLock) {
            requireAcceptingUpdates();
            cache.put(key, data);
            if (!savingStrategy.writesThrough()) {
                markDirty(key);
            } else {
                savingStrategy.save(data);
            }
        }
    }

    public void updateAll(Map<String, T> data) {
        synchronized (dirtyLock) {
            requireAcceptingUpdates();
            cache.putAll(data);
            if (!savingStrategy.writesThrough()) {
                data.keySet().forEach(this::markDirty);
            } else {
                savingStrategy.saveAll(data.values());
            }
        }
    }

    public void flush() {
        flushDirty(false);
    }

    public int getPendingPersistenceCount() {
        synchronized (dirtyLock) {
            return dirtyVersions.size();
        }
    }

    public void shutdown() {
        quiesce();
        flushDirty(true);
    }

    public void quiesce() {
        synchronized (dirtyLock) {
            acceptingUpdates = false;
        }
        if (flushScheduler != null) {
            flushScheduler.shutdown();
        }
    }

    private void requireAcceptingUpdates() {
        if (!acceptingUpdates) {
            throw new IllegalStateException("Data manager is shutting down; update was rejected");
        }
    }

    public void clear() {
        synchronized (dirtyLock) {
            cache.clear();
            dirtyVersions.clear();
            inFlightVersions.clear();
        }
    }

    private void flushDirty(boolean finalFlush) {
        Map<String, DirtyValue<T>> batch = new LinkedHashMap<>();
        synchronized (dirtyLock) {
            if (!finalFlush && !acceptingUpdates) {
                return;
            }
            for (Map.Entry<String, Long> entry : dirtyVersions.entrySet()) {
                String key = entry.getKey();
                if (inFlightVersions.containsKey(key)) {
                    continue;
                }
                T value = cache.get(key);
                if (value != null) {
                    long version = entry.getValue();
                    batch.put(key, new DirtyValue<>(version, savingStrategy.snapshot(value)));
                    inFlightVersions.put(key, version);
                }
            }
            if (batch.isEmpty()) {
                return;
            }
            try {
                savingStrategy.saveAllAcknowledged(batch.values().stream().map(DirtyValue::value).toList())
                    .whenComplete((unused, failure) -> completeBatch(batch, failure));
            } catch (RuntimeException exception) {
                completeBatch(batch, exception);
            }
        }
    }

    private void saveCreatedValue(String key, long version, T value) {
        Map<String, DirtyValue<T>> batch = Map.of(key, new DirtyValue<>(version, value));
        try {
            savingStrategy.saveAcknowledged(value)
                .whenComplete((unused, failure) -> completeBatch(batch, failure));
        } catch (RuntimeException exception) {
            completeBatch(batch, exception);
        }
    }

    private void completeBatch(Map<String, DirtyValue<T>> batch, Throwable failure) {
        synchronized (dirtyLock) {
            batch.forEach((key, submitted) -> {
                inFlightVersions.remove(key, submitted.version());
                if (failure == null) {
                    dirtyVersions.remove(key, submitted.version());
                }
            });
        }
        if (failure != null) {
            LOGGER.log(Level.SEVERE, "Database save failed for " + batch.size() + " cached value(s); they remain dirty for retry.", failure);
        }
    }

    private long markDirty(String key) {
        long version = ++nextVersion;
        dirtyVersions.put(key, version);
        return version;
    }

    private ScheduledExecutorService createFlushScheduler(Long flushInterval, TimeUnit flushUnit) {
        if (flushInterval == null || flushUnit == null || flushInterval <= 0) {
            return null;
        }

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable);
            thread.setName("emf-data-flush-" + THREAD_COUNTER.getAndIncrement());
            thread.setDaemon(true);
            return thread;
        });
        scheduler.scheduleAtFixedRate(() -> flushDirty(false), flushInterval, flushInterval, flushUnit);
        return scheduler;
    }

    private void debug(String message) {
        try {
            EvenMoreFish plugin = EvenMoreFish.getInstance();
            plugin.debug(message);
        } catch (RuntimeException ignored) {
            // Unit tests and early bootstrap paths may not have an initialized plugin singleton.
        }
    }

    private static final AtomicInteger THREAD_COUNTER = new AtomicInteger(1);
    private static final Logger LOGGER = Logger.getLogger(DataManager.class.getName());

    private record DirtyValue<T>(long version, T value) {}
}
