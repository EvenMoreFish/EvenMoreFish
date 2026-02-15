package com.oheers.fish.gui;

import com.oheers.fish.api.config.ConfigBase;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Internal
public abstract class EMFGui {

    protected final Section config;
    protected final Player player;

    public EMFGui(@NotNull Section config, @NotNull HumanEntity human) {
        this.config = config;
        if (!(human instanceof Player p)) {
            throw new IllegalStateException("Provided HumanEntity is not a Player.");
        }
        this.player = p;
    }

    public abstract void open();

    public @NotNull Section getConfig() {
        return this.config;
    }

}
