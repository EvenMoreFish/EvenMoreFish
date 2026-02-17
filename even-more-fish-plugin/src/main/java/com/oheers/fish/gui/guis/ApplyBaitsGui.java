package com.oheers.fish.gui.guis;

import com.oheers.fish.config.GuiConfig;
import com.oheers.fish.gui.EMFGui;
import dev.triumphteam.gui.guis.BaseGui;
import dev.triumphteam.gui.guis.Gui;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

public class ApplyBaitsGui extends EMFGui {

    public ApplyBaitsGui(@NotNull HumanEntity human, @Nullable Inventory baitInventory) {
        super(
            GuiConfig.getInstance().getApplyBaitsConfig(),
            human
        );
    }

    @Override
    public void open() {

    }

    @Override
    public @NotNull BaseGui buildGui() {
        return Gui.gui().create();
    }

}
