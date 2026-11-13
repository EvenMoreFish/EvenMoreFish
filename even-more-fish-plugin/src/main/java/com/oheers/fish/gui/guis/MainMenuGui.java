package com.oheers.fish.gui.guis;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.config.gui.impl.MainMenuGuiConfig;
import com.oheers.fish.gui.EMFConfigGui;
import com.oheers.fish.messages.ConfigMessage;
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
        addReplacement("{toggle-status}", () -> {
            if (EvenMoreFish.getInstance().getToggle().isCustomFishingDisabled(player)) {
                return ConfigMessage.CUSTOM_FISHING_DISABLED.getMessage().getComponentMessage(player);
            } else {
                return ConfigMessage.CUSTOM_FISHING_ENABLED.getMessage().getComponentMessage(player);
            }
        });
    }

    @Override
    public void configure(@NonNull InventoryGui inventoryGui) {}

    @Override
    protected void addClickActions(@NonNull Map<String, ConfigGuiClickAction> map) {}

}
