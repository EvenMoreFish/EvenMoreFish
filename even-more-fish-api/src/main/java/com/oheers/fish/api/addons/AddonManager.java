package com.oheers.fish.api.addons;

import com.oheers.fish.api.FileUtil;
import com.oheers.fish.api.plugin.EMFPlugin;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

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

        jars.forEach(this::processJar);
    }

    private void processJar(File jar) {
        List<Class<? extends AddonLoader>> classes = FileUtil.findClasses(jar, AddonLoader.class);
        classes.forEach(this::loadAddonLoader);
    }

    private void loadAddonLoader(Class<? extends AddonLoader> clazz) {
        if (clazz == null) {
            return;
        }
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
