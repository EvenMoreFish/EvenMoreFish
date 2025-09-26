package com.oheers.fish.api;

import com.oheers.fish.api.plugin.EMFPlugin;
import org.bukkit.configuration.InvalidConfigurationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.function.Predicate;

public abstract class AbstractFileBasedManager<T> {
    protected AbstractFileBasedManager<?> dependency;

    protected final TreeMap<String, T> itemMap;
    private boolean loaded = false;

    protected AbstractFileBasedManager() {
        this.itemMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        this.dependency = null;
        performPreLoadConversions();
    }

    protected AbstractFileBasedManager(AbstractFileBasedManager<?> dependency) {
        this.itemMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        this.dependency = dependency;
        performPreLoadConversions();
    }

    // Lifecycle methods
    public void load() {
        if (isLoaded()) {
            return;
        }

        if (dependency != null && !dependency.isLoaded()) {
            throw new IllegalStateException(
                    this.getClass().getSimpleName() + " cannot load before "
                            + dependency.getClass().getSimpleName()
            );
        }

        loadItems();
        logLoadedItems();
        loaded = true;
    }

    public void reload() {
        if (!isLoaded()) {
            return;
        }
        clearMap(true);
        loadItems();
        logLoadedItems();
    }

    public void unload() {
        if (!isLoaded()) {
            return;
        }
        clearMap(false);
        loaded = false;
    }


    public boolean isLoaded() {
        return loaded;
    }

    // Getters
    public @NotNull TreeMap<String, T> getItemMap() {
        return itemMap;
    }

    public @Nullable T getItem(@Nullable String id) {
        if (id == null || id.isEmpty()) {
            return null;
        }
        return itemMap.get(id);
    }

    // Loading implementation
    protected abstract void loadItems();

    protected abstract void logLoadedItems();

    protected abstract void performPreLoadConversions();

    protected void clearMap(boolean reload) {
        itemMap.clear();
    }

    // Helper method for loading items from files
    protected <U> void loadItemsFromFiles(
            String folderName,
            FunctionWithException<File, U, InvalidConfigurationException> itemLoader,
            Function<U, String> idExtractor,
            Predicate<U> shouldSkipItem
    ) {
        File itemsFolder = new File(EMFPlugin.getInstance().getDataFolder(), folderName);
        if (!itemsFolder.exists()) {
            FileUtil.loadDefaultFiles(folderName, itemsFolder);
        }

        FileUtil.regenExampleFiles(folderName, itemsFolder);
        List<File> itemFiles = FileUtil.getFilesInDirectory(itemsFolder, true, true);

        if (itemFiles.isEmpty()) {
            EMFPlugin.getInstance().debug("No files found in directory: " + itemsFolder);
            return;
        }

        itemFiles.stream()
                .map(file -> {
                    try {
                        return itemLoader.apply(file);
                    } catch (InvalidConfigurationException e) {
                        Logging.warn("Invalid configuration in file: " + file.getName());
                        Logging.warn(e.getMessage());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .filter(item -> !shouldSkipItem.test(item))
                .forEach(item -> {
                    String id = idExtractor.apply(item);
                    if (itemMap.containsKey(id)) {
                        Logging.warn("An item with the ID '" + id + "' already exists! Skipping.");
                        return;
                    }
                    itemMap.put(id, (T) item);
                });
    }

    @FunctionalInterface
    public interface FunctionWithException<T, R, E extends Exception> {
        R apply(T t) throws E;
    }
}
