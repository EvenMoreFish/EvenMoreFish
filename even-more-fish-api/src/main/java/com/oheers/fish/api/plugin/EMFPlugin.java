package com.oheers.fish.api.plugin;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class EMFPlugin extends JavaPlugin {

    private static EMFPlugin instance;

    protected EMFPlugin() {
        if (instance != null) {
            throw new UnsupportedOperationException("EMFPlugin has already been assigned!");
        }
        instance = this;
    }

    public static @NotNull EMFPlugin getInstance() {
        if (instance == null) {
            throw new RuntimeException("EMFPlugin not found. This should not happen!");
        }
        return instance;
    }

    public abstract void reload(@Nullable CommandSender sender);

}
