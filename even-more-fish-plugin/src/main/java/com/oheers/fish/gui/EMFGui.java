package com.oheers.fish.gui;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import dev.triumphteam.gui.builder.gui.BaseGuiBuilder;
import dev.triumphteam.gui.components.exception.GuiException;
import dev.triumphteam.gui.guis.BaseGui;
import dev.triumphteam.gui.guis.Gui;
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
    protected final BaseGui gui;

    private final Map<String, ?> replacements = new HashMap<>();

    public EMFGui(@NotNull Section config, @NotNull HumanEntity human) {
        this.config = config;
        if (!(human instanceof Player p)) {
            throw new IllegalStateException("Provided HumanEntity is not a Player.");
        }
        this.player = p;
        this.gui = createGui();
    }

    public void open() {
        gui.open(player);
    }

    public @NotNull Section getConfig() {
        return this.config;
    }

    public @NotNull Player getPlayer() {
        return this.player;
    }

    public @NotNull Map<String, ?> getReplacements() {
        return this.replacements;
    }

    public abstract @NotNull BaseGuiBuilder<?, ?> buildGui();

    // TODO create Gui and give to GuiReader
    private @NotNull BaseGui createGui() {
        BaseGuiBuilder<?, ?> builder = buildGui();
        builder.disableAllInteractions();
        try {
            return new GuiReader(this, builder).createGui();
        } catch (GuiException exception) {
            throw new EMFGuiException(
                "Failed to create " + getClass().getName() + " for " + player.getName(),
                exception
            );
        }
    }

}
