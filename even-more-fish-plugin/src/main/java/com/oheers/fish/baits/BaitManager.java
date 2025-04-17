package com.oheers.fish.baits;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.FileUtil;
import com.oheers.fish.baits.configs.BaitConversions;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class BaitManager {
    
    private static BaitManager instance;

    private final TreeMap<String, Bait> baitMap;
    private boolean loaded;
    
    private BaitManager() {
        baitMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

        new BaitConversions().performCheck();
    }
    
    public static BaitManager getInstance() {
        if (instance == null) {
            instance = new BaitManager();
        }
        return instance;
    }
    
    public void load() {
        if (isLoaded()) {
            return;
        }
        loadBaits();
        logLoadedItems();
        loaded = true;
    }
    
    public void reload() {
        if (!isLoaded()) {
            return;
        }
        baitMap.clear();
        loadBaits();
        logLoadedItems();
    }
    
    public void unload() {
        if (!isLoaded()) {
            return;
        }
        baitMap.clear();
        loaded = false;
    }
    
    public boolean isLoaded() {
        return loaded;
    }

    public Map<String, Bait> getBaitMap() {
        return Map.copyOf(baitMap);
    }

    public @Nullable Bait getBait(@Nullable String baitName) {
        if (baitName == null) {
            return null;
        }
        return baitMap.get(baitName);
    }

    public @Nullable Bait getBait(@Nullable ItemStack itemStack) {
        if (itemStack == null) {
            return null;
        }
        return getBait(BaitNBTManager.getBaitName(itemStack));
    }

    // Loading things

    private void logLoadedItems() {
        EvenMoreFish.getInstance().getLogger().info("Loaded BaitManager with " + baitMap.size() + " Baits.");
    }

    private void loadBaits() {
        baitMap.clear();

        File baitsFolder = new File(EvenMoreFish.getInstance().getDataFolder(), "baits");
        if (EvenMoreFish.getInstance().isFirstLoad()) {
            loadDefaultFiles(baitsFolder);
        }
        regenExampleFile(baitsFolder);

        List<File> baitFiles = FileUtil.getFilesInDirectory(baitsFolder, true, true);
        if (baitFiles.isEmpty()) {
            return;
        }

        baitFiles.forEach(file -> {
            EvenMoreFish.getInstance().debug("Loading " + file.getName() + " bait");
            Bait bait;
            try {
                bait = new Bait(file);
            } catch (InvalidConfigurationException exception) {
                return;
            }
            if (bait.isDisabled()) {
                return;
            }
            String id = bait.getId();
            if (baitMap.containsKey(id)) {
                EvenMoreFish.getInstance().getLogger().warning("A bait with the id: " + id + " already exists! Skipping.");
                return;
            }
            baitMap.put(id, bait);
        });
    }

    private void regenExampleFile(@NotNull File targetDirectory) {
        new File(targetDirectory, "_example.yml").delete();
        FileUtil.loadFileOrResource(targetDirectory, "_example.yml", "baits/_example.yml", EvenMoreFish.getInstance());
    }

    private void loadDefaultFiles(@NotNull File targetDirectory) {
        EvenMoreFish.getInstance().getLogger().info("Loading default rarity configs.");
        FileUtil.loadFileOrResource(targetDirectory, "epic-elixir.yml", "baits/epic-elixir.yml", EvenMoreFish.getInstance());
        FileUtil.loadFileOrResource(targetDirectory, "fresh-water.yml", "baits/fresh-water.yml", EvenMoreFish.getInstance());
        FileUtil.loadFileOrResource(targetDirectory, "infinite-bait.yml", "baits/infinite-bait.yml", EvenMoreFish.getInstance());
        FileUtil.loadFileOrResource(targetDirectory, "legendary-lure.yml", "baits/legendary-lure.yml", EvenMoreFish.getInstance());
        FileUtil.loadFileOrResource(targetDirectory, "shrimp.yml", "baits/shrimp.yml", EvenMoreFish.getInstance());
        FileUtil.loadFileOrResource(targetDirectory, "stringy-worms.yml", "baits/stringy-worms.yml", EvenMoreFish.getInstance());
    }
    
}
