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
        EMFPlugin.getInstance().debug("Loading " + getName() + " " + getVersion() + " by " + getAuthor());
        loadAddons();
        EMFPlugin.getInstance().debug("Finished loading " + getName());
    }

    public abstract boolean canLoad();

}
