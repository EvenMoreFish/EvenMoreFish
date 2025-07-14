package com.oheers.fish.plugin;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.addons.InternalAddonLoader;
import com.oheers.fish.api.FileUtil;
import com.oheers.fish.api.addons.AddonManager;
import com.oheers.fish.config.MainConfig;

import java.io.File;
import java.io.IOException;
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
        saveDefaultAddons();

        this.addonManager = new AddonManager(plugin);

        // Load external addons
        this.addonManager.load();

        // Load internal addons
        new InternalAddonLoader(plugin).load();
    }

    private void saveDefaultAddons() {
        if (!MainConfig.getInstance().useAdditionalAddons()) {
            return;
        }

        final Set<String> addons = getAddonFileNames();

        if (addons.isEmpty()) {
            plugin.getLogger().warning(() -> "Could not find any addons.");
            return;
        }

        for (String fileName: addons) {
            File addonFile = new File(plugin.getDataFolder(), "addons/" + fileName);
            try {
                plugin.saveResource("addons/" + fileName, true);
                final File jarFile = new File(plugin.getDataFolder(), "addons/" + fileName.replace(".addon", ".jar"));
                if (addonFile.renameTo(jarFile)) {
                    plugin.debug(Level.INFO, "Converted addon file: " + fileName);
                }
            } catch (IllegalArgumentException e) {
                plugin.debug(Level.WARNING, "Default addon not found: " + fileName);
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
