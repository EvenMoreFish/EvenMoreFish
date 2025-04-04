package com.oheers.fish.addons;


import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.addons.impl.Head64ItemAddon;
import com.oheers.fish.api.FileUtil;
import com.oheers.fish.api.addons.Addon;
import com.oheers.fish.api.addons.Futures;
import com.oheers.fish.api.addons.ItemAddon;
import com.oheers.fish.api.addons.exceptions.JavaVersionException;
import com.oheers.fish.api.addons.exceptions.NoPrefixException;
import com.oheers.fish.api.addons.exceptions.RequiredPluginException;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class AddonManager {

    private static final String ADDON_FOLDER = "addons";
    private final EvenMoreFish plugin;
    private final File folder;
    private final Map<String, Addon> addonMap;

    private final Map<String, Boolean> loadingMap;

    public AddonManager(final @NotNull EvenMoreFish plugin) {
        this.plugin = plugin;
        this.folder = new File(plugin.getDataFolder(), ADDON_FOLDER);
        this.addonMap = new HashMap<>();
        this.loadingMap = new HashMap<>();

        if (!this.folder.exists() && !this.folder.mkdirs()) {
            plugin.getLogger().warning("Could not create addons folder.");
        }
    }

    @Nullable
    public ItemStack getItemStack(final String prefix, final String id) throws NoPrefixException {
        if (!addonMap.containsKey(prefix)) {
            if (!loadingMap.getOrDefault(prefix, true)) {
                throw new NoPrefixException(prefix);
            }
            return null;
        }

        final Addon addon = addonMap.get(prefix);
        if (!(addon instanceof ItemAddon itemAddon)) {
            return null;
        }
        return itemAddon.getItemStack(id);
    }


    public boolean registerAddon(final Addon addon) {
        if (addon == null) {
            EvenMoreFish.debug("Attempted to register a null addon.");
            return false;
        }

        final String prefix = addon.getPrefix().toLowerCase(Locale.ROOT);

        // Check if the addon can be registered
        if (!canRegisterAddon(addon, prefix)) {
            return false;
        }

        // Register the addon
        registerAddonInternal(addon, prefix);
        return true;
    }

    private boolean canRegisterAddon(final @NotNull Addon addon, final String prefix) {
        try {
            if (!addon.canRegister()) {
                setAddonLoadingStatus(prefix, true);
                return false;
            }
        } catch (JavaVersionException | RequiredPluginException e) {
            EvenMoreFish.debug("Addon registration failed for prefix: " + prefix + ". Reason: " + e.getMessage());
            setAddonLoadingStatus(prefix, true);
            return false;
        }
        return true;
    }

    private void registerAddonInternal(final @NotNull Addon addon, final String prefix) {
        setAddonLoadingStatus(prefix, false);
        this.addonMap.put(prefix, addon);

        // Register as a listener if applicable
        if (addon instanceof Listener listener) {
            Bukkit.getPluginManager().registerEvents(listener, plugin);
        }
    }

    private void setAddonLoadingStatus(final String prefix, final boolean isLoading) {
        this.loadingMap.put(prefix, isLoading);
    }

    public Optional<Addon> registerAddon(final @NotNull Class<? extends Addon> addon) {
        try {
            final Addon addonInstance = createAddonInstance(addon);

            if (addonInstance == null) {
                return Optional.empty();
            }

            Objects.requireNonNull(addonInstance.getAuthor(), "The expansion author is null!");
            Objects.requireNonNull(addonInstance.getPrefix(), "The expansion identifier is null!");

            try {
                if (!addonInstance.canRegister()) {
                    plugin.getLogger().warning(() -> String.format("Cannot load expansion %s due to an unknown issue.", addonInstance.getPrefix()));
                    this.loadingMap.put(addonInstance.getPrefix(), true);
                    return Optional.empty();
                }
            } catch (JavaVersionException | RequiredPluginException e) {
                EvenMoreFish.debug(Level.WARNING, e.getMessage());
                this.loadingMap.put(addonInstance.getPrefix(), true);
                return Optional.empty();
            }

            if (addonInstance instanceof Listener listener) {
                Bukkit.getPluginManager().registerEvents(listener, plugin);
            }

            this.addonMap.put(addonInstance.getPrefix(), addonInstance);
            return Optional.of(addonInstance);
        } catch (LinkageError | NullPointerException ex) {
            plugin.getLogger().log(Level.SEVERE, String.format("Failed to load addon class %s%s", addon.getSimpleName(), ex.getMessage()), ex);
        }

        return Optional.empty();
    }

    @Nullable
    public Addon createAddonInstance(@NotNull final Class<? extends Addon> clazz) throws LinkageError {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (final Exception ex) {
            if (ex.getCause() instanceof LinkageError linkageError) {
                throw linkageError;
            }
            plugin.getLogger().warning("There was an issue with loading an addon.");
            return null;
        }
    }


    public CompletableFuture<List<Class<? extends Addon>>> findLocalAddons() {
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".jar"));
        if (files == null) {
            plugin.getLogger().warning(() -> String.format("Could not find any files in folder %s", folder.getName()));
            return CompletableFuture.completedFuture(Collections.emptyList());
        }

        return Arrays.stream(files)
                .map(this::findAddonsInFileAsync)
                .flatMap(List::stream)
                .filter(Objects::nonNull)
                .collect(Futures.collector());
    }

    public List<CompletableFuture<Class<? extends Addon>>> findAddonsInFileAsync(final File file) {
        return FileUtil.findClassesAsync(file, Addon.class);
    }

    private void registerInternal(final @NotNull Addon @NotNull ... addons) {
        for (final Addon addon : addons) {
            registerAddon(addon);
        }
        plugin.getLogger().info(() -> String.format("Registered %s internal addons", addons.length));

    }

    private void registerAll() {
        Futures.onMainThread(plugin, findLocalAddons(), (classes, exception) -> {
            if (exception != null) {
                plugin.getLogger().log(Level.SEVERE, "Failed to load class files of addon.", exception);
                return;
            }

            final List<Addon> registered = classes.stream()
                    .filter(Objects::nonNull)
                    .map(this::registerAddon)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .toList();

            final String message = String.format("%s new addons registered! (%s total)", registered.size(), addonMap.size());
            plugin.getLogger().info(message);
        });
    }

    public void load() {
        registerInternal(
                new Head64ItemAddon()
        );

        registerAll();

        for (Map.Entry<String, Addon> entry : addonMap.entrySet()) {
            if (entry.getValue() instanceof Listener listener) {
                Bukkit.getPluginManager().registerEvents(listener, plugin);
            }
        }
    }

    public Map<String, Addon> getAddonMap() {
        return addonMap;
    }

    public boolean isLoading(final String prefix) {
        return loadingMap.getOrDefault(prefix, true);
    }

}
