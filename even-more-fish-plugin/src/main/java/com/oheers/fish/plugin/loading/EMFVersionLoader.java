package com.oheers.fish.plugin.loading;

import com.oheers.fish.api.plugin.EMFPlugin;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

/**
 * Uses reflection and classloaders to load our version-dependent jar files.
 */
public class EMFVersionLoader {

    private final EMFPlugin plugin;
    private final URLClassLoader jar;
    private final EMFVersionProvider version;

    public EMFVersionLoader(@NotNull EMFPlugin plugin, @NotNull ClassLoader parent) {
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

    private URLClassLoader getClassLoader(ClassLoader parent) {
        try (InputStream is = getURL(parent).openStream()) {
            plugin.getDataFolder().mkdirs();
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

    private URL getURL(ClassLoader classLoader) {
        String minecraftVersion = Bukkit.getMinecraftVersion();
        if (minecraftVersion.startsWith("26.2")) {
            return classLoader.getResource("versions/26-2.jar");
        } else if (minecraftVersion.startsWith("26.1")) {
            return classLoader.getResource("versions/26-1.jar");
        // 1.21 has multiple version jars.
        } else if (minecraftVersion.startsWith("1.21")) {
            List<String> oldVersions = List.of("1.21", "1.21.1", "1.21.3", "1.21.4");
            if (oldVersions.contains(minecraftVersion)) {
                return classLoader.getResource("versions/1.21.1-4.jar");
            } else {
                return classLoader.getResource("versions/1.21.5-11.jar");
            }
        } else if (minecraftVersion.startsWith("1.20")) {
            return classLoader.getResource("versions/1-20.jar");
        } else {
            throw new IllegalStateException("EvenMoreFish does not support this Minecraft version.");
        }
    }

}
