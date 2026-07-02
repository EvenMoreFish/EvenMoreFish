package com.oheers.fish.api.addons;

import com.oheers.fish.api.FileUtil;
import com.oheers.fish.api.plugin.EMFPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class AddonManager {
    private static final String ADDON_FOLDER = "addons";
    private final File folder;
    private final EMFPlugin plugin;
    private final List<AddonLoader> loadedAddons = new ArrayList<>();

    // Inject plugin instance for better testability
    public AddonManager(EMFPlugin plugin) {
        this.plugin = plugin;
        this.folder = new File(plugin.getDataFolder(), ADDON_FOLDER);

        // Clean up in case of a crash.
        FileUtil.deleteDirectory(this.folder);
        if (!this.folder.mkdirs()) {
            plugin.getLogger().warning("Could not create addons folder.");
        }
    }

    public void load() {
        List<File> jarFiles = FileUtil.getFilesInDirectoryWithExtension(
                folder, ".jar", true, true);

        jarFiles.forEach(this::processJar);
    }

    public void unload() {
        FileUtil.deleteDirectory(this.folder);
    }

    private void processJar(File jar) {
        List<Class<? extends AddonLoader>> classes = FileUtil.findClasses(jar, AddonLoader.class);
        if (classes.isEmpty()) {
            plugin.getLogger().warning(() -> "No AddonLoader classes found in %s".formatted(jar.getName()));
            return;
        }
        for (Class<? extends AddonLoader> clazz: classes) {
            loadAddonLoader(clazz, jar);
        }

    }

    private void loadAddonLoader(Class<? extends AddonLoader> clazz, File addonFile) {
        try {
            AddonLoader loaderInstance = clazz.getDeclaredConstructor(EMFPlugin.class, File.class).newInstance(plugin, addonFile);
            loaderInstance.load();
            loadedAddons.add(loaderInstance);
        } catch (Exception exception) {
            plugin.getLogger().log(Level.WARNING, "Could not load addon %s:%s".formatted(clazz.getName(), exception.getMessage()), exception);
        }
    }



    // Optional: Add method to unload all addons
    public void unloadAll() {
        loadedAddons.forEach(AddonLoader::unload);
        loadedAddons.clear();
    }
}