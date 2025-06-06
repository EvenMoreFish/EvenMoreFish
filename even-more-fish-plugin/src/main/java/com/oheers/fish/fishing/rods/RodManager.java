package com.oheers.fish.fishing.rods;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.FileUtil;
import com.oheers.fish.utils.nbt.NbtKeys;
import com.oheers.fish.utils.nbt.NbtUtils;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;
import java.util.TreeMap;

public class RodManager {

    private static final RodManager instance = new RodManager();

    private final TreeMap<String, CustomRod> rodMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    private boolean loaded = false;

    private RodManager() {}

    public static RodManager getInstance() {
        return instance;
    }

    public void load() {
        if (isLoaded()) {
            return;
        }
        new RodConversion().performCheck();
        loadRods();
        logLoadedItems();
        loaded = true;
    }

    public void reload() {
        if (!isLoaded()) {
            return;
        }
        clearMap(true);
        loadRods();
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

    // Getters for Rods

    public @NotNull TreeMap<String, CustomRod> getRodMap() {
        return rodMap;
    }

    public @Nullable CustomRod getRod(@Nullable String id) {
        if (id == null || id.isEmpty()) {
            return null;
        }
        return rodMap.get(id);
    }

    public @Nullable CustomRod getRod(@NotNull ItemStack item) {
        String rodId = NbtUtils.getString(item, NbtKeys.EMF_ROD_ID);
        if (rodId != null) {
            return getRod(rodId);
        }
        // For the old nbt-rod tags
        if (NbtUtils.hasKey(item, NbtKeys.EMF_ROD_NBT)) {
            return getRod("default");
        }
        return null;
    }

    // Loading Things

    private void logLoadedItems() {
        EvenMoreFish.getInstance().getLogger().info("Loaded RodManager with " + rodMap.size() + " Custom Rods.");
    }

    private void loadRods() {
        File rodsFolder = new File(EvenMoreFish.getInstance().getDataFolder(), "rods");
        if (EvenMoreFish.getInstance().isFirstLoad()) {
            loadDefaultFiles(rodsFolder);
        }
        regenExampleFile(rodsFolder);
        List<File> rodFiles = FileUtil.getFilesInDirectory(rodsFolder, true, true);

        if (rodFiles.isEmpty()) {
            return;
        }

        rodFiles.forEach(file -> {
            EvenMoreFish.getInstance().debug("Loading " + file.getName() + " custom rod");
            CustomRod rod;
            try {
                rod = new CustomRod(file);
            } catch (InvalidConfigurationException exception) { // Skip invalid configs
                return;
            }
            // Skip disabled files.
            if (rod.isDisabled()) {
                return;
            }
            // Skip duplicate IDs
            String id = rod.getId();
            if (rodMap.containsKey(id)) {
                EvenMoreFish.getInstance().getLogger().warning("A custom rod with the id: " + id + " already exists! Skipping.");
                return;
            }
            if (rod.getRecipe() != null) {
                rod.getRecipe().register();
            }
            rodMap.put(id, rod);
        });
    }

    private void regenExampleFile(@NotNull File targetDirectory) {
        new File(targetDirectory, "_example.yml").delete();
        FileUtil.loadFileOrResource(targetDirectory, "_example.yml", "rods/_example.yml", EvenMoreFish.getInstance());
    }

    private void loadDefaultFiles(@NotNull File targetDirectory) {
        EvenMoreFish.getInstance().getLogger().info("Loading default rod configs.");
        FileUtil.loadFileOrResource(targetDirectory, "default.yml", "rods/default.yml", EvenMoreFish.getInstance());
    }

    private void clearMap(boolean reload) {
        if (reload) {
            rodMap.forEach((id, rod) -> {
                if (rod.getRecipe() != null) {
                    rod.getRecipe().unregister();
                }
            });
        }
        rodMap.clear();
    }

}
