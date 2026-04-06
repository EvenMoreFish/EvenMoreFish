/**
 * This file contains parts of code from PlaceholderAPI.
 * Specifically, parts related to finding classes.
 * <p>
 * PlaceholderAPI
 * Copyright (c) 2015 - 2021 PlaceholderAPI Team
 * <p>
 * PlaceholderAPI free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * PlaceholderAPI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.oheers.fish.api;

import com.oheers.fish.api.plugin.EMFPlugin;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class FileUtil {

    private FileUtil() {
        throw new UnsupportedOperationException();
    }

    private static final Map<File, URLClassLoader> fileClassLoaders = new HashMap<>();

    public static <T> @NotNull List<Class<? extends T>> findClasses(@NotNull final File file, @NotNull final Class<T> clazz) {
        if (!file.exists()) {
            return Collections.emptyList();
        }

        final List<Class<? extends T>> classes = new ArrayList<>();

        final List<String> matches = matchingNames(file);

        for (final String match : matches) {
            try {
                final URL jar = file.toURI().toURL();
                // We need persistent class loaders, so we store a map of them.
                // I'm not sure if this would cause memory problems. I assume not since this only ever happens once, but idk.
                final URLClassLoader loader = fileClassLoaders.computeIfAbsent(
                    file,
                    f -> new URLClassLoader(new URL[]{jar}, clazz.getClassLoader())
                );
                Class<? extends T> addonClass = loadClass(loader, match, clazz);
                if (addonClass != null) {
                    classes.add(addonClass);
                }
            } catch (final VerifyError ex) {
                EMFPlugin.getInstance().getLogger().severe(() -> String.format("Failed to load addon class %s", file.getName()));
                EMFPlugin.getInstance().getLogger().severe(() -> String.format("Cause: %s %s", ex.getClass().getSimpleName(), ex.getMessage()));
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }


        return classes;
    }

    private static @NotNull List<String> matchingNames(final File file) {
        final List<String> matches = new ArrayList<>();
        try {
            final URL jar = file.toURI().toURL();
            try (final JarInputStream stream = new JarInputStream(jar.openStream())) {
                JarEntry entry;
                while ((entry = stream.getNextJarEntry()) != null) {
                    final String name = entry.getName();
                    if (!name.endsWith(".class")) {
                        continue;
                    }

                    matches.add(name.substring(0, name.lastIndexOf('.')).replace('/', '.'));
                }
            }
        } catch (Exception e) {
            return Collections.emptyList();
        }
        return matches;
    }

    private static <T> @Nullable Class<? extends T> loadClass(final @NotNull URLClassLoader loader, final String match, @NotNull final Class<T> clazz) throws ClassNotFoundException {
        try {
            final Class<?> loaded = loader.loadClass(match);
            if (clazz.isAssignableFrom(loaded)) {
                return (loaded.asSubclass(clazz));
            }
        } catch (final NoClassDefFoundError ignored) {
            //ignored
        } catch(final UnsupportedClassVersionError e) {
            EMFPlugin.getInstance().debug(e.getMessage());
        }
        return null;
    }

    /**
     * Loads a file from the specified directory or creates it by copying from a plugin resource if it doesn't exist.
     *
     * <p>The method first checks if the specified directory exists, creating it if necessary. Then it checks
     * for the existence of the target file. If the file exists, it is returned immediately. If not, the method
     * attempts to create the file and populate it by copying from the specified plugin resource.</p>
     *
     * @param directory The directory where the file should be located. Will be created if it doesn't exist.
     * @param fileName The name of the file to load or create in the directory.
     * @param resourceName The name of the resource in the plugin's JAR file to copy if the file doesn't exist.
     * @param plugin The plugin instance used for resource access and logging.
     * @return The loaded or created {@link File} object, or {@code null} if any operation fails.
     *
     * @throws NullPointerException If any of the parameters are {@code null}.
     *
     * @implNote The method will attempt to create parent directories if they don't exist.
     *           If file creation or resource copying fails, error messages will be logged
     *           through the plugin's logger.
     */
    public static Optional<File> loadFileOrResource(@NotNull File directory, @NotNull String fileName, @NotNull String resourceName, @NotNull Plugin plugin, boolean overwrite) {
        Objects.requireNonNull(directory, "directory cannot be null");
        Objects.requireNonNull(fileName, "fileName cannot be null");
        Objects.requireNonNull(resourceName, "resourceName cannot be null");
        Objects.requireNonNull(plugin, "plugin cannot be null");

        if (!directory.exists() && !directory.mkdirs()) {
            plugin.getLogger().severe(() -> "Could not create directory: " + directory);
            return Optional.empty(); // for now, maybe Optional
        }

        File configFile = new File(directory, fileName);

        // If file already exists, return it
        if (configFile.exists() && !overwrite) {
            return Optional.of(configFile);
        }

        try {
            if (!configFile.createNewFile()) {
                plugin.getLogger().severe(() -> "Could not create file: " + configFile);
                return Optional.empty();
            }
        } catch (IOException ex) {
            plugin.getLogger().log(Level.SEVERE, "Failed to create file: %s".formatted(configFile.getName()), ex);
            return Optional.empty();
        }


        try (InputStream stream = plugin.getResource(resourceName)) {
            if (stream == null) {
                plugin.getLogger().severe(() -> "Resource not found: " + resourceName);
                return Optional.empty();
            }

            Files.copy(stream, configFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return Optional.of(configFile);
        } catch (IOException ex) {
            plugin.getLogger().log(Level.SEVERE, "Failed to copy resource %s to %s".formatted(resourceName, configFile), ex);
            return Optional.empty();
        }
    }

    public static Optional<File> loadFileOrResource(@NotNull File directory, @NotNull String fileName, @NotNull String resourceName, @NotNull Plugin plugin) {
        return loadFileOrResource(directory, fileName, resourceName, plugin, false);
    }

    public static List<File> getFilesInDirectoryWithExtension(@NotNull File directory, @Nullable String extension, boolean ignoreUnderscoreFiles, boolean recursive) {
        List<File> finalList = new ArrayList<>();
        if (!directory.exists() || !directory.isDirectory()) {
            return finalList;
        }
        try {
            FilenameFilter filter = extension == null ? null : (dir, name) -> name.endsWith(extension);
            File[] fileArray = directory.listFiles(filter);
            if (fileArray == null) {
                return finalList;
            }
            for (File file : fileArray) {
                if (ignoreUnderscoreFiles && file.getName().startsWith("_")) {
                    continue;
                }
                if (file.isDirectory() && recursive) {
                    finalList.addAll(getFilesInDirectory(file, ignoreUnderscoreFiles, true));
                } else if (file.isFile()) {
                    finalList.add(file);
                }
            }
        } catch (SecurityException exception) {
            EMFPlugin.getInstance().getLogger().log(Level.WARNING, "Failed to retrieve files in %s: Access Denied.".formatted(directory.getAbsolutePath()), exception);
        }
        return finalList;
    }

    /**
     * Retrieves all files in the given directory.
     *
     * @param directory The directory to search
     * @param ignoreUnderscoreFiles Should files that start with an underscore be ignored?
     * @param recursive Should this also search subdirectories?
     * @return A list of files in the directory. Returns an empty list if none.
     */
    public static List<File> getFilesInDirectory(@NotNull File directory, boolean ignoreUnderscoreFiles, boolean recursive) {
        return getFilesInDirectoryWithExtension(directory, null, ignoreUnderscoreFiles, recursive);
    }

    public static boolean doesDirectoryContainFile(@NotNull File directory, @NotNull String fileName, boolean recursive) {
        for (File file : getFilesInDirectory(directory, false, recursive)) {
            if (file.getName().equals(fileName)) {
                return true;
            }
        }
        return false;
    }


    /**
     * Gets all .addon filenames from a specific path within the JAR
     * @param clazz Any class from your plugin (used to get the ClassLoader)
     * @param jarPath The path within the JAR (e.g. "addons")
     * @return Set of .addon filenames (just names, not full paths)
     * @throws IOException If there's an error reading the JAR
     */
    public static Set<String> getAddonFilenames(@NotNull Class<?> clazz, String jarPath) throws IOException {
        URL jarLocation = clazz.getProtectionDomain().getCodeSource().getLocation();
        Optional<File> jarFile = fromURL(jarLocation);
        if (jarFile.isEmpty()) {
            EMFPlugin.getInstance().debug("Empty Jar file");
            return Collections.emptySet();
        }

        // Normalize the jarPath to use forward slashes and remove leading/trailing slashes
        final String normalizedJarPath = jarPath.replace('\\', '/').replaceAll("^/+|/+$", "");
        try (JarFile jar = new JarFile(jarFile.get())) {
            return jar.stream()
                    .map(JarEntry::getName)
                    .filter(name -> name.startsWith(normalizedJarPath + "/"))
                    .filter(name -> name.endsWith(".addon"))
                    .map(name -> name.substring(name.lastIndexOf('/') + 1))
                    .peek(result -> EMFPlugin.getInstance().debug("Found matching addon: " + result))
                    .collect(Collectors.toSet());
        }
    }

    private static Optional<File> fromURL(@NotNull URL jarLocation) {
        try {
            return Optional.of(new File(jarLocation.toURI()));
        } catch (URISyntaxException e) {
            EMFPlugin.getInstance().getLogger().log(Level.WARNING, "Failed to convert JAR URL to file path",e);
            return Optional.empty();
        }
    }

    /**
     * Generic method to load files from a jar directory with filtering
     *
     * @param jarDirectory The directory in the jar (e.g., "rarities")
     * @param targetDirectory Where to copy the files
     * @param filter Predicate to determine which files to include
     */
    public static void loadFilesFromJarDirectory(
            @NotNull String jarDirectory,
            @NotNull File targetDirectory,
            @NotNull Predicate<String> filter,
            boolean overwrite
    ) {
        final List<String> fileList = getFilesFromJarDirectory(jarDirectory);
        if (fileList.isEmpty()) {
            EMFPlugin.getInstance().getLogger().warning(() -> "No files in jar directory, either this impl is wrong, or the path is.");
            return;
        }

        fileList.stream()
                .filter(filter)
                .forEach(file -> {
                    String fileName = file.substring(file.lastIndexOf('/') + 1);
                    FileUtil.loadFileOrResource(
                            targetDirectory,
                            fileName,
                            file,
                            EMFPlugin.getInstance(),
                            overwrite
                    );
                });
    }

    /**
     * Gets all files from a specific directory in the jar
     *
     * @param jarDirectory The directory to scan (e.g., "rarities")
     * @return List of file paths in format "directory/file.ext"
     */
    public static List<String> getFilesFromJarDirectory(@NotNull String jarDirectory) {
        try {
            // Remove trailing slash if present
            String dirPath = jarDirectory.endsWith("/")
                    ? jarDirectory.substring(0, jarDirectory.length() - 1)
                    : jarDirectory;

            // Get resource URL
            URL dirURL = EMFPlugin.getInstance().getClass().getClassLoader().getResource(dirPath);

            if (dirURL == null) {
                EMFPlugin.getInstance().getLogger().warning(() -> "Directory not found: " + dirPath);
                return Collections.emptyList();
            }

            // Handle jar protocol
            if ("jar".equals(dirURL.getProtocol())) {
                return getFilesFromJar(dirURL, dirPath);
            }
            // Handle file protocol (for IDE/testing)
            else if ("file".equals(dirURL.getProtocol())) {
                return getFilesFromFileSystem(dirURL, dirPath);
            }

            return Collections.emptyList();
        } catch (Exception e) {
            EMFPlugin.getInstance().getLogger().warning(
                    "Error reading jar directory '" + jarDirectory + "': " + e.getMessage()
            );
            return Collections.emptyList();
        }
    }

    private static List<String> getFilesFromJar(URL dirURL, String dirPath) throws IOException {
        List<String> files = new ArrayList<>();
        String jarPath = dirURL.getPath().substring(5, dirURL.getPath().indexOf("!")); // Strip out only the JAR file
        try (JarFile jar = new JarFile(URLDecoder.decode(jarPath, StandardCharsets.UTF_8))) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                String entry = entries.nextElement().getName();
                if (entry.startsWith(dirPath) && !entry.equals(dirPath + "/")) {
                    files.add(entry);
                }
            }
        }
        return files;
    }

    private static List<String> getFilesFromFileSystem(URL dirURL, String dirPath) {
        File[] files = new File(dirURL.getPath()).listFiles();
        if (files == null || files.length == 0) {
            return List.of();
        }
        return Arrays.stream(files)
                .map(File::getName)
                .map(name -> dirPath + "/" + name)
                .toList();
    }

    public static void regenExampleFiles(final String jarDirectory, final File targetDirectory) {
        FileUtil.loadFilesFromJarDirectory(
                jarDirectory,
                targetDirectory,
                file -> file.startsWith("_example") && file.endsWith(".yml"),
                true
        );
    }

    public static void loadDefaultFiles(final String jarDirectory, final File targetDirectory) {
        EMFPlugin.getInstance().getLogger().info(() -> "Loading default %s configs from jar".formatted(jarDirectory));

        FileUtil.loadFilesFromJarDirectory(
                jarDirectory,
                targetDirectory,
                file -> !file.startsWith("_") && file.endsWith(".yml"),
                false
        );
    }
}