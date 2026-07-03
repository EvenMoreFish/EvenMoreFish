package com.oheers.fish.plugin;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.addons.InternalAddonLoader;
import com.oheers.fish.api.FileUtil;
import com.oheers.fish.api.addons.AddonManager;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.fishing.processors.LavaFishingProcessor;
import org.bukkit.Bukkit;
import org.evenmorefish.dimensionfishing.DimensionFishing;
import org.evenmorefish.dimensionfishing.hooks.evenmorefish.VoidFishingProcessor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.Set;
import java.util.logging.Level;

public class IntegrationManager {
    private final EvenMoreFish plugin;
    private AddonManager addonManager;

    public IntegrationManager(EvenMoreFish plugin) {
        this.plugin = plugin;
    }

    public void loadAddons() {
        this.addonManager = new AddonManager(plugin);

        saveDefaultAddons();

        // Load external addons
        this.addonManager.load();

        // Load internal addons
        new InternalAddonLoader(plugin).load();
    }

    public void unloadAddons() {
        this.addonManager.unload();
    }

    public void setupDimensionFishing() {
        Bukkit.getPluginManager().registerEvents(new LavaFishingProcessor(), plugin);
        Bukkit.getPluginManager().registerEvents(new VoidFishingProcessor(), plugin);
        DimensionFishing.getInstance().reload(null);
    }

    private void saveDefaultAddons() {
        if (!MainConfig.getInstance().useAdditionalAddons()) {
            return;
        }

        final Set<String> addons = getAddonFileNames();
        if (addons.isEmpty()) {
            plugin.getLogger().warning("Could not find any addons.");
            return;
        }

        final Path addonsDir = plugin.getDataFolder().toPath().resolve("addons");

        for (String fileName : addons) {
            try {
                final String resourcePath = "addons/" + fileName;
                plugin.saveResource(resourcePath, true);

                final Path addonFile = addonsDir.resolve(fileName);
                final Path jarFile = addonsDir.resolve(fileName.replace(".addon", ".jar"));

                Files.move(addonFile, jarFile, StandardCopyOption.REPLACE_EXISTING);
                plugin.debug(Level.INFO, "Converted addon file: " + fileName);
            } catch (IllegalArgumentException e) {
                plugin.debug(Level.WARNING, "Default addon not found: " + fileName);
            } catch (IOException e) {
                plugin.debug(Level.WARNING, "Failed to rename addon file: " + fileName, e);
            }
        }
    }

    private Set<String> getAddonFileNames() {
        try {
            return FileUtil.getAddonFilenames(getClass(),"addons");
        } catch (IOException e) {
            return Collections.emptySet();
        }
    }

    public AddonManager getAddonManager() {
        return addonManager;
    }
}
