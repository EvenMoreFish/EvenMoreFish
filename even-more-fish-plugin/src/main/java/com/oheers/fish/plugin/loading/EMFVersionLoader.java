package com.oheers.fish.plugin.loading;

import com.oheers.fish.api.plugin.EMFPlugin;
import org.bukkit.Bukkit;
import org.jspecify.annotations.NonNull;

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

    public EMFVersionLoader(@NonNull EMFPlugin plugin, @NonNull ClassLoader parent) {
        this.plugin = plugin;
        this.jar = getClassLoader(parent);
        this.version = fetchVersion();
    }

    public @NonNull EMFVersionProvider getVersionProvider() {
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
            Class<?> clazz = jar.loadClass("org.evenmorefish.fish.EMFVersion");
            return (EMFVersionProvider) clazz.getDeclaredConstructor(EMFPlugin.class).newInstance(plugin);
        } catch (Exception exception) {
            throw new RuntimeException("Failed to load EvenMoreFish", exception);
        }
    }

    private URLClassLoader getClassLoader(ClassLoader parent) {
        try (InputStream is = getURL(parent).openStream()) {
            plugin.getDataFolder().mkdirs(); // Ensures the folder is always present.
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
        String version = Bukkit.getMinecraftVersion();
        // Minecraft 26.2.x
        if (version.startsWith("26.2")) return classLoader.getResource("versions/26-2.jar");
        // Minecraft 26.1.x
        if (version.startsWith("26.1")) return classLoader.getResource("versions/26-1.jar");
        // Minecraft 1.20.x
        if (version.startsWith("1.20")) return classLoader.getResource("versions/1-20.jar");
        // Minecraft 1.21.x
        if (version.startsWith("1.21")) {
            return switch (version) {
                // 1.21.0 - Not supported.
                case "1.21" -> throw new IllegalStateException("EvenMoreFish does not support this Minecraft version.");
                case "1.21.1", "1.21.3", "1.21.4" -> classLoader.getResource("versions/1.21.1-4.jar");
                default -> classLoader.getResource("versions/1.21.5-11.jar");
            };
        }
        throw new IllegalStateException("EvenMoreFish does not support this Minecraft version.");
    }

}
