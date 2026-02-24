package com.oheers.fish.gui.guis;

import com.oheers.fish.config.GuiConfig;
import com.oheers.fish.gui.EMFGui;
import dev.triumphteam.gui.builder.gui.BaseGuiBuilder;
import dev.triumphteam.gui.components.nbt.NbtWrapper;
import dev.triumphteam.gui.components.nbt.Pdc;
import dev.triumphteam.gui.guis.Gui;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;

public class SellGui extends EMFGui {

    private static final NamespacedKey guiItemKey = new NamespacedKey("evenmorefish", "mf-gui");

    public SellGui(@NotNull HumanEntity human, @NotNull SellState state, @Nullable Inventory fishInventory) {
        super(
            GuiConfig.getInstance().getSellMenuConfig(state),
            human
        );
    }

    @Override
    public @NotNull BaseGuiBuilder<?, ?> buildGui() {
        return Gui.gui();
    }

    @Override
    public @Nullable String getItemCharacterKey() {
        return "deposit-character";
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
