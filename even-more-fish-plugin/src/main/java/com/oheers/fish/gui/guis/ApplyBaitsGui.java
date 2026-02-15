package com.oheers.fish.gui.guis;

import com.oheers.fish.config.GuiConfig;
import com.oheers.fish.gui.EMFGui;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

}
