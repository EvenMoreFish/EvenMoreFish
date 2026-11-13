package com.oheers.fish.gui.guis;

import com.oheers.fish.config.gui.impl.MainMenuGuiConfig;
import com.oheers.fish.gui.EMFConfigGui;
import de.themoep.inventorygui.InventoryGui;
import org.bukkit.entity.HumanEntity;
import org.jspecify.annotations.NonNull;

import java.util.Map;

public class MainMenuGui extends EMFConfigGui {

    public MainMenuGui(@NonNull HumanEntity viewer) {
        super(
            MainMenuGuiConfig.getInstance(),
            viewer
        );
    }

    @Override
    public void configure(@NonNull InventoryGui inventoryGui) {}

    @Override
    protected void addClickActions(@NonNull Map<String, ConfigGuiClickAction> map) {}

}
