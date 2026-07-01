package com.oheers.fish.plugin.loading;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.plugin.EMFPlugin;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;

/**
 * Uses reflection and classloaders to load our version-dependent jar files.
 */
public class EMFLoader {

    private final EMFPlugin plugin;
    private final URLClassLoader jar;
    private final EMFVersionProvider version;

    public EMFLoader(@NotNull EMFPlugin plugin, @NotNull ClassLoader parent) {
        this.plugin = plugin;
        this.jar = getClassLoader(parent);
        this.version = fetchVersion();
    }

    public @NotNull EMFVersionProvider getVersionProvider() {
        return this.version;
    }

    public void onDisable() {
        try {
            jar.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Fetches the correct resource jar, adds it to the classpath, and returns the EMFVersionProvider instance.
     */
    private EMFVersionProvider fetchVersion() {
        try  {
            Class<?> clazz = jar.loadClass("com.oheers.fish.EMFVersion");
            return (EMFVersionProvider) clazz.getDeclaredConstructor(EMFPlugin.class).newInstance(plugin);
        } catch (Exception exception) {
            throw new RuntimeException("Failed to load EvenMoreFish", exception);
        }
    }

    private URL getURL(ClassLoader classLoader) {
        String minecraftVersion = Bukkit.getMinecraftVersion();
        if (minecraftVersion.startsWith("1.20")) {
            return classLoader.getResource("versions/1-20.jar");
        } else if (minecraftVersion.startsWith("1.21")) {
            return classLoader.getResource("versions/1-21.jar");
        } else if (minecraftVersion.startsWith("26.1")) {
            return classLoader.getResource("versions/26-1.jar");
        } else if (minecraftVersion.startsWith("26.2")) {
            return classLoader.getResource("versions/26-2.jar");
        } else {
            throw new IllegalStateException("EvenMoreFish does not support this Minecraft version.");
        }
    }

    private URLClassLoader getClassLoader(ClassLoader parent) {
        try (InputStream is = getURL(parent).openStream()) {
            File file = new File(plugin.getDataFolder(), Bukkit.getMinecraftVersion() + ".jar");
            file.deleteOnExit();
            Files.copy(is, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return new URLClassLoader(
                new URL[]{file.toURI().toURL()},
                parent
            );
        } catch (IOException exception) {
            throw new RuntimeException("Failed to load EvenMoreFish", exception);
        }
    }

}
