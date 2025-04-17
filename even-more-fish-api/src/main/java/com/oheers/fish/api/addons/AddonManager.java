package com.oheers.fish.api.addons;

import com.oheers.fish.api.FileUtil;
import com.oheers.fish.api.plugin.EMFPlugin;
import com.oheers.fish.api.reward.RewardType;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class AddonManager {

    private static final String ADDON_FOLDER = "addons";
    private final File folder;

    public AddonManager() {
        this.folder = new File(EMFPlugin.getInstance().getDataFolder(), ADDON_FOLDER);

        if (!this.folder.exists() && !this.folder.mkdirs()) {
            EMFPlugin.getInstance().getLogger().warning("Could not create addons folder.");
        }
    }

    public void load() {
        // Retrieve all jar files in the addons folder
        List<File> jars = FileUtil.getFilesInDirectoryWithExtension(
            folder,
            ".jar",
            true,
            true
        );

        System.out.println(jars);

        jars.forEach(this::processJar);
    }

    private void processJar(File jar) {
        List<CompletableFuture<Class<? extends AddonLoader>>> futures = FileUtil.findClassesAsync(jar, AddonLoader.class);

        futures.forEach(future ->
            future.thenAccept(this::loadAddonLoader)
                .exceptionally(ex -> {
                    EMFPlugin.getInstance().getLogger().log(Level.WARNING, "Error processing JAR file: " + jar.getName(), ex);
                    return null;
                })
        );
    }

    private void loadAddonLoader(Class<? extends AddonLoader> clazz) {
        if (clazz == null) {
            return;
        }
        System.out.println(clazz.getSimpleName());
        try {
            AddonLoader loaderInstance = clazz.getDeclaredConstructor().newInstance();
            loaderInstance.load();
        } catch (SecurityException |
                 NoSuchMethodException |
                 InstantiationException |
                 IllegalAccessException |
                 InvocationTargetException exception) {
            throw new RuntimeException(exception);
        }
    }

}
