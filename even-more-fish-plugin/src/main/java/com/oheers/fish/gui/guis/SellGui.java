package com.oheers.fish.gui.guis;

import com.oheers.fish.config.GuiConfig;
import com.oheers.fish.exceptions.InvalidFishException;
import com.oheers.fish.gui.EMFGui;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import dev.triumphteam.gui.builder.gui.BaseGuiBuilder;
import dev.triumphteam.gui.guis.BaseGui;
import dev.triumphteam.gui.guis.Gui;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SellGui extends EMFGui {

    public SellGui(@NotNull HumanEntity human, @NotNull SellState state, @Nullable Inventory fishInventory) {
        super(
            GuiConfig.getInstance().getSellMenuConfig(state),
            human
        );
    }

    @Override
    public void open() {

    }

    @Override
    public @NotNull BaseGuiBuilder<?, ?> buildGui() {
        return Gui.gui();
    }

    public enum SellState {
        NORMAL("sell-menu-normal"),
        CONFIRM("sell-menu-confirm");

        private final String configLocation;

        SellState(@NotNull String configLocation) {
            this.configLocation = configLocation;
        }

        public @NotNull String getConfigLocation() {
            return configLocation;
        }
    }

}
