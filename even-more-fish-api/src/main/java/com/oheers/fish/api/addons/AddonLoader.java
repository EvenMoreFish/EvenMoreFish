package com.oheers.fish.api.addons;

import com.oheers.fish.api.plugin.EMFPlugin;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Level;

public abstract class AddonLoader {
    private final File addonFile;
    private final EMFPlugin plugin;
    private AddonMetadata cachedMetadata;

    protected AddonLoader(EMFPlugin plugin, File addonFile) {
        this.plugin = Objects.requireNonNull(plugin, "Plugin instance cannot be null");
        this.addonFile = addonFile; //can be null (i.e. internal addon)
    }


    /**
     * Gets the addon metadata, first trying to load from META-INF/addon-loader.properties,
     * then falling back to loadAddonMetadata() if the file doesn't exist.
     */
    public AddonMetadata getAddonMetadata() {
        if (cachedMetadata == null) {
            // First try to load from properties file
            if (addonFile != null) {
                cachedMetadata = loadMetadataFromProperties(addonFile);
            }

            // If not found, try the default implementation
            if (cachedMetadata == null) {
                cachedMetadata = loadAddonMetadata();
            }

            // If still null, throw exception
            if (cachedMetadata == null) {
                throw new IllegalStateException("Could not load addon metadata - neither properties file exists nor metadata was provided");
            }
        }
        return cachedMetadata;
    }


    /**
     * Attempts to load metadata from META-INF/addon-loader.properties
     * @return The loaded metadata, or null if the file doesn't exist
     */
    private AddonMetadata loadMetadataFromProperties(File addonFile) {
        try (JarFile jar = new JarFile(addonFile)) {
            Manifest manifest = jar.getManifest();
            if (manifest == null) {
                plugin.getLogger().warning("No manifest found in addon: " + addonFile.getName());
                return null;
            }

            Attributes attributes = manifest.getMainAttributes();

            String name = attributes.getValue("name");
            String version = attributes.getValue("version");
            String authorsStr = attributes.getValue("authors");
            String website = attributes.getValue("website");
            String description = attributes.getValue("description");
            String dependenciesStr = attributes.getValue("dependencies");

            List<String> authors = authorsStr != null ?
                    Arrays.stream(authorsStr.replaceAll("[\\[\\]]", "").split(","))
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .toList() :
                    Collections.emptyList();

            List<String> dependencies = dependenciesStr != null ?
                    Arrays.stream(dependenciesStr.replaceAll("[\\[\\]]", "").split(","))
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .toList() :
                    Collections.emptyList();

            if (name == null || version == null || authors.isEmpty()) {
                plugin.getLogger().warning("MANIFEST.MF is missing required fields (name, version, or authors)");
                plugin.getLogger().warning(() -> "Name: %s, Version: %s, Authors: %s".formatted(name == null, version == null, authors.isEmpty()));
                return null;
            }

            return new AddonMetadata(
                    name,
                    version,
                    authors,
                    description != null ? description : "",
                    website,
                    dependencies
            );
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Failed to load addon metadata from manifest file", e);
            return null;
        }
    }

    /**
     * Default implementation that returns null. Can be overridden by subclasses
     * to provide custom metadata loading behavior.
     */
    protected AddonMetadata loadAddonMetadata() {
        return null;
    }

    public abstract void loadAddons();


    public final void load() {
        if (!canLoad()) {
            plugin.debug("Skipping loading of addon %s - canLoad() returned false".formatted(getAddonMetadata().name()));
            return;
        }

        try {
            validateMetadata();
            loadAddons();
            plugin.getLogger().info("Loaded Addon %s %s by %s".formatted(getAddonMetadata().name(), getAddonMetadata().version(), getAddonMetadata().authors()));
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load addon %s".formatted(getAddonMetadata().name()), e);
        }
    }

    protected void validateMetadata() {
        AddonMetadata meta = getAddonMetadata();
        if (meta == null) {
            throw new IllegalStateException("Addon metadata cannot be null");
        }
        if (meta.name().isBlank()) {
            throw new IllegalStateException("Addon name cannot be null or blank");
        }
    }

    public abstract boolean canLoad();

    public void unload() {
        // Default empty implementation
    }

    public void reload() {
        unload();
        load();
    }

}
