package com.oheers.fish.gui;

import com.oheers.fish.api.config.ConfigBase;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import dev.triumphteam.gui.BaseGui;
import dev.triumphteam.gui.paper.Gui;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

@ApiStatus.Internal
public abstract class EMFGui {

    protected final Section config;
    protected final Player player;
    protected final BaseGui<Player> gui;

    private final Map<String, ?> replacements = new HashMap<>();

    public EMFGui(@NotNull Section config, @NotNull HumanEntity human) {
        this.config = config;
        if (!(human instanceof Player p)) {
            throw new IllegalStateException("Provided HumanEntity is not a Player.");
        }
        this.player = p;
        this.gui = createGui();
    }

    public abstract void open();

    public @NotNull Section getConfig() {
        return this.config;
    }

    public @NotNull Player getPlayer() {
        return this.player;
    }

    public @NotNull Map<String, ?> getReplacements() {
        return this.replacements;
    }

    // TODO create Gui and give to GuiReader
    private @NotNull BaseGui<Player> createGui() {
        return Gui.of(1).build();
    }

}
