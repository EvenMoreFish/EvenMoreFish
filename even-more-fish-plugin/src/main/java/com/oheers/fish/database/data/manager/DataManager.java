package com.oheers.fish.database.data.manager;


import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.database.data.strategy.DataSavingStrategy;

import java.util.Map;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;

public class DataManager<T> {
    private final Map<String, T> cache = new ConcurrentHashMap<>();
    private final Set<String> dirtyKeys = ConcurrentHashMap.newKeySet();
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
        if (fallbackValue != null) {
            cache.put(key, fallbackValue);
            if (savingStrategy.writesThroughOnCreate()) {
                savingStrategy.save(fallbackValue);
            } else {
                dirtyKeys.add(key);
            }
            debug("Using fallback value.");
        }
        return fallbackValue;
    }

    public T get(String key) {
        if (defaultLoader == null) {
            throw new IllegalStateException("No default loader configured");
        }
        return get(key, defaultLoader);
    }

    public void update(String key, T data) {
        cache.put(key, data);
        if (savingStrategy.writesThrough()) {
            savingStrategy.save(data);
            dirtyKeys.remove(key);
        } else {
            dirtyKeys.add(key);
        }
    }

    public void updateAll(Map<String, T> data) {
        cache.putAll(data);
        if (savingStrategy.writesThrough()) {
            savingStrategy.saveAll(data.values());
            dirtyKeys.removeAll(data.keySet());
        } else {
            dirtyKeys.addAll(data.keySet());
        }
    }

    public void flush() {
        flushDirty();
    }

    public void shutdown() {
        if (flushScheduler != null) {
            flushScheduler.shutdown();
        }
        flushDirty();
    }

    private void flushDirty() {
        if (dirtyKeys.isEmpty()) {
            return;
        }

        Map<String, T> dirtySnapshot = new LinkedHashMap<>();
        for (String key : dirtyKeys) {
            T value = cache.get(key);
            if (value != null) {
                dirtySnapshot.put(key, value);
            }
        }

        if (dirtySnapshot.isEmpty()) {
            dirtyKeys.clear();
            return;
        }

        Collection<T> dirtyValues = dirtySnapshot.values();
        savingStrategy.saveAll(dirtyValues);
        dirtyKeys.removeAll(dirtySnapshot.keySet());
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
        scheduler.scheduleAtFixedRate(this::flushDirty, flushInterval, flushInterval, flushUnit);
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
}
