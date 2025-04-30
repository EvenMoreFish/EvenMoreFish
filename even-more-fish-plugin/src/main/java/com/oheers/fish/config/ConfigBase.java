package com.oheers.fish.config;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.Settings;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

public class ConfigBase {

    private final boolean preventIO;
    private final String fileName;
    private final String resourceName;
    private final Plugin plugin;
    private final boolean configUpdater;

    private YamlDocument config = null;
    private File file = null;

    public ConfigBase(@NotNull File file, @NotNull Plugin plugin, boolean configUpdater) {
        this.preventIO = false;
        this.fileName = file.getName();
        this.resourceName = null;
        this.plugin = plugin;
        this.configUpdater = configUpdater;
        reload(file);
        update();

        getConfig().remove("config-version");
        save();
    }

    public ConfigBase(@NotNull String fileName, @NotNull String resourceName, @NotNull Plugin plugin, boolean configUpdater) {
        this.preventIO = false;
        this.fileName = fileName;
        this.resourceName = resourceName;
        this.plugin = plugin;
        this.configUpdater = configUpdater;
        reload(new File(getPlugin().getDataFolder(), getFileName()));
        update();

        getConfig().remove("config-version");
        save();
    }

    /**
     * Creates an instance of ConfigBase with a blank file. This disables all I/O methods.
     */
    public ConfigBase() {
        this.preventIO = true;
        this.fileName = null;
        this.resourceName = null;
        this.plugin = null;
        this.configUpdater = false;

        try {
            this.config = YamlDocument.create(InputStream.nullInputStream(), getSettings());
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    public void reload(@NotNull File configFile) {

        if (preventIO) {
            return;
        }

        final Settings[] settings = getSettings();

        try {
            InputStream resource = getResourceName() == null ? null : getPlugin().getResource(getResourceName());
            if (resource == null) {
                this.config = YamlDocument.create(configFile, settings);
            } else {
                this.config = YamlDocument.create(configFile, resource, settings);
            }
            this.file = configFile;
        } catch (IOException ex) {
            plugin.getLogger().log(Level.SEVERE, ex.getMessage(), ex);
        }
        convertLegacy(getConfig());
        save();
    }

    public void reload() {
        if (preventIO) {
            return;
        }
        reload(this.file);
    }

    public final YamlDocument getConfig() {
        if (this.config == null) {
            throw new RuntimeException(getFileName() + " has not loaded properly. Please check for startup errors.");
        }
        return this.config;
    }

    public final File getFile() { return this.file; }

    public final Plugin getPlugin() { return this.plugin; }

    public final String getFileName() { return this.fileName; }

    public final String getResourceName() { return this.resourceName; }

    public Settings[] getSettings() {
        List<Settings> settingsList = new ArrayList<>(Arrays.asList(
                getGeneralSettings(),
                getDumperSettings(),
                getLoaderSettings()
        ));

        if (configUpdater) {
            settingsList.add(getUpdaterSettings());
        }

        return settingsList.toArray(Settings[]::new);
    }

    public GeneralSettings getGeneralSettings() {
        return GeneralSettings.builder().setUseDefaults(false).build();
    }

    public DumperSettings getDumperSettings() {
        return DumperSettings.DEFAULT;
    }

    public LoaderSettings getLoaderSettings() {
        return LoaderSettings.DEFAULT;
    }

    public UpdaterSettings getUpdaterSettings() {
        return UpdaterSettings.builder()
            .setVersioning(new BasicVersioning("version"))
            .setKeepAll(true)
            .setEnableDowngrading(false)
            .build();
    }

    public void save() {
        if (preventIO) {
            return;
        }
        try {
            getConfig().save();
        } catch (IOException exception) {
            EvenMoreFish.getInstance().getLogger().warning("Failed to save " + getFileName());
        }
    }

    public void update() {
        if (preventIO || !configUpdater) {
            return;
        }
        try {
            getConfig().update();
        } catch (IOException exception) {
            EvenMoreFish.getInstance().getLogger().warning("Failed to update " + getFileName());
        }
    }

    // MiniMessage conversion methods. DO NOT REMOVE

    public List<String> getMiniMessageExclusions() {
        return List.of();
    }

    /**
     * Converts all Legacy colors to MiniMessage.
     */
    @SuppressWarnings("unchecked")
    public void convertLegacy(@NotNull YamlDocument document) {
        List<String> excludedKeys = getMiniMessageExclusions();
        for (String key : document.getRoutesAsStrings(true)) {
            if (excludedKeys.contains(key)) {
                continue;
            }
            if (document.isString(key)) {
                String updated = convertLegacyString(document.getString(key));
                document.set(key, updated);
            } else if (document.isList(key)) {
                List<?> list = document.getList(key);
                List<String> strings;
                try {
                    strings = (List<String>) list;
                } catch (ClassCastException exception) {
                    continue;
                }
                List<String> updated = strings.stream().map(this::convertLegacyString).toList();
                document.set(key, updated);
            }
        }
    }

    /**
     * Converts a Legacy String to MiniMessage.
     * If the message already contains a MiniMessage tag, this does nothing.
     */
    private String convertLegacyString(@NotNull String message) {
        // Exclude command reward types
        if (message.toUpperCase().startsWith("COMMAND:")) {
            return message;
        }

        // If the message isn't legacy, don't do anything
        if (!FishUtils.isLegacyString(message)) {
            return message;
        }

        // Get MiniMessage serializer
        final MiniMessage miniMessageSerializer = MiniMessage.builder()
            .strict(true)
            .postProcessor(component -> component)
            .build();

        // Get LegacyComponentSerializer
        final LegacyComponentSerializer legacySerializer = LegacyComponentSerializer.builder()
            .character('&')
            .hexColors()
            .build();

        // Legacy -> Component -> MiniMessage
        Component legacy = legacySerializer.deserialize(message);
        return miniMessageSerializer.serialize(legacy);
    }

}
