package com.oheers.fish.api.addons;

import com.oheers.fish.api.plugin.EMFPlugin;
import org.jetbrains.annotations.NotNull;

public abstract class AddonLoader {

    public abstract @NotNull String getName();

    public abstract @NotNull String getVersion();

    public abstract @NotNull String getAuthor();

    public void loadAddons() {}

    public final void load() {
        if (!canLoad()) {
            return;
        }
        loadAddons();
        EMFPlugin.getInstance().getLogger().info("Loaded " + getName() + " " + getVersion() + " by " + getAuthor());
    }

    public abstract boolean canLoad();

}
